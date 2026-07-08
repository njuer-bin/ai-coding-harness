package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class SearchCodeTool implements Tool {

    @Override
    public String getName() { return "SEARCH"; }

    @Override
    public ToolResult execute(Action action) {
        try {
            String keyword = (String) action.getParameters().get("keyword");
            if (keyword == null) {
                return new ToolResult(false, -1, "", "Missing required parameter: keyword", 0L);
            }
            String path = (String) action.getParameters().getOrDefault("path", ".");
            List<String> results = Files.walk(Path.of(path))
                .filter(Files::isRegularFile)
                .filter(p -> {
                    try { return Files.readString(p).contains(keyword); }
                    catch (Exception e) { return false; }
                })
                .map(p -> p.toString() + ":" + keyword)
                .collect(Collectors.toList());
            String stdout = results.isEmpty() ? "" : String.join("\n", results);
            return new ToolResult(true, 0, stdout, "", 0L);
        } catch (Exception e) {
            return new ToolResult(false, -1, "", e.getMessage(), 0L);
        }
    }
}