package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import java.util.Map;

public class GitTool implements Tool {
    @Override
    public String getName() { return "GIT"; }

    @Override
    public ToolResult execute(Action action) {
        String subcommand = (String) action.getParameters().getOrDefault("subcommand", "status");
        ExecuteShellTool shell = new ExecuteShellTool();
        Action shellAction = new Action("EXECUTE_COMMAND", Map.of("command", "git " + subcommand));
        return shell.execute(shellAction);
    }
}