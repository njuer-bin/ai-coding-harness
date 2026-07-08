package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SearchCodeToolTest {

    @Test
    void testSearchFindsKeyword(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("test.java");
        Files.writeString(file, "class HelloWorld {}");
        SearchCodeTool tool = new SearchCodeTool();
        Action action = new Action("SEARCH", Map.of("keyword", "HelloWorld", "path", tempDir.toString()));
        ToolResult result = tool.execute(action);
        assertTrue(result.isSuccess());
        assertTrue(result.getStdout().contains("test.java"));
    }

    @Test
    void testSearchNoMatch(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("test.java");
        Files.writeString(file, "class Foo {}");
        SearchCodeTool tool = new SearchCodeTool();
        Action action = new Action("SEARCH", Map.of("keyword", "HelloWorld", "path", tempDir.toString()));
        ToolResult result = tool.execute(action);
        assertTrue(result.isSuccess());
        assertEquals("", result.getStdout());
    }

    @Test
    void testSearchInSubdirectory(@TempDir Path tempDir) throws Exception {
        Path subDir = tempDir.resolve("src");
        Files.createDirectories(subDir);
        Path file = subDir.resolve("App.java");
        Files.writeString(file, "public class App {}");
        SearchCodeTool tool = new SearchCodeTool();
        Action action = new Action("SEARCH", Map.of("keyword", "App", "path", tempDir.toString()));
        ToolResult result = tool.execute(action);
        assertTrue(result.isSuccess());
        assertTrue(result.getStdout().contains("App.java"));
    }

    @Test
    void testSearchNonExistentPath() {
        SearchCodeTool tool = new SearchCodeTool();
        Action action = new Action("SEARCH", Map.of("keyword", "test", "path", "/nonexistent/path"));
        ToolResult result = tool.execute(action);
        assertFalse(result.isSuccess());
    }

    @Test
    void testSearchMissingKeyword(@TempDir Path tempDir) {
        SearchCodeTool tool = new SearchCodeTool();
        Action action = new Action("SEARCH", Map.of("path", tempDir.toString()));
        ToolResult result = tool.execute(action);
        assertFalse(result.isSuccess());
    }
}