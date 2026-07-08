package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobListFilesToolTest {

    @Test
    void testGlobFindsPomFile(@TempDir Path tempDir) throws Exception {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, "<project/>");
        GlobListFilesTool tool = new GlobListFilesTool();
        Action action = new Action("GLOB", Map.of("pattern", "*.xml", "baseDir", tempDir.toString()));
        ToolResult result = tool.execute(action);
        assertTrue(result.isSuccess());
        assertTrue(result.getStdout().contains("pom.xml"));
    }

    @Test
    void testGlobFindsNoMatch(@TempDir Path tempDir) throws Exception {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, "<project/>");
        GlobListFilesTool tool = new GlobListFilesTool();
        Action action = new Action("GLOB", Map.of("pattern", "*.java", "baseDir", tempDir.toString()));
        ToolResult result = tool.execute(action);
        assertTrue(result.isSuccess());
        assertEquals("", result.getStdout());
    }

    @Test
    void testGlobDefaultBaseDir(@TempDir Path tempDir) throws Exception {
        // Create a temp file in a subdirectory
        Path subDir = tempDir.resolve("sub");
        Files.createDirectories(subDir);
        Path testFile = subDir.resolve("test.txt");
        Files.writeString(testFile, "hello");

        GlobListFilesTool tool = new GlobListFilesTool();
        Action action = new Action("GLOB", Map.of("pattern", "**/*.txt", "baseDir", tempDir.toString()));
        ToolResult result = tool.execute(action);
        assertTrue(result.isSuccess());
        assertTrue(result.getStdout().contains("test.txt"));
    }

    @Test
    void testGlobInvalidPattern() {
        GlobListFilesTool tool = new GlobListFilesTool();
        Action action = new Action("GLOB", Map.of("pattern", "[invalid"));
        ToolResult result = tool.execute(action);
        assertFalse(result.isSuccess());
    }

    @Test
    void testGlobMissingPattern() {
        GlobListFilesTool tool = new GlobListFilesTool();
        Action action = new Action("GLOB", Map.of());
        ToolResult result = tool.execute(action);
        assertFalse(result.isSuccess());
    }
}