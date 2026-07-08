package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;

import java.util.HashMap;
import java.util.Map;

public class ToolRegistry {
    private final Map<String, Tool> tools = new HashMap<>();
    private long defaultTimeoutMs = 30000L;

    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    public void setDefaultTimeoutMs(long timeoutMs) {
        this.defaultTimeoutMs = timeoutMs;
    }

    public long getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }

    public ToolResult execute(Action action) {
        if (action == null) {
            return new ToolResult(false, -1, "", "Action must not be null", 0L);
        }
        Tool tool = tools.get(action.getType());
        if (tool == null) {
            return new ToolResult(false, -1, "", "Unknown tool: " + action.getType(), 0L);
        }
        return tool.execute(action);
    }
}