# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Building a **Coding Agent Harness** — a CLI tool that manages the full lifecycle of an AI coding agent: context organization → LLM call → action parsing → tool execution → result feedback → self-correction. This is an AI4SE course project.

**Tech Stack:** Java 25, Maven, picocli (CLI), Jackson (JSON), JUnit 5, Mockito

**Architecture:** Layered — CLI → Engine → Guardrail/Tools/Feedback → LLM/Memory/Config

## Key Commands

```bash
# Build
cd untitled && mvn compile

# Run all tests
cd untitled && mvn test

# Run a single test class
cd untitled && mvn test -Dtest=EngineTest

# Package fat JAR
cd untitled && mvn package

# Run CLI
java -jar untitled/target/coding-agent-1.0.0.jar --help
```

## Project Structure

```
untitled/src/main/java/com/codingagent/
├── CodingAgentCLI.java          # Main entry point (picocli)
├── config/                      # Config + CredentialManager
├── engine/                      # Engine main loop
├── feedback/                    # Validator → FailureClassifier → RetryOrchestrator
├── guardrail/                   # Guardrail (ALLOW/BLOCK/REQUIRE_HITL)
├── llm/                         # LLMProvider + MockLLM
├── memory/                      # Memory persistence
├── model/                       # Core models (Action, ToolResult, etc.)
└── tool/                        # Tool interface + 8 tool implementations
```

## Current Progress

**All 23 tasks complete!** See `G:/ai暑校/.superpowers/sdd/progress.md` for full status.

Latest commits (in order):
- `1b3a067` - Task 23: cross-platform shell detection
- `4bc4979` - Final review fixes (Critical + Important)
- `e409093` - README.md (通用要求 §五-4)
- `5cfe341` - AGENT_LOG.md (通用要求 §4.9)
- `3d9aa3c` - REFLECTION.md (通用要求 §五-8)
- `e4c0df9` - WebUI server mode (--server, 通用要求 §五-9)
- `cc369ff` - WebUI thread keepalive fix

**Head:** `cc369ff`

## How to Resume

If the session was interrupted, tell the new Claude instance:
1. Read the progress ledger: `.superpowers/sdd/progress.md`
2. Read the project instructions: `CLAUDE.md`
3. The project is fully complete — no remaining tasks

## Key Deliverables

| File | Description |
|------|-------------|
| `SPEC.md` | Design document |
| `PLAN.md` | Implementation plan |
| `SPEC_PROCESS.md` | Cold-start validation record |
| `README.md` | Project overview, install, run, security |
| `AGENT_LOG.md` | Process log with timestamps |
| `REFLECTION.md` | 2200-word post-project reflection |
| `untitled/Dockerfile` | Container distribution |
| `.github/workflows/ci.yml` | GitHub Actions CI |
| `untitled/src/main/java/com/codingagent/WebServer.java` | WebUI (--server mode) |

## Unpushed Commits

The following commits are local only and need `git push origin main`:
- `e409093` README.md
- `5cfe341` AGENT_LOG.md
- `3d9aa3c` REFLECTION.md
- `e4c0df9` WebUI server
- `cc369ff` WebUI fix

## Test Status

```bash
cd untitled && mvn test    # 109/109 PASS
mvn package -DskipTests    # fat JAR build
java -jar target/coding-agent-1.0.0-jar-with-dependencies.jar --help  # CLI help
java -jar target/coding-agent-1.0.0-jar-with-dependencies.jar --server  # WebUI at :8080
```

## How to Resume

If the session was interrupted, tell the new Claude instance:
1. Read the progress ledger: `.superpowers/sdd/progress.md`
2. Read the plan: `PLAN.md`
3. Start from the first incomplete task using the subagent-driven-development skill

## Key Design Decisions

- All core interfaces are Java interfaces (injectable for testing)
- MockLLM uses a Queue for deterministic responses
- Guardrail normalizes paths before matching
- CredentialManager uses Base64 + XOR (0x5A) obfuscation
- API keys NEVER hardcoded — stored at `~/.coding-agent/credentials`
- Package: `com.codingagent`
- Project dir: `untitled/` (Maven root)