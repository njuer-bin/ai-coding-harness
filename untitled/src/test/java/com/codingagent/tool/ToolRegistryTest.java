package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ToolRegistryTest {
    @Test
    void testRegisterAndExecute() {
        ToolRegistry registry = new ToolRegistry();
        Tool mockTool = new Tool() {
            @Override public String getName() { return "MOCK_TOOL"; }
            @Override public ToolResult execute(Action action) {
                return new ToolResult(true, 0, "ok", "", 0L);
            }
        };
        registry.register(mockTool);
        Action action = new Action("MOCK_TOOL", Map.of());
        ToolResult result = registry.execute(action);
        assertTrue(result.isSuccess());
    }

    @Test
    void testUnknownToolReturnsError() {
        ToolRegistry registry = new ToolRegistry();
        Action action = new Action("UNKNOWN", Map.of());
        ToolResult result = registry.execute(action);
        assertFalse(result.isSuccess());
        assertTrue(result.getStderr().contains("Unknown tool"));
    }

    @Test
    void testTimeoutConfiguration() {
        ToolRegistry registry = new ToolRegistry();
        assertEquals(30000L, registry.getDefaultTimeoutMs());
        registry.setDefaultTimeoutMs(60000L);
        assertEquals(60000L, registry.getDefaultTimeoutMs());
    }

    @Test
    void testNullActionReturnsError() {
        ToolRegistry registry = new ToolRegistry();
        ToolResult result = registry.execute(null);
        assertFalse(result.isSuccess());
    }
}