package com.codingagent.feedback;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import com.codingagent.model.Feedback;
import com.codingagent.model.enums.FeedbackStatus;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {
    @Test
    void testPassOnSuccess() {
        ValidatorImpl validator = new ValidatorImpl();
        ToolResult result = new ToolResult(true, 0, "ok", "", 100L);
        Action action = new Action("READ_FILE", Map.of("path", "test.txt"));
        Feedback fb = validator.validate(result, action);
        assertEquals(FeedbackStatus.PASS, fb.getStatus());
    }

    @Test
    void testFailOnNonZeroExit() {
        ValidatorImpl validator = new ValidatorImpl();
        ToolResult result = new ToolResult(false, 1, "", "error", 100L);
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "bad-command"));
        Feedback fb = validator.validate(result, action);
        assertEquals(FeedbackStatus.FAIL, fb.getStatus());
    }

    @Test
    void testToolErrorOnException() {
        ValidatorImpl validator = new ValidatorImpl();
        ToolResult result = new ToolResult(false, -1, "", "TIMEOUT", 100L);
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "slow-command"));
        Feedback fb = validator.validate(result, action);
        assertEquals(FeedbackStatus.TOOL_ERROR, fb.getStatus());
    }
}