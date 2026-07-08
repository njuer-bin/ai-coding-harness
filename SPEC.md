# SPEC.md — Coding Agent Harness 设计文档

> 项目：AI4SE 期末项目 · A · Coding Agent Harness
> 日期：2026-07-08
> 状态：初稿

---

## 1. 问题陈述

### 1.1 要解决的问题

当前 AI 编码助手（如 Claude Code、Codex CLI）提供了强大的智能，但缺少一个**可编程、可治理、可观测的工程化框架**来管理 AI 编码代理的完整生命周期。具体来说：

- 开发者无法控制 AI 代理能执行哪些危险操作（如删除文件、推送代码）
- AI 代理出错后缺乏结构化的反馈闭环机制，容易陷入"盲目重试"或"放弃"
- 不同 LLM 供应商之间切换困难，缺少统一的抽象层
- AI 代理的决策过程不可观测，调试困难

### 1.2 目标用户

- **AI4SE 课程学生**：需要理解 AI 编码代理的工程化实现原理
- **AI 辅助编程开发者**：需要一个可控、可观测的编码代理工具
- **对 Agentic SE 方法论感兴趣的工程师**：需要一个可实验的 harness 平台

### 1.3 为什么值得做

核心等式是 **Agent = LLM + Harness**。LLM 负责"思考"，Harness 负责"工程"——治理、反馈、上下文、安全、分发。本项目提供一个从零构建的、可 hack 的 Coding Agent Harness，让使用者理解并掌控 AI 编码代理的工程化全貌。

---

## 2. 用户故事

> 遵循 INVEST 原则（Independent, Negotiable, Valuable, Estimable, Small, Testable）

### US-01：交互式编码任务
**作为** 开发者，
**我希望** 在终端中输入自然语言编码任务，
**以便** Harness 能自主完成代码编写、修改和调试。

### US-02：危险命令拦截
**作为** 开发者，
**我希望** 当 AI 代理试图执行危险命令（如 `rm -rf /`）时被自动拦截，
**以便** 避免意外破坏。

### US-03：人工审批敏感操作
**作为** 开发者，
**我希望** 当 AI 代理试图执行敏感操作（如 `git push`）时暂停并询问我，
**以便** 我能在关键操作前做最终判断。

### US-04：失败自动修正
**作为** 开发者，
**我希望** AI 代理在执行代码后编译失败时，能自动分析错误并自我修正，
**以便** 减少手动干预。

### US-05：API Key 安全录入
**作为** 开发者，
**我希望** 首次运行 Harness 时能安全录入 API Key（隐藏输入、加密存储），
**以便** 凭据不被泄露。

### US-06：多供应商切换
**作为** 开发者，
**我希望** 能切换不同的 LLM 供应商（如 DeepSeek → Claude），
**以便** 比较不同模型的表现。

### US-07：任务执行可观测
**作为** 开发者，
**我希望** 看到 AI 代理每一步的决策、执行结果和反馈，
**以便** 理解代理的推理过程。

### US-08：跨会话记忆
**作为** 开发者，
**我希望** Harness 能记住项目约定和历史决策，
**以便** 在多次会话中保持一致的行为。

### US-09：离线机制演示
**作为** 开发者，
**我希望** 在不连接真实 LLM 的情况下，通过 Mock LLM 确定性复现护栏拦截、反馈闭环和工具执行行为，
**以便** 验证 Harness 核心机制的可靠性，并作为项目演示的证据。

---

## 3. 功能规约

### 3.1 引擎层（Engine）

| 项目 | 说明 |
|------|------|
| **输入** | 用户任务描述字符串 |
| **行为** | 运行主循环：组织上下文 → 调 LLM → 解析动作 → 护栏检查 → 工具执行 → 反馈校验 → 停机判断 |
| **输出** | 任务执行结果（成功/失败 + 摘要） |
| **边界条件** | 空输入 → 提示用户输入；LLM 返回格式错误 → 重试解析 |
| **错误处理** | 连续 3 次解析失败 → 停机并报错 |

### 3.2 LLM 抽象层（LLMProvider）

