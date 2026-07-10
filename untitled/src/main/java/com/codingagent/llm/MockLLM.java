package com.codingagent.llm;

import com.codingagent.model.Context;
import com.codingagent.model.LLMResponse;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Mock implementation of LLMProvider that returns preset responses from a queue.
 * This class is NOT thread-safe — concurrent access to the queue is not guarded.
 * Intended for single-threaded testing and demo scenarios only.
 */
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