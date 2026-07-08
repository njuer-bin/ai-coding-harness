package com.codingagent.feedback;

import com.codingagent.model.Feedback;
import com.codingagent.model.enums.FailureCategory;
import com.codingagent.model.enums.FeedbackStatus;

import java.util.HashMap;
import java.util.Map;

public class RetryOrchestratorImpl implements RetryOrchestrator {

    private final Map<FailureCategory, Integer> consecutiveFailures;
    private final Map<FailureCategory, Integer> baseMaxRetries;

    public RetryOrchestratorImpl() {
        this.consecutiveFailures = new HashMap<>();
        this.baseMaxRetries = new HashMap<>();
        // Initialize default max retries
        // Values represent the maximum retryCount threshold (inclusive):
        // retryCount >= threshold means no more retries
        baseMaxRetries.put(FailureCategory.COMPILE_ERROR, 3);
        baseMaxRetries.put(FailureCategory.TEST_FAILURE, 3);
        baseMaxRetries.put(FailureCategory.LINT_ERROR, 2);
        baseMaxRetries.put(FailureCategory.TIMEOUT, 2);
        baseMaxRetries.put(FailureCategory.EXECUTION_ERROR, 2);
        baseMaxRetries.put(FailureCategory.UNKNOWN, 1);
    }

    @Override
    public boolean shouldRetry(Feedback feedback) {
        if (feedback.getStatus() == FeedbackStatus.PASS) {
            return false;
        }

        FailureCategory category = feedback.getCategory();
        int maxAllowed = getMaxRetries(category);

        if (feedback.getRetryCount() >= maxAllowed) {
            return false;
        }

        recordFailure(category);
        return true;
    }

    @Override
    public void recordFailure(FailureCategory category) {
        // Reset all other categories' consecutive counts
        for (FailureCategory key : FailureCategory.values()) {
            if (key != category) {
                consecutiveFailures.put(key, 0);
            }
        }
        // Increment this category's consecutive count
        consecutiveFailures.merge(category, 1, Integer::sum);
    }

    @Override
    public int getMaxRetries(FailureCategory category) {
        int base = baseMaxRetries.getOrDefault(category, 1);
        int consecutive = consecutiveFailures.getOrDefault(category, 0);

        if (consecutive >= 3) {
            return Math.max(1, base / 2);
        }
        return base;
    }

    @Override
    public int getConsecutiveFailures(FailureCategory category) {
        return consecutiveFailures.getOrDefault(category, 0);
    }
}