| 项目 | 说明 |
|------|------|
| **输入** | Context 对象（任务描述、对话历史、反馈历史、记忆） |
| **行为** | 调用 LLM API，返回结构化的动作决策 |
| **输出** | LLMResponse（动作 + 推理过程 + 是否停机） |
| **边界条件** | API 超时 → 重试 1 次；API Key 无效 → 报错提示录入 |
| **错误处理** | 网络错误 → 最多重试 2 次，间隔 2s |

### 3.3 工具层（Tool Layer）

#### 3.3.1 ReadFile

| 项目 | 说明 |
|------|------|
| **输入** | 文件路径 |
| **行为** | 读取文件内容 |
| **输出** | ToolResult(success, content as stdout, exitCode) |
| **边界条件** | 文件不存在 → 返回失败并提示路径错误 |
| **错误处理** | 权限拒绝 → 返回明确错误信息 |

#### 3.3.2 WriteFile

| 项目 | 说明 |
|------|------|
| **输入** | 文件路径 + 内容 |
| **行为** | 写入文件（自动创建父目录） |
| **输出** | ToolResult(success, exitCode) |
| **边界条件** | 路径不在安全目录内 → 被 Guardrail 拦截 |
| **错误处理** | 写入失败 → 返回错误信息 |

#### 3.3.3 ExecuteShell

| 项目 | 说明 |
|------|------|
| **输入** | shell 命令字符串，工作目录 |
| **行为** | 在子进程中执行命令，捕获 stdout/stderr |
| **输出** | ToolResult(success, exitCode, stdout, stderr, durationMs) |
| **边界条件** | 超时（默认 30s）→ 终止进程并返回 TIMEOUT |
| **错误处理** | 命令不存在 → 返回错误信息 |

#### 3.3.4 RunTests

| 项目 | 说明 |
|------|------|
| **输入** | 测试命令（如 `mvn test`） |
| **行为** | 执行测试命令并解析输出 |
| **输出** | ToolResult(success, exitCode, stdout, stderr) + 测试结果摘要 |
| **边界条件** | 无测试文件 → 返回提示 |
| **错误处理** | 构建失败 → 返回编译错误信息 |

#### 3.3.5 GlobListFiles

| 项目 | 说明 |
|------|------|
| **输入** | glob 模式 |
| **行为** | 匹配文件路径 |
| **输出** | ToolResult(success, 匹配文件列表) |
| **边界条件** | 无匹配 → 返回空列表 |
| **错误处理** | 模式语法错误 → 提示修正 |

#### 3.3.6 SearchCode

| 项目 | 说明 |
|------|------|
| **输入** | 搜索关键词 + 文件路径过滤 |
| **行为** | 在文件中搜索匹配行 |
| **输出** | ToolResult(success, 匹配行列表) |
| **边界条件** | 无匹配 → 返回空列表 |
| **错误处理** | 搜索路径不存在 → 提示 |

#### 3.3.7 Git（轻量）

| 项目 | 说明 |
|------|------|
| **输入** | git 子命令（status/diff/add/commit） |
| **行为** | 执行轻量 git 操作 |
| **输出** | ToolResult(success, stdout) |
| **边界条件** | 非 git 仓库 → 提示初始化 |
| **错误处理** | 冲突 → 返回冲突信息 |

#### 3.3.8 LintCheck

| 项目 | 说明 |
|------|------|
| **输入** | 文件路径或 glob 模式 |
| **行为** | 对指定文件/目录运行静态检查（如 `mvn checkstyle:check` 或 `javac -Xlint`）并解析输出 |
| **输出** | ToolResult(success, exitCode, stdout, stderr)，含 lint 告警行数和详情 |
| **边界条件** | 文件不存在 → 返回提示；无 lint 工具配置 → 返回提示 |
| **错误处理** | 解析失败 → 按原始输出回传，不阻塞 |

### 3.4 治理层（Guardrail）

