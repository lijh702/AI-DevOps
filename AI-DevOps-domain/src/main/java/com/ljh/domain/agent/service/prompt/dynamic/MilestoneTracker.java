package com.ljh.domain.agent.service.prompt.dynamic;

import com.ljh.domain.agent.adapter.repository.IChatHistoryRepository;
import com.ljh.domain.agent.model.valobj.prompt.MilestoneVO;
import com.ljh.domain.agent.service.memory.CoreMemoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 里程碑追踪器
 * <p>
 * 增强点（P1-3 对话内学习）：
 * 1. 扩展纠正检测模式（8种 → 覆盖 Android detectCorrectionAndMemorize 的所有模式）
 * 2. USER_CORRECTION 事件触发 CoreMemoryService 写入长期记忆
 * 3. 工具错误（tool_error）也触发记忆写入
 */
@Slf4j
@Component
public class MilestoneTracker {

    @Resource
    private IChatHistoryRepository chatHistoryRepository;

    @Resource
    private CoreMemoryService coreMemoryService;

    private static final int MAX_MILESTONES = 50;
    /**
     * 按会话ID分组，每个会话独立维护
     枚举值	            含义	    触发场景	                示例内容
     TASK_CHANGE	    任务变更	用户改变主意、调整方向	    "不对，应该用PostgreSQL而不是MySQL"
     TASK_COMPLETE	    任务完成	用户确认任务结束	        "部署完成了，可以访问了"
     USER_CORRECTION	用户纠正	用户明确制止AI的行为	    "不要删除这个文件！"
     ERROR	            工具错误	命令执行失败、系统报错	    "Permission denied: /var/log/app.log"
     DECISION	        关键决策	用户做出重要选择	        "我决定使用微服务架构"
     FILE_SWITCH	    文件切换	用户在编辑不同文件	        "切换到 application.yml"
     */
    private final Map<String, LinkedList<MilestoneVO>> milestones = new ConcurrentHashMap<>();

    // ── 扩展的纠正检测模式（对标 Android 8种模式） ──
    /** 遗憾/后悔：用户意识到之前的决策不对 */
    private static final Pattern REGRET_PATTERN = Pattern.compile(
            "(不对|不是这样|不是这样做的|错了|搞错了|失误|后悔|应该|其实应该|本来应该)");

    /** 方向变更：用户要求换思路 */
    private static final Pattern DIRECTION_CHANGE_PATTERN = Pattern.compile(
            "(换个思路|换种方式|换个方向|改一下|换个方案|试试另一种|不如|还是用|重新来|重来)");

    /** 直接制止：用户要求停止当前行为 */
    private static final Pattern USER_CORRECTION_PATTERN = Pattern.compile(
            "(不要|别|别再|停|停下来|不用了|取消|算了)");

    /** 工具错误模式 */
    private static final Pattern TOOL_ERROR_PATTERN = Pattern.compile(
            "(?i)(error|failed|exception|permission denied|not found|refused|timeout|crash|fatal)");

    /** 完成模式 */
    private static final Pattern COMPLETE_PATTERN = Pattern.compile(
            "(完成了|搞定|结束|好了|OK|ok|没问题)");

