package com.codingagent.config;

public interface Config {
    String getApiKey();
    void setApiKey(String apiKey);
    String getDefaultModel();
    void setDefaultModel(String model);
    String getLlmApiUrl();
    void setLlmApiUrl(String url);
    int getDefaultTimeoutMs();
    void setDefaultTimeoutMs(int timeoutMs);
    boolean isDebugMode();
    void setDebugMode(boolean debug);
}