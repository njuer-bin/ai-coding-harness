package com.codingagent.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void testDefaultValues(@TempDir Path tempDir) {
        Config config = new ConfigImpl(tempDir.toString());
        assertEquals("deepseek-chat", config.getDefaultModel());
        assertEquals("https://api.deepseek.com/v1", config.getLlmApiUrl());
        assertEquals(30000, config.getDefaultTimeoutMs());
        assertFalse(config.isDebugMode());
        assertEquals("", config.getApiKey());
    }

    @Test
    void testConfigFileCorrupted(@TempDir Path tempDir) throws Exception {
        // Write invalid JSON to the config file
        Files.writeString(tempDir.resolve("config.json"), "not-valid-json{{{");
        Config config = new ConfigImpl(tempDir.toString());
        // Should fall back to defaults without throwing
        assertEquals("deepseek-chat", config.getDefaultModel());
        assertEquals("https://api.deepseek.com/v1", config.getLlmApiUrl());
        assertEquals(30000, config.getDefaultTimeoutMs());
        assertFalse(config.isDebugMode());
        assertEquals("", config.getApiKey());
    }
}