package com.codingagent.llm;

import com.codingagent.model.Action;
import com.codingagent.model.Context;
import com.codingagent.model.LLMResponse;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MockLLMTest {
    @Test
    void testMockLLMReturnsPresetResponse() {
        MockLLM mock = new MockLLM();
        Action action = new Action("READ_FILE", Map.of("path", "test.txt"));
        LLMResponse preset = new LLMResponse(action, "testing", true);
        mock.setNextResponse(preset);
        LLMResponse result = mock.send(new Context());
        assertEquals(preset, result);
    }

    @Test
    void testMockLLMThrowsOnNoPreset() {
        MockLLM mock = new MockLLM();
        assertThrows(IllegalStateException.class, () -> mock.send(new Context()));
    }

    @Test
    void testMockLLMSupportsMultipleResponses() {
        MockLLM mock = new MockLLM();
        Action a1 = new Action("READ_FILE", Map.of("path", "a.txt"));
        Action a2 = new Action("WRITE_FILE", Map.of("path", "b.txt"));
        mock.setNextResponse(new LLMResponse(a1, "first", false));
        mock.setNextResponse(new LLMResponse(a2, "second", true));
        assertEquals("READ_FILE", mock.send(new Context()).getAction().getType());
        assertEquals("WRITE_FILE", mock.send(new Context()).getAction().getType());
    }
}