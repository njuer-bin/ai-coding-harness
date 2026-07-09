# AGENT_LOG.md — Coding Agent Harness

> 按时间顺序记录关键节点。每条记录包含：时间戳、Task 编号、触发的 Superpowers 技能、关键配置、输出/commit、人工干预、学到的教训。

---

## 2026-07-08

### 条目 1: 项目初始化

| 字段 | 内容 |
|------|------|
| **Task** | — |
| **技能** | `brainstorming` → `writing-plans` |
| **输出** | SPEC.md, PLAN.md 初版 |
| **Commit** | `c822f63` |

**教训：** brainstorming 阶段智能体追问了项目定位和目标用户，帮助明确了 Coding Agent Harness 的核心等式（Agent = LLM + Harness）。

---

### 条目 2: Maven 项目骨架

| 字段 | 内容 |
|------|------|
| **Task** | 1 |
| **技能** | `subagent-driven-development` |
| **输出** | pom.xml（picocli, Jackson, JUnit 5, Mockito 依赖）|
| **Commit** | `223b92c` |

---

### 条目 3: 冷启动验证

| 字段 | 内容 |
|------|------|
| **Task** | 2, 4, 9 |
| **技能** | `cold-start-validation` |
| **输出** | 32 个测试全部通过；SPEC_PROCESS.md |
| **Commit** | `39a45a2` |
| **教训** | 新 agent 发现工作区无源码需要手工提取 pom.xml；Guardrail 路径匹配边界需补充 Path.normalize()；MockLLM 需标注非线程安全 |

---

### 条目 4: Tool 接口与 ToolRegistry

| 字段 | 内容 |
|------|------|
| **Task** | 3 |
| **技能** | `subagent-driven-development` + `test-driven-development` |
| **输出** | Tool 接口（getName, execute, getTimeoutMs）+ ToolRegistry（HashMap 注册分发）|
| **Commits** | `8c0a970`, `8115625` |
| **人工干预** | 初始实现缺少 timeout 约束，评审发现后添加 `getTimeoutMs()` 默认方法 |

---

### 条目 5: ReadFileTool + WriteFileTool

| 字段 | 内容 |
|------|------|
| **Task** | 5 |
| **技能** | `subagent-driven-development` |
| **输出** | 文件读写工具，异常 → 失败 ToolResult |
| **Commit** | `426cb71` |

---

### 条目 6: ExecuteShellTool

| 字段 | 内容 |
|------|------|
| **Task** | 6 |
| **技能** | `subagent-driven-development` |
| **输出** | ProcessBuilder + 30s 超时控制 |
| **Commits** | `648078c`, `651fa1e` |
| **人工干预** | 初始使用硬编码超时常量，评审后改为使用 `getTimeoutMs()` |

---

### 条目 7: RunTestsTool + GlobListFilesTool + SearchCodeTool

| 字段 | 内容 |
|------|------|
| **Task** | 7 |
| **技能** | `subagent-driven-development` |
| **输出** | 14 个测试，覆盖 match、no-match、子目录、无效参数 |
| **Commit** | `1ddaf09` |

---

### 条目 8: GitTool + LintCheckTool

| 字段 | 内容 |
|------|------|
| **Task** | 8 |
| **技能** | `subagent-driven-development` |
| **输出** | 7 个测试，含默认子命令、git log、告警计数 |
| **Commit** | `16f14bf` |

---

### 条目 9: Validator（反馈闭环第一层）

| 字段 | 内容 |
|------|------|
| **Task** | 10 |
| **技能** | `subagent-driven-development` |
| **输出** | Validator 接口 + ValidatorImpl（exitCode==0 → PASS, -1 → TOOL_ERROR, 其余 → FAIL）|
| **Commit** | `418ed20` |

---

### 条目 10: SPEC/PLAN 修订与 Task 补充

| 字段 | 内容 |
|------|------|
| **Task** | — |
| **输出** | 补充 spec.md, plan.md, task 文件 |
| **Commit** | `c1588c2` |

