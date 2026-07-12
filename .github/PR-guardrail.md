# PR: Guardrail

**Branch:** `feature/guardrail` → `main`
**Create PR:** https://github.com/njuer-bin/ai-coding-harness/pull/new/feature/guardrail

## Files
- `guardrail/Guardrail.java` — Interface
- `guardrail/GuardrailImpl.java` — Implementation with path normalization

## Capabilities
- **Dangerous commands**: `rm -rf /`, `mkfs`, `dd`, `shutdown` → BLOCK
- **Dangerous paths**: Writing to `/etc/`, `/usr/`, `/boot/` → BLOCK
- **Path normalization**: `Path.normalize()` prevents `../../etc/shadow` traversal
- **HITL**: `git push`, `deploy`, `npm publish` → REQUIRE_HITL

## Verification
- `mvn test -Dtest=GuardrailTest` — 13/13 PASS
- `mvn test -Dtest=Demo1GuardrailTest` — 4/4 PASS