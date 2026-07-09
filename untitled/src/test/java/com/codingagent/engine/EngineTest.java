package com.codingagent.engine;

import com.codingagent.config.ConfigImpl;
import com.codingagent.feedback.FailureClassifierImpl;
import com.codingagent.feedback.RetryOrchestratorImpl;
import com.codingagent.feedback.ValidatorImpl;
import com.codingagent.guardrail.GuardrailImpl;
import com.codingagent.llm.MockLLM;
import com.codingagent.memory.Memory;
import com.codingagent.memory.MemoryImpl;
import com.codingagent.model.Action;
import com.codingagent.model.LLMResponse;
import com.codingagent.model.ToolResult;
import com.codingagent.tool.Tool;
import com.codingagent.tool.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EngineTest {

    @Test
    void testEngineHITLCallback(@TempDir Path tempDir) {
        MockLLM llm = new MockLLM();
        ToolRegistry registry = new ToolRegistry();
        GuardrailImpl guardrail = new GuardrailImpl();
        ValidatorImpl validator = new ValidatorImpl();
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        Memory memory = new MemoryImpl(tempDir.resolve("mem.json").toString());
        ConfigImpl config = new ConfigImpl(tempDir.toString());

        // Register a mock tool that returns success
        registry.register(new Tool() {
            @Override
            public String getName() { return "EXECUTE_COMMAND"; }

            @Override
            public ToolResult execute(Action action) {
                return new ToolResult(true, 0, "done", "", 100L);
            }
        });

        Engine engine = new Engine(llm, registry, guardrail, validator, classifier, orchestrator, memory, config);
        engine.setHITLCallback(action -> true); // 自动批准

        llm.setNextResponse(new LLMResponse(
                new Action("EXECUTE_COMMAND", Map.of("command", "git push origin main")),
                "pushing code", false
        ));

        EngineResult result = engine.run("push code");
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void testEngineBasicRun(@TempDir Path tempDir) {
        MockLLM llm = new MockLLM();
        ToolRegistry registry = new ToolRegistry();
        GuardrailImpl guardrail = new GuardrailImpl();
        ValidatorImpl validator = new ValidatorImpl();
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        Memory memory = new MemoryImpl(tempDir.resolve("mem.json").toString());
        ConfigImpl config = new ConfigImpl(tempDir.toString());

        // Register a mock tool that returns success
        registry.register(new Tool() {
            @Override
            public String getName() { return "EXECUTE_COMMAND"; }

            @Override
            public ToolResult execute(Action action) {
                return new ToolResult(true, 0, "done", "", 100L);
            }
        });

        Engine engine = new Engine(llm, registry, guardrail, validator, classifier, orchestrator, memory, config);

        llm.setNextResponse(new LLMResponse(
                new Action("EXECUTE_COMMAND", Map.of("command", "echo hello")),
                "echoing", false
        ));

        EngineResult result = engine.run("say hello");
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Basic run should succeed");
    }

    @Test
    void testEngineBlockedByGuardrail(@TempDir Path tempDir) {
        MockLLM llm = new MockLLM();
        ToolRegistry registry = new ToolRegistry();
        GuardrailImpl guardrail = new GuardrailImpl();
        ValidatorImpl validator = new ValidatorImpl();
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        Memory memory = new MemoryImpl(tempDir.resolve("mem.json").toString());
        ConfigImpl config = new ConfigImpl(tempDir.toString());

        Engine engine = new Engine(llm, registry, guardrail, validator, classifier, orchestrator, memory, config);

        llm.setNextResponse(new LLMResponse(
                new Action("EXECUTE_COMMAND", Map.of("command", "rm -rf /")),
                "removing everything", false
        ));

        EngineResult result = engine.run("delete everything");
        assertNotNull(result);
        assertFalse(result.isSuccess(), "Dangerous command should be blocked");
    }
}