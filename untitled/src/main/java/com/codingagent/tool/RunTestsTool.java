package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import java.util.Map;

public class RunTestsTool implements Tool {

    @Override
    public String getName() { return "RUN_TESTS"; }

    @Override
    public ToolResult execute(Action action) {
        String command = (String) action.getParameters().getOrDefault("command", "mvn test");
        // Delegate to ExecuteShellTool for execution
        ExecuteShellTool shell = new ExecuteShellTool();
        Action shellAction = new Action("EXECUTE_COMMAND", Map.of("command", command));
        ToolResult result = shell.execute(shellAction);
        // Parse test result summary
        int passed = result.getStdout().contains("BUILD SUCCESS") ? 1 : 0;
        result.setStructuredOutput(Map.of("passed", passed, "exitCode", result.getExitCode()));
        return result;
    }
}