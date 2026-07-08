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

Check `G:/ai暑校/.superpowers/sdd/progress.md` for the exact task completion status.

Completed tasks (up to Task 14): Project setup, models, Tool interface + registry, 8 tools, LLM abstraction, Guardrail, Validator, FailureClassifier, RetryOrchestrator, Memory, Config + CredentialManager.

**Next task:** Task 15 — Engine main loop (with HITL callback). An implementer subagent is currently running for this task.

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