# PR: Infrastructure

**Branch:** `feature/infrastructure` → `main`
**Create PR:** https://github.com/njuer-bin/ai-coding-harness/pull/new/feature/infrastructure

## Files
- `memory/Memory.java` + `MemoryImpl.java` — JSON file persistence
- `config/Config.java` + `ConfigImpl.java` — Configuration management
- `config/CredentialManager.java` — Base64+XOR encrypted credential storage
- `log/Logger.java` — Color-coded log levels
- `Dockerfile` — Container distribution
- `.github/workflows/ci.yml` — CI with unit-test + package jobs

## Verification
- `mvn test -Dtest=MemoryTest,CredentialManagerTest,ConfigTest` — 10/10 PASS
- `mvn package -DskipTests` — BUILD SUCCESS
- `docker build -t coding-agent .` — Image build