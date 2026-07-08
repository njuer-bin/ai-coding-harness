package com.codingagent.llm;

import com.codingagent.model.Context;
import com.codingagent.model.LLMResponse;
import java.util.LinkedList;
import java.util.Queue;

public class MockLLM implements LLMProvider {
    private final Queue<LLMResponse> responses = new LinkedList<>();

    public void setNextResponse(LLMResponse response) {
        responses.add(response);
    }

    @Override
    public LLMResponse send(Context context) {
        LLMResponse response = responses.poll();
        if (response == null) {
            throw new IllegalStateException("No preset response available");
        }
        return response;
    }
}