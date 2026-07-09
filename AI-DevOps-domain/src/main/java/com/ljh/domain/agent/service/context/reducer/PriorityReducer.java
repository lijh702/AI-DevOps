package com.ljh.domain.agent.service.context.reducer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 保留重要消息（System消息、工具结果、用户关键消息）
 */
@Component
public class PriorityReducer implements MessageReducer {

    @Override
    public List<Map<String, Object>> reduce(List<Map<String, Object>> messages, int tokenBudget) {
        // 为每条消息推断优先级
        List<PrioritizedMessage> prioritized = messages.stream()
            .map(m -> new PrioritizedMessage(m, inferPriority(m)))
            .collect(Collectors.toList());

        // 至少保留最近 2 条
        int minKeep = Math.min(2, prioritized.size());
        List<PrioritizedMessage> kept = new ArrayList<>(prioritized.subList(
            prioritized.size() - minKeep, prioritized.size()));

        // 从低优先级开始丢弃，直到满足 token 预算
        int usedTokens = estimateTokens(kept);
        for (int i = prioritized.size() - minKeep - 1; i >= 0; i--) {
            PrioritizedMessage pm = prioritized.get(i);
            int msgTokens = estimateToken(pm.getMessage());
            if (usedTokens + msgTokens <= tokenBudget) {
                kept.add(0, pm);
                usedTokens += msgTokens;
            }
        }

        return kept.stream().map(PrioritizedMessage::getMessage).collect(Collectors.toList());
    }

    /**
     * 条件	                                优先级	    意图
     * 角色为 tool 且内容包含错误关键词	        CRITICAL	工具调用出错的信息极其重要，不能丢弃
     * 角色为 user 且内容包含路径或配置文件后缀	HIGH	    用户可能正在询问配置、文件路径等，需重点关注
     * 角色为 system	                        HIGH	    系统提示词一般定义对话基调，重要性高
     * 角色为 assistant且内容超长（>5000字符）	LOW	        特别长的模型回复往往包含大量冗长信息，优先丢弃
     * 其他情况	                            MEDIUM	    默认中等优先级
     */
    private Priority inferPriority(Map<String, Object> message) {
        String role = (String) message.get("role");
        String content = String.valueOf(message.get("content"));

        if ("tool".equals(role) && containsAny(content, "error", "failed", "exception", "permission denied")) {
            return Priority.CRITICAL;
        }
        if ("user".equals(role) && containsAny(content, "/", ".conf", ".yml", ".properties")) {
            return Priority.HIGH;
        }
        if ("system".equals(role)) {
            return Priority.HIGH;
        }
        if ("assistant".equals(role) && content.length() > 5000) {
            return Priority.LOW;
        }
        return Priority.MEDIUM;
    }

    private boolean containsAny(String content, String... keywords) {
        if (content == null) return false;
        String lower = content.toLowerCase();
        for (String keyword : keywords) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    private int estimateToken(Map<String, Object> message) {
        String content = String.valueOf(message.get("content"));
        // 粗略估算：每 2 个字符 1 个 token
        return content != null ? content.length() / 2 : 0;
    }

    /**
     * 采用 极其粗略 的估算：每 2 个字符 ≈ 1 个 Token。
     * todo这并非精确的 tokenization，但在很多场景下作为简单的近似足够用。如果需要精确控制，可替换为真正的 tokenizer。
     */
    private int estimateTokens(List<PrioritizedMessage> messages) {
        return messages.stream().mapToInt(m -> estimateToken(m.getMessage())).sum();
    }

    @Data
    @AllArgsConstructor
    private static class PrioritizedMessage {
        private Map<String, Object> message;
        private Priority priority;
    }

    enum Priority { CRITICAL, HIGH, MEDIUM, LOW }
}