**教训：** SPEC 和 PLAN 需要随实现推进持续更新，冷启动验证发现的缺陷需回写到文档中。

---

### 条目 11: FailureClassifier（反馈闭环第二层）

| 字段 | 内容 |
|------|------|
| **Task** | 11 |
| **技能** | `subagent-driven-development` |
| **输出** | 5 个分类 + 2 个边界测试（null stderr、空输出）|
| **Commits** | `2a63112`, `b6e1e8c` |
| **人工干预** | 初始提交格式不符合项目风格，修复缩进 |

---

### 条目 12: RetryOrchestrator（反馈闭环第三层）

| 字段 | 内容 |
|------|------|
| **Task** | 12 |
| **技能** | `subagent-driven-development` |
| **输出** | 动态重试决策器，不同 FailureCategory 不同 maxRetries |
| **Commit** | `81151e7` |
| **人工干预** | 后续 commit `5fb2cff` 将 TIMEOUT maxRetries 从 2 修正为 1（对齐 SPEC） |

**教训：** SPEC 中的具体数值需要与实现严格对齐，subagent 容易忽略这类细节。

---

### 条目 13: Memory 层

| 字段 | 内容 |
|------|------|
| **Task** | 13 |
| **技能** | `subagent-driven-development` |
| **输出** | Memory 接口 + MemoryImpl（JSON 文件持久化，损坏自动重建）|
| **Commit** | `d02ba55` |

---

### 条目 14: Config + CredentialManager

| 字段 | 内容 |
|------|------|
| **Task** | 14 |
| **技能** | `subagent-driven-development` |
| **输出** | Config 接口 + ConfigImpl + CredentialManager（Base64 + XOR 加密）+ KeychainAdapter 接口 |
| **Commits** | `5855f98`, `3b3d257` |
| **人工干预** | 评审发现缺失 `testCorruptedFileReturnsNull` 边界测试，补充提交 |

---

### 条目 15: 会话连续性维护

| 字段 | 内容 |
|------|------|
| **Task** | — |
| **技能** | — |
| **输出** | 更新 CLAUDE.md 和 PLAN.md 确保新 session 可恢复 |
| **Commit** | `c596862` |

**教训：** 长时间开发会话会被压缩，CLAUDE.md 中的恢复指引对维持进度至关重要。

---

## 2026-07-09

### 条目 16: Engine 主循环（含 HITL 回调）

| 字段 | 内容 |
|------|------|
| **Task** | 15 |
| **技能** | `subagent-driven-development` |
| **输出** | Engine.java（8 参数构造函数，run() 主循环）+ EngineResult + HITLCallback 接口 |
| **Commits** | `432c072`, `724f54d` |
| **人工干预** | 评审发现 HITLCallback 声明为 package-private（应 public），以及冗余的 `setShouldRetry(true)`。提取 HITLCallback 为独立文件并修正可见性 |

**教训：** subagent 倾向于将小接口声明为 package-private，但跨包使用时必须 public。Java 要求 public 接口在独立文件中声明。

**次要遗留：** HITL 测试断言较弱（仅 assertNotNull），未测试 rejection 路径；EngineResult 2 参数构造函数未使用；Config 字段未在 run() 中读取。

---

### 条目 17: CLI 层（picocli）

| 字段 | 内容 |
|------|------|
| **Task** | 16 |
| **技能** | `subagent-driven-development` |
| **输出** | CodingAgentCLI.java — picocli 注解 + 全依赖注入 + HITL 交互回调 |
| **Commit** | `e377001` |
| **评审** | ✅ Approved（Minor：无换行结尾、buildEngine 私有降低可测试性、无异常处理） |

---

### 条目 18: DeepSeekProvider

| 字段 | 内容 |
|------|------|
| **Task** | 17 |
| **技能** | `subagent-driven-development` |
| **输出** | DeepSeekProvider.java（HttpClient + MAX_RETRIES=2 + RETRY_DELAY_MS=2000 + 环境变量回退）|
| **提交** | `1692858` |
| **测试** | 8 个测试（含 invalid key、parseResponse、constructor、STOP 检测）|

