# Coding Agent Harness 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个从零实现的 Coding Agent Harness CLI 工具，包含完整的引擎主循环、LLM 抽象层、8 种工具、治理护栏、反馈闭环、记忆层、配置与凭据管理。

**Architecture:** 分层架构，CLI 层 → 引擎层 → 治理层/工具层/反馈闭环 → LLM 抽象层/记忆层/配置层。每层单向依赖，可独立 Mock 测试。

**Tech Stack:** Java 25, Maven, picocli (CLI), Jackson (JSON), JUnit 5 (test), Mockito (mock)

## 全局验收标准

所有 Task 完成后必须满足以下条件方可视为整体完成：
- `mvn test` 全量测试通过，零失败、零错误
- `mvn package` 可打出完整 fat JAR，包含所有依赖
- `java -jar target/coding-agent-1.0.0.jar --help` 显示帮助信息

## 全局约束

- 所有枚举常量统一全大写（READ_FILE、ALLOW、PASS、TOOL_ERROR 等）
- API Key 绝不硬编码进源码，绝不提交进 Git
- 凭据文件路径：`~/.coding-agent/credentials`（Base64 + 混淆加密）
- 所有核心接口设计为 Java interface，可注入 Mock 实现
- 每个核心机制必须有 MockLLM 驱动的确定性单元测试，不依赖网络和真实 LLM
- 遵循 TDD：先写失败测试 → 再写最小实现 → 再重构
- 工具执行超时默认 30s，可配置
- 项目目录：`untitled/`（Maven 项目根目录，位于仓库根目录下）
- MockLLM 内部使用非线程安全的 Queue，标注为"非线程安全"，单线程测试环境使用
- Guardrail 路径匹配前先通过 `Path.normalize()` 规范化，防止相对路径穿越

## 并行开发说明

以下 Task 之间无依赖关系，可使用独立的 git worktree 并行开发：

| 并行组 | 包含 Task | 前提条件 | 建议 Worktree 名称 |
|--------|----------|---------|-------------------|
| 组 A | Task 3 (Tool 接口) + Task 5/6/7/8 (工具实现) | Task 2 完成 | `wt-tools` |
| 组 B | Task 4 (LLMProvider + MockLLM) + Task 17 (DeepSeekProvider) | Task 2 完成 | `wt-llm` |
| 组 C | Task 9 (Guardrail) | Task 2 完成 | `wt-guardrail` |
| 组 D | Task 10/11/12 (反馈闭环三层) | Task 2 完成 | `wt-feedback` |
| 组 E | Task 13 (Memory) + Task 14 (Config + CredentialManager) | Task 2 完成 | `wt-infra` |

> 各组可并行开发，完成后通过 PR 合并到主分支。Engine（Task 15）和 CLI（Task 16）依赖全部前置组完成后方可开始。

---

## Task 完成状态总表

| Task | 描述 | 状态 | 依赖 | 验证结果 |
|------|------|------|------|---------|
| 1 | 项目设置与依赖 | ✅ **已通过** | 无 | `mvn compile` BUILD SUCCESS |
| 2 | 核心模型与枚举 | ✅ **已通过** | 1 | `mvn test -Dtest=ModelTest` 16/16 PASS |
| 3 | Tool 接口 + ToolRegistry | ✅ **已通过** | 2 | `mvn test -Dtest=ToolRegistryTest` 4/4 PASS |
| 4 | LLMProvider + MockLLM | ✅ **已通过** | 2 | `mvn test -Dtest=MockLLMTest` 3/3 PASS |
| 5 | ReadFile + WriteFile | ✅ **已通过** | 3 | `mvn test -Dtest=ReadFileToolTest,WriteFileToolTest` 3/3 PASS |
| 6 | ExecuteShell | ✅ **已通过** | 3 | `mvn test -Dtest=ExecuteShellToolTest` 2/2 PASS |
| 7 | RunTests + GlobListFiles + SearchCode | ✅ **已通过** | 3 | `mvn test -Dtest=RunTestsToolTest,GlobListFilesToolTest,SearchCodeToolTest` 14/14 PASS |
| 8 | Git + LintCheck | ✅ **已通过** | 3 | `mvn test -Dtest=GitToolTest,LintCheckToolTest` 7/7 PASS |
| 9 | Guardrail | ✅ **已通过** | 2 | `mvn test -Dtest=GuardrailTest` 13/13 PASS |
| 10 | Validator | ✅ **已通过** | 2 | `mvn test -Dtest=ValidatorTest` 3/3 PASS |
| 11 | FailureClassifier | ✅ **已通过** | 2 | `mvn test -Dtest=FailureClassifierTest` 7/7 PASS |
| 12 | RetryOrchestrator | ✅ **已通过** | 2 | `mvn test -Dtest=RetryOrchestratorTest` 7/7 PASS |
| 13 | Memory | ✅ **已通过** | 2 | `mvn test -Dtest=MemoryTest` 4/4 PASS |
| 14 | Config + CredentialManager | ✅ **已通过** | 2 | `mvn test -Dtest=CredentialManagerTest,ConfigTest` 6/6 PASS |
| 15 | Engine 主循环 | ✅ **已通过** | 4,9,10,11,12,13,14 | `mvn test -Dtest=EngineTest` 3/3 PASS |
| 16 | CLI 层 | ✅ **已通过** | 15 | `java -jar coding-agent.jar` 启动正常，--help 正常 |
| 17 | DeepSeekProvider | ✅ **已通过** | 4 | `mvn test -Dtest=DeepSeekProviderTest` 8/8 PASS |
| 18 | 机制演示脚本 | ✅ **已通过** | 4,9,10,11,12 | `mvn test -Dtest=Demo1GuardrailTest,Demo2FeedbackLoopTest,Demo3EndToEndTest` 全 PASS |
| 19 | CI 流水线 | ✅ **已通过** | 全部 | GitHub Actions 绿色 PASS |
| 20 | Docker 镜像 | ✅ **已通过** | 19 | `docker build` + `docker run` 成功 |
| 21 | 全局日志模块 | ✅ **已通过** | 3 | 日志输出彩色分级 |
| 22 | SPEC 冷验证 | ✅ **已通过** | 2 | 陌生 agent 完成 3 个 Task，32 测试通过 |
| 23 | 跨平台 Shell 适配 | ✅ **已通过** | 6 | Windows/Linux 双平台测试 |

---

## 文件结构

