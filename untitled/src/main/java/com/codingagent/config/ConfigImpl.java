package com.codingagent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigImpl implements Config {
    private static final String FILE_NAME = "config.json";

    private final File configFile;
    private final ObjectMapper mapper;
    private final Map<String, Object> data;

    private static final String DEFAULT_MODEL = "deepseek-chat";
    private static final String DEFAULT_API_URL = "https://api.deepseek.com/v1";
    private static final int DEFAULT_TIMEOUT_MS = 30000;
    private static final boolean DEFAULT_DEBUG = false;
    private static final String DEFAULT_API_KEY = "";
    private static final int DEFAULT_MAX_ITERATIONS = 10;

    public ConfigImpl(String configDir) {
        this.configFile = new File(configDir, FILE_NAME);
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.data = new HashMap<>(loadFromFile());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadFromFile() {
        if (!configFile.exists()) {
            return new HashMap<>();
        }
        try {
            Object parsed = mapper.readValue(configFile, Object.class);
            if (parsed instanceof Map) {
                return (Map<String, Object>) parsed;
            }
            return new HashMap<>();
        } catch (IOException e) {
            // corrupted file — silently use defaults
            return new HashMap<>();
        }
    }

    private void saveToFile() {
        try {
            mapper.writeValue(configFile, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist config", e);
        }
    }

    private String getString(String key, String defaultValue) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : defaultValue;
    }

    private int getInt(String key, int defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    @Override
    public String getApiKey() {
        return getString("apiKey", DEFAULT_API_KEY);
    }

    @Override
    public void setApiKey(String apiKey) {
        data.put("apiKey", apiKey);
        saveToFile();
    }

    @Override
    public String getDefaultModel() {
        return getString("defaultModel", DEFAULT_MODEL);
    }

    @Override
    public void setDefaultModel(String model) {
        data.put("defaultModel", model);
        saveToFile();
    }

    @Override
    public String getLlmApiUrl() {
        return getString("llmApiUrl", DEFAULT_API_URL);
    }

    @Override
    public void setLlmApiUrl(String url) {
        data.put("llmApiUrl", url);
        saveToFile();
    }

    @Override
    public int getDefaultTimeoutMs() {
        return getInt("defaultTimeoutMs", DEFAULT_TIMEOUT_MS);
    }

    @Override
    public void setDefaultTimeoutMs(int timeoutMs) {
        data.put("defaultTimeoutMs", timeoutMs);
        saveToFile();
    }

    @Override
    public boolean isDebugMode() {
        return getBoolean("debugMode", DEFAULT_DEBUG);
    }

    @Override
    public void setDebugMode(boolean debug) {
        data.put("debugMode", debug);
        saveToFile();
    }

    @Override
    public int getMaxIterations() {
        return getInt("maxIterations", DEFAULT_MAX_ITERATIONS);
    }

    @Override
    public void setMaxIterations(int maxIterations) {
        data.put("maxIterations", maxIterations);
        saveToFile();
    }
}