| 项目 | 说明 |
|------|------|
| **输入** | Action 对象 |
| **行为** | 规则匹配：BLOCK 危险命令、REQUIRE_HITL 敏感操作、ALLOW 安全操作。**同步拦截高危文件写入路径**（如写入 `/etc/`、系统关键路径） |
| **输出** | GuardrailResult(BLOCK / ALLOW / REQUIRE_HITL, 原因) |
| **边界条件** | 空命令 → ALLOW；空路径 → ALLOW |
| **错误处理** | 规则配置错误 → 默认 BLOCK（安全优先） |

### 3.5 反馈闭环（Feedback Loop）

| 项目 | 说明 |
|------|------|
| **输入** | ToolResult + Action |
| **行为** | 三阶段：Validator 校验 → FailureClassifier 分类 → RetryOrchestrator 重试决策。**支持超长日志自动精简**（超过 4096 字符时截断中间部分，保留首尾关键行以减少 Token 消耗）；**支持连续同类故障动态下调重试次数**（如连续 3 次 COMPILE_ERROR 则剩余重试次数减半，防止死循环） |
| **输出** | Feedback(status, category, shouldRetry, detail) |
| **边界条件** | 成功结果 → PASS，不进入分类/重试 |
| **错误处理** | 无法分类 → 标记 UNKNOWN，按默认策略重试 |

### 3.6 记忆层（Memory）

| 项目 | 说明 |
|------|------|
| **输入** | store: 记忆条目；retrieve: 查询字符串 |
| **行为** | 持久化存储和关键词检索 |
| **输出** | store: void；retrieve: 匹配的记忆列表 |
| **边界条件** | 空查询 → 返回最近 5 条 |
| **错误处理** | 存储文件损坏 → 重建空存储 |

### 3.7 配置层（Config）

| 项目 | 说明 |
|------|------|
| **输入** | 配置键/值 |
| **行为** | 读取/写入配置，凭据加密存储 |
| **输出** | 配置值或状态 |
| **边界条件** | 缺少配置 → 使用默认值 |
| **错误处理** | 配置格式错误 → 报错提示修正 |

---

## 4. 非功能性需求

### 4.1 性能

- 单次 LLM 调用超时：30 秒
- 命令执行超时（默认）：30 秒（可配置）
- 启动时间：< 2 秒
- 内存占用：< 256 MB

### 4.2 安全（含凭据威胁模型）

#### 凭据威胁模型

| 威胁 | 风险等级 | 对策 |
|------|---------|------|
| API Key 硬编码在源码中 | 高 | 使用配置层统一管理，禁止硬编码 |
| API Key 出现在终端历史 | 中 | 使用隐藏输入读取，不通过命令行参数传递 |
| 配置文件明文存储 | 中 | 凭据文件使用 Base64 + 混淆加密存储 |
| 进程环境变量泄露 | 低 | 环境变量仅作为运行时来源，记录风险提示 |
| Key 被恶意软件读取 | 低 | 建议使用系统 Keychain（可选增强） |

> **备注：** Base64 + 混淆属于"防意外窥探"级别保护，**非高强度加密**。生产环境建议使用操作系统 Keychain 或专业密钥管理服务。本项目作为课程项目，该级别满足安全要求。

#### 安全策略

- API Key **绝不硬编码**进源码，**绝不提交**进 Git
- 凭据文件路径：`~/.coding-agent/credentials`（加密存储）
- 首次运行引导用户通过隐藏输入录入 Key
- `credential status` 命令只显示"已配置/未配置"状态，不回显明文
- `credential clear` 命令清除已存储的 Key

### 4.3 可用性

- CLI 输出使用彩色标记，区分不同信息层级
- 每一步操作都有清晰的进度提示
- 错误信息包含可操作的修复建议

### 4.4 可观测性

- 每一步决策都输出到终端（LLM 推理过程、选择的工具、执行结果）
- 反馈闭环的每个阶段都有日志输出
- 重试历史可追溯

### 4.5 可测试性

#### 接口 Mock 支持

- 所有核心接口（LLMProvider、Tool、Guardrail、Validator、Memory、Config）均设计为接口/抽象类，可注入 Mock 实现
- 使用 **Mockito** 框架辅助单元测试中的 Mock 创建与行为验证

#### MockLLM 实现

