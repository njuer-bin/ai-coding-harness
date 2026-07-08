package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class GlobListFilesTool implements Tool {

    @Override
    public String getName() { return "GLOB"; }

    @Override
    public ToolResult execute(Action action) {
        try {
            String pattern = (String) action.getParameters().get("pattern");
            if (pattern == null) {
                return new ToolResult(false, -1, "", "Missing required parameter: pattern", 0L);
            }
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            String baseDir = (String) action.getParameters().getOrDefault("baseDir", ".");
            Path base = Path.of(baseDir);
            List<String> matches = Files.walk(base)
                .filter(p -> !p.equals(base))
                .filter(p -> matcher.matches(base.relativize(p)))
                .map(Path::toString)
                .collect(Collectors.toList());
            String stdout = matches.isEmpty() ? "" : String.join("\n", matches);
            return new ToolResult(true, 0, stdout, "", 0L);
        } catch (Exception e) {
            return new ToolResult(false, -1, "", e.getMessage(), 0L);
        }
    }
}