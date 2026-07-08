package com.ljh.domain.agent.service.intent;

import com.ljh.domain.agent.model.valobj.intent.ConversationContextVO;
import com.ljh.domain.agent.model.valobj.intent.IntentResultVO;

public interface IIntentClassifier {
    IntentResultVO classify(String message, ConversationContextVO context);
}
