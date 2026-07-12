# PR: Tools Layer

**Branch:** `feature/tools` → `main`
**Create PR:** https://github.com/njuer-bin/ai-coding-harness/pull/new/feature/tools

## Files
- `tool/Tool.java` — Tool interface
- `tool/ToolRegistry.java` — Tool registration and dispatch
- `tool/ReadFileTool.java` + `WriteFileTool.java`
- `tool/ExecuteShellTool.java` — ProcessBuilder with 30s timeout
- `tool/RunTestsTool.java` + `GlobListFilesTool.java`
- `tool/SearchCodeTool.java` + `GitTool.java` + `LintCheckTool.java`

## Verification
- `mvn test -Dtest=ToolRegistryTest,ReadFileToolTest,WriteFileToolTest,ExecuteShellToolTest,RunTestsToolTest,GlobListFilesToolTest,SearchCodeToolTest,GitToolTest,LintCheckToolTest` — All PASS