package com.codingagent.feedback;

import com.codingagent.model.ToolResult;
import com.codingagent.model.enums.FailureCategory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FailureClassifierTest {

    private final FailureClassifier classifier = new FailureClassifierImpl();

    @Test
    void testCompileError() {
        ToolResult result = new ToolResult(false, 1, "", "error: cannot find symbol", 100L);
        assertEquals(FailureCategory.COMPILE_ERROR, classifier.classify(result));
    }

    @Test
    void testTestFailure() {
        ToolResult result = new ToolResult(false, 1, "Tests run: 5, Failures: 2", "", 100L);
        assertEquals(FailureCategory.TEST_FAILURE, classifier.classify(result));
    }

    @Test
    void testLintError() {
        ToolResult result = new ToolResult(false, 1, "Checkstyle: warning", "", 100L);
        assertEquals(FailureCategory.LINT_ERROR, classifier.classify(result));
    }

    @Test
    void testTimeout() {
        ToolResult result = new ToolResult(false, -1, "", "TIMEOUT", 100L);
        assertEquals(FailureCategory.TIMEOUT, classifier.classify(result));
    }

    @Test
    void testUnknown() {
        ToolResult result = new ToolResult(false, 1, "", "some weird error", 100L);
        assertEquals(FailureCategory.UNKNOWN, classifier.classify(result));
    }

    @Test
    void testNullStderr() {
        ToolResult result = new ToolResult(false, 0, "output", null, 100L);
        assertEquals(FailureCategory.UNKNOWN, classifier.classify(result));
    }

    @Test
    void testEmptyOutput() {
        ToolResult result = new ToolResult(false, 0, "", "", 100L);
        assertEquals(FailureCategory.UNKNOWN, classifier.classify(result));
    }
}
