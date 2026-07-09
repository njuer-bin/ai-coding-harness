package com.codingagent.demo;

import com.codingagent.model.*;
import com.codingagent.model.enums.*;
import com.codingagent.feedback.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Demo2FeedbackLoopTest {
    @Test void testFeedbackLoopEndToEnd() {
        ValidatorImpl validator = new ValidatorImpl();
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();

        // 模拟编译失败
        ToolResult result = new ToolResult(false, 1, "", "error: cannot find symbol", 500L);
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "javac Main.java"));

        // Phase 1: Validator
        Feedback feedback = validator.validate(result, action);
        assertEquals(FeedbackStatus.FAIL, feedback.getStatus());

        // Phase 2: Classifier
        FailureCategory category = classifier.classify(result);
        assertEquals(FailureCategory.COMPILE_ERROR, category);

        // Phase 3: RetryOrchestrator
        feedback.setCategory(category);
        feedback.setRetryCount(1);
        assertTrue(orchestrator.shouldRetry(feedback));
    }
}