---

### 条目 19: 机制演示脚本

| 字段 | 内容 |
|------|------|
| **Task** | 18 |
| **技能** | `subagent-driven-development` |
| **输出** | Demo1GuardrailTest（4 个护栏测试）、Demo2FeedbackLoopTest（反馈闭环测试）、Demo3EndToEndTest（端到端测试）|
| **Commit** | `939c998` |

---

### 条目 20: CI 流水线 + Docker + Logger + 跨平台 Shell

| 字段 | 内容 |
|------|------|
| **Tasks** | 19, 20, 21, 23 |
| **技能** | 直接创建（YAML/Dockerfile/Logger 为纯转录） |
| **输出** | .github/workflows/ci.yml, Dockerfile, Logger.java + LoggerTest.java, ExecuteShellTool 跨平台适配 |
| **Commits** | `ad54fbf`, `a9c371d`, `ca1de05`, `1b3a067` |

**教训：** Logger 的测试因静态状态跨测试方法共享而失败（testLogLevel 将 level 改为 WARN 后 testInfoLog 受影响）。解决方案：在每个测试方法开始时重置静态状态。

---

### 条目 21: 最终全分支评审

| 字段 | 内容 |
|------|------|
| **技能** | `requesting-code-review`（Fable 5 模型） |
| **发现** | 2 Critical + 4 Important |
| **修复** | DeepSeekProvider action 类型大写、Guardrail Path.normalize()、Engine 重试 TOOL_ERROR、HITL 断言加强、移除未用构造函数、Config 驱动 maxIterations |
| **Commit** | `4bc4979` |

**关键 Critical 修复：**

1. **DeepSeekProvider 大小写不匹配**：`extractAction()` 返回的 type 为小写（`read_file`），但 ToolRegistry 使用大写 key（`READ_FILE`）。添加 `.toUpperCase()`。
2. **Guardrail 路径穿越漏洞**：PLAN 要求 `Path.normalize()` 但未实现。添加规范化，防止 `../../etc/shadow` 绕过。

**教训：** 最终评审使用最强模型（Fable 5）执行全分支扫描，发现了 per-task 评审遗漏的跨层问题。Final review 不可跳过。

---

### 条目 22: README.md 补全

| 字段 | 内容 |
|------|------|
| **Task** | 交付物清单 §五-4 |
| **输出** | README.md（项目简介、安装、运行、分发、目录结构、安全边界）|
| **Commit** | `e409093` |

---

## 汇总统计

| 指标 | 数值 |
|------|------|
| 总 Task 数 | 23 |
| 总 Commit 数 | 31 |
| 测试总数 | 109（全部通过）|
| 技能使用 | brainstorming, writing-plans, subagent-driven-development, test-driven-development, requesting-code-review, finishing-a-development-branch |
| 人工干预次数 | 6 次（超时硬编码、代码风格、边界测试补充、HITL 可见性、TIMEOUT 值、最终评审修复）|
| 最长自主运行 | 约 2–3 小时（Task 16 CLI 层实现含编译验证）|

## 个人反思

1. **subagent 的局限性**：subagent 擅长从明确 spec 生成代码，但容易忽略跨层约束（如 enum 命名规范、路径安全）。两阶段评审（spec 合规 + 代码质量）有效捕获了这些问题。

2. **Review 模型选择**：per-task 评审使用 sonnet 足够，但最终全分支评审必须用最强模型（Fable 5）。Fable 5 发现了 per-task 评审遗漏的跨层问题（DeepSeekProvider 大小写不匹配、Guardrail 路径穿越）。

3. **静态状态问题**：Logger 测试因静态字段跨测试方法共享而失败。需要在每个测试中重置状态，或使用测试隔离机制。

4. **中国网络环境**：推送 GitHub 需要稳定的网络连接，必要时使用代理或 SSH。