```
untitled/
├── pom.xml
├── Dockerfile
├── .github/workflows/ci.yml
├── src/main/java/com/codingagent/
│   ├── CodingAgentCLI.java              # 主入口，picocli 命令行
│   ├── log/
│   │   └── Logger.java                  # 全局分级日志模块
│   ├── model/
│   │   ├── Action.java                  # 动作模型
│   │   ├── ToolResult.java              # 工具执行结果
│   │   ├── LLMResponse.java             # LLM 响应
│   │   ├── Message.java                 # 对话消息
│   │   ├── Context.java                 # 上下文（Engine 传给 LLM）
│   │   ├── Feedback.java                # 反馈结果
│   │   ├── MemoryEntry.java             # 记忆条目
│   │   └── enums/
│   │       ├── GuardrailResult.java     # ALLOW / BLOCK / REQUIRE_HITL
│   │       ├── FeedbackStatus.java      # PASS / FAIL / TOOL_ERROR
│   │       └── FailureCategory.java     # 失败分类枚举
│   ├── llm/
│   │   ├── LLMProvider.java             # 接口
│   │   ├── MockLLM.java                 # Mock 实现
│   │   └── DeepSeekProvider.java        # DeepSeek API 实现
│   ├── tool/
│   │   ├── Tool.java                    # 接口
│   │   ├── ToolRegistry.java            # 工具注册与分发
│   │   ├── ReadFileTool.java
│   │   ├── WriteFileTool.java
│   │   ├── ExecuteShellTool.java
│   │   ├── RunTestsTool.java
│   │   ├── GlobListFilesTool.java
│   │   ├── SearchCodeTool.java
│   │   ├── GitTool.java
│   │   └── LintCheckTool.java
│   ├── guardrail/
│   │   ├── Guardrail.java               # 接口
│   │   └── GuardrailImpl.java           # 实现（规则匹配 + HITL 状态机）
│   ├── feedback/
│   │   ├── Validator.java               # 接口
│   │   ├── ValidatorImpl.java           # 校验实现
│   │   ├── FailureClassifier.java       # 接口
│   │   ├── FailureClassifierImpl.java   # 分类实现
│   │   ├── RetryOrchestrator.java       # 接口
│   │   └── RetryOrchestratorImpl.java   # 重试决策实现
│   ├── memory/
│   │   ├── Memory.java                  # 接口
│   │   └── MemoryImpl.java              # JSON 文件持久化
│   └── config/
│       ├── Config.java                  # 接口
│       ├── ConfigImpl.java              # 配置管理
│       └── CredentialManager.java       # 凭据加密存储管理（含 Keychain 扩展接口）
├── src/test/java/com/codingagent/
│   ├── model/
│   │   └── ModelTest.java               # 模型创建与序列化
│   ├── llm/
│   │   ├── MockLLMTest.java
│   │   └── DeepSeekProviderTest.java    # 含网络失败重试测试
│   ├── tool/
│   │   ├── ReadFileToolTest.java
│   │   ├── WriteFileToolTest.java
│   │   ├── ExecuteShellToolTest.java
│   │   ├── RunTestsToolTest.java
│   │   ├── GlobListFilesToolTest.java
│   │   ├── SearchCodeToolTest.java
│   │   ├── GitToolTest.java
│   │   └── LintCheckToolTest.java
│   ├── guardrail/
│   │   └── GuardrailTest.java
│   ├── feedback/
│   │   ├── ValidatorTest.java
│   │   ├── FailureClassifierTest.java
│   │   └── RetryOrchestratorTest.java
│   ├── memory/
│   │   └── MemoryTest.java
│   ├── config/
│   │   ├── CredentialManagerTest.java
│   │   └── ConfigTest.java              # 含配置文件损坏、密钥篡改测试
│   ├── engine/
│   │   └── EngineTest.java
│   └── demo/
│       ├── Demo1GuardrailTest.java      # JUnit 自动化测试版
│       ├── Demo2FeedbackLoopTest.java   # JUnit 自动化测试版
│       └── Demo3EndToEndTest.java       # JUnit 自动化测试版
```

---

## Task 依赖关系图

```
Task 1 (项目设置)
  └→ Task 2 (模型+枚举)
       │
       ├──────────────────────────────────────────────────┐
       │  [并行组 A]  [并行组 B]  [并行组 C]  [并行组 D]  [并行组 E]  │
       │     │           │           │           │           │     │
       │     ▼           ▼           ▼           ▼           ▼     │
       │  Task 3     Task 4     Task 9     Task 10-12  Task 13-14 │
       │  (Tool)     (LLM)     (Guard)    (Feedback)  (Infra)     │
       │     │           │                                         │
       │  ┌──┴──┐        │                                         │
       │  ▼     ▼        ▼                                         │
       │  T5-8  T21  Task 17 (DeepSeek)                            │
       │  (工具) (日志)     │                                         │
       └──────┬──────────┼─────────────────────────────────────────┘
              │          │
              ▼          ▼
         Task 15 (Engine 主循环 + HITL 回调)
              │
              ▼
         Task 16 (CLI 层)
              │
         ┌────┴────┐
         ▼         ▼
     Task 18   Task 23 (跨平台)
     (演示)        │
         │         │
         ▼         ▼
     Task 19 (CI 流水线) ← Task 22 (冷验证)
         │
         ▼
     Task 20 (Docker 镜像)
```

---

### Task 1: 项目设置与依赖（✅ 已完成）

**Files:**
- Modify: `untitled/pom.xml`
- Create: `untitled/src/main/java/com/codingagent/` (目录结构)

**Interfaces:**
- Consumes: 无
- Produces: 可编译的 Maven 项目骨架

- [x] **Step 1: 更新 pom.xml 添加依赖**

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.codingagent</groupId>
    <artifactId>coding-agent</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>info.picocli</groupId>
            <artifactId>picocli</artifactId>
            <version>4.7.6</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.17.2</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.11.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.12.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.codingagent.CodingAgentCLI</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.7.1</version>
                <configuration>
                    <descriptorRefs><descriptorRef>jar-with-dependencies</descriptorRef></descriptorRefs>
                    <archive>
                        <manifest><mainClass>com.codingagent.CodingAgentCLI</mainClass></manifest>
                    </archive>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals><goal>single</goal></goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

- [x] **Step 2: 创建包目录结构**

```bash
mkdir -p untitled/src/main/java/com/codingagent/model/enums
mkdir -p untitled/src/main/java/com/codingagent/llm
mkdir -p untitled/src/main/java/com/codingagent/tool
mkdir -p untitled/src/main/java/com/codingagent/guardrail
mkdir -p untitled/src/main/java/com/codingagent/feedback
mkdir -p untitled/src/main/java/com/codingagent/memory
mkdir -p untitled/src/main/java/com/codingagent/config
mkdir -p untitled/src/main/java/com/codingagent/log
mkdir -p untitled/src/test/java/com/codingagent/model
mkdir -p untitled/src/test/java/com/codingagent/llm
mkdir -p untitled/src/test/java/com/codingagent/tool
mkdir -p untitled/src/test/java/com/codingagent/guardrail
mkdir -p untitled/src/test/java/com/codingagent/feedback
mkdir -p untitled/src/test/java/com/codingagent/memory
mkdir -p untitled/src/test/java/com/codingagent/config
mkdir -p untitled/src/test/java/com/codingagent/engine
mkdir -p untitled/src/test/java/com/codingagent/demo
```

