package com.codingagent.engine;

import com.codingagent.config.Config;
import com.codingagent.feedback.FailureClassifier;
import com.codingagent.feedback.RetryOrchestrator;
import com.codingagent.feedback.Validator;
import com.codingagent.guardrail.Guardrail;
import com.codingagent.llm.LLMProvider;
import com.codingagent.memory.Memory;
import com.codingagent.model.Action;
import com.codingagent.model.Context;
import com.codingagent.model.Feedback;
import com.codingagent.model.LLMResponse;
import com.codingagent.model.MemoryEntry;
import com.codingagent.model.ToolResult;
import com.codingagent.model.enums.FailureCategory;
import com.codingagent.model.enums.FeedbackStatus;
import com.codingagent.model.enums.GuardrailResult;
import com.codingagent.tool.ToolRegistry;

import java.util.ArrayList;
import java.util.List;

public class Engine {

    private final LLMProvider llm;
    private final ToolRegistry registry;
    private final Guardrail guardrail;
    private final Validator validator;
    private final FailureClassifier classifier;
    private final RetryOrchestrator orchestrator;
    private final Memory memory;
    private final Config config;
    private HITLCallback hitlCallback;

    public Engine(LLMProvider llm, ToolRegistry registry, Guardrail guardrail,
                  Validator validator, FailureClassifier classifier,
                  RetryOrchestrator orchestrator, Memory memory, Config config) {
        this.llm = llm;
        this.registry = registry;
        this.guardrail = guardrail;
        this.validator = validator;
        this.classifier = classifier;
        this.orchestrator = orchestrator;
        this.memory = memory;
        this.config = config;
    }

    public void setHITLCallback(HITLCallback callback) {
        this.hitlCallback = callback;
    }

    public EngineResult run(String taskDescription) {
        List<String> log = new ArrayList<>();
        int maxIterations = config.getMaxIterations();

        Context context = new Context();
        context.setTaskDescription(taskDescription);

        for (int iteration = 0; iteration < maxIterations; iteration++) {

            LLMResponse response = llm.send(context);

            if (response.isStopRequested()) {
                log.add("LLM requested stop");
                return new EngineResult(true, "Task completed: LLM requested stop", log);
            }

            Action action = response.getAction();

            // Guardrail check
            GuardrailResult guardResult = guardrail.check(action);
            if (guardResult == GuardrailResult.BLOCK) {
                log.add("Blocked by guardrail: " + action.getType());
                return new EngineResult(false, "Blocked by guardrail: " + action.getType(), log);
            }

            // HITL check
            if (guardResult == GuardrailResult.REQUIRE_HITL) {
                log.add("HITL required for: " + action.getType());
                if (hitlCallback != null && hitlCallback.confirm(action)) {
                    log.add("HITL approved");
                } else {
                    return new EngineResult(false, "HITL rejected: " + action.getType(), log);
                }
            }

            // Execute action
            ToolResult toolResult = registry.execute(action);

            // Validate result
            Feedback feedback = validator.validate(toolResult, action);

            // Handle failure with retry logic (FAIL or TOOL_ERROR)
            if (feedback.getStatus() != FeedbackStatus.PASS) {
                FailureCategory category = classifier.classify(toolResult);
                feedback.setCategory(category);

                boolean shouldRetry = orchestrator.shouldRetry(feedback);
                if (shouldRetry) {
                    MemoryEntry entry = new MemoryEntry();
                    entry.setContent("Retrying after failure: " + feedback.getDetail());
                    entry.setType("FEEDBACK");
                    entry.setTimestamp(System.currentTimeMillis());
                    memory.store(entry);
                    // Continue loop to get next LLM response
                    continue;
                }
            }

            // Store memory entry for this action
            MemoryEntry entry = new MemoryEntry();
            entry.setContent("Action: " + action.getType() + " status=" + feedback.getStatus());
            entry.setType("CONTEXT");
            entry.setTimestamp(System.currentTimeMillis());
            memory.store(entry);

            // Determine final result
            boolean success = (feedback.getStatus() == FeedbackStatus.PASS && toolResult.isSuccess());
            String summary;
            if (success) {
                summary = "Completed: " + action.getType();
                log.add(summary);
            } else {
                summary = "Failed: " + feedback.getDetail();
                log.add(summary);
            }
            return new EngineResult(success, summary, log);
        }

        // Max iterations reached without completion
        return new EngineResult(false, "Max iterations reached", log);
    }
}