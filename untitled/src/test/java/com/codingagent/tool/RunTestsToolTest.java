package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RunTestsToolTest {

    @Test
    void testRunTestsToolReturnsResult() {
        RunTestsTool tool = new RunTestsTool();
        Action action = new Action("RUN_TESTS", Map.of("command", "echo test-passed"));
        ToolResult result = tool.execute(action);
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void testRunTestsToolDefaultCommand() {
        RunTestsTool tool = new RunTestsTool();
        // When no command is provided, defaults to "mvn test"
        Action action = new Action("RUN_TESTS", Map.of());
        ToolResult result = tool.execute(action);
        // Should not crash; may fail if mvn not available, but returns a result
        assertNotNull(result);
    }

    @Test
    void testRunTestsToolFailure() {
        RunTestsTool tool = new RunTestsTool();
        Action action = new Action("RUN_TESTS", Map.of("command", "exit 1"));
        ToolResult result = tool.execute(action);
        assertFalse(result.isSuccess());
        assertEquals(1, result.getExitCode());
    }

    @Test
    void testRunTestsToolStructuredOutput() {
        RunTestsTool tool = new RunTestsTool();
        Action action = new Action("RUN_TESTS", Map.of("command", "echo test-passed"));
        ToolResult result = tool.execute(action);
        assertNotNull(result.getStructuredOutput());
        assertTrue(result.getStructuredOutput().containsKey("passed"));
        assertTrue(result.getStructuredOutput().containsKey("exitCode"));
    }
}