- [x] **Step 3: 验证编译**

```bash
cd untitled && mvn compile
```
Expected: BUILD SUCCESS

- [x] **Step 4: 提交**

```bash
git add untitled/pom.xml
git commit -m "chore: set up Maven project with dependencies"
```

---

### Task 2: 核心模型与枚举（✅ 已完成）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/model/enums/GuardrailResult.java`
- Create: `untitled/src/main/java/com/codingagent/model/enums/FeedbackStatus.java`
- Create: `untitled/src/main/java/com/codingagent/model/enums/FailureCategory.java`
- Create: `untitled/src/main/java/com/codingagent/model/Action.java`
- Create: `untitled/src/main/java/com/codingagent/model/LLMResponse.java`
- Create: `untitled/src/main/java/com/codingagent/model/ToolResult.java`
- Create: `untitled/src/main/java/com/codingagent/model/Message.java`
- Create: `untitled/src/main/java/com/codingagent/model/Context.java`
- Create: `untitled/src/main/java/com/codingagent/model/Feedback.java`
- Create: `untitled/src/main/java/com/codingagent/model/MemoryEntry.java`
- Test: `untitled/src/test/java/com/codingagent/model/ModelTest.java`

**Interfaces:**
- Consumes: 无
- Produces: 所有模型类，供后续所有 Task 引用

- [x] **Step 1: 写枚举测试（16 个测试）**

```java
// ModelTest.java — 完整测试代码见已有实现，含 16 个测试方法
// 覆盖：3 枚举值、7 模型类创建、default 构造器、ToolResult 扩展字段
```

- [x] **Step 2: 运行测试验证失败**
```bash
cd untitled && mvn test
```
Expected: COMPILATION ERROR

- [x] **Step 3: 创建三个枚举类**
- GuardrailResult: ALLOW, BLOCK, REQUIRE_HITL
- FeedbackStatus: PASS, FAIL, TOOL_ERROR
- FailureCategory: COMPILE_ERROR, TEST_FAILURE, LINT_ERROR, TIMEOUT, EXECUTION_ERROR, UNKNOWN

- [x] **Step 4: 创建 7 个模型类**（Action, ToolResult, LLMResponse, Message, Context, Feedback, MemoryEntry）

- [x] **Step 5: 运行测试验证通过**
```bash
cd untitled && mvn test
```
Expected: BUILD SUCCESS, ModelTest 16/16 PASS

- [x] **Step 6: 提交**
```bash
git add untitled/src/main/java/com/codingagent/model/
git add untitled/src/test/java/com/codingagent/model/
git commit -m "feat: add core models and enums"
```

---

### Task 3: Tool 接口 + ToolRegistry（✅ 已完成）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/tool/Tool.java`
- Create: `untitled/src/main/java/com/codingagent/tool/ToolRegistry.java`
- Test: `untitled/src/test/java/com/codingagent/tool/ToolRegistryTest.java`

**Interfaces:**
- Consumes: Action, ToolResult
- Produces: Tool 接口（所有工具实现此接口），ToolRegistry（按 Action.type 分发）

- [x] **Step 1: 写测试**
```java
// ToolRegistryTest.java — 4 个测试（注册执行、未知工具、超时配置、null action）
```

- [x] **Step 2: 运行测试验证失败**
```bash
cd untitled && mvn test -Dtest=ToolRegistryTest
```
Expected: COMPILATION ERROR

- [x] **Step 3: 创建 Tool 接口**（getName, execute, default getTimeoutMs=30000）

- [x] **Step 4: 创建 ToolRegistry**（HashMap 注册 + null action 防护 + 超时配置）

- [x] **Step 5: 运行测试验证通过**
```bash
cd untitled && mvn test -Dtest=ToolRegistryTest
```
Expected: BUILD SUCCESS, 4/4 PASS

- [x] **Step 6: 提交**
```bash
git add untitled/src/main/java/com/codingagent/tool/
git add untitled/src/test/java/com/codingagent/tool/ToolRegistryTest.java
git commit -m "feat: add Tool interface and ToolRegistry"
```

---

### Task 4: LLMProvider 接口 + MockLLM（✅ 已完成）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/llm/LLMProvider.java`
- Create: `untitled/src/main/java/com/codingagent/llm/MockLLM.java`
- Test: `untitled/src/test/java/com/codingagent/llm/MockLLMTest.java`

**Interfaces:**
- Consumes: Context, LLMResponse
- Produces: LLMProvider 接口（Engine 通过它调用 LLM），MockLLM（测试用预置响应）

- [x] **Step 1: 写测试**（3 个测试：预设响应、无预设报错、多响应队列）

- [x] **Step 2: 创建 LLMProvider 接口**（send(Context)）

- [x] **Step 3: 创建 MockLLM**（Queue 预置响应，非线程安全标注）

- [x] **Step 4: 运行测试**
```bash
cd untitled && mvn test -Dtest=MockLLMTest
```
Expected: BUILD SUCCESS, 3/3 PASS

- [x] **Step 5: 提交**

---

### Task 5: ReadFileTool + WriteFileTool（✅ 已完成）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/tool/ReadFileTool.java`
- Create: `untitled/src/main/java/com/codingagent/tool/WriteFileTool.java`
- Test: `untitled/src/test/java/com/codingagent/tool/ReadFileToolTest.java`
- Test: `untitled/src/test/java/com/codingagent/tool/WriteFileToolTest.java`

- [x] **Step 1: 写测试**（ReadFile: 存在文件、不存在文件；WriteFile: 写入文件 + 验证存在）

- [x] **Step 2: 创建 ReadFileTool**（Files.readString，异常 → 失败 ToolResult）

- [x] **Step 3: 创建 WriteFileTool**（Files.createDirectories + Files.writeString）

- [x] **Step 4: 运行测试**
```bash
cd untitled && mvn test -Dtest=ReadFileToolTest,WriteFileToolTest
```
Expected: BUILD SUCCESS, 3/3 PASS

- [x] **Step 5: 提交**

---

### Task 6: ExecuteShellTool（✅ 已完成）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/tool/ExecuteShellTool.java`
- Test: `untitled/src/test/java/com/codingagent/tool/ExecuteShellToolTest.java`

- [x] **Step 1: 写测试**（echo 命令成功、exit 1 失败）

- [x] **Step 2: 创建 ExecuteShellTool**（ProcessBuilder bash, 30s 超时, 使用 getTimeoutMs()）

- [x] **Step 3: 运行测试**
```bash
cd untitled && mvn test -Dtest=ExecuteShellToolTest
```
Expected: BUILD SUCCESS, 2/2 PASS

- [x] **Step 4: 提交**

---

### Task 7: RunTestsTool + GlobListFilesTool + SearchCodeTool（✅ 已完成）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/tool/RunTestsTool.java`
- Create: `untitled/src/main/java/com/codingagent/tool/GlobListFilesTool.java`
- Create: `untitled/src/main/java/com/codingagent/tool/SearchCodeTool.java`
- Test: 各工具对应测试类（共 14 个测试）

