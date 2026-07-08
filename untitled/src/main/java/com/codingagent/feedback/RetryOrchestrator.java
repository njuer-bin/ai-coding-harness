package com.codingagent.feedback;

import com.codingagent.model.Feedback;
import com.codingagent.model.enums.FailureCategory;

public interface RetryOrchestrator {
    boolean shouldRetry(Feedback feedback);
    void recordFailure(FailureCategory category);
    int getMaxRetries(FailureCategory category);
    int getConsecutiveFailures(FailureCategory category);
}