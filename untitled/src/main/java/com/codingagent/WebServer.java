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
import com.codingagent.model.Action;
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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Embedded HTTP server that provides a WebUI for the Coding Agent Harness.
 * Accessible at http://localhost:8080 when started with --server.
 */
public class WebServer {

    private final int port;

    public WebServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleIndex);
        server.createContext("/run", this::handleRun);
        server.setExecutor(null);
        server.start();
        System.out.println("WebUI server started at http://localhost:" + port + "/");
        System.out.println("Press Ctrl+C to stop.");
    }

    private void handleIndex(HttpExchange exchange) throws IOException {
        String html = getIndexHtml();
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void handleRun(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String html = getIndexHtml();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            return;
        }

        // Parse form body
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseForm(body);
        String taskDescription = params.getOrDefault("task", "");

        // Build engine
        Engine engine = buildEngine();
        engine.setHITLCallback(action -> {
            // Auto-approve HITL in web mode (for simplicity in demo)
            System.out.println("[WebUI] HITL auto-approved: " + action.getType());
            return true;
        });

        // Run engine
        EngineResult result = engine.run(taskDescription);

        // Build response HTML
        String html = getResultHtml(taskDescription, result);
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Engine buildEngine() {
        String homeDir = System.getProperty("user.home");
        String codingAgentDir = homeDir + "/.coding-agent";
        ConfigImpl config = new ConfigImpl(codingAgentDir);
        MockLLM llm = new MockLLM();
        llm.setNextResponse(new LLMResponse(null, "Task completed by initial mock", true));
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new ReadFileTool());
        toolRegistry.register(new WriteFileTool());
        toolRegistry.register(new ExecuteShellTool());
        toolRegistry.register(new RunTestsTool());
        toolRegistry.register(new GlobListFilesTool());
        toolRegistry.register(new SearchCodeTool());
        toolRegistry.register(new GitTool());
        toolRegistry.register(new LintCheckTool());
        GuardrailImpl guardrail = new GuardrailImpl();
        ValidatorImpl validator = new ValidatorImpl();
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        MemoryImpl memory = new MemoryImpl(codingAgentDir + "/memory.json");
        return new Engine(llm, toolRegistry, guardrail, validator, classifier, orchestrator, memory, config);
    }

    private Map<String, String> parseForm(String body) {
        Map<String, String> params = new LinkedHashMap<>();
        if (body == null || body.isEmpty()) return params;
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private String getIndexHtml() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"zh-CN\">\n" +
               "<head>\n" +
               "  <meta charset=\"UTF-8\">\n" +
               "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "  <title>Coding Agent Harness — WebUI</title>\n" +
               "  <style>\n" +
               "    * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
               "    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
               "           background: #f5f5f5; color: #333; padding: 2rem; }\n" +
               "    .container { max-width: 800px; margin: 0 auto; }\n" +
               "    h1 { font-size: 1.5rem; margin-bottom: 0.5rem; color: #1a1a2e; }\n" +
               "    p.subtitle { color: #666; margin-bottom: 2rem; font-size: 0.9rem; }\n" +
               "    textarea { width: 100%; min-height: 120px; padding: 1rem; font-size: 1rem;\n" +
               "               border: 1px solid #ddd; border-radius: 8px; resize: vertical;\n" +
               "               font-family: 'SF Mono', 'Cascadia Code', monospace; }\n" +
               "    textarea:focus { outline: none; border-color: #4a6cf7; box-shadow: 0 0 0 3px rgba(74,108,247,0.1); }\n" +
               "    button { background: #4a6cf7; color: white; border: none; padding: 0.75rem 2rem;\n" +
               "             font-size: 1rem; border-radius: 8px; cursor: pointer; margin-top: 1rem; }\n" +
               "    button:hover { background: #3b5de7; }\n" +
               "    button:disabled { background: #aaa; cursor: not-allowed; }\n" +
               "    .card { background: white; border-radius: 12px; padding: 1.5rem;\n" +
               "            box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-top: 1.5rem; }\n" +
               "    .status { display: inline-block; padding: 0.25rem 0.75rem; border-radius: 20px;\n" +
               "              font-weight: 600; font-size: 0.85rem; }\n" +
               "    .status.success { background: #d4edda; color: #155724; }\n" +
               "    .status.failure { background: #f8d7da; color: #721c24; }\n" +
               "    .log-entry { padding: 0.35rem 0; border-bottom: 1px solid #f0f0f0;\n" +
               "                 font-family: 'SF Mono', 'Cascadia Code', monospace; font-size: 0.85rem; }\n" +
               "    .loader { display: none; border: 3px solid #f3f3f3; border-top: 3px solid #4a6cf7;\n" +
               "              border-radius: 50%; width: 24px; height: 24px; animation: spin 1s linear infinite;\n" +
               "              margin: 1rem auto; }\n" +
               "    @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }\n" +
               "    .footer { margin-top: 2rem; text-align: center; color: #999; font-size: 0.8rem; }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class=\"container\">\n" +
               "    <h1>Coding Agent Harness</h1>\n" +
               "    <p class=\"subtitle\">AI 编码代理引擎 — 输入任务描述，引擎将组织上下文、调用 LLM、执行工具并反馈结果</p>\n" +
               "    <form id=\"runForm\" action=\"/run\" method=\"POST\">\n" +
               "      <textarea name=\"task\" id=\"taskInput\" placeholder=\"例如：读取 src/main/java 下的所有 Java 文件\"></textarea>\n" +
               "      <br>\n" +
               "      <button type=\"submit\" id=\"runBtn\">运行任务</button>\n" +
               "    </form>\n" +
               "    <div class=\"loader\" id=\"loader\"></div>\n" +
               "    <div id=\"result\"></div>\n" +
               "  </div>\n" +
               "  <script>\n" +
               "    document.getElementById('runForm').addEventListener('submit', async function(e) {\n" +
               "      e.preventDefault();\n" +
               "      const btn = document.getElementById('runBtn');\n" +
               "      const loader = document.getElementById('loader');\n" +
               "      const result = document.getElementById('result');\n" +
               "      const task = document.getElementById('taskInput').value;\n" +
               "      btn.disabled = true; loader.style.display = 'block'; result.innerHTML = '';\n" +
               "      try {\n" +
               "        const res = await fetch('/run', {\n" +
               "          method: 'POST',\n" +
               "          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },\n" +
               "          body: new URLSearchParams({ task: task })\n" +
               "        });\n" +
               "        const html = await res.text();\n" +
               "        result.innerHTML = html;\n" +
               "      } catch (err) {\n" +
               "        result.innerHTML = '<div class=\"card\"><p style=\"color:red\">请求失败: ' + err.message + '</p></div>';\n" +
               "      } finally {\n" +
               "        btn.disabled = false; loader.style.display = 'none';\n" +
               "      }\n" +
               "    });\n" +
               "  </script>\n" +
               "</body>\n" +
               "</html>";
    }

    private String getResultHtml(String taskDescription, EngineResult result) {
        String statusClass = result.isSuccess() ? "success" : "failure";
        String statusText = result.isSuccess() ? "SUCCESS" : "FAILURE";
        StringBuilder logHtml = new StringBuilder();
        for (String entry : result.getLog()) {
            logHtml.append("<div class=\"log-entry\">").append(escapeHtml(entry)).append("</div>\n");
        }

        return "<div class=\"card\">\n" +
               "  <h2>执行结果</h2>\n" +
               "  <p><strong>任务：</strong>" + escapeHtml(taskDescription) + "</p>\n" +
               "  <p><strong>状态：</strong><span class=\"status " + statusClass + "\">" + statusText + "</span></p>\n" +
               "  <p><strong>摘要：</strong>" + escapeHtml(result.getSummary()) + "</p>\n" +
               "  <h3 style=\"margin-top:1rem;font-size:0.95rem;\">日志</h3>\n" +
               "  <div style=\"margin-top:0.5rem;\">" + logHtml + "</div>\n" +
               "  <a href=\"/\" style=\"display:inline-block;margin-top:1rem;color:#4a6cf7;\">← 返回</a>\n" +
               "</div>";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}