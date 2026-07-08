package com.codingagent.model;

public class LLMResponse {
    private Action action;
    private String reasoning;
    private boolean stopRequested;

    public LLMResponse() {}

    public LLMResponse(Action action, String reasoning, boolean stopRequested) {
        this.action = action;
        this.reasoning = reasoning;
        this.stopRequested = stopRequested;
    }

    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public boolean isStopRequested() { return stopRequested; }
    public void setStopRequested(boolean stopRequested) { this.stopRequested = stopRequested; }
}