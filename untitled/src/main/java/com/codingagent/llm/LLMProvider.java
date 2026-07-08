package com.codingagent.llm;

import com.codingagent.model.Context;
import com.codingagent.model.LLMResponse;

public interface LLMProvider {
    LLMResponse send(Context context);
}