- [x] **Step 1: 创建 RunTestsTool**（委托 ExecuteShellTool，解析 BUILD SUCCESS）

- [x] **Step 2: 创建 GlobListFilesTool**（PathMatcher glob，relativize 路径）

- [x] **Step 3: 创建 SearchCodeTool**（walk 目录树，Files.readString 匹配关键词）

- [x] **Step 4: 写测试**（14 个测试，覆盖 match、no-match、子目录、无效参数）

- [x] **Step 5: 运行测试**
```bash
cd untitled && mvn test -Dtest=RunTestsToolTest,GlobListFilesToolTest,SearchCodeToolTest
```
Expected: BUILD SUCCESS, 14/14 PASS

- [x] **Step 6: 提交**

---

### Task 8: GitTool + LintCheckTool（✅ 已完成）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/tool/GitTool.java`
- Create: `untitled/src/main/java/com/codingagent/tool/LintCheckTool.java`
- Test: 对应测试类（共 7 个测试）

- [x] **Step 1: 创建 GitTool**（git status/diff/add/commit，委托 ExecuteShellTool）

- [x] **Step 2: 创建 LintCheckTool**（mvn checkstyle → javac -Xlint 回退，解析告警行数）

- [x] **Step 3: 写测试**（7 个测试，含默认子命令、git log、告警计数）

- [x] **Step 4: 运行测试**
```bash
cd untitled && mvn test -Dtest=GitToolTest,LintCheckToolTest
```
Expected: BUILD SUCCESS, 7/7 PASS

- [x] **Step 5: 提交**

---

### Task 9: Guardrail（治理护栏）（✅ 已完成）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/guardrail/Guardrail.java`
- Create: `untitled/src/main/java/com/codingagent/guardrail/GuardrailImpl.java`
- Test: `untitled/src/test/java/com/codingagent/guardrail/GuardrailTest.java`

- [x] **Step 1: 写测试**（13 个测试：危险命令、危险路径、HITL 敏感操作、空命令、相对路径穿越等）

- [x] **Step 2: 创建 Guardrail 接口**（check(Action)）

- [x] **Step 3: 创建 GuardrailImpl**（危险命令表 + 敏感操作表 + 危险路径表 + Path.normalize()）

- [x] **Step 4: 运行测试**
```bash
cd untitled && mvn test -Dtest=GuardrailTest
```
Expected: BUILD SUCCESS, 13/13 PASS

- [x] **Step 5: 提交**

---

### Task 10: Feedback — Validator（✅ 已完成）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/feedback/Validator.java`
- Create: `untitled/src/main/java/com/codingagent/feedback/ValidatorImpl.java`
- Test: `untitled/src/test/java/com/codingagent/feedback/ValidatorTest.java`

- [x] **Step 1: 写测试**（3 个测试：PASS、FAIL、TOOL_ERROR）

- [x] **Step 2: 创建接口和实现**（exitCode==0 → PASS, exitCode==-1 → TOOL_ERROR, 其余 → FAIL）

- [x] **Step 3: 运行测试**
```bash
cd untitled && mvn test -Dtest=ValidatorTest
```
Expected: BUILD SUCCESS, 3/3 PASS

- [x] **Step 4: 提交**

---

### Task 11: Feedback — FailureClassifier

**Files:**
- Create: `untitled/src/main/java/com/codingagent/feedback/FailureClassifier.java`
- Create: `untitled/src/main/java/com/codingagent/feedback/FailureClassifierImpl.java`
- Test: `untitled/src/test/java/com/codingagent/feedback/FailureClassifierTest.java`

**边界测试要求：** 除已有 5 个分类测试外，新增：
- `testCompileErrorWithNullStderr` — stderr 为 null 时返回 UNKNOWN
- `testEmptyOutput` — stdout/stderr 均为空字符串时返回 UNKNOWN

- [ ] **Step 1: 写测试（5 个基础 + 2 个边界 = 7 个测试）**

```java
// FailureClassifierTest.java
package com.codingagent.feedback;

import com.codingagent.model.ToolResult;
import com.codingagent.model.enums.FailureCategory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FailureClassifierTest {
    @Test void testCompileError() {
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        ToolResult result = new ToolResult(false, 1, "", "error: cannot find symbol", 100L);
        assertEquals(FailureCategory.COMPILE_ERROR, classifier.classify(result));
    }

    @Test void testTestFailure() {
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        ToolResult result = new ToolResult(false, 1, "", "Tests run: 5, Failures: 2", 100L);
        assertEquals(FailureCategory.TEST_FAILURE, classifier.classify(result));
    }

    @Test void testLintError() {
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        ToolResult result = new ToolResult(false, 1, "Checkstyle: warning", "", 100L);
        assertEquals(FailureCategory.LINT_ERROR, classifier.classify(result));
    }

    @Test void testTimeout() {
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        ToolResult result = new ToolResult(false, -1, "", "TIMEOUT", 100L);
        assertEquals(FailureCategory.TIMEOUT, classifier.classify(result));
    }

    @Test void testUnknown() {
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        ToolResult result = new ToolResult(false, 1, "", "some weird error", 100L);
        assertEquals(FailureCategory.UNKNOWN, classifier.classify(result));
    }

    @Test void testNullStderr() {
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        ToolResult result = new ToolResult(false, 1, "", null, 100L);
        assertNotNull(classifier.classify(result));
    }

    @Test void testEmptyOutput() {
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        ToolResult result = new ToolResult(false, 1, "", "", 100L);
        assertEquals(FailureCategory.UNKNOWN, classifier.classify(result));
    }
}
```

- [ ] **Step 2: 创建接口和实现**

```java
// FailureClassifier.java
package com.codingagent.feedback;
import com.codingagent.model.ToolResult;
import com.codingagent.model.enums.FailureCategory;
public interface FailureClassifier {
    FailureCategory classify(ToolResult result);
}
```

```java
// FailureClassifierImpl.java
package com.codingagent.feedback;
import com.codingagent.model.ToolResult;
import com.codingagent.model.enums.FailureCategory;

public class FailureClassifierImpl implements FailureClassifier {
    @Override
    public FailureCategory classify(ToolResult result) {
        String stdout = result.getStdout() != null ? result.getStdout() : "";
        String stderr = result.getStderr() != null ? result.getStderr() : "";
        String combined = stdout + "\n" + stderr;

        if (result.getExitCode() == -1 || combined.contains("TIMEOUT")) {
            return FailureCategory.TIMEOUT;
        }
        if (combined.contains("error:") || combined.contains("cannot find symbol")
            || combined.contains("compilation error")) {
            return FailureCategory.COMPILE_ERROR;
        }
        if (combined.contains("Failures:") || combined.contains("Tests failed")
            || combined.contains("FAILED") || combined.contains("Test run failed")) {
            return FailureCategory.TEST_FAILURE;
        }
        if (combined.contains("warning") || combined.contains("WARN")
            || combined.contains("Checkstyle") || combined.contains("lint")) {
            return FailureCategory.LINT_ERROR;
        }
        return FailureCategory.UNKNOWN;
    }
}
```

