package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class WriteFileToolTest {
    @Test
    void testWriteFile(@TempDir Path tempDir) {
        Path file = tempDir.resolve("output.txt");
        WriteFileTool tool = new WriteFileTool();
        Action action = new Action("WRITE_FILE", Map.of(
            "path", file.toString(),
            "content", "hello"
        ));
        ToolResult result = tool.execute(action);
        assertTrue(result.isSuccess());
        assertTrue(Files.exists(file));
    }
}