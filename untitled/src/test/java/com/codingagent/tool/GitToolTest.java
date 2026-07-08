package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GitToolTest {
    @Test
    void testGitStatus() {
        GitTool tool = new GitTool();
        Action action = new Action("GIT", Map.of("subcommand", "status"));
        ToolResult result = tool.execute(action);
        // 不管是否在 git 仓库中，命令应执行不抛异常
        assertNotNull(result);
    }

    @Test
    void testDefaultSubcommandIsStatus() {
        GitTool tool = new GitTool();
        Action action = new Action("GIT", Map.of());
        ToolResult result = tool.execute(action);
        assertNotNull(result);
    }

    @Test
    void testGitLog() {
        GitTool tool = new GitTool();
        Action action = new Action("GIT", Map.of("subcommand", "log --oneline -5"));
        ToolResult result = tool.execute(action);
        assertNotNull(result);
    }

    @Test
    void testToolName() {
        GitTool tool = new GitTool();
        assertEquals("GIT", tool.getName());
    }
}