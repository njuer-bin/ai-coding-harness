package com.codingagent.model;

import com.codingagent.model.enums.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    @Test
    void testGuardrailResultValues() {
        assertNotNull(GuardrailResult.valueOf("ALLOW"));
        assertNotNull(GuardrailResult.valueOf("BLOCK"));
        assertNotNull(GuardrailResult.valueOf("REQUIRE_HITL"));
    }

    @Test
    void testFeedbackStatusValues() {
        assertNotNull(FeedbackStatus.valueOf("PASS"));
        assertNotNull(FeedbackStatus.valueOf("FAIL"));
        assertNotNull(FeedbackStatus.valueOf("TOOL_ERROR"));
    }

    @Test
    void testFailureCategoryValues() {
        assertNotNull(FailureCategory.valueOf("COMPILE_ERROR"));
        assertNotNull(FailureCategory.valueOf("TEST_FAILURE"));
        assertNotNull(FailureCategory.valueOf("LINT_ERROR"));
        assertNotNull(FailureCategory.valueOf("TIMEOUT"));
        assertNotNull(FailureCategory.valueOf("EXECUTION_ERROR"));
        assertNotNull(FailureCategory.valueOf("UNKNOWN"));
    }

    @Test
    void testActionCreation() {
        Action action = new Action("READ_FILE", Map.of("path", "/test.txt"));
        assertEquals("READ_FILE", action.getType());
        assertEquals("/test.txt", action.getParameters().get("path"));
    }

    @Test
    void testActionDefaultConstructor() {
        Action action = new Action();
        action.setType("WRITE_FILE");
        action.setParameters(Map.of("content", "hello"));
        assertEquals("WRITE_FILE", action.getType());
        assertEquals("hello", action.getParameters().get("content"));
    }

    @Test
    void testToolResultCreation() {
        ToolResult result = new ToolResult(true, 0, "output", "", 100L);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getExitCode());
        assertEquals("output", result.getStdout());
        assertEquals("", result.getStderr());
        assertEquals(100L, result.getDurationMs());
    }

    @Test
    void testToolResultWithStructuredOutput() {
        ToolResult result = new ToolResult(true, 0, "output", "", 100L);
        result.setStructuredOutput(Map.of("warnings", 3));
        result.setErrorLines(List.of("line 5: error"));
        assertEquals(3, result.getStructuredOutput().get("warnings"));
        assertEquals(1, result.getErrorLines().size());
    }

    @Test
    void testToolResultDefaultConstructor() {
        ToolResult result = new ToolResult();
        result.setSuccess(false);
        result.setExitCode(-1);
        assertFalse(result.isSuccess());
        assertEquals(-1, result.getExitCode());
    }

    @Test
    void testMessageCreation() {
        Message msg = new Message("USER", "hello");
        assertEquals("USER", msg.getRole());
        assertEquals("hello", msg.getContent());
        assertTrue(msg.getTimestamp() > 0);
    }

    @Test
    void testMessageDefaultConstructor() {
        Message msg = new Message();
        msg.setRole("ASSISTANT");
        msg.setContent("world");
        assertEquals("ASSISTANT", msg.getRole());
        assertEquals("world", msg.getContent());
    }

    @Test
    void testLLMResponseCreation() {
        Action action = new Action("READ_FILE", Map.of("path", "test.txt"));
        LLMResponse resp = new LLMResponse(action, "need to read file", false);
        assertEquals(action, resp.getAction());
        assertFalse(resp.isStopRequested());
        assertEquals("need to read file", resp.getReasoning());
    }

    @Test
    void testLLMResponseDefaultConstructor() {
        LLMResponse resp = new LLMResponse();
        resp.setStopRequested(true);
        assertTrue(resp.isStopRequested());
    }

    @Test
    void testFeedbackCreation() {
        Feedback fb = new Feedback(FeedbackStatus.PASS, FailureCategory.COMPILE_ERROR, "ok", 0, false);
        assertEquals(FeedbackStatus.PASS, fb.getStatus());
        assertEquals(FailureCategory.COMPILE_ERROR, fb.getCategory());
        assertEquals("ok", fb.getDetail());
        assertEquals(0, fb.getRetryCount());
        assertFalse(fb.isShouldRetry());
    }

    @Test
    void testFeedbackDefaultConstructor() {
        Feedback fb = new Feedback();
        fb.setStatus(FeedbackStatus.FAIL);
        fb.setShouldRetry(true);
        assertEquals(FeedbackStatus.FAIL, fb.getStatus());
        assertTrue(fb.isShouldRetry());
    }

    @Test
    void testContextCreation() {
        Context ctx = new Context();
        ctx.setTaskDescription("do something");
        assertEquals("do something", ctx.getTaskDescription());
        assertNotNull(ctx.getConversation());
        assertNotNull(ctx.getPreviousFeedback());
        assertNotNull(ctx.getRelevantMemories());
    }

    @Test
    void testMemoryEntryCreation() {
        MemoryEntry entry = new MemoryEntry();
        entry.setId("1");
        entry.setContent("Project uses Java 21");
        entry.setType("CONVENTION");
        entry.setTags(List.of("java", "version"));
        assertEquals("1", entry.getId());
        assertEquals("Project uses Java 21", entry.getContent());
        assertEquals("CONVENTION", entry.getType());
        assertEquals(2, entry.getTags().size());
    }
}