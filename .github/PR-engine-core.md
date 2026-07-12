# PR: Engine Core

**Branch:** `feature/engine-core` → `main`
**Create PR:** https://github.com/njuer-bin/ai-coding-harness/pull/new/feature/engine-core

## Files
- `engine/Engine.java` — Agent main loop (context → LLM → action → execute → feedback)
- `engine/EngineResult.java` — Execution result model
- `engine/HITLCallback.java` — Human-in-the-loop callback interface
- `CodingAgentCLI.java` — picocli CLI entry point
- `WebServer.java` — Embedded HTTP server for WebUI mode

## Verification
- `mvn test -Dtest=EngineTest` — 3/3 PASS
- `java -jar coding-agent.jar --help` — CLI help works
- `java -jar coding-agent.jar --server` — WebUI at :8080