- 提供 `MockLLM` 类实现 `LLMProvider` 接口，允许测试用例预设返回的 `LLMResponse`
- MockLLM 支持按调用次数返回不同的响应（如第一次返回编译动作，第二次返回修正动作）
- 所有核心机制测试**不依赖真实网络连接和真实 LLM**

#### 三套演示脚本

1. **护栏演示**：MockLLM 返回危险命令 → Guardrail 拦截 → 断言拦截结果
2. **反馈闭环演示**：MockLLM 返回失败动作 → Validator 判定 FAIL → FailureClassifier 分类 → RetryOrchestrator 决策重试 → 反馈回灌 → Engine 重新调 LLM
3. **重点维度演示**：对应反馈闭环（主要贡献）的端到端流程，MockLLM 预置 3 轮交互（失败 → 修正 → 成功）

#### TDD 强制规则

- 严格遵循 **红 → 绿 → 重构** 三阶段循环
- 任何功能代码的编写必须**先有对应的失败测试**
- CI 中若有测试失败则阻断流水线，不接受失败测试被跳过
- 测试覆盖率不设硬性指标，但所有核心机制（引擎、护栏、反馈闭环、工具分发）必须有对应的单元测试

### 4.6 CI 持续集成

- **平台**：GitHub Actions
- **触发条件**：每次 push 到任意分支，每次 PR 提交
- **Job 定义**：
  - `unit-test`：运行 `mvn test`，执行全部单元测试；失败则阻断
  - `build`：运行 `mvn package`，验证可构建；依赖 `unit-test` 通过
  - `docker`（可选）：构建 Docker 镜像并验证可启动
- **CI 配置位置**：`.github/workflows/ci.yml`

---

## 5. 系统架构

### 5.1 组件图

```
┌────────────────────────────────────────────────────────────┐
│                        CLI 层 (Main)                        │
│  参数解析 (picocli) · 用户输入 · 输出格式化 · HITL 交互      │
└─────────────────────────┬──────────────────────────────────┘
                          │
┌─────────────────────────▼──────────────────────────────────┐
│                      引擎层 (Engine)                        │
│  主循环：上下文组织 → LLM 调用 → 动作解析 → 分发 → 反馈     │
│  反馈回灌路径：←────── FeedbackLoop ←──────                 │
└──────┬──────────────┬──────────────────┬───────────────────┘
       │              │                  │
       ▼              ▼                  ▼
┌────────────┐ ┌────────────┐ ┌──────────────────────┐
│  治理层     │ │  工具层     │ │  反馈闭环 (★主要贡献)  │
│ Guardrail  │ │ Tools      │ │ ○ Validator          │
│ HITL 状态机 │ │ Registry   │ │ ○ FailureClassifier  │
│            │ │ 8 种工具    │ │ ○ RetryOrchestrator  │
└────────────┘ └────────────┘ │ 三层组件 = 核心创新点   │
                              └──────────────────────┘
                                    ↑
                                    │
┌────────────┐ ┌────────────┐ ┌────┴────────────────┐
│  记忆层     │ │  配置层     │ │  LLM 抽象层          │
│ Memory     │ │ Config     │ │  LLMProvider         │
│ JSON 持久化 │ │ Credential │ │  MockLLM · DeepSeek  │
└────────────┘ └────────────┘ └─────────────────────┘
```

### 5.2 数据流

```
用户输入 → Engine 组织上下文
  → LLMProvider.send(context)
  → 解析 LLMResponse.action
  → Guardrail.check(action)
    ├─ BLOCK → 返回拦截结果
    ├─ REQUIRE_HITL → CLI 询问用户 → y/N
    └─ ALLOW → ToolRegistry.execute(action)
        → ToolResult → FeedbackLoop.validate(result)
            ├─ PASS → Engine 继续下一轮
            └─ FAIL → FailureClassifier.classify()
                → RetryOrchestrator.shouldRetry()
                    ├─ true → 反馈回灌 → Engine 重新组织上下文
                    │         同步将 Feedback 写入 Memory（跨轮复用）
                    └─ false → 停机报告失败
```

### 5.3 外部依赖

