package com.ljh.domain.agent.service.intent;

import com.ljh.domain.agent.model.valobj.intent.ConversationContextVO;
import com.ljh.domain.agent.model.valobj.intent.IntentHistoryEntryVO;
import com.ljh.domain.agent.model.valobj.intent.IntentResultVO;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话上下文管理器，负责维护和更新多轮对话中的状态信息
 * 承担了上下文创建、更新、过期清理等职责
 */
@Component
public class ContextTracker {
    private static final int WINDOW_SIZE = 10;

    // 线程安全的缓存容器，以 sessionId 为键，存储所有活跃会话的上下文对象
    private final Map<String, ConversationContextVO> contexts = new ConcurrentHashMap<>();

    public ConversationContextVO getContext(String sessionId) {
        return contexts.computeIfAbsent(sessionId, id -> ConversationContextVO.builder()
            .recentIntents(new LinkedList<>())
            .turnCount(0)
            .sessionStartTime(System.currentTimeMillis())
            .build());
    }

    public void updateContext(String sessionId, IntentResultVO result) {
        ConversationContextVO ctx = getContext(sessionId);
        // 添加意图历史
        ctx.getRecentIntents().addLast(IntentHistoryEntryVO.builder()
            .intent(result.getIntent())
            .confidence(result.getConfidence())
            .timestamp(System.currentTimeMillis())
            .build());
        // 上下窗口裁剪
        if (ctx.getRecentIntents().size() > WINDOW_SIZE) {
            ctx.getRecentIntents().removeFirst();
        }
        ctx.setTurnCount(ctx.getTurnCount() + 1);
        ctx.setLastIntent(result.getIntent());
    }

    public void clear(String sessionId) {
        contexts.remove(sessionId);
    }
}
