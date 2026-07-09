package com.codingagent.llm;

import com.codingagent.model.Context;
import com.codingagent.model.LLMResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeepSeekProviderTest {

    @Test
    void testSendWithInvalidKeyReturnsError() {
        DeepSeekProvider provider = new DeepSeekProvider("invalid-key", "deepseek-chat");
        LLMResponse response = provider.send(new Context());
        assertNull(response.getAction());
        assertTrue(response.isStopRequested());
    }

    @Test
    void testConstructorFallsBackToEnvVar() {
        // When apiKey is null, should use env var (which might not be set in test)
        DeepSeekProvider provider = new DeepSeekProvider(null, "deepseek-chat");
        assertNotNull(provider);
    }

    @Test
    void testConstructorWithEmptyApiKey() {
        // When apiKey is empty, should use env var
        DeepSeekProvider provider = new DeepSeekProvider("", "deepseek-chat");
        assertNotNull(provider);
    }

    @Test
    void testParseResponseWithValidJson() {
        DeepSeekProvider provider = new DeepSeekProvider("test-key", "deepseek-chat");
        String json = "{\n" +
                "  \"id\": \"test-id\",\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"role\": \"assistant\",\n" +
                "        \"content\": \"I will search for the file.\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        LLMResponse response = provider.parseResponse(json);
        assertNotNull(response);
        assertEquals("I will search for the file.", response.getReasoning());
        assertNull(response.getAction());
        assertFalse(response.isStopRequested());
    }

    @Test
    void testParseResponseWithAction() {
        DeepSeekProvider provider = new DeepSeekProvider("test-key", "deepseek-chat");
        String json = "{\n" +
                "  \"id\": \"test-id\",\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"role\": \"assistant\",\n" +
                "        \"content\": \"ACTION: read_file\\nfile_path=/home/test.txt\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        LLMResponse response = provider.parseResponse(json);
        assertNotNull(response);
        assertNotNull(response.getAction());
        assertEquals("READ_FILE", response.getAction().getType());
        assertFalse(response.isStopRequested());
    }

    @Test
    void testParseResponseWithStop() {
        DeepSeekProvider provider = new DeepSeekProvider("test-key", "deepseek-chat");
        String json = "{\n" +
                "  \"id\": \"test-id\",\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"role\": \"assistant\",\n" +
                "        \"content\": \"Task is complete. STOP\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        LLMResponse response = provider.parseResponse(json);
        assertNotNull(response);
        assertTrue(response.isStopRequested());
    }

    @Test
    void testParseResponseWithEmptyChoices() {
        DeepSeekProvider provider = new DeepSeekProvider("test-key", "deepseek-chat");
        String json = "{\n" +
                "  \"id\": \"test-id\",\n" +
                "  \"choices\": []\n" +
                "}";
        LLMResponse response = provider.parseResponse(json);
        assertNotNull(response);
        assertNull(response.getAction());
        assertFalse(response.isStopRequested());
    }

    @Test
    void testParseResponseWithInvalidJson() {
        DeepSeekProvider provider = new DeepSeekProvider("test-key", "deepseek-chat");
        LLMResponse response = provider.parseResponse("invalid json");
        assertNotNull(response);
        assertTrue(response.isStopRequested());
    }
}