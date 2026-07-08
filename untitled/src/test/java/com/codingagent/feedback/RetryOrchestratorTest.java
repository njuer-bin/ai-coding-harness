package com.codingagent.feedback;

import com.codingagent.model.Feedback;
import com.codingagent.model.enums.FailureCategory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryOrchestratorTest {

    @Test
    void testCompileErrorRetryAllowed() {
        RetryOrchestrator orchestrator = new RetryOrchestratorImpl();
        Feedback feedback = new Feedback();
        feedback.setStatus(com.codingagent.model.enums.FeedbackStatus.FAIL);
        feedback.setCategory(FailureCategory.COMPILE_ERROR);
        feedback.setRetryCount(1);
        assertTrue(orchestrator.shouldRetry(feedback));
    }

    @Test
    void testTimeoutRetryLimited() {
        RetryOrchestrator orchestrator = new RetryOrchestratorImpl();
        Feedback feedback = new Feedback();
        feedback.setStatus(com.codingagent.model.enums.FeedbackStatus.FAIL);
        feedback.setCategory(FailureCategory.TIMEOUT);
        feedback.setRetryCount(1);
        assertTrue(orchestrator.shouldRetry(feedback));
    }

    @Test
    void testMaxRetriesExceeded() {
        RetryOrchestrator orchestrator = new RetryOrchestratorImpl();
        Feedback feedback = new Feedback();
        feedback.setStatus(com.codingagent.model.enums.FeedbackStatus.FAIL);
        feedback.setCategory(FailureCategory.COMPILE_ERROR);
        feedback.setRetryCount(3);
        assertFalse(orchestrator.shouldRetry(feedback));
    }

    @Test
    void testMaxRetriesExceededForTimeout() {
        RetryOrchestrator orchestrator = new RetryOrchestratorImpl();
        Feedback feedback = new Feedback();
        feedback.setStatus(com.codingagent.model.enums.FeedbackStatus.FAIL);
        feedback.setCategory(FailureCategory.TIMEOUT);
        feedback.setRetryCount(2);
        assertFalse(orchestrator.shouldRetry(feedback));
    }

    @Test
    void testPassDoesNotRetry() {
        RetryOrchestrator orchestrator = new RetryOrchestratorImpl();
        Feedback feedback = new Feedback();
        feedback.setStatus(com.codingagent.model.enums.FeedbackStatus.PASS);
        feedback.setCategory(FailureCategory.COMPILE_ERROR);
        feedback.setRetryCount(0);
        assertFalse(orchestrator.shouldRetry(feedback));
    }

    @Test
    void testConsecutiveFailureReducesRetries() {
        RetryOrchestrator orchestrator = new RetryOrchestratorImpl();
        // Record 3 consecutive COMPILE_ERROR failures to trigger dynamic reduction
        orchestrator.recordFailure(FailureCategory.COMPILE_ERROR);
        orchestrator.recordFailure(FailureCategory.COMPILE_ERROR);
        orchestrator.recordFailure(FailureCategory.COMPILE_ERROR);
        // After reduction, max should be max(1, 3/2) = 1
        Feedback feedback = new Feedback();
        feedback.setStatus(com.codingagent.model.enums.FeedbackStatus.FAIL);
        feedback.setCategory(FailureCategory.COMPILE_ERROR);
        feedback.setRetryCount(3);
        assertFalse(orchestrator.shouldRetry(feedback));
    }

    @Test
    void testGetConsecutiveFailures() {
        RetryOrchestrator orchestrator = new RetryOrchestratorImpl();
        orchestrator.recordFailure(FailureCategory.COMPILE_ERROR);
        orchestrator.recordFailure(FailureCategory.COMPILE_ERROR);
        assertEquals(2, orchestrator.getConsecutiveFailures(FailureCategory.COMPILE_ERROR));
    }
}