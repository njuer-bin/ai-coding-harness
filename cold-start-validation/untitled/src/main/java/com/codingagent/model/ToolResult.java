package com.codingagent.model;

import java.util.List;
import java.util.Map;

public class ToolResult {
    private boolean success;
    private int exitCode;
    private String stdout;
    private String stderr;
    private long durationMs;
    private Map<String, Object> structuredOutput;
    private List<String> errorLines;

    public ToolResult() {}

    public ToolResult(boolean success, int exitCode, String stdout, String stderr, long durationMs) {
        this.success = success;
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
        this.durationMs = durationMs;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getExitCode() { return exitCode; }
    public void setExitCode(int exitCode) { this.exitCode = exitCode; }
    public String getStdout() { return stdout; }
    public void setStdout(String stdout) { this.stdout = stdout; }
    public String getStderr() { return stderr; }
    public void setStderr(String stderr) { this.stderr = stderr; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public Map<String, Object> getStructuredOutput() { return structuredOutput; }
    public void setStructuredOutput(Map<String, Object> structuredOutput) { this.structuredOutput = structuredOutput; }
    public List<String> getErrorLines() { return errorLines; }
    public void setErrorLines(List<String> errorLines) { this.errorLines = errorLines; }
}