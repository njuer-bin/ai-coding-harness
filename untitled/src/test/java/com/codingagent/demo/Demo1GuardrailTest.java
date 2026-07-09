package com.codingagent.demo;

import com.codingagent.model.Action;
import com.codingagent.model.enums.GuardrailResult;
import com.codingagent.guardrail.GuardrailImpl;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Demo1GuardrailTest {
    @Test void testBlockDangerousCommand() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "rm -rf /"));
        assertEquals(GuardrailResult.BLOCK, guardrail.check(action));
    }

    @Test void testRequireHITLForPush() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "git push origin main"));
        assertEquals(GuardrailResult.REQUIRE_HITL, guardrail.check(action));
    }

    @Test void testAllowSafeOperation() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("READ_FILE", Map.of("path", "test.txt"));
        assertEquals(GuardrailResult.ALLOW, guardrail.check(action));
    }

    @Test void testBlockDangerousWrite() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("WRITE_FILE", Map.of("path", "/etc/passwd", "content", "hack"));
        assertEquals(GuardrailResult.BLOCK, guardrail.check(action));
    }
}