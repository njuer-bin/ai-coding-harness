package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;

import java.util.HashMap;
import java.util.Map;

public class ToolRegistry {
    private final Map<String, Tool> tools = new HashMap<>();

    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    public ToolResult execute(Action action) {
        Tool tool = tools.get(action.getType());
        if (tool == null) {
            return new ToolResult(false, -1, "", "Unknown tool: " + action.getType(), 0L);
        }
        return tool.execute(action);
    }
}