package com.codingagent.engine;

import java.util.ArrayList;
import java.util.List;

public class EngineResult {
    private boolean success;
    private String summary;
    private List<String> log;

    public EngineResult() {}

    public EngineResult(boolean success, String summary) {
        this.success = success;
        this.summary = summary;
        this.log = new ArrayList<>();
    }

    public EngineResult(boolean success, String summary, List<String> log) {
        this.success = success;
        this.summary = summary;
        this.log = log != null ? log : new ArrayList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getLog() {
        return log;
    }

    public void setLog(List<String> log) {
        this.log = log;
    }
}