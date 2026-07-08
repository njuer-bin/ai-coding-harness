package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReadFileTool implements Tool {
    @Override
    public String getName() { return "READ_FILE"; }

    @Override
    public ToolResult execute(Action action) {
        try {
            String path = (String) action.getParameters().get("path");
            String content = Files.readString(Path.of(path));
            return new ToolResult(true, 0, content, "", 0L);
        } catch (Exception e) {
            return new ToolResult(false, -1, "", e.getMessage(), 0L);
        }
    }
}