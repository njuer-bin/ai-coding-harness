package com.codingagent.model;

import com.codingagent.model.enums.*;

public class Feedback {
    private FeedbackStatus status;
    private FailureCategory category;
    private String detail;
    private int retryCount;      // Engine 全局统一计数器
    private boolean shouldRetry;

    public Feedback() {}

    public Feedback(FeedbackStatus status, FailureCategory category, String detail, int retryCount, boolean shouldRetry) {
        this.status = status;
        this.category = category;
        this.detail = detail;
        this.retryCount = retryCount;
        this.shouldRetry = shouldRetry;
    }

    public FeedbackStatus getStatus() { return status; }
    public void setStatus(FeedbackStatus status) { this.status = status; }
    public FailureCategory getCategory() { return category; }
    public void setCategory(FailureCategory category) { this.category = category; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public boolean isShouldRetry() { return shouldRetry; }
    public void setShouldRetry(boolean shouldRetry) { this.shouldRetry = shouldRetry; }
}