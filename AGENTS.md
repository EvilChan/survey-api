# Survey Platform — Agent 指南

Java 21 / Spring Boot / MyBatis-Plus / PostgreSQL。按 **package-by-feature** 组织（`auth`、`rbac`、`common`…）。

## 全局约定
- 接口统一返回 `com.survey.common.api.ApiResult`
- 业务失败抛 `com.survey.common.error.BizException`，错误码只用 `ErrorCode` 枚举
- `common` 只放跨模块复用能力；业务实体与仅单模块使用的 DTO 放各业务包

## 规范入口（细则勿重复抄写到本文件）
- Common 分包与用法：[`src/main/java/com/survey/common/AGENTS.md`](src/main/java/com/survey/common/AGENTS.md)
- ErrorCode 新增流程：[`src/main/java/com/survey/common/error/AGENTS.md`](src/main/java/com/survey/common/error/AGENTS.md)
- 错误码对照表：[`docs/error-codes.md`](docs/error-codes.md)

## Superpowers 工作流
- 开发新功能或修问题、在写/提交 spec 或 plan 之前：先从主分支创建并切换到新分支（`feature/...` 或 `fix/...`），再进入 brainstorming / writing-plans
- 已在目标功能分支上可跳过

## 文档
- 设计/计划：`docs/superpowers/specs/`、`docs/superpowers/plans/`
