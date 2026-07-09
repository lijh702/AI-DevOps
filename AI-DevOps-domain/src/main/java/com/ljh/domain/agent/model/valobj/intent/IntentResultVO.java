package com.ljh.domain.agent.model.valobj.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntentResultVO {
    //目标意图类型
    private IntentTypeEnumVO intent;
    // 置信度 [0.0,1.0]
    private double confidence;
    /**
     * key: 实体类型（service, file, command 等）
     * value: 实体值
     */
    private Map<String, String> entities;
    // llm原始调用
    private String rawResponse;
}
