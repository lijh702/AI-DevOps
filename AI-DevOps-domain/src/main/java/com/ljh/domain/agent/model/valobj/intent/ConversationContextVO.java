package com.ljh.domain.agent.model.valobj.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 会话级别：每个 sessionId 对应一个 ConversationContextVO 实例，贯穿整个对话生命周期。
 * 状态聚合：集中管理会话的元信息（轮次、时间）、历史意图、最近实体，避免分散在多个服务中。
 * 上下文增强：为后续的意图识别、实体填充、任务拆解等提供历史参考，使系统具备多轮对话能力。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationContextVO {

    // 存储最近若干轮的用户意图历史，用于意图流转分析、上下文相关性判断。使用 LinkedList 便于在头部插入新记录，尾部淘汰旧记录
    private LinkedList<IntentHistoryEntryVO> recentIntents;

    // 当前会话的总对话轮次计数
    private int turnCount;

    // 会话开始时间戳
    private long sessionStartTime;

    // 上一轮识别出的用户意图类型
    private IntentTypeEnumVO lastIntent;

    /**
     * 最近实体追踪（用于指代消解，如用户说"再部署一次"，可从中取出上次部署的服务名）
     * key: 实体类型（service, file, command 等）
     * value: 实体值
     */
    @Builder.Default
    private Map<String, String> lastEntities = new HashMap<>();

    /**
     * 获取最近的实体值
     * @param entityKey 实体类型
     * @return 实体值，不存在返回 null
     */
    public String getLastEntity(String entityKey) {
        return lastEntities != null ? lastEntities.get(entityKey) : null;
    }

    /**
     * 记录实体
     */
    public void putEntity(String key, String value) {
        if (lastEntities == null) lastEntities = new HashMap<>();
        if (key != null && value != null) {
            lastEntities.put(key, value);
        }
    }
}
