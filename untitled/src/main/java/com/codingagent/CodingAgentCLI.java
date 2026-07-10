package com.codingagent;

import com.codingagent.config.ConfigImpl;
import com.codingagent.config.CredentialManager;
import com.codingagent.engine.Engine;
import com.codingagent.engine.EngineResult;
import com.codingagent.feedback.FailureClassifierImpl;
import com.codingagent.feedback.RetryOrchestratorImpl;
import com.codingagent.feedback.ValidatorImpl;
import com.codingagent.guardrail.GuardrailImpl;
import com.codingagent.llm.MockLLM;
import com.codingagent.memory.MemoryImpl;
import com.codingagent.model.LLMResponse;
import com.codingagent.tool.ExecuteShellTool;
import com.codingagent.tool.GitTool;
import com.codingagent.tool.GlobListFilesTool;
import com.codingagent.tool.LintCheckTool;
import com.codingagent.tool.ReadFileTool;
import com.codingagent.tool.RunTestsTool;
import com.codingagent.tool.SearchCodeTool;
import com.codingagent.tool.ToolRegistry;
import com.codingagent.tool.WriteFileTool;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(name = "coding-agent", mixinStandardHelpOptions = true,
         description = "AI Coding Agent Harness — manages the full lifecycle of an AI coding agent",
         subcommands = {CodingAgentCLI.CredentialCommand.class})
public class CodingAgentCLI implements Runnable {

    @Parameters(index = "0", description = "Task description for the coding agent", arity = "0..1")
    private String task;

    @Option(names = "--server", description = "Start in WebUI server mode (default port: 8080)")
    private boolean serverMode;

    @Option(names = "--port", description = "Port for WebUI server (default: 8080)")
    private int port = 8080;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CodingAgentCLI()).execute(args);
        System.exit(exitCode);
    }

    static String getCodingAgentDir() {
        return System.getProperty("user.home") + "/.coding-agent";
    }

    static CredentialManager createCredentialManager() {
        return new CredentialManager(getCodingAgentDir() + "/credentials");
    }

    private Engine buildEngine() {
        String codingAgentDir = getCodingAgentDir();

        // Config
        ConfigImpl config = new ConfigImpl(codingAgentDir);

        // Mock LLM with a default stop response so the engine exits cleanly
        MockLLM llm = new MockLLM();
        llm.setNextResponse(new LLMResponse(null, "Task completed by initial mock", true));

        // Tool registry — register all 8 tools
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new ReadFileTool());
        toolRegistry.register(new WriteFileTool());
        toolRegistry.register(new ExecuteShellTool());
        toolRegistry.register(new RunTestsTool());
        toolRegistry.register(new GlobListFilesTool());
        toolRegistry.register(new SearchCodeTool());
        toolRegistry.register(new GitTool());
        toolRegistry.register(new LintCheckTool());

        // Guardrail
        GuardrailImpl guardrail = new GuardrailImpl();

        // Feedback chain
        ValidatorImpl validator = new ValidatorImpl();
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();

        // Memory
        MemoryImpl memory = new MemoryImpl(codingAgentDir + "/memory.json");

        return new Engine(llm, toolRegistry, guardrail, validator, classifier, orchestrator, memory, config);
    }

    @Override
    public void run() {
        // WebUI mode: start embedded HTTP server
        if (serverMode) {
            try {
                WebServer webServer = new WebServer(port);
                webServer.start();
            } catch (Exception e) {
                System.err.println("Failed to start WebUI server: " + e.getMessage());
                System.exit(1);
            }
            return;
        }

        // CLI mode: obtain task description from argument or stdin
        String resolvedTask = this.task;
        Scanner scanner = new Scanner(System.in);

        if (resolvedTask == null || resolvedTask.trim().isEmpty()) {
            System.out.print("> ");
            if (scanner.hasNextLine()) {
                resolvedTask = scanner.nextLine().trim();
            }
        }

        if (resolvedTask == null || resolvedTask.trim().isEmpty()) {
            resolvedTask = "Default task";
        }

        // Build engine with all dependencies
        Engine engine = buildEngine();

        // Set HITL callback — prompts user for Y/N approval
        engine.setHITLCallback(action -> {
            System.out.println("\n WARNING: " + action.getType() + " requires approval");
            System.out.print("  Allow execution? [y/N] ");
            String input = scanner.nextLine().trim();
            return input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes");
        });

        // Run the engine
        EngineResult result = engine.run(resolvedTask);

        // Print result
        System.out.println("\n=== Result ===");
        System.out.println("Status: " + (result.isSuccess() ? "SUCCESS" : "FAILURE"));
        System.out.println("Summary: " + result.getSummary());
        System.out.println("\n--- Log ---");
        for (String entry : result.getLog()) {
            System.out.println("  " + entry);
        }
    }

    // ─── credential subcommand ───────────────────────────────────────────────

    @Command(name = "credential", mixinStandardHelpOptions = true,
             description = "Manage API credentials (Base64 + XOR obfuscated storage)",
             subcommands = {
                 CredentialCommand.InitCommand.class,
                 CredentialCommand.StatusCommand.class,
                 CredentialCommand.UpdateCommand.class,
                 CredentialCommand.ClearCommand.class
             })
    static class CredentialCommand implements Runnable {

        @Command(name = "init", description = "Initialize or overwrite the API key")
        static class InitCommand implements Callable<Integer> {
            @Parameters(index = "0", description = "API key to store")
            private String apiKey;

            @Override
            public Integer call() {
                CredentialManager cm = createCredentialManager();
                cm.store(apiKey);
                System.out.println("Credential saved to " + getCodingAgentDir() + "/credentials");
                return 0;
            }
        }

        @Command(name = "status", description = "Show whether a credential is configured")
        static class StatusCommand implements Callable<Integer> {
            @Override
            public Integer call() {
                CredentialManager cm = createCredentialManager();
                String key = cm.load();
                if (key == null || key.isEmpty()) {
                    System.out.println("No credential configured.");
                    System.out.println("Use: coding-agent credential init <api-key>");
                    return 1;
                }
                System.out.println("Credential configured: " + key.substring(0, Math.min(8, key.length())) + "...");
                return 0;
            }
        }

        @Command(name = "update", description = "Update the existing API key")
        static class UpdateCommand implements Callable<Integer> {
            @Parameters(index = "0", description = "New API key")
            private String apiKey;

            @Override
            public Integer call() {
                CredentialManager cm = createCredentialManager();
                cm.store(apiKey);
                System.out.println("Credential updated.");
                return 0;
            }
        }

        @Command(name = "clear", description = "Delete the stored credential")
        static class ClearCommand implements Callable<Integer> {
            @Override
            public Integer call() {
                CredentialManager cm = createCredentialManager();
                boolean deleted = cm.clear();
                if (deleted) {
                    System.out.println("Credential cleared.");
                } else {
                    System.out.println("No credential to clear.");
                }
                return 0;
            }
        }

        @Override
        public void run() {
            // No subcommand given — show help
            new CommandLine(this).usage(System.out);
        }
    }
}