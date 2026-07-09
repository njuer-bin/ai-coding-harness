package com.codingagent;

import com.codingagent.config.ConfigImpl;
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
import picocli.CommandLine.Parameters;

import java.util.Scanner;

@Command(name = "coding-agent", mixinStandardHelpOptions = true,
         description = "AI Coding Agent Harness — manages the full lifecycle of an AI coding agent")
public class CodingAgentCLI implements Runnable {

    @Parameters(index = "0", description = "Task description for the coding agent")
    private String task;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CodingAgentCLI()).execute(args);
        System.exit(exitCode);
    }

    private Engine buildEngine() {
        String homeDir = System.getProperty("user.home");
        String codingAgentDir = homeDir + "/.coding-agent";

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
        // Obtain task description: from CLI argument or stdin prompt
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
}