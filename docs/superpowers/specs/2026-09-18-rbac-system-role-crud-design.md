# RBAC 系统角色 CRUD 设计

日期：2026-09-18  
状态：已确认  
范围：RBAC 第一期 — **角色本体**（不含用户-角色、权限点、鉴权）

## 1. 目标与非目标

### 目标
- 提供系统角色的创建、详情、分页列表、更新、逻辑删除、启用、停用
- 在 `common` 落地通用分页入参/出参，供后续模块复用
- 内置角色（如 `SUPER_ADMIN`）受保护：不可删、不可改 `code`、不可停用
- 接口遵循根 `AGENTS.md` 动作式 API 约定；本期**不做登录鉴权**

### 非目标
- 用户表、用户-角色绑定
- 权限/菜单资源及角色-权限绑定
- Spring Security / Token 校验、接口级授权
- 修改既有 `sys_role` 表结构（沿用已有 DDL）

## 2. 方案选择

采用**经典分层 + 明确业务规则**（Controller / Service / Mapper + 独立 DTO），而非极薄 MP CRUD 或过重领域端口抽象。

理由：与现有 package-by-feature、`ApiResult` / `BizException` 一致；内置角色规则与独立启停接口可集中测试；后续扩权限绑定时结构可沿用。

## 3. 架构与组件

```
com.survey.rbac/
  SysRole.java
  SysRoleMapper.java
  SysRoleService.java
  SysRoleController.java
  dto/
    RoleCreateRequest
    RoleUpdateRequest
    RoleListRequest          # 继承或组合 PageQuery + status + keyword
    RoleIdRequest            # delete / enable / disable 等
    RoleResponse

com.survey.common.api/
  PageQuery                  # page、size
  PageResult<T>              # total、records
```

- Controller：校验 + 调 Service，返回 `ApiResult`
- Service：唯一性、内置保护、启停幂等
- Mapper：MyBatis-Plus；软删用逻辑删除字段/注解

## 4. 数据模型

沿用 `src/main/resources/db/sys_role.sql`：

| 字段 | 说明 |
|------|------|
| id | BIGSERIAL PK |
| code | 业务唯一键（未删除范围内唯一） |
| name | 展示名 |
| description | 描述 |
| builtin | 内置标记；创建接口固定为 false |
| status | 1 启用 / 0 停用 |
| sort | 排序，升序 |
| created_at / updated_at | TIMESTAMPTZ |
| deleted | 逻辑删除 0/1 |

种子：`SUPER_ADMIN`（builtin=true，可重复执行插入）。

## 5. API

路径前缀：`/api/rbac/role`。风格见根 `AGENTS.md`（动作式；简单读 GET，复杂查与写 POST）。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/rbac/role/detail?id=` | 详情 |
| POST | `/api/rbac/role/list` | 分页列表；body：`page`/`size` + `status?` + `keyword?`（匹配 code/name）；默认按 `sort`、`id` 升序 |
| POST | `/api/rbac/role/create` | 创建；`builtin=false`，`status` 默认 1 |
| POST | `/api/rbac/role/update` | 更新；body 含 `id`；可改 name/description/sort；普通角色可改 code；**不可**改 status/builtin |
| POST | `/api/rbac/role/delete` | 逻辑删除；body：`{ id }` |
| POST | `/api/rbac/role/enable` | `status=1`；body：`{ id }` |
| POST | `/api/rbac/role/disable` | `status=0`；body：`{ id }` |

### 分页（common）
- `page` 从 1 起；`size` 默认 20，上限 100
- 出参：`ApiResult.data` = `PageResult{ total, records }`

## 6. 业务规则

1. `code` 在未删除范围内唯一；创建必填；更新时普通角色可改 code，冲突抛业务错误
2. 内置角色：禁删、禁改 code、禁 disable；允许改 name/description/sort；enable 幂等成功
3. 不存在或已删除 → `ROLE_NOT_FOUND`
4. 启停若已是目标状态 → 幂等成功
5. 客户端不可通过 create 指定 `builtin=true`

## 7. 错误码

在 `ErrorCode` B 段按序新增，并更新 `docs/error-codes.md`。无需改 `GlobalExceptionHandler`。

| code | identifier | 含义 | http |
|------|------------|------|------|
| B0001 | ROLE_NOT_FOUND | 角色不存在或已删除 | 404 |
| B0002 | ROLE_CODE_DUPLICATE | 角色编码冲突 | 409 |
| B0003 | ROLE_BUILTIN_CODE_IMMUTABLE | 内置角色禁止修改编码 | 400 |
| B0004 | ROLE_BUILTIN_DELETE_FORBIDDEN | 内置角色禁止删除 | 400 |
| B0005 | ROLE_BUILTIN_DISABLE_FORBIDDEN | 内置角色禁止停用 | 400 |

参数类错误复用 `C0001 PARAM_INVALID`。

## 8. 测试与验收

### 测试
- Service：创建；code 冲突；普通角色改 code；内置禁改 code/禁删/禁停用；启停幂等；删后 detail → NOT_FOUND
- Controller：detail 为 GET；其余动作为 POST；成功/失败 `ApiResult` 与错误码映射抽测

### 验收
- 种子 `SUPER_ADMIN` 可查、可改 name，不可删、不可停用
- 普通角色完成 create → list/detail → update → disable/enable → delete 全流程

## 9. 实现顺序（供 plan 参考）

1. `PageQuery` / `PageResult` + 必要时更新 `common/AGENTS.md`
2. 错误码 B0001–B0005 + 对照表
3. 实体 / Mapper / DDL 纳入迁移或文档化执行方式
4. Service + 单测
5. Controller + DTO + 接口测试