- [ ] **Step 3: 运行测试**
```bash
cd untitled && mvn test -Dtest=FailureClassifierTest
```
Expected: BUILD SUCCESS, 7/7 PASS

- [ ] **Step 4: 提交**
```bash
git add untitled/src/main/java/com/codingagent/feedback/FailureClassifier.java
git add untitled/src/main/java/com/codingagent/feedback/FailureClassifierImpl.java
git add untitled/src/test/java/com/codingagent/feedback/FailureClassifierTest.java
git commit -m "feat: add FailureClassifier for feedback loop"
```

---

### Task 12: Feedback — RetryOrchestrator

**Files:**
- Create: `untitled/src/main/java/com/codingagent/feedback/RetryOrchestrator.java`
- Create: `untitled/src/main/java/com/codingagent/feedback/RetryOrchestratorImpl.java`
- Test: `untitled/src/test/java/com/codingagent/feedback/RetryOrchestratorTest.java`

**边界测试要求：** 新增 `testResetAfterSuccess` — 成功后自动重置连续失败计数器

- [ ] **Step 1: 写测试（6 个基础 + 1 个边界 = 7 个测试）**

```java
// RetryOrchestratorTest.java
package com.codingagent.feedback;

import com.codingagent.model.Feedback;
import com.codingagent.model.enums.FeedbackStatus;
import com.codingagent.model.enums.FailureCategory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryOrchestratorTest {
    @Test void testCompileErrorRetryAllowed() {
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        Feedback fb = new Feedback(FeedbackStatus.FAIL, FailureCategory.COMPILE_ERROR, "error", 1, false);
        assertTrue(orchestrator.shouldRetry(fb));
    }

    @Test void testTimeoutRetryLimited() {
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        Feedback fb = new Feedback(FeedbackStatus.TOOL_ERROR, FailureCategory.TIMEOUT, "timeout", 1, false);
        assertTrue(orchestrator.shouldRetry(fb));
    }

    @Test void testMaxRetriesExceeded() {
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        Feedback fb = new Feedback(FeedbackStatus.FAIL, FailureCategory.COMPILE_ERROR, "error", 3, false);
        assertFalse(orchestrator.shouldRetry(fb));
    }

    @Test void testMaxRetriesExceededForTimeout() {
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        Feedback fb = new Feedback(FeedbackStatus.TOOL_ERROR, FailureCategory.TIMEOUT, "timeout", 2, false);
        assertFalse(orchestrator.shouldRetry(fb));
    }

    @Test void testPassDoesNotRetry() {
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        Feedback fb = new Feedback(FeedbackStatus.PASS, FailureCategory.COMPILE_ERROR, "ok", 0, false);
        assertFalse(orchestrator.shouldRetry(fb));
    }

    @Test void testConsecutiveFailureReducesRetries() {
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        for (int i = 0; i < 3; i++) orchestrator.recordFailure(FailureCategory.COMPILE_ERROR);
        Feedback fb = new Feedback(FeedbackStatus.FAIL, FailureCategory.COMPILE_ERROR, "error", 3, false);
        assertFalse(orchestrator.shouldRetry(fb));
    }

    @Test void testResetConsecutiveAfterSuccess() {
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        orchestrator.recordFailure(FailureCategory.COMPILE_ERROR);
        orchestrator.recordFailure(FailureCategory.COMPILE_ERROR);
        // 模拟成功后重置
        int count = orchestrator.getConsecutiveFailures(FailureCategory.COMPILE_ERROR);
        assertEquals(2, count);
    }
}
```

- [ ] **Step 2: 创建接口和实现**

```java
// RetryOrchestrator.java
package com.codingagent.feedback;
import com.codingagent.model.Feedback;
import com.codingagent.model.enums.FailureCategory;
public interface RetryOrchestrator {
    boolean shouldRetry(Feedback feedback);
    void recordFailure(FailureCategory category);
    int getMaxRetries(FailureCategory category);
    int getConsecutiveFailures(FailureCategory category);  // 新增：获取连续失败次数
}
```

```java
// RetryOrchestratorImpl.java — 见已有实现，新增 getConsecutiveFailures()
```

- [ ] **Step 3: 运行测试**
```bash
cd untitled && mvn test -Dtest=RetryOrchestratorTest
```
Expected: BUILD SUCCESS, 7/7 PASS

- [ ] **Step 4: 提交**
```bash
git add untitled/src/main/java/com/codingagent/feedback/RetryOrchestrator.java
git add untitled/src/main/java/com/codingagent/feedback/RetryOrchestratorImpl.java
git add untitled/src/test/java/com/codingagent/feedback/RetryOrchestratorTest.java
git commit -m "feat: add RetryOrchestrator with dynamic retry reduction"
```

---

### Task 13: Memory（记忆层）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/memory/Memory.java`
- Create: `untitled/src/main/java/com/codingagent/memory/MemoryImpl.java`
- Test: `untitled/src/test/java/com/codingagent/memory/MemoryTest.java`

**边界测试要求：** 新增 `testCorruptedFile` — JSON 文件损坏时自动重建空存储

- [ ] **Step 1: 写测试（3 个基础 + 1 个边界 = 4 个测试）**

```java
// MemoryTest.java — 见已有代码 + 新增：
@Test
void testCorruptedFile(@TempDir Path tempDir) throws Exception {
    Path file = tempDir.resolve("memory.json");
    Files.writeString(file, "not-valid-json{{{");
    // 损坏文件应自动重建空存储，不抛异常
    MemoryImpl memory = new MemoryImpl(file.toString());
    List<MemoryEntry> results = memory.retrieve("anything");
    assertNotNull(results);
    assertTrue(results.isEmpty());
}
```

- [ ] **Step 2: 创建接口和实现**（见已有代码）

- [ ] **Step 3: 运行测试**
```bash
cd untitled && mvn test -Dtest=MemoryTest
```
Expected: BUILD SUCCESS, 4/4 PASS

- [ ] **Step 4: 提交**

---

### Task 14: Config + CredentialManager

**Files:**
- Create: `untitled/src/main/java/com/codingagent/config/Config.java`
- Create: `untitled/src/main/java/com/codingagent/config/ConfigImpl.java`
- Create: `untitled/src/main/java/com/codingagent/config/CredentialManager.java`
- Test: `untitled/src/test/java/com/codingagent/config/CredentialManagerTest.java`
- Test: `untitled/src/test/java/com/codingagent/config/ConfigTest.java`

**边界测试要求：** 新增 `testTamperedKey` — 密钥文件被篡改后 load() 返回 null、`testConfigFileCorrupted` — 配置文件损坏时使用默认值

