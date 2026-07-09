package com.codingagent.tool;

import com.codingagent.model.Action;
import com.codingagent.model.ToolResult;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class ExecuteShellTool implements Tool {

    @Override
    public String getName() { return "EXECUTE_COMMAND"; }

    @Override
    public ToolResult execute(Action action) {
        long start = System.currentTimeMillis();
        try {
            String command = (String) action.getParameters().get("command");
            ProcessBuilder pb = new ProcessBuilder(getShellCommand(command));
            pb.redirectErrorStream(false);
            Process process = pb.start();

            boolean finished = process.waitFor(getTimeoutMs(), TimeUnit.MILLISECONDS);
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

    public String getOsName() { return System.getProperty("os.name").toLowerCase(); }

    private String[] getShellCommand(String command) {
        String os = getOsName();
        if (os.contains("win")) {
            return new String[]{"cmd.exe", "/c", command};
        }
        return new String[]{"bash", "-c", command};
    }
}