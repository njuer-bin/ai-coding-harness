# Coding Agent Harness

一个从零实现的 AI 编码代理框架（harness），用于管理编码智能体的完整生命周期：上下文组织 → LLM 调用 → 动作解析 → 工具执行 → 结果反馈 → 自我修正。

**核心等式：Agent = LLM + Harness**

LLM 负责"决定下一步做什么"，而 harness 负责将其封装为可稳定、可靠工作的系统。本项目实现了 harness 的六个核心维度：决策封装、工具分发、治理护栏、反馈闭环、上下文与记忆、配置管理。

---

## 快速开始

### 前置条件

- **JDK 21+**（推荐 Eclipse Temurin）
- **Maven 3.9+**
- **Docker**（可选，用于容器分发）

### 安装与构建

```bash
# 克隆仓库
git clone https://github.com/njuer-bin/ai-coding-harness.git
cd ai-coding-harness

# 编译
cd untitled && mvn compile

# 运行测试
mvn test

# 打包 fat JAR
mvn package -DskipTests
```

### 运行

```bash
# 查看帮助
java -jar target/coding-agent-1.0.0-jar-with-dependencies.jar --help

# 运行任务（从命令行参数）
java -jar target/coding-agent-1.0.0-jar-with-dependencies.jar "读取 src/main/java 下的所有 Java 文件"

# 运行任务（从 stdin 交互）
java -jar target/coding-agent-1.0.0-jar-with-dependencies.jar
# 出现提示符 "> " 后输入任务描述
```

CLI 启动后将：

1. 使用 MockLLM（预置响应队列）驱动引擎
2. 遇到需要人工确认的操作（如 `git push`、危险命令）时，弹出 `[y/N]` 交互提示
3. 执行完成后打印结果（状态、摘要、日志）

### WebUI 模式（线上部署）

```bash
# 启动 WebUI 服务器（默认端口 8080）
java -jar target/coding-agent-1.0.0-jar-with-dependencies.jar --server

# 指定端口
java -jar target/coding-agent-1.0.0-jar-with-dependencies.jar --server --port=3000
```

启动后在浏览器中访问 `http://localhost:8080/`，通过 Web 表单提交任务并查看执行结果。

### Docker 部署

```bash
# 构建镜像
cd untitled && mvn package -DskipTests
docker build -t coding-agent .

# 运行 CLI 模式
docker run -i --rm coding-agent "your task description"

# 运行 WebUI 模式（暴露端口）
docker run -p 8080:8080 --rm coding-agent --server
```

---

## 分发

### Docker 容器

```bash
# 构建镜像（需要先打包 JAR）
cd untitled && mvn package -DskipTests
docker build -t coding-agent .

# 运行
docker run --rm coding-agent --help

# 交互模式
docker run -it --rm coding-agent "your task description"
```

### 已知限制

| 项目 | 说明 |
|------|------|
| 平台 | Windows / Linux / macOS（需 JDK 21） |
| 架构 | x86_64, ARM64 |
| LLM | 默认使用 MockLLM（预置响应）；可替换为 DeepSeekProvider（需 API Key） |
| Shell | Windows 使用 `cmd.exe /c`，Linux/macOS 使用 `bash -c` |

---

## 目录结构

