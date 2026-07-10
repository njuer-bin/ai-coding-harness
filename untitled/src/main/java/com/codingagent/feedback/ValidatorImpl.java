package com.codingagent.feedback;
import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import com.codingagent.model.Feedback;
import com.codingagent.model.enums.FeedbackStatus;
import com.codingagent.model.enums.FailureCategory;

public class ValidatorImpl implements Validator {
    @Override
    public Feedback validate(ToolResult result, Action action) {
        if (result.isSuccess() && result.getExitCode() == 0) {
            return new Feedback(FeedbackStatus.PASS, FailureCategory.NONE, "OK", 0, false);
        }
        // Timeout or exception -> TOOL_ERROR
        if (result.getExitCode() == -1 || (result.getStderr() != null && result.getStderr().contains("TIMEOUT"))) {
            return new Feedback(FeedbackStatus.TOOL_ERROR, FailureCategory.TIMEOUT, result.getStderr(), 0, false);
        }
        return new Feedback(FeedbackStatus.FAIL, FailureCategory.COMPILE_ERROR, result.getStderr(), 0, false);
    }
}