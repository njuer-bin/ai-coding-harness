package com.codingagent.guardrail;

import com.codingagent.model.Action;
import com.codingagent.model.enums.GuardrailResult;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GuardrailTest {
    @Test
    void testBlockDangerousCommand() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "rm -rf /"));
        assertEquals(GuardrailResult.BLOCK, guardrail.check(action));
    }

    @Test
    void testBlockDangerousCommandStar() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "rm -rf /*"));
        assertEquals(GuardrailResult.BLOCK, guardrail.check(action));
    }

    @Test
    void testBlockDangerousWrite() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("WRITE_FILE", Map.of("path", "/etc/passwd"));
        assertEquals(GuardrailResult.BLOCK, guardrail.check(action));
    }

    @Test
    void testBlockSystemPathWrite() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("WRITE_FILE", Map.of("path", "/usr/local/config.txt"));
        assertEquals(GuardrailResult.BLOCK, guardrail.check(action));
    }

    @Test
    void testRequireHITLForPush() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "git push origin main"));
        assertEquals(GuardrailResult.REQUIRE_HITL, guardrail.check(action));
    }

    @Test
    void testRequireHITLForCommit() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "git commit -m 'fix'"));
        assertEquals(GuardrailResult.REQUIRE_HITL, guardrail.check(action));
    }

    @Test
    void testRequireHITLForDeploy() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "deploy --prod"));
        assertEquals(GuardrailResult.REQUIRE_HITL, guardrail.check(action));
    }

    @Test
    void testAllowSafeCommand() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("READ_FILE", Map.of("path", "test.txt"));
        assertEquals(GuardrailResult.ALLOW, guardrail.check(action));
    }

    @Test
    void testAllowSafeShellCommand() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "echo hello"));
        assertEquals(GuardrailResult.ALLOW, guardrail.check(action));
    }

    @Test
    void testEmptyCommandAllowed() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", ""));
        assertEquals(GuardrailResult.ALLOW, guardrail.check(action));
    }

    @Test
    void testEmptyPathAllowed() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("WRITE_FILE", Map.of("path", ""));
        assertEquals(GuardrailResult.ALLOW, guardrail.check(action));
    }

    @Test
    void testBlockMkfsCommand() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "mkfs.ext4 /dev/sda1"));
        assertEquals(GuardrailResult.BLOCK, guardrail.check(action));
    }

    @Test
    void testAllowSafeWriteToLocalPath() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("WRITE_FILE", Map.of("path", "./src/main/java/test.txt"));
        assertEquals(GuardrailResult.ALLOW, guardrail.check(action));
    }
}