```
ai-coding-harness/
├── untitled/                          # Maven 项目根目录
│   ├── pom.xml                        # 项目依赖与构建配置
│   ├── Dockerfile                     # 容器镜像构建
│   └── src/
│       ├── main/java/com/codingagent/
│       │   ├── CodingAgentCLI.java    # CLI 入口（picocli）
│       │   ├── engine/                # 引擎主循环 + HITL 回调
│       │   │   ├── Engine.java        # 主循环（LLM→护栏→执行→反馈→记忆）
│       │   │   ├── EngineResult.java  # 执行结果模型
│       │   │   └── HITLCallback.java  # 人机交互回调接口
│       │   ├── config/                # 配置与凭据管理
│       │   │   ├── Config.java        # 配置接口
│       │   │   ├── ConfigImpl.java    # 配置实现
│       │   │   └── CredentialManager.java  # 凭据加密存储（含 KeychainAdapter）
│       │   ├── feedback/              # 反馈闭环（三层）
│       │   │   ├── Validator.java           # 校验器接口
│       │   │   ├── ValidatorImpl.java       # 校验实现
│       │   │   ├── FailureClassifier.java   # 失败分类器接口
│       │   │   ├── FailureClassifierImpl.java   # 分类实现
│       │   │   ├── RetryOrchestrator.java   # 重试编排器接口
│       │   │   └── RetryOrchestratorImpl.java   # 重试决策实现
│       │   ├── guardrail/             # 治理护栏
│       │   │   ├── Guardrail.java     # 接口
│       │   │   └── GuardrailImpl.java # 实现（危险命令/路径/HITL）
│       │   ├── llm/                   # LLM 抽象层
│       │   │   ├── LLMProvider.java   # 接口
│       │   │   ├── MockLLM.java       # Mock 实现（队列驱动）
│       │   │   └── DeepSeekProvider.java  # DeepSeek API（含重试）
│       │   ├── memory/                # 记忆层
│       │   │   ├── Memory.java        # 接口
│       │   │   └── MemoryImpl.java    # JSON 文件持久化
│       │   ├── model/                 # 核心数据模型
│       │   │   ├── Action.java        # 动作模型
│       │   │   ├── ToolResult.java    # 工具执行结果
│       │   │   ├── LLMResponse.java   # LLM 响应
│       │   │   ├── Context.java       # 上下文
│       │   │   ├── Feedback.java      # 反馈结果
│       │   │   ├── MemoryEntry.java   # 记忆条目
│       │   │   └── enums/             # 枚举（GuardrailResult, FeedbackStatus, FailureCategory）
│       │   ├── tool/                  # 工具接口与实现
│       │   │   ├── Tool.java          # 工具接口
│       │   │   ├── ToolRegistry.java  # 工具注册与分发
│       │   │   ├── ReadFileTool.java      # 读文件
│       │   │   ├── WriteFileTool.java     # 写文件
│       │   │   ├── ExecuteShellTool.java  # 执行 Shell 命令
│       │   │   ├── RunTestsTool.java      # 运行测试
│       │   │   ├── GlobListFilesTool.java # 文件搜索（glob）
│       │   │   ├── SearchCodeTool.java    # 代码搜索
│       │   │   ├── GitTool.java           # Git 操作
│       │   │   └── LintCheckTool.java     # 代码检查
│       │   └── log/                   # 全局日志
│       │       └── Logger.java        # 彩色分级日志（DEBUG/INFO/WARN/ERROR）
│       └── test/java/com/codingagent/
│           ├── engine/EngineTest.java     # 引擎主循环测试
│           ├── llm/MockLLMTest.java       # MockLLM 测试
│           ├── llm/DeepSeekProviderTest.java  # DeepSeek 重试测试
│           ├── tool/                      # 8 个工具测试
│           ├── guardrail/GuardrailTest.java   # 护栏测试
│           ├── feedback/                  # Validator/Classifier/Orchestrator 测试
│           ├── memory/MemoryTest.java     # 记忆持久化测试
│           ├── config/                    # Config + CredentialManager 测试
│           └── demo/                      # 机制演示
│               ├── Demo1GuardrailTest.java    # 护栏拦截演示
│               ├── Demo2FeedbackLoopTest.java # 反馈闭环演示
│               └── Demo3EndToEndTest.java     # 端到端流程演示
├── .github/workflows/ci.yml           # GitHub Actions CI
├── SPEC.md                            # 设计文档
├── PLAN.md                            # 实现计划
├── SPEC_PROCESS.md                    # 冷启动验证记录
└── AI4SE_Final_Project_通用要求.md      # 课程通用要求
```

---

## 测试

