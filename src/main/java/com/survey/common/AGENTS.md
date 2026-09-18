# com.survey.common

跨模块复用的 API 壳、错误模型与 Web 横切；**禁止**放入业务实体或单模块逻辑。

## 分包约定
- `api/`：`ApiResult`、`FieldErrorDetail`、`PageQuery`、`PageResult`
- `error/`：`ErrorCode`、`BizException`（细则见 [`error/AGENTS.md`](error/AGENTS.md)）
- `web/`：`GlobalExceptionHandler`、`TraceIdFilter`
- `mybatis/`：`MybatisPlusConfig`（分页拦截器等 MyBatis-Plus 全局配置）

## 使用要点
- Controller 成功返回 `ApiResult.ok(...)` / `ApiResult.ok()`
- 业务失败抛 `BizException`，由 `GlobalExceptionHandler` 转成 `ApiResult.fail(...)`
- 新增通用数据结构放 `api/`；仅一处使用的 DTO 放业务模块
