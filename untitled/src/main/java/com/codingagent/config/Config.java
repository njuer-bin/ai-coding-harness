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

    /**
     * Returns the maximum number of iterations for the Engine main loop.
     * Default is 10 if not configured.
     */
    default int getMaxIterations() {
        return 10;
    }

    /**
     * Sets the maximum number of iterations for the Engine main loop.
     */
    default void setMaxIterations(int maxIterations) {
        // no-op by default; implement in concrete classes that support it
    }
}