package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import java.util.Map;

public class LintCheckTool implements Tool {
    @Override
    public String getName() { return "LINT_CHECK"; }

    @Override
    public ToolResult execute(Action action) {
        String target = (String) action.getParameters().getOrDefault("target", ".");
        ExecuteShellTool shell = new ExecuteShellTool();
        // 尝试运行 mvn checkstyle:check，若失败则回退到 javac -Xlint
        Action shellAction = new Action("EXECUTE_COMMAND", Map.of("command",
            "mvn checkstyle:check 2>/dev/null || javac -Xlint " + target + " 2>&1 || true"));
        ToolResult result = shell.execute(shellAction);
        // 解析告警行数
        long warningCount = result.getStdout().lines()
            .filter(l -> l.contains("warning") || l.contains("WARN") || l.contains("Checkstyle"))
            .count();
        result.setStructuredOutput(Map.of("warnings", (int) warningCount));
        if (warningCount > 0) {
            result.setErrorLines(result.getStdout().lines()
                .filter(l -> l.contains("warning") || l.contains("error"))
                .toList());
        }
        return result;
    }
}