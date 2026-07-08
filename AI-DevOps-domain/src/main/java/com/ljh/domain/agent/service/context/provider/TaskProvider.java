package com.ljh.domain.agent.service.context.provider;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对话历史：
 * [user] "帮我订一张去北京的机票"
 * [assistant] "请问出发日期是？"
 * [user] "明天上午"
 * messageHistory = [
 *     {"role": "user", "content": "帮我订一张去北京的机票"},
 *     {"role": "assistant", "content": "请问出发日期是？"},
 *     {"role": "user", "content": "自定义工具栏…"}
 * ]
 * 提取结果 → taskDescription: "帮我订一张去北京的机票"
 * {"taskDescription": "帮我订一张去北京的机票"}
 *   从多轮对话中摘出用户的初始需求
 *
 */
@Component
public class TaskProvider implements ContextProvider {
    @Override public String getName() { return "task"; }
    @Override public int getOrder() { return 20; }
    @Override public boolean enabled() { return true; }

    @Override
    public Map<String, Object> provide(String sessionId, String userId, String terminalSessionId, List<Map<String, Object>> messageHistory) {
        Map<String, Object> result = new HashMap<>();
        
        if (messageHistory != null) {
            messageHistory.stream()
                .filter(m -> "user".equals(m.get("role")))
                .findFirst()
                .ifPresent(m -> result.put("taskDescription", m.get("content")));
        }
        
        return result;
    }
}
