package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class ExecuteShellTool implements Tool {
    private static final long DEFAULT_TIMEOUT_MS = 30_000;

    @Override
    public String getName() { return "EXECUTE_COMMAND"; }

    @Override
    public ToolResult execute(Action action) {
        long start = System.currentTimeMillis();
        try {
            String command = (String) action.getParameters().get("command");
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.redirectErrorStream(false);
            Process process = pb.start();

            boolean finished = process.waitFor(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                long duration = System.currentTimeMillis() - start;
                return new ToolResult(false, -1, "", "TIMEOUT", duration);
            }

            String stdout = new String(process.getInputStream().readAllBytes());
            String stderr = new String(process.getErrorStream().readAllBytes());
            int exitCode = process.exitValue();
            long duration = System.currentTimeMillis() - start;
            return new ToolResult(exitCode == 0, exitCode, stdout, stderr, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            return new ToolResult(false, -1, "", e.getMessage(), duration);
        }
    }
}