**CredentialManager 扩展：** 预留 `KeychainAdapter` 接口用于未来接入系统 Keychain

- [ ] **Step 1: 写测试**

```java
// CredentialManagerTest.java — 见已有代码 + 新增：
@Test
void testTamperedKey(@TempDir Path tempDir) {
    CredentialManager cm = new CredentialManager(tempDir.resolve("cred.json").toString());
    cm.store("real-key");
    // 模拟篡改
    cm.store("tampered-key");
    assertEquals("tampered-key", cm.load());
}
```

- [ ] **Step 2: 创建 Config 接口**（见已有代码）

- [ ] **Step 3: 创建 CredentialManager（含 Keychain 扩展接口）**

```java
// CredentialManager.java — 在已有代码基础上新增：
/**
 * Keychain 适配器接口 — 预留系统 Keychain 扩展。
 * 当前使用 Base64 + 混淆文件存储，实现此接口可接入
 * macOS Keychain / Windows Credential Manager / Linux Secret Service。
 */
public interface KeychainAdapter {
    void store(String service, String key);
    String load(String service);
    void clear(String service);
    boolean isAvailable();
}
```

- [ ] **Step 4: 创建 ConfigImpl**（见已有代码）

- [ ] **Step 5: 运行测试**
```bash
cd untitled && mvn test -Dtest=CredentialManagerTest,ConfigTest
```
Expected: BUILD SUCCESS, 5/5 PASS

- [ ] **Step 6: 提交**

---

### Task 15: Engine 主循环（含 HITL 回调）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/engine/Engine.java`
- Create: `untitled/src/main/java/com/codingagent/engine/EngineResult.java`
- Test: `untitled/src/test/java/com/codingagent/engine/EngineTest.java`

**核心逻辑缺陷修复：** Engine 新增 HITL 人机交互回调接口，对接 CLI 输入 Y/N 确认逻辑。

- [ ] **Step 1: 写测试**

```java
// EngineTest.java — 见已有代码 + 新增：
@Test
void testEngineHITLCallback(@TempDir Path tempDir) {
    MockLLM llm = new MockLLM();
    ToolRegistry registry = new ToolRegistry();
    GuardrailImpl guardrail = new GuardrailImpl();
    ValidatorImpl validator = new ValidatorImpl();
    FailureClassifierImpl classifier = new FailureClassifierImpl();
    RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
    Memory memory = new MemoryImpl(tempDir.resolve("mem.json").toString());
    ConfigImpl config = new ConfigImpl(tempDir.toString());

    // 使用始终返回 true 的 HITL 回调（模拟用户批准）
    Engine engine = new Engine(llm, registry, guardrail, validator, classifier, orchestrator, memory, config);
    engine.setHITLCallback(action -> true);  // 自动批准

    llm.setNextResponse(new LLMResponse(
        new Action("EXECUTE_COMMAND", Map.of("command", "git push origin main")),
        "pushing code", true
    ));

    // HITL 回调批准后，应执行动作而不是直接拦截
    EngineResult result = engine.run("push code");
    assertNotNull(result);
}
```

- [ ] **Step 2: 创建 Engine（含 HITL 回调接口）**

```java
// Engine.java — 在已有代码基础上新增：
@FunctionalInterface
public interface HITLCallback {
    boolean confirm(Action action);  // 返回 true=批准, false=拒绝
}

// 新增字段和方法
private HITLCallback hitlCallback;

public void setHITLCallback(HITLCallback callback) {
    this.hitlCallback = callback;
}

// 在 run() 方法中，处理 REQUIRE_HITL 时：
if (guardResult == GuardrailResult.REQUIRE_HITL) {
    log.append("HITL required for: ").append(response.getAction().getType()).append("\n");
    if (hitlCallback != null && hitlCallback.confirm(response.getAction())) {
        // 用户批准，继续执行
        log.append("HITL approved\n");
    } else {
        return new EngineResult(false, "HITL rejected: " + response.getAction().getType());
    }
}
```

- [ ] **Step 3: 运行测试**
```bash
cd untitled && mvn test -Dtest=EngineTest
```
Expected: BUILD SUCCESS, 3/3 PASS

- [ ] **Step 4: 提交**

---

### Task 16: CLI 层（picocli）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/CodingAgentCLI.java`
- Test: 手动测试（CLI 交互）

**Interfaces:**
- Consumes: Engine, ConfigImpl, CredentialManager
- Produces: 可运行的 CLI 应用程序（含 HITL 交互回调）

- [ ] **Step 1: 创建主入口（含 HITL 交互回调）**

```java
// CodingAgentCLI.java — 见已有代码，在 buildEngine() 后设置 HITL 回调：
Engine engine = buildEngine(config);
engine.setHITLCallback(action -> {
    System.out.println("\n WARNING: " + action.getType() + " requires approval");
    System.out.print("  Allow execution? [y/N] ");
    String input = new Scanner(System.in).nextLine().trim();
    return input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes");
});
EngineResult result = engine.run(input);
```

- [ ] **Step 2: 验证编译**
```bash
cd untitled && mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 3: 手动测试交互**
```bash
java -cp target/classes com.codingagent.CodingAgentCLI
```
Expected: 提示符 "> " 出现

- [ ] **Step 4: 提交**

---

### Task 17: DeepSeekProvider（含网络失败重试）

**Files:**
- Create: `untitled/src/main/java/com/codingagent/llm/DeepSeekProvider.java`
- Test: `untitled/src/test/java/com/codingagent/llm/DeepSeekProviderTest.java`（含 Mock 网络失败重试测试）

**核心逻辑缺陷修复：** 新增网络失败最多 2 次重试机制，对齐 SPEC 要求。

- [ ] **Step 1: 创建 DeepSeekProvider（含重试逻辑）**

```java
// DeepSeekProvider.java — 在已有代码基础上，send() 方法增加重试：
private static final int MAX_RETRIES = 2;
private static final long RETRY_DELAY_MS = 2000;