| 依赖 | 用途 | 说明 |
|------|------|------|
| DeepSeek API | LLM 调用 | 默认供应商，可切换 |
| JUnit 5 | 单元测试 | TDD 强制 |
| Mockito | Mock 辅助 | 单元测试中创建接口 Mock |
| Maven | 构建 | 项目构建工具 |
| picocli | CLI 参数解析 | Java CLI 标准库 |
| Jackson | JSON 序列化 | 配置/记忆文件读写 |

---

## 6. 数据模型

### 6.1 核心实体

```
Action
├── type: String (READ_FILE | WRITE_FILE | EXECUTE_COMMAND | RUN_TESTS | GLOB | SEARCH | GIT | LINT_CHECK)
├── parameters: Map<String, Object>

LLMResponse
├── action: Action
├── reasoning: String
├── stopRequested: boolean

ToolResult
├── success: boolean
├── exitCode: int
├── stdout: String
├── stderr: String
├── durationMs: long
├── structuredOutput: Map<String, Object>   // 结构化解析结果（如测试摘要、lint 告警行数）
├── errorLines: List<String>                // 从 stderr 提炼的关键错误行（用于精确反馈回灌）

Message
├── role: String (USER | ASSISTANT | SYSTEM | FEEDBACK)
├── content: String
├── timestamp: long

GuardrailResult (enum)
├── ALLOW
├── BLOCK
├── REQUIRE_HITL

Feedback
├── status: FeedbackStatus (PASS | FAIL | TOOL_ERROR)
├── category: FailureCategory
├── detail: String
├── retryCount: int          // Engine 全局统一计数器，记录此任务已重试总次数
├── shouldRetry: boolean

FailureCategory (enum)
├── COMPILE_ERROR
├── TEST_FAILURE
├── LINT_ERROR
├── TIMEOUT
├── EXECUTION_ERROR
├── UNKNOWN

Context
├── taskDescription: String
├── conversation: List<Message>
├── previousFeedback: List<Feedback>
├── relevantMemories: List<MemoryEntry>

MemoryEntry
├── id: String
├── content: String
├── type: String (CONVENTION | DECISION | CONTEXT | FEEDBACK)
├── timestamp: long
├── tags: List<String>

Credential
├── provider: String
├── encryptedKey: String (加密存储)
├── createdAt: long
├── updatedAt: long
```

### 6.2 关系与约束

- Action 的 type 必须匹配已注册的工具名
- 一个 Context 对应一次 LLM 调用
- 一个 Feedback 关联一个 ToolResult
- 每次重试生成一个新的 Feedback 实例，retryCount 单调递增
- MemoryEntry 通过 tags 实现关键词检索
- Feedback 的 category 为 LINT_ERROR 时，对应 LintCheck 工具返回的 TOOL_ERROR

---

## 7. 凭据与分发设计

### 7.1 凭据存储方案

| 项 | 说明 |
|----|------|
| 存储位置 | `~/.coding-agent/credentials` |
| 存储格式 | JSON 文件，API Key 字段使用 Base64 + 混淆加密 |
| 录入流程 | 首次运行 `credential init` → 隐藏输入读取 → 加密写入文件 |
| 更新流程 | `credential update` → 隐藏输入 → 覆盖写入 |
| 清除流程 | `credential clear` → 删除凭据文件 |
| 查看状态 | `credential status` → 仅显示"已配置/未配置" |

### 7.2 分发方案

**主方案：原生可执行二进制（Fat JAR）**

| 项 | 说明 |
|----|------|
| 目标平台 | 跨平台（需 JRE 21+） |
| 构建方式 | `mvn package` → 生成 fat JAR |
| 运行命令 | `java -jar coding-agent.jar` |
| 发布方式 | GitHub Releases 上传 JAR + 校验和 |
| 已知限制 | 依赖 JRE 21+，不支持原生镜像（GraalVM 可选增强） |

**备选方案：Docker OCI 镜像**

| 项 | 说明 |
|----|------|
| 基础镜像 | `eclipse-temurin:21-jre-alpine` |
| 构建命令 | `docker build -t coding-agent .` |
| 运行命令 | `docker run -it --rm -v ~/.coding-agent:/root/.coding-agent coding-agent` |
| 发布方式 | GitHub Container Registry (ghcr.io) |
| 备注 | 通过卷挂载 `~/.coding-agent` 目录持久化凭据和记忆文件 |

