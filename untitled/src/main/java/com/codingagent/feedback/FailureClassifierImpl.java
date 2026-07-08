package com.codingagent.feedback;

import com.codingagent.model.ToolResult;
import com.codingagent.model.enums.FailureCategory;

public class FailureClassifierImpl implements FailureClassifier {

    @Override
    public FailureCategory classify(ToolResult result) {
        // 1. Timeout check
        if (result.getExitCode() == -1 ||
            (result.getStderr() != null && result.getStderr().contains("TIMEOUT"))) {
            return FailureCategory.TIMEOUT;
        }

        String combined = buildCombinedOutput(result);

        // 2. Compile error check
        if (containsAny(combined, "error:", "cannot find symbol", "compilation error")) {
            return FailureCategory.COMPILE_ERROR;
        }

        // 3. Test failure check
        if (containsAny(combined, "Failures:", "Tests failed", "FAILED", "Test run failed")) {
            return FailureCategory.TEST_FAILURE;
        }

        // 4. Lint error check
        if (containsAny(combined, "warning", "WARN", "Checkstyle", "lint")) {
            return FailureCategory.LINT_ERROR;
        }

        // 5. Unknown
        return FailureCategory.UNKNOWN;
    }

    private String buildCombinedOutput(ToolResult result) {
        String stdout = result.getStdout() != null ? result.getStdout() : "";
        String stderr = result.getStderr() != null ? result.getStderr() : "";
        return stdout + "\n" + stderr;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}