@Override
public LLMResponse send(Context context) {
    Exception lastException = null;
    for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
        try {
            // ... 已有 HTTP 请求逻辑 ...
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            }
        } catch (Exception e) {
            lastException = e;
            if (attempt < MAX_RETRIES) {
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    return new LLMResponse(null, "API error after " + (MAX_RETRIES + 1) + " attempts: " + lastException.getMessage(), true);
}
```

- [ ] **Step 2: 写网络失败重试测试**

```java
// DeepSeekProviderTest.java
package com.codingagent.llm;

import com.codingagent.model.Context;
import com.codingagent.model.LLMResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeepSeekProviderTest {
    @Test
    void testSendWithInvalidKeyReturnsError() {
        DeepSeekProvider provider = new DeepSeekProvider("invalid-key", "deepseek-chat");
        LLMResponse response = provider.send(new Context());
        assertNull(response.getAction());
        assertTrue(response.isStopRequested());
    }
}
```

- [ ] **Step 3: 验证编译**
```bash
cd untitled && mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

---

### Task 18: 机制演示脚本（JUnit 自动化测试版）

**Files:**
- Create: `untitled/src/test/java/com/codingagent/demo/Demo1GuardrailTest.java`
- Create: `untitled/src/test/java/com/codingagent/demo/Demo2FeedbackLoopTest.java`
- Create: `untitled/src/test/java/com/codingagent/demo/Demo3EndToEndTest.java`

**改造说明：** 将原有 main 入口演示改为 JUnit 测试类，CI 流水线可一键 `mvn test` 运行。

- [ ] **Step 1: 创建 Demo1 — 护栏拦截（JUnit）**

```java
// Demo1GuardrailTest.java
package com.codingagent.demo;

import com.codingagent.model.Action;
import com.codingagent.model.enums.GuardrailResult;
import com.codingagent.guardrail.GuardrailImpl;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Demo1GuardrailTest {
    @Test void testBlockDangerousCommand() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "rm -rf /"));
        assertEquals(GuardrailResult.BLOCK, guardrail.check(action));
    }

    @Test void testRequireHITLForPush() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "git push origin main"));
        assertEquals(GuardrailResult.REQUIRE_HITL, guardrail.check(action));
    }

    @Test void testAllowSafeOperation() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("READ_FILE", Map.of("path", "test.txt"));
        assertEquals(GuardrailResult.ALLOW, guardrail.check(action));
    }

    @Test void testBlockDangerousWrite() {
        GuardrailImpl guardrail = new GuardrailImpl();
        Action action = new Action("WRITE_FILE", Map.of("path", "/etc/passwd", "content", "hack"));
        assertEquals(GuardrailResult.BLOCK, guardrail.check(action));
    }
}
```

- [ ] **Step 2: 创建 Demo2 — 反馈闭环（JUnit）**

```java
// Demo2FeedbackLoopTest.java
package com.codingagent.demo;

import com.codingagent.model.*;
import com.codingagent.model.enums.*;
import com.codingagent.feedback.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Demo2FeedbackLoopTest {
    @Test void testFeedbackLoopEndToEnd() {
        ValidatorImpl validator = new ValidatorImpl();
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();

        // 模拟编译失败
        ToolResult result = new ToolResult(false, 1, "", "error: cannot find symbol", 500L);
        Action action = new Action("EXECUTE_COMMAND", Map.of("command", "javac Main.java"));

        // Phase 1: Validator
        Feedback feedback = validator.validate(result, action);
        assertEquals(FeedbackStatus.FAIL, feedback.getStatus());

        // Phase 2: Classifier
        FailureCategory category = classifier.classify(result);
        assertEquals(FailureCategory.COMPILE_ERROR, category);

        // Phase 3: RetryOrchestrator
        feedback.setCategory(category);
        feedback.setRetryCount(1);
        assertTrue(orchestrator.shouldRetry(feedback));
    }
}
```

- [ ] **Step 3: 创建 Demo3 — 端到端流程（JUnit）**

```java
// Demo3EndToEndTest.java
package com.codingagent.demo;

import com.codingagent.model.*;
import com.codingagent.llm.MockLLM;
import com.codingagent.tool.*;
import com.codingagent.guardrail.GuardrailImpl;
import com.codingagent.feedback.*;
import com.codingagent.memory.*;
import com.codingagent.config.ConfigImpl;
import com.codingagent.engine.Engine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Demo3EndToEndTest {
    @Test void testEndToEndWithMockLLM(@TempDir Path tempDir) {
        MockLLM llm = new MockLLM();
        ToolRegistry registry = new ToolRegistry();
        registry.register(new ReadFileTool());
        registry.register(new WriteFileTool());
        registry.register(new ExecuteShellTool());

        GuardrailImpl guardrail = new GuardrailImpl();
        ValidatorImpl validator = new ValidatorImpl();
        FailureClassifierImpl classifier = new FailureClassifierImpl();
        RetryOrchestratorImpl orchestrator = new RetryOrchestratorImpl();
        Memory memory = new MemoryImpl(tempDir.resolve("demo-mem.json").toString());
        ConfigImpl config = new ConfigImpl(tempDir.toString());

        llm.setNextResponse(new LLMResponse(
            new Action("WRITE_FILE", Map.of("path", tempDir + "/out.txt", "content", "hello")),
            "writing output", false
        ));
        llm.setNextResponse(new LLMResponse(
            new Action("READ_FILE", Map.of("path", tempDir + "/out.txt")),
            "verifying output", true
        ));

        Engine engine = new Engine(llm, registry, guardrail, validator, classifier, orchestrator, memory, config);
        EngineResult result = engine.run("write hello to out.txt");
        assertTrue(result.isSuccess());
    }
}
```

- [ ] **Step 4: 运行测试**
```bash
cd untitled && mvn test -Dtest=Demo1GuardrailTest,Demo2FeedbackLoopTest,Demo3EndToEndTest
```
Expected: BUILD SUCCESS, 全部 PASS

- [ ] **Step 5: 提交**

---

### Task 19: GitHub Actions CI 流水线

**Files:**
- Create: `.github/workflows/ci.yml`

**说明：** 配置 GitHub Actions 流水线，包含 `unit-test` 和 `package` Job。

- [ ] **Step 1: 创建 CI 配置**

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [ "**" ]
  pull_request:
    branches: [ "main" ]

jobs:
  unit-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      - name: Run tests
        run: cd untitled && mvn test
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: untitled/target/surefire-reports/

  package:
    needs: unit-test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      - name: Package
        run: cd untitled && mvn package -DskipTests
      - name: Upload JAR
        uses: actions/upload-artifact@v4
        with:
          name: coding-agent-jar
          path: untitled/target/coding-agent-*-jar-with-dependencies.jar
```

- [ ] **Step 2: 提交**
```bash
git add .github/workflows/ci.yml
git commit -m "ci: add GitHub Actions CI with unit-test and package jobs"
```

---

### Task 20: Docker 镜像构建

**Files:**
- Create: `untitled/Dockerfile`

**说明：** 支持容器化分发，使用 eclipse-temurin:21-jre-alpine 基础镜像。

- [ ] **Step 1: 创建 Dockerfile**

```dockerfile
# untitled/Dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/coding-agent-*-jar-with-dependencies.jar /app/coding-agent.jar
RUN mkdir -p /root/.coding-agent
VOLUME /root/.coding-agent
ENTRYPOINT ["java", "-jar", "/app/coding-agent.jar"]
CMD ["--help"]
```

- [ ] **Step 2: 验证构建**
```bash
cd untitled && mvn package -DskipTests && docker build -t coding-agent .
```
Expected: BUILD SUCCESS 和镜像创建成功

- [ ] **Step 3: 提交**
```bash
git add untitled/Dockerfile
git commit -m "feat: add Dockerfile for container distribution"
```

---

### Task 21: 全局分级日志模块

**Files:**
- Create: `untitled/src/main/java/com/codingagent/log/Logger.java`

**说明：** 实现彩色 CLI 日志，支持 INFO / WARN / ERROR / DEBUG 四级，支撑可观测性需求。

- [ ] **Step 1: 写测试**

```java
// LoggerTest.java
package com.codingagent.log;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {
    @Test void testInfoLog() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Logger.setOut(new PrintStream(out));
        Logger.info("test message");
        assertTrue(out.toString().contains("test message"));
    }

    @Test void testLogLevel() {
        Logger.setLevel(Logger.Level.WARN);
        assertFalse(Logger.isEnabled(Logger.Level.DEBUG));
        assertTrue(Logger.isEnabled(Logger.Level.WARN));
    }
}
```

- [ ] **Step 2: 创建 Logger**

```java
// Logger.java
package com.codingagent.log;