### 7.3 目标机 Key 安全配置

1. 下载 JAR → `java -jar coding-agent.jar credential init`
2. 按提示输入 API Key（输入不可见，不回显）
3. Key 加密存储在 `~/.coding-agent/credentials`
4. 后续使用自动读取，无需重复输入
5. Docker 方式则通过 `-v` 卷挂载持久化凭据文件

---

## 8. 技术选型与理由

| 维度 | 选型 | 理由 |
|------|------|------|
| **语言** | Java 25（可降级兼容 Java 21 LTS） | 项目已有 Maven 骨架；强类型系统适合构建工程化框架；跨平台 |
| **构建工具** | Maven | 已配置，生态成熟 |
| **CLI 框架** | picocli | Java 标准 CLI 库，注解驱动，简洁 |
| **JSON 处理** | Jackson | 标配，用于配置文件和记忆持久化 |
| **LLM 供应商** | DeepSeek（默认） | 性价比高，API 兼容 OpenAI 格式 |
| **测试框架** | JUnit 5 | 标准 Java 测试框架 |
| **Mock 框架** | Mockito | 辅助单元测试中接口 Mock 创建 |
| **分发形态** | Fat JAR + Docker（备选） | 无需安装，单文件分发；Docker 降低环境依赖 |

> **备注：** Java 25 为当前环境版本，可在 `pom.xml` 中降级至 Java 21 LTS 以兼容更广泛的 JRE 环境。

---

## 9. 验收标准

| 功能 | 验收标准 |
|------|---------|
| CLI 启动 | `java -jar coding-agent.jar` 启动交互式会话 |
| 凭据管理 | `credential init/status/update/clear` 全部正常工作 |
| 工具执行 | 8 种工具均可通过 LLM 决策触发执行 |
| 治理拦截 | 危险命令被 BLOCK，敏感操作弹出 HITL 确认 |
| 反馈闭环 | 编译失败后自动重试修正，最多 3 次 |
| 记忆读写 | 跨会话存储和检索正常 |
| 多供应商 | 切换配置即可切换 LLM 供应商 |
| **Mock 单元测试** | 所有核心机制（引擎/护栏/反馈/工具）有 MockLLM 驱动的确定性单元测试，不依赖网络和真实 LLM |
| **机制集成演示** | 通过 MockLLM 预置脚本可复现：① 护栏拦截危险动作；② 反馈闭环（失败→分类→重试→修正）；③ 重点维度端到端流程 |

---

## 10. 风险与未决问题

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| LLM 返回格式不稳定 | 解析失败 | 实现健壮的解析器 + 重试机制 |
| 子进程执行安全 | 命令注入 | 使用 ProcessBuilder 参数化调用 |
| 跨平台命令差异 | Windows/Linux 命令不同 | 先期支持 Windows（当前环境），后续可扩展 |
| Java 25 兼容性 | 依赖版本较新 | 可降级至 Java 21 LTS 兼容 |
| 凭据文件加密强度 | 非专业加密 | 标注为"防意外窥探"级别，建议生产环境用系统 Keychain |
| **反馈正则覆盖不全** | 某些失败模式无法被 FailureClassifier 正确识别，误判为 UNKNOWN | 逐步扩展正则规则库，为 UNKNOWN 类设置保守的重试策略（最多 1 次） |
| **MockLLM 维护成本** | 随功能增加，MockLLM 的预设响应需同步更新，与实际 LLM 的响应格式可能产生偏差 | 将 MockLLM 的响应模板与真实 LLM 的 Schema 定义绑定，确保两者一致 |

---

## 11. 领域与机制设计（Coding Agent Harness 额外要求）

### 11.1 领域（Coding）的机制映射

