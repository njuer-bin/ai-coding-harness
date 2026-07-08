# 冷启动验证 — Coding Agent Harness

## 验证要求

这是 AI4SE 期末项目的冷启动验证环节。你是一个**与主开发 agent 不同类型**的第二个智能体。

**规则：**
1. 这是一个**全新 session**，没有之前的对话历史
2. 你的输入材料仅限此目录下的 `SPEC.md`（设计文档）和 `PLAN.md`（实现计划）
3. 请从 `PLAN.md` 中选择 **1–2 个 task** 自主推进实现（推荐 Task 9 Guardrail 或 Task 4 MockLLM）
4. **遇到不确定之处请暂停询问**，不要凭猜测继续

## 项目信息

- 项目类型：Coding Agent Harness（用 Java 构建 AI 编码代理框架）
- 技术栈：Java 21 + Maven（项目在 `untitled/` 目录）
- 当前状态：pom.xml 已配置，目录结构已创建，**尚无 Java 源代码**
- 核心贡献维度：反馈闭环（Feedback Loop）
- 第二维度：治理护栏（Guardrail）
- LLM 默认供应商：DeepSeek

## 完成后请记录

1. 你选择了哪些 task？
2. 你在哪里暂停并提问了？为什么？
3. SPEC/PLAN 中有哪些不清晰的地方？
4. 你的产出与预期差距多大？
5. 你对 SPEC/PLAN 的修订建议

将这些记录到 `SPEC_PROCESS.md` 中。