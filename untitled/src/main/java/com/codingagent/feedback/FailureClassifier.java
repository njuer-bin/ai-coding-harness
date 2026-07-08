package com.codingagent.feedback;

import com.codingagent.model.ToolResult;
import com.codingagent.model.enums.FailureCategory;

public interface FailureClassifier {
    FailureCategory classify(ToolResult result);
}
