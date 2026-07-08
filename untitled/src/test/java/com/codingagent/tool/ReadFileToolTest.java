package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ReadFileToolTest {
    @Test
    void testReadExistingFile(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "hello world");
        ReadFileTool tool = new ReadFileTool();
        Action action = new Action("READ_FILE", Map.of("path", file.toString()));
        ToolResult result = tool.execute(action);
        assertTrue(result.isSuccess());
        assertEquals("hello world", result.getStdout());
    }

    @Test
    void testReadNonExistentFile() {
        ReadFileTool tool = new ReadFileTool();
        Action action = new Action("READ_FILE", Map.of("path", "/nonexistent/file.txt"));
        ToolResult result = tool.execute(action);
        assertFalse(result.isSuccess());
    }
}