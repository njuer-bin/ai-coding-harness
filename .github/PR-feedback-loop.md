# PR: Feedback Loop (★ Main Contribution)

**Branch:** `feature/feedback-loop` → `main`
**Create PR:** https://github.com/njuer-bin/ai-coding-harness/pull/new/feature/feedback-loop

## Files
- `feedback/Validator.java` + `ValidatorImpl.java` — Exit code + regex validation
- `feedback/FailureClassifier.java` + `FailureClassifierImpl.java` — 6 failure categories
- `feedback/RetryOrchestrator.java` + `RetryOrchestratorImpl.java` — Dynamic retry

## Depth Features
1. **6 failure categories**: TIMEOUT, COMPILE_ERROR, TEST_FAILURE, LINT_ERROR, EXECUTION_ERROR, UNKNOWN
2. **Per-category max retries**: COMPILE=3, TEST=3, LINT=2, TIMEOUT=1, EXECUTION=2, UNKNOWN=1
3. **Consecutive failure throttling**: 3+ same-category failures → maxRetries halved
4. **Fully deterministic**: All logic is code, no LLM prompts

## Verification
- `mvn test -Dtest=ValidatorTest,FailureClassifierTest,RetryOrchestratorTest` — 17/17 PASS
- `mvn test -Dtest=Demo2FeedbackLoopTest` — 1/1 PASS
- `mvn test -Dtest=Demo3EndToEndTest` — 1/1 PASS