package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class LintCheckToolTest {
    @Test
    void testLintCheckRuns() {
        LintCheckTool tool = new LintCheckTool();
        Action action = new Action("LINT_CHECK", Map.of("target", "."));
        ToolResult result = tool.execute(action);
        assertNotNull(result);
    }

    @Test
    void testToolName() {
        LintCheckTool tool = new LintCheckTool();
        assertEquals("LINT_CHECK", tool.getName());
    }

    @Test
    void testWarningCountIsSet() {
        LintCheckTool tool = new LintCheckTool();
        Action action = new Action("LINT_CHECK", Map.of("target", "."));
        ToolResult result = tool.execute(action);
        assertNotNull(result);
        assertNotNull(result.getStructuredOutput());
        assertTrue(result.getStructuredOutput().containsKey("warnings"));
    }
}