# SPEC_PROCESS.md — 冷启动验证记录

> 验证日期：2026-07-08
> 验证 Agent：Claude Code（Fable 5），与主开发 agent 不同类型的第二个智能体
> 输入材料：仅 SPEC.md + PLAN.md

---

## 1. 选择的 Task

| 任务 | 来源 | 说明 |
|------|------|------|
| **Task 2 (部分)** | PLAN.md 第 237–512 行 | 核心模型与枚举 — 3 个枚举 + 7 个模型类 + ModelTest（16 个测试） |
| **Task 4** | PLAN.md 第 631–737 行 | LLMProvider 接口 + MockLLM 实现 + MockLLMTest（3 个测试） |
| **Task 9** | PLAN.md 第 1301–1447 行 | Guardrail 接口 + GuardrailImpl + GuardrailTest（13 个测试） |

共 **32 个单元测试**，全部通过。

### 为何选择这些 Task

- README.md 明确推荐 Task 4 (MockLLM) 和 Task 9 (Guardrail)
- 两者都位于依赖图的前端（仅依赖 Task 2 模型层），无需先实现 Engine 或其他组件
- 分别覆盖了 LLM 抽象层和治理层，体现两个不同维度

---

## 2. 暂停并提问的地方

### 2.1 工作区无源码，是否需要补充模型层

**位置：** 开始实施前

**原因：** 当前工作区中没有 `untitled/` 目录，也完全没有 Java 源码。Task 4 和 Task 9 都依赖 `Action`、`LLMResponse`、`GuardrailResult` 等模型类，而这些类在 git 中也不存在。

**选择：** 先创建 Task 2 的模型层再实现 Task 4 + Task 9。

---

## 3. SPEC/PLAN 中的不清晰之处

### 3.1 项目目录状态与实际不符

- PLAN.md 第 27 行（文件结构树）显示 `untitled/` 为项目根目录
- README.md 第 16 行称"项目在 `untitled/` 目录"
- 实际：`untitled/` 目录不存在于工作区中
- `pom.xml` 虽然在 git commit 中，但 `git checkout` 也无法恢复（index 为空）
- 通过 `git show HEAD:untitled/pom.xml > untitled/pom.xml` 手工提取

### 3.2 前置任务已完成状态不清晰

- PLAN.md 的 Task 1 有 checkbox，但无任何标记表示已完成
- 第一个 commit 信息 "chore: set up Maven project with dependencies" 似乎对应 Task 1
- 但目录结构（Step 2）和验证编译（Step 3）并未执行
- **建议：** PLAN 应在 Task 的 checkbox 旁标注完成状态，或改用 `- [x]` 标记已完成的步骤

### 3.3 Guardrail 的路径匹配边界不明确

- SPEC.md §3.4 说"同步拦截高危文件写入路径（如写入 `/etc/`、系统关键路径）"
- GuardrailImpl 用 `path.startsWith(dangerousPath)` 检查
- 但 `path` 可能是绝对路径（`/etc/passwd`）、相对路径（`../../etc/passwd`）或纯文件名
- **建议：** 明确是否需要路径规范化（`Path.normalize()`），以及相对路径穿越的检测策略

### 3.4 MockLLM 的线程安全性未提及

- MockLLM 内部使用 `LinkedList`（非线程安全）
- 如果 Engine 在多线程场景中使用，可能出现竞态
- **建议：** 标注 MockLLM 为"非线程安全"，或改用 `ConcurrentLinkedQueue`

---

## 4. 产出与预期差距

| 维度 | 预期 | 实际 | 差距 |
|------|------|------|------|
| 测试数量 | Task 4 有 3 个测试、Task 9 有 5 个测试（PLAN 中） | Task 4: 3 个，Task 9: 13 个 | **超出预期** — 额外补充了边界测试（空路径、安全写入、Mkfs 命令、deploy 敏感命令等） |
| 编译验证 | `mvn compile` 成功 | ✅ `mvn test` 全部 32 个测试通过 | 一致 |
| 代码覆盖率 | 核心机制有对应单元测试 | Model + MockLLM + Guardrail 全覆盖 | 符合预期 |
| TDD 流程 | 红 → 绿 → 重构 | 每阶段都先运行测试确认红，再写代码到绿 | 一致 |
| **未实现** | 依赖 Task 4/9 的上层组件（Engine、CLI） | 未实现 | 符合预期（不在选择的 1–2 个 task 范围内） |

---

## 5. 对 SPEC/PLAN 的修订建议

### 5.1 SPEC 修订建议

| 位置 | 原文 | 建议 |
|------|------|------|
| §3.4 治理层 | "同步拦截高危文件写入路径（如写入 /etc/、系统关键路径）" | 补充路径规范化要求："路径应先通过 `Path.normalize()` 规范化后再做匹配检测，防止 `../../etc/shadow` 绕过" |
| §5.3 外部依赖 | 未列测试框架版本 | 补充 JUnit 5 和 Mockito 的具体版本号（与 pom.xml 保持一致） |
| §6.1 ToolResult | `errorLines: List<String>` 提炼自 stderr | 建议增加字段描述："从 stderr 中通过正则提取的关键错误行，用于精确反馈回灌" |

### 5.2 PLAN 修订建议

| 位置 | 原文 | 建议 |
|------|------|------|
| Task 1 | `- [ ]` 未标记完成状态 | 已提交的步骤应标记为 `- [x]`，或提供 task 完成状态总表 |
| Task 2 | ModelTest 有 11 个测试方法 | 建议补充 default constructor 的测试（已在本验证中补充） |
| Task 9 | GuardrailTest 有 5 个测试 | 建议补充边界测试案例：空路径 ALLOW、安全路径 ALLOW、相对路径穿越 |
| 全局 | 所有 task 使用 checkbox | 建议维护一张完成状态总表，标注每个 Task 的 % 完成度和依赖是否满足 |

### 5.3 代码层面发现

- **ToolResult 的 `errorLines` 字段**：SPEC 和 PLAN 都提到了这个字段，但在 Task 2 的模型代码中没有显式 setter/getter。已在本次实现中补全。
- **Guardrail 的路径安全**：当前实现使用 `String.startsWith()`，如果传入 `path="/var/data"` 不会被拦截（不匹配 `/etc/` 等前缀）。这符合 SPEC 要求，但建议在文档中明确哪些路径被视为"安全"。

---

## 6. 验证总结

```
SPEC_PROCESS 验证结论：
├── 选择的 task：Task 2（模型层）+ Task 4（MockLLM）+ Task 9（Guardrail）
├── 测试总数：32（全部通过）
├── 中断提问：1 次（工作区无源码，是否补模型层）
├── 文档缺陷：4 类（目录状态不符、完成状态不清、路径边界模糊、线程安全未提）
├── 产出质量：超出预期（额外补充了 8 个边界测试用例）
└── 整体评价：SPEC 和 PLAN 基本清晰，细节上可通过本次验证结果完善
```