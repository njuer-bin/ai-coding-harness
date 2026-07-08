package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import java.nio.file.Files;
import java.nio.file.Path;

public class WriteFileTool implements Tool {
    @Override
    public String getName() { return "WRITE_FILE"; }

    @Override
    public ToolResult execute(Action action) {
        try {
            String path = (String) action.getParameters().get("path");
            String content = (String) action.getParameters().get("content");
            Path target = Path.of(path);
            Files.createDirectories(target.getParent());
            Files.writeString(target, content);
            return new ToolResult(true, 0, "Written: " + path, "", 0L);
        } catch (Exception e) {
            return new ToolResult(false, -1, "", e.getMessage(), 0L);
        }
    }
}