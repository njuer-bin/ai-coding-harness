package com.codingagent.llm;

import com.codingagent.model.Action;
import com.codingagent.model.Context;
import com.codingagent.model.Feedback;
import com.codingagent.model.LLMResponse;
import com.codingagent.model.MemoryEntry;
import com.codingagent.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * LLMProvider implementation that calls the DeepSeek API with network retry logic.
 */
public class DeepSeekProvider implements LLMProvider {

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 2000;
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final String apiKey;
    private final String modelName;
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new DeepSeekProvider.
     *
     * @param apiKey    the API key, or null/empty to use DEEPSEEK_API_KEY env var
     * @param modelName the model name (e.g. "deepseek-chat")
     */
    public DeepSeekProvider(String apiKey, String modelName) {
        this.apiKey = (apiKey != null && !apiKey.isEmpty())
                ? apiKey
                : System.getenv("DEEPSEEK_API_KEY");
        this.modelName = modelName;
        this.client = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public LLMResponse send(Context context) {
        Exception lastException = null;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                String requestBody = buildRequestBody(context);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .timeout(TIMEOUT)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseResponse(response.body());
                } else {
                    // Non-200 status: treat as error, retry if possible
                    lastException = new RuntimeException("HTTP " + response.statusCode()
                            + ": " + response.body());
                }
            } catch (Exception e) {
                lastException = e;
            }

            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return new LLMResponse(null,
                "API error after " + (MAX_RETRIES + 1) + " attempts: "
                        + lastException.getMessage(),
                true);
    }

    /**
     * Builds the JSON request body for the DeepSeek chat completions API.
     */
    private String buildRequestBody(Context context) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", modelName);

        ArrayNode messagesArray = root.putArray("messages");

        // System prompt from task description
        if (context.getTaskDescription() != null && !context.getTaskDescription().isEmpty()) {
            ObjectNode sysMsg = messagesArray.addObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", context.getTaskDescription());
        }

        // Conversation history
        List<Message> conversation = context.getConversation();
        if (conversation != null) {
            for (Message msg : conversation) {
                ObjectNode msgNode = messagesArray.addObject();
                msgNode.put("role", msg.getRole().toLowerCase());
                msgNode.put("content", msg.getContent());
            }
        }

        // Previous feedback
        List<Feedback> previousFeedback = context.getPreviousFeedback();
        if (previousFeedback != null && !previousFeedback.isEmpty()) {
            ObjectNode feedbackMsg = messagesArray.addObject();
            feedbackMsg.put("role", "user");
            StringBuilder feedbackContent = new StringBuilder("Previous feedback:\n");
            for (Feedback fb : previousFeedback) {
                feedbackContent.append("- Status: ").append(fb.getStatus())
                        .append(", Detail: ").append(fb.getDetail())
                        .append("\n");
            }
            feedbackMsg.put("content", feedbackContent.toString());
        }

        // Relevant memories
        List<MemoryEntry> relevantMemories = context.getRelevantMemories();
        if (relevantMemories != null && !relevantMemories.isEmpty()) {
            ObjectNode memoryMsg = messagesArray.addObject();
            memoryMsg.put("role", "user");
            StringBuilder memoryContent = new StringBuilder("Relevant memories:\n");
            for (MemoryEntry entry : relevantMemories) {
                memoryContent.append("- ").append(entry.getContent()).append("\n");
            }
            memoryMsg.put("content", memoryContent.toString());
        }

        return objectMapper.writeValueAsString(root);
    }

    /**
     * Parses the DeepSeek API JSON response into an LLMResponse.
     */
    LLMResponse parseResponse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);

            // Extract assistant message content
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    String content = message.get("content").asText();
                    if (content != null && !content.isEmpty()) {
                        return parseContent(content);
                    }
                }
            }

            return new LLMResponse(null, "No valid response content", false);
        } catch (Exception e) {
            return new LLMResponse(null, "Failed to parse response: " + e.getMessage(), true);
        }
    }

    /**
     * Parses the assistant's text content into an LLMResponse.
     * Attempts to extract an action from the content.
     */
    private LLMResponse parseContent(String content) {
        // Check for stop request
        if (content.toUpperCase().contains("<STOP>") || content.toUpperCase().contains("STOP")) {
            return new LLMResponse(null, content, true);
        }

        // Try to extract an action in the format: ACTION: type\nparam1=value1\nparam2=value2
        Action action = extractAction(content);
        if (action != null) {
            return new LLMResponse(action, content, false);
        }

        // No action found, return reasoning only
        return new LLMResponse(null, content, false);
    }

    /**
     * Attempts to extract an Action from the assistant's response content.
     * Looks for patterns like "ACTION: tool_name" or JSON action blocks.
     */
    private Action extractAction(String content) {
        // Pattern 1: ACTION: <type> followed by key=value pairs
        String[] lines = content.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.toUpperCase().startsWith("ACTION:")) {
                String type = trimmed.substring("ACTION:".length()).trim();
                if (!type.isEmpty()) {
                    Map<String, Object> params = new java.util.LinkedHashMap<>();
                    for (int j = i + 1; j < lines.length; j++) {
                        String line = lines[j].trim();
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String key = line.substring(0, eqIdx).trim();
                            String value = line.substring(eqIdx + 1).trim();
                            if (!key.isEmpty()) {
                                params.put(key, value);
                            }
                        }
                    }
                    return new Action(type, params);
                }
            }
        }

        return null;
    }
}