```bash
# 运行全部测试（109 个）
cd untitled && mvn test

# 运行单个测试类
mvn test -Dtest=EngineTest
mvn test -Dtest=GuardrailTest

# 运行机制演示
mvn test -Dtest=Demo1GuardrailTest,Demo2FeedbackLoopTest,Demo3EndToEndTest

# CI 自动运行：每次 push 触发 GitHub Actions
```

所有核心机制均使用 MockLLM 驱动，**不依赖网络和真实 LLM**，保证离线可运行、结果确定性。

---

## 核心架构

```
用户输入 → CLI (picocli)
                ↓
          Engine 主循环
                ↓
    ┌───────────┼───────────┐
    ↓           ↓           ↓
  LLM 调用   治理护栏    反馈闭环
  (Provider) (Guardrail) (Validator→
    ↓           ↓        Classifier→
  动作解析    拦截/HITL  Orchestrator)
    ↓           ↓           ↓
    └───────────┼───────────┘
                ↓
           工具执行
        (ToolRegistry)
                ↓
     ┌──────────┴──────────┐
     ↓                     ↓
  记忆存储              输出结果
  (Memory)          (EngineResult)
```

### 六个核心维度

| 维度 | 实现 | 关键类 |
|------|------|--------|
| **决策封装** | 组织上下文 → 调用 LLM → 解析动作 | `Engine`, `LLMProvider`, `Action` |
| **工具分发** | 8 种工具注册与按类型分发 | `ToolRegistry`, `Tool` 接口 |
| **治理护栏** | 危险命令/路径拦截 + HITL 审批 | `GuardrailImpl`（路径规范化） |
| **反馈闭环** | 校验 → 分类 → 重试决策 | `Validator` → `FailureClassifier` → `RetryOrchestrator` |
| **记忆** | JSON 持久化存储与检索 | `MemoryImpl` |
| **配置与凭据** | 配置文件 + 加密凭据存储 | `ConfigImpl`, `CredentialManager` |

---

## 安全边界说明

### 凭据安全

- API Key **绝不硬编码**进源码，通过环境变量 `DEEPSEEK_API_KEY` 或 `CredentialManager` 传入
- `CredentialManager` 使用 **Base64 + XOR（0x5A）混淆加密** 存储凭据
- 凭据文件路径：`~/.coding-agent/credentials`
- 预留 `KeychainAdapter` 接口，可扩展接入系统钥匙串（macOS Keychain / Windows Credential Manager / Linux Secret Service）

### 治理护栏

- **危险命令拦截**：`rm -rf /`、`mkfs`、`dd`、`shutdown` 等命令自动 BLOCK
- **危险路径拦截**：写入 `/etc/`、`/boot/` 等系统关键路径自动 BLOCK
- **路径规范化**：匹配前通过 `Path.normalize()` 规范化，防止 `../../etc/shadow` 等路径穿越攻击
- **HITL（人工确认）**：`git push`、`deploy`、`npm publish` 等发布操作需要用户 Y/N 确认
- **安全操作放行**：读取文件、搜索代码等只读操作直接 ALLOW

### 工具执行安全

- 所有工具通过 `ToolResult` 返回结构化结果（成功/失败、退出码、stdout、stderr）
- Shell 命令执行超时默认为 30 秒（可配置）
- 超时后进程被强制终止

---

## 技术栈

| 组件 | 选型 | 理由 |
|------|------|------|
| 语言 | Java 21 | 强类型、跨平台、丰富的标准库 |
| 构建 | Maven | 依赖管理成熟，插件生态丰富 |
| CLI | picocli 4.7.6 | 注解驱动、自动生成帮助 |
| JSON | Jackson 2.17.2 | 高性能 JSON 序列化 |
| 测试 | JUnit 5 + Mockito | 行业标准测试框架 |
| LLM | DeepSeek API（可替换） | 性价比高，支持 Chat Completions |
| 分发 | Docker + fat JAR | 一次构建到处运行 |

---

## 许可证

本项目为 AI4SE 课程项目。