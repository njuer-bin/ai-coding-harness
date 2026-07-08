package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ExecuteShellToolTest {
    @Test
    void testEchoCommand() {
        ExecuteShellTool tool = new ExecuteShellTool();
        Action action = new Action("EXECUTE_COMMAND", Map.of(
            "command", "echo hello"
        ));
        ToolResult result = tool.execute(action);
        assertTrue(result.isSuccess());
        assertTrue(result.getStdout().contains("hello"));
    }

    @Test
    void testCommandFailure() {
        ExecuteShellTool tool = new ExecuteShellTool();
        Action action = new Action("EXECUTE_COMMAND", Map.of(
            "command", "exit 1"
        ));
        ToolResult result = tool.execute(action);
        assertFalse(result.isSuccess());
        assertEquals(1, result.getExitCode());
    }
}