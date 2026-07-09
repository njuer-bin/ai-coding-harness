package com.codingagent.demo;

import com.codingagent.model.*;
import com.codingagent.llm.MockLLM;
import com.codingagent.tool.*;
import com.codingagent.guardrail.GuardrailImpl;
import com.codingagent.feedback.*;
import com.codingagent.memory.*;
import com.codingagent.config.ConfigImpl;
import com.codingagent.engine.Engine;
import com.codingagent.engine.EngineResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Demo3EndToEndTest {
    @Test void testEndToEndWithMockLLM(@TempDir Path tempDir) {
        MockLLM llm = new MockLLM();
        ToolRegistry registry = new ToolRegistry();
        registry.register(new ReadFileTool());
        registry.register(new WriteFileTool());
        registry.register(new ExecuteShellTool());

        GuardrailImpl guardrail = new GuardrailImpl();
        ValidatorImpl validator = new ValidatorImpl();
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        Memory memory = new MemoryImpl(tempDir.resolve("demo-mem.json").toString());
        ConfigImpl config = new ConfigImpl(tempDir.toString());

        llm.setNextResponse(new LLMResponse(
            new Action("WRITE_FILE", Map.of("path", tempDir + "/out.txt", "content", "hello")),
            "writing output", false
        ));
        llm.setNextResponse(new LLMResponse(
            new Action("READ_FILE", Map.of("path", tempDir + "/out.txt")),
            "verifying output", true
        ));

        Engine engine = new Engine(llm, registry, guardrail, validator, classifier, orchestrator, memory, config);
        EngineResult result = engine.run("write hello to out.txt");
        assertTrue(result.isSuccess());
    }
}