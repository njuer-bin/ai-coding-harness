package com.codingagent.feedback;
import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import com.codingagent.model.Feedback;
public interface Validator {
    Feedback validate(ToolResult result, Action action);
}