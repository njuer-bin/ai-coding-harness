package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;

public interface Tool {
    String getName();
    ToolResult execute(Action action);
    default long getTimeoutMs() { return 30000L; }
}