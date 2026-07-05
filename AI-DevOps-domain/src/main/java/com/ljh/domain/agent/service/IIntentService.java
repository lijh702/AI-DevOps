package com.ljh.domain.agent.service;

import com.ljh.domain.agent.model.valobj.intent.IntentResultVO;

public interface IIntentService {
    IntentResultVO classify(String sessionId, String userId, String message);
}
