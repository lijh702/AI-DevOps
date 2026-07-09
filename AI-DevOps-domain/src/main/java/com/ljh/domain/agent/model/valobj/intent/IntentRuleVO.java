package com.ljh.domain.agent.model.valobj.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 规则定义
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntentRuleVO {
    //目标意图
    private IntentTypeEnumVO intent;
    // 关键词列表
    private List<String> keywords;
    // 正则模式列表
    private List<String> patterns;
    // 上下文加权映射
    private Map<List<String>, Double> contextBoost;
}