    public void detectAndRecord(String sessionId, String role, String content) {
        if (sessionId == null || content == null || content.isEmpty()) return;

        MilestoneVO.Type type = null;

        if ("user".equals(role)) {
            if (REGRET_PATTERN.matcher(content).matches()) {
                type = MilestoneVO.Type.TASK_CHANGE;
            } else if (COMPLETE_PATTERN.matcher(content).matches()) {
                type = MilestoneVO.Type.TASK_COMPLETE;
            } else if (USER_CORRECTION_PATTERN.matcher(content).matches()) {
                type = MilestoneVO.Type.USER_CORRECTION;
            } else if (DIRECTION_CHANGE_PATTERN.matcher(content).matches()) {
                type = MilestoneVO.Type.TASK_CHANGE;
            }
        }

        if ("tool".equals(role)) {
            if (TOOL_ERROR_PATTERN.matcher(content).matches()) {
                type = MilestoneVO.Type.ERROR;
            }
        }

        if (type != null) {
            push(sessionId, MilestoneVO.builder()
                    .type(type)
                    .content(truncate(content, 200))
                    .timestamp(System.currentTimeMillis())
                    .build());
            log.info("里程碑记录: sessionId={}, type={}, content={}", sessionId, type, truncate(content, 100));

            // ── P1-3: 对话内学习 ──
            // USER_CORRECTION 和 TASK_CHANGE(regret) 触发写入核心记忆
            if (type == MilestoneVO.Type.USER_CORRECTION || type == MilestoneVO.Type.TASK_CHANGE) {
                try {
                    coreMemoryService.addCorrectionMemory(content, sessionId);
                    log.info("对话内学习: 纠正已写入核心记忆, sessionId={}", sessionId);
                } catch (Exception e) {
                    log.error("对话内学习写入失败", e);
                }
            }

            // 工具错误也可以记录为记忆（用户可能想知道什么命令容易出错）
            if (type == MilestoneVO.Type.ERROR) {
                try {
                    String keywords = extractErrorKeywords(content);
                    coreMemoryService.addMemory("user", "Fact",
                            "工具执行错误: " + truncate(content, 30),
                            keywords, truncate(content, 200), 2, sessionId);
                } catch (Exception e) {
                    log.error("工具错误记忆写入失败", e);
                }
            }
        }
    }

    private void push(String sessionId, MilestoneVO milestoneVO) {
        // 从Map中获取指定会话的里程碑列表，如果该会话还没有任何里程碑记录，则自动创建一个新的空列表并返回
        LinkedList<MilestoneVO> list = milestones.computeIfAbsent(sessionId, k -> new LinkedList<>());
        synchronized (list) {
            list.addLast(milestoneVO);
            while (list.size() > MAX_MILESTONES) {
                list.removeFirst();
            }
        }

        // [Phase 5] 异步保存里程碑到数据库
        try {
            chatHistoryRepository.saveMilestone(sessionId, milestoneVO);
        } catch (Exception e) {
            log.error("保存里程碑失败", e);
        }
    }

    public List<MilestoneVO> getRecent(String sessionId, int limit) {
        // 优先从数据库读取，以保证会话重启后仍然有效
        try {
            List<MilestoneVO> recentMilestones = chatHistoryRepository.getRecentMilestones(sessionId, limit);
            if (recentMilestones != null && !recentMilestones.isEmpty()) {
                // 因为返回的是时间倒序（最新的在前），LLM需要最新的在后，可以反转一下
                List<MilestoneVO> reversed = new ArrayList<>(recentMilestones);
                Collections.reverse(reversed);
                return reversed;
            }
        } catch (Exception e) {
            log.error("获取近期里程碑失败", e);
        }

        // 降级回内存
        LinkedList<MilestoneVO> list = milestones.getOrDefault(sessionId, new LinkedList<>());
        synchronized (list) {
            int from = Math.max(0, list.size() - limit);
            return new ArrayList<>(list.subList(from, list.size()));
        }
    }

    public void clear(String sessionId) {
        milestones.remove(sessionId);
    }

    private boolean matches(String content, String regex) {
        return Pattern.compile(".*(" + regex + ").*").matcher(content).matches();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    private String extractErrorKeywords(String content) {
        // 从错误信息中提取关键错误类型
        String lower = content.toLowerCase();
        List<String> keywords = new ArrayList<>();
        if (lower.contains("nullpointer")) keywords.add("NullPointerException");
        if (lower.contains("timeout")) keywords.add("timeout");
        if (lower.contains("permission")) keywords.add("permission-denied");
        if (lower.contains("connection")) keywords.add("connection-error");
        if (lower.contains("not found")) keywords.add("not-found");
        if (lower.contains("oom")) keywords.add("OOM");
        if (keywords.isEmpty()) keywords.add("error");
        return keywords.stream().reduce((a, b) -> a + "," + b).orElse("error");
    }
}