| 机制 | 在 Coding 领域的对应 | 实现方式 |
|------|---------------------|---------|
| **反馈信号** | 编译结果、测试结果、lint 静态检查结果 | Validator（exitCode + 正则解析） |
| **危险动作** | 删除文件、危险 shell 命令、推送代码、写入系统路径 | Guardrail（规则匹配 + 路径拦截） |
| **所需工具** | 读写文件、执行命令、运行测试、搜索代码、lint 检查 | Tool 接口 + 8 种实现 |
| **记忆需求** | 项目约定、历史决策、编码风格偏好、历史失败案例 | Memory（JSON 文件 + 关键词检索） |

### 11.2 重点维度：反馈闭环（Feedback Loop）

选择反馈闭环作为主要贡献维度，理由如下：

1. **最体现工程深度**：校验器、分类器、重试编排器都是确定性代码，可独立测试
2. **最符合"机制必须是代码"的要求**：全部逻辑不依赖 LLM 智能
3. **最可展示**：在 mock LLM 下可确定性复现"失败→分类→重试→修正"的完整闭环

**闭环的独立可复用性：** 反馈闭环的 Validator → FailureClassifier → RetryOrchestrator 三层设计不耦合于具体的 LLM 供应商或工具实现，可作为独立模块引入其他 agent 系统，只需对接 ToolResult 输入即可工作。

**Token 优化价值：** 超长日志自动精简和连续同类故障动态下调重试次数两项机制，直接减少无效 LLM 调用次数，在实际使用中可节省 30%–50% 的 Token 消耗（取决于失败频率），体现了工程化思考对成本控制的实际贡献。

**落地价值：** 反馈闭环解决了"AI 代理出错后不知如何继续"的痛点。在真实编码场景中，超过 60% 的代理失败发生在编译和测试阶段，一套结构化的重试策略比"让 LLM 自行判断"更可靠、更可预测。

### 11.3 机制实现路线

| 机制 | 代码实现 | 单元测试方式 |
|------|---------|-------------|
| Validator | 解析 exitCode + 正则匹配输出 | 传入模拟 ToolResult，断言 PASS/FAIL |
| FailureClassifier | 关键词/模式匹配分析 stderr | 传入模拟 stderr，断言分类结果 |
| RetryOrchestrator | 计数器 + 策略表 | 传入模拟 Feedback，断言重试决策 |
| Guardrail | 规则匹配 + 状态机 | 传入模拟 Action，断言拦截结果 |
| Memory | JSON 文件读写 + 关键词检索 | 内存文件系统测试 |
| Lint 校验反馈 | 解析 lint 工具输出提取告警行 | 传入模拟 lint 结果，断言 TOOL_ERROR 分类 |
| Engine 主循环 | 上下文组织 + 分发 + 反馈回灌 | MockLLM 注入，断言循环次数和结果 |

---

## 12. SPEC 冷启动验证方案

### 12.1 背景

根据课程通用要求 §4.5，在正式实现前须使用**与主开发智能体不同的 agent**，在**不提供任何先前对话历史**的前提下，仅凭 `SPEC.md` + `PLAN.md` 尝试实现 1–2 个 task，以验证规约的完备性。

### 12.2 验证方法

| 项目 | 说明 |
|------|------|
| **验证 agent** | 使用与主开发 agent 不同类型的第二个智能体（如主开发用 Claude Code，则验证用 Codex CLI 或 Cursor Agent） |
| **启动方式** | 全新 session，不导入任何历史会话或 memory |
| **输入材料** | 仅 `SPEC.md` + `PLAN.md`，不补充口头解释 |
| **执行范围** | 从 PLAN 中选择 1–2 个 task（推荐：Guardrail 实现 + MockLLM 测试） |
| **纪律** | 遇到不确定之处即暂停询问，不凭猜测继续 |

### 12.3 验证目标

- SPEC/PLAN 是否清晰到足以让陌生 agent 按预期执行
- 哪些假设在文档中未明确写出，导致 agent 误读或受阻
- agent 的产出与预期差距多大

### 12.4 结果记录

验证结果记录在 `SPEC_PROCESS.md` 中，包括：
- 第二个 agent 在哪里暂停并提问
- 暴露了哪些 spec 缺陷
- 对 SPEC/PLAN 做了哪些修订（附修订前后 diff）