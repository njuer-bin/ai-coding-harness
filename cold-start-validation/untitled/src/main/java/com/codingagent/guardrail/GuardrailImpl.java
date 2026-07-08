package com.codingagent.guardrail;

import com.codingagent.model.Action;
import com.codingagent.model.enums.GuardrailResult;
import java.util.List;

public class GuardrailImpl implements Guardrail {
    private static final List<String> DANGEROUS_COMMANDS = List.of(
        "rm -rf /", "rm -rf /*", "mkfs", "dd if=", ">:",
        "format", "fdisk", "shutdown", "reboot", "init 0"
    );
    private static final List<String> SENSITIVE_PREFIXES = List.of(
        "git push", "git commit", "docker push", "npm publish", "deploy"
    );
    private static final List<String> DANGEROUS_PATHS = List.of(
        "/etc/", "/usr/", "/bin/", "/boot/", "/dev/", "/sys/"
    );

    @Override
    public GuardrailResult check(Action action) {
        String type = action.getType();
        String command = (String) action.getParameters().getOrDefault("command", "");
        String path = (String) action.getParameters().getOrDefault("path", "");

        // 空命令且空路径 → ALLOW
        if (command.isEmpty() && path.isEmpty()) {
            return GuardrailResult.ALLOW;
        }

        // 检查危险命令
        if ("EXECUTE_COMMAND".equals(type)) {
            for (String dangerous : DANGEROUS_COMMANDS) {
                if (command.contains(dangerous)) {
                    return GuardrailResult.BLOCK;
                }
            }
            for (String sensitive : SENSITIVE_PREFIXES) {
                if (command.startsWith(sensitive)) {
                    return GuardrailResult.REQUIRE_HITL;
                }
            }
        }

        // 检查高危文件写入路径
        if ("WRITE_FILE".equals(type)) {
            for (String dangerousPath : DANGEROUS_PATHS) {
                if (path.startsWith(dangerousPath)) {
                    return GuardrailResult.BLOCK;
                }
            }
        }

        return GuardrailResult.ALLOW;
    }
}