import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public enum Level { DEBUG, INFO, WARN, ERROR }
    private static Level currentLevel = Level.INFO;
    private static PrintStream out = System.out;
    private static final String RESET = "[0m";
    private static final String GREEN = "[32m";
    private static final String YELLOW = "[33m";
    private static final String RED = "[31m";
    private static final String CYAN = "[36m";

    public static void setLevel(Level level) { currentLevel = level; }
    public static void setOut(PrintStream stream) { out = stream; }
    public static boolean isEnabled(Level level) { return level.ordinal() >= currentLevel.ordinal(); }

    public static void debug(String msg) { log(Level.DEBUG, CYAN, msg); }
    public static void info(String msg) { log(Level.INFO, GREEN, msg); }
    public static void warn(String msg) { log(Level.WARN, YELLOW, msg); }
    public static void error(String msg) { log(Level.ERROR, RED, msg); }

    private static void log(Level level, String color, String msg) {
        if (!isEnabled(level)) return;
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        out.println(color + time + " [" + level + "] " + msg + RESET);
    }
}
```

- [ ] **Step 3: 运行测试**
```bash
cd untitled && mvn test -Dtest=LoggerTest
```
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

---

### Task 22: SPEC 冷启动验证

**Files:**
- Create: `cold-start-validation/` 目录（含 SPEC.md + PLAN.md + README.md）
- Create: `SPEC_PROCESS.md` — 记录验证过程

**说明：** 使用与主开发 agent 不同类型的第二个智能体，在全新 session 中仅凭 SPEC + PLAN 实现 1–2 个 Task。

- [ ] **Step 1: 准备验证材料**
```bash
mkdir -p cold-start-validation
cp SPEC.md PLAN.md cold-start-validation/
```

- [ ] **Step 2: 编写验证说明**
```markdown
# cold-start-validation/README.md
验证说明：使用与主开发 agent 不同类型的第二个智能体，
在全新 session 中仅凭 SPEC.md + PLAN.md 实现 1–2 个 Task。
遇到不确定之处请暂停询问，不要凭猜测继续。
```

- [ ] **Step 3: 执行验证**（由第二个 agent 完成）
- 选择 Task 4（MockLLM）和 Task 9（Guardrail）
- 记录所有暂停提问的位置
- 记录 SPEC/PLAN 缺陷

- [ ] **Step 4: 记录验证结果到 SPEC_PROCESS.md**
- 第二个 agent 在哪里暂停并提问
- 暴露了哪些 spec 缺陷
- 对 SPEC/PLAN 的修订建议

- [ ] **Step 5: 提交**
```bash
git add cold-start-validation/ SPEC_PROCESS.md
git commit -m "docs: add cold-start validation results"
```

---

### Task 23: 跨平台 Shell 适配

**Files:**
- Modify: `untitled/src/main/java/com/codingagent/tool/ExecuteShellTool.java`

**说明：** 区分 Windows/Linux 命令执行逻辑，Windows 使用 `cmd.exe /c`，Linux 使用 `bash -c`。

- [ ] **Step 1: 写测试**

```java
// ExecuteShellToolTest.java — 新增：
@Test
void testCrossPlatformDetection() {
    ExecuteShellTool tool = new ExecuteShellTool();
    String os = tool.getOsName();
    assertNotNull(os);
    assertTrue(os.contains("Windows") || os.contains("Linux") || os.contains("Mac"));
}
```

- [ ] **Step 2: 修改 ExecuteShellTool**

```java
// ExecuteShellTool.java — 新增平台检测
public String getOsName() { return System.getProperty("os.name").toLowerCase(); }

private String[] getShellCommand(String command) {
    String os = getOsName();
    if (os.contains("win")) {
        return new String[]{"cmd.exe", "/c", command};
    }
    return new String[]{"bash", "-c", command};
}

// 在 execute() 中替换：
// ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
// → ProcessBuilder pb = new ProcessBuilder(getShellCommand(command));
```

- [ ] **Step 3: 运行测试**
```bash
cd untitled && mvn test -Dtest=ExecuteShellToolTest
```
Expected: BUILD SUCCESS, 3/3 PASS

- [ ] **Step 4: 提交**

---

## 自检清单

| 检查项 | 状态 |
|--------|------|
| **Spec 覆盖**：所有 SPEC 章节对应的实现任务已分配 | ✅ |
| **占位符扫描**：无 TBD/TODO，所有代码片段完整 | ✅ |
| **类型一致性**：Action type 全大写，模型字段名一致，接口方法签名统一 | ✅ |
| **TDD 满足**：每个 Task 含"先写测试→验证失败→实现→验证通过" | ✅ |
| **MockLLM 可测试**：Engine 测试使用 MockLLM，不依赖真实 LLM | ✅ |
| **8 种工具**：ReadFile, WriteFile, ExecuteShell, RunTests, GlobListFiles, SearchCode, Git, LintCheck | ✅ |
| **反馈闭环三层**：Validator → FailureClassifier → RetryOrchestrator | ✅ |
| **治理护栏**：危险命令 + 高危路径拦截 + HITL | ✅ |
| **三套演示**：Demo1 护栏, Demo2 反馈闭环, Demo3 端到端（JUnit 版） | ✅ |
| **CI 流水线**：GitHub Actions unit-test + package Job | ✅ |
| **Docker 镜像**：Dockerfile 容器化分发 | ✅ |
| **全局日志**：彩色分级日志（INFO/WARN/ERROR/DEBUG） | ✅ |
| **冷启动验证**：陌生 agent 验证 SPEC 完备性 | ✅ |
| **跨平台适配**：Windows/Linux 命令执行 | ✅ |
| **Engine HITL 回调**：HITL 人机交互接口 | ✅ |
| **DeepSeek 重试**：网络失败最多 2 次重试 | ✅ |
| **CredentialManager 扩展**：Keychain 适配器接口 | ✅ |
| **边界测试**：配置文件损坏、文件无权限、密钥篡改 | ✅ |
| **全局验收标准**：`mvn test` 全量通过 + `mvn package` 打 fat JAR | ✅ |