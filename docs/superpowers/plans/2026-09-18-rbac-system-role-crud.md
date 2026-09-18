# RBAC 系统角色 CRUD Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 RBAC 第一期「系统角色本体」CRUD（含分页、启停、内置角色保护），并在 `common` 落地通用分页类型。

**Architecture:** package-by-feature 的 `com.survey.rbac`（Entity / Mapper / Service / Controller + dto）；业务规则集中在 Service；统一 `ApiResult` + `BizException`/`ErrorCode`；动作式 API（详情 GET，其余 POST）。

**Tech Stack:** Java 21、Spring Boot 4.1.1、MyBatis-Plus 3.5.x、PostgreSQL、JUnit 5 + Mockito（Service）、MockMvc（Controller）。

## Global Constraints

- 接口返回 `com.survey.common.api.ApiResult`；业务失败只抛 `BizException` + `ErrorCode`
- API 动作式：`/api/{module}/{resource}/{action}`；简单读 GET，复杂查与写 POST；资源名单数 `role`
- 本期不做登录鉴权；不建用户/权限绑定
- 沿用 `src/main/resources/db/sys_role.sql`，不改表结构
- Git 提交说明用简体中文（类型前缀 + 中文简述）
- TDD：先写失败测试，再写最小实现；每任务结束提交

## File Map

| 路径 | 职责 |
|------|------|
| `src/main/java/com/survey/common/api/PageQuery.java` | 分页入参（page/size 规范化） |
| `src/main/java/com/survey/common/api/PageResult.java` | 分页出参（total/records） |
| `src/main/java/com/survey/common/AGENTS.md` | 注明分页类型 |
| `src/main/java/com/survey/common/error/ErrorCode.java` | 增加 B0001–B0005 |
| `docs/error-codes.md` | 对照表 |
| `src/main/java/com/survey/common/web/GlobalExceptionHandler.java` | 校验异常 → `PARAM_INVALID`（若引入 Bean Validation） |
| `src/main/java/com/survey/rbac/SysRole.java` | 实体 |
| `src/main/java/com/survey/rbac/SysRoleMapper.java` | Mapper |
| `src/main/java/com/survey/rbac/SysRoleService.java` | 业务规则 |
| `src/main/java/com/survey/rbac/SysRoleController.java` | HTTP 入口 |
| `src/main/java/com/survey/rbac/dto/*.java` | 模块 DTO |
| `src/test/java/com/survey/common/api/PageQueryTest.java` | 分页规范化单测 |
| `src/test/java/com/survey/rbac/SysRoleServiceTest.java` | Service 单测 |
| `src/test/java/com/survey/rbac/SysRoleControllerTest.java` | Controller MockMvc 测试 |
| `pom.xml` | 增加 `spring-boot-starter-validation`（若尚未存在） |
| `src/main/resources/db/sys_role.sql` | 已存在；实现前人工/本地执行一次 |

---

### Task 1: 通用分页 PageQuery / PageResult

**Files:**
- Create: `src/main/java/com/survey/common/api/PageQuery.java`
- Create: `src/main/java/com/survey/common/api/PageResult.java`
- Create: `src/test/java/com/survey/common/api/PageQueryTest.java`
- Modify: `src/main/java/com/survey/common/AGENTS.md`

**Interfaces:**
- Consumes: 无
- Produces:
  - `PageQuery` record：`Integer page`, `Integer size`；方法 `int pageOrDefault()`, `int sizeOrDefault()`（page&lt;1→1；size 空或&lt;1→20；size&gt;100→100）
  - `PageResult<T>` record：`long total`, `List<T> records`；静态工厂 `of(long total, List<T> records)`

- [ ] **Step 1: 写失败测试**

```java
package com.survey.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PageQueryTest {

    @Test
    void defaultsWhenNull() {
        PageQuery q = new PageQuery(null, null);
        assertEquals(1, q.pageOrDefault());
        assertEquals(20, q.sizeOrDefault());
    }

    @Test
    void clampsSizeUpperBound() {
        assertEquals(100, new PageQuery(1, 500).sizeOrDefault());
    }

    @Test
    void normalizesInvalidPage() {
        assertEquals(1, new PageQuery(0, 10).pageOrDefault());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./mvnw -q -Dtest=PageQueryTest test`  
Expected: 编译失败（找不到 `PageQuery`）或测试失败

- [ ] **Step 3: 最小实现**

```java
package com.survey.common.api;

public record PageQuery(Integer page, Integer size) {
    public int pageOrDefault() {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    public int sizeOrDefault() {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }
}
```

```java
package com.survey.common.api;

import java.util.List;

public record PageResult<T>(long total, List<T> records) {
    public static <T> PageResult<T> of(long total, List<T> records) {
        return new PageResult<>(total, records == null ? List.of() : records);
    }
}
```

在 `common/AGENTS.md` 的 `api/` 一行改为包含：`PageQuery`、`PageResult`。

- [ ] **Step 4: 运行测试确认通过**

Run: `./mvnw -q -Dtest=PageQueryTest test`  
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/survey/common/api/PageQuery.java \
  src/main/java/com/survey/common/api/PageResult.java \
  src/test/java/com/survey/common/api/PageQueryTest.java \
  src/main/java/com/survey/common/AGENTS.md
git commit -m "$(cat <<'EOF'
feat: 增加通用分页 PageQuery/PageResult

为角色列表及后续模块统一 page/size 与 total/records 约定。
EOF
)"
```

---

### Task 2: 角色错误码 B0001–B0005

**Files:**
- Modify: `src/main/java/com/survey/common/error/ErrorCode.java`
- Modify: `docs/error-codes.md`

**Interfaces:**
- Consumes: 现有 `ErrorCode` 构造
- Produces: `ROLE_NOT_FOUND`、`ROLE_CODE_DUPLICATE`、`ROLE_BUILTIN_CODE_IMMUTABLE`、`ROLE_BUILTIN_DELETE_FORBIDDEN`、`ROLE_BUILTIN_DISABLE_FORBIDDEN`

- [ ] **Step 1: 按 ErrorCode AGENTS 追加枚举（B 段从 B0001 起）**

在 `PARAM_INVALID` 之后增加分区注释与五项：

```java
    // 业务错误 B0001-B9999 — rbac/role
    /** 角色不存在或已删除 */
    ROLE_NOT_FOUND("B0001", "ROLE_NOT_FOUND", "角色不存在或已删除", 404),
    /** 角色编码与未删除记录冲突 */
    ROLE_CODE_DUPLICATE("B0002", "ROLE_CODE_DUPLICATE", "角色编码已存在", 409),
    /** 内置角色禁止修改编码 */
    ROLE_BUILTIN_CODE_IMMUTABLE("B0003", "ROLE_BUILTIN_CODE_IMMUTABLE", "内置角色禁止修改编码", 400),
    /** 内置角色禁止删除 */
    ROLE_BUILTIN_DELETE_FORBIDDEN("B0004", "ROLE_BUILTIN_DELETE_FORBIDDEN", "内置角色禁止删除", 400),
    /** 内置角色禁止停用 */
    ROLE_BUILTIN_DISABLE_FORBIDDEN("B0005", "ROLE_BUILTIN_DISABLE_FORBIDDEN", "内置角色禁止停用", 400),
```

- [ ] **Step 2: 更新 `docs/error-codes.md`**

追加五行：`B0001`…`B0005`，模块 `rbac`，日期 `2026-09-18`。

- [ ] **Step 3: 编译确认**

Run: `./mvnw -q -DskipTests compile`  
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/survey/common/error/ErrorCode.java docs/error-codes.md
git commit -m "$(cat <<'EOF'
feat: 新增角色相关业务错误码 B0001-B0005

覆盖未找到、编码冲突与内置角色保护场景。
EOF
)"
```

---

### Task 3: SysRole 实体与 Mapper

**Files:**
- Create: `src/main/java/com/survey/rbac/SysRole.java`
- Create: `src/main/java/com/survey/rbac/SysRoleMapper.java`

**Interfaces:**
- Consumes: MyBatis-Plus `BaseMapper`；全局 `logic-delete-field: deleted`
- Produces: `SysRole` 字段与表一致；`SysRoleMapper extends BaseMapper<SysRole>`

- [ ] **Step 1: 确认本地已执行 DDL**

Run（本地 psql 或任意客户端）: 执行 `src/main/resources/db/sys_role.sql`  
Expected: 表 `sys_role` 存在，且有 `SUPER_ADMIN` 种子（可重复执行）

- [ ] **Step 2: 实现实体与 Mapper**

```java
package com.survey.rbac;

import java.time.Instant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean builtin;
    private Integer status;
    private Integer sort;
    private Instant createdAt;
    private Instant updatedAt;
    @TableLogic
    private Integer deleted;

    // getters / setters（或 Lombok 若项目后续引入；本期手写 getter/setter）
}
```

```java
package com.survey.rbac;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
```

字段映射依赖 `map-underscore-to-camel-case: true`。时间列用 `Instant`（与项目既有约定一致）。

- [ ] **Step 3: 编译确认**

Run: `./mvnw -q -DskipTests compile`  
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/survey/rbac/SysRole.java src/main/java/com/survey/rbac/SysRoleMapper.java
git commit -m "$(cat <<'EOF'
feat: 增加 sys_role 实体与 Mapper

为角色 CRUD 提供 MyBatis-Plus 持久化入口。
EOF
)"
```

---

### Task 4: SysRoleService（TDD）

**Files:**
- Create: `src/main/java/com/survey/rbac/SysRoleService.java`
- Create: `src/test/java/com/survey/rbac/SysRoleServiceTest.java`
- Create: `src/main/java/com/survey/rbac/dto/RoleCreateRequest.java`
- Create: `src/main/java/com/survey/rbac/dto/RoleUpdateRequest.java`
- Create: `src/main/java/com/survey/rbac/dto/RoleListRequest.java`
- Create: `src/main/java/com/survey/rbac/dto/RoleResponse.java`

**Interfaces:**
- Consumes: `SysRoleMapper`；`PageQuery`/`PageResult`；`ErrorCode` B0001–B0005；`BizException`
- Produces（方法签名固定）:
  - `RoleResponse create(RoleCreateRequest req)`
  - `RoleResponse detail(Long id)`
  - `PageResult<RoleResponse> list(RoleListRequest req)`
  - `RoleResponse update(RoleUpdateRequest req)`
  - `void delete(Long id)`
  - `void enable(Long id)`
  - `void disable(Long id)`

DTO 约定：

```java
// RoleCreateRequest: code, name, description(可选), sort(可选，默认 0)
// RoleUpdateRequest: id, code(可选-普通可改), name, description, sort
// RoleListRequest: 组合 PageQuery 字段 + Integer status + String keyword
// RoleResponse: id, code, name, description, builtin, status, sort, createdAt, updatedAt
```

- [ ] **Step 1: 写失败的 Service 单测（Mockito mock Mapper）**

覆盖至少：
1. `create` 成功：`builtin=false`，`status=1`；`insert` 被调用  
2. `create` code 已存在 → `ROLE_CODE_DUPLICATE`  
3. `update` 普通角色可改 code  
4. `update` 内置改 code → `ROLE_BUILTIN_CODE_IMMUTABLE`  
5. `delete` 内置 → `ROLE_BUILTIN_DELETE_FORBIDDEN`  
6. `disable` 内置 → `ROLE_BUILTIN_DISABLE_FORBIDDEN`  
7. `enable`/`disable` 已是目标状态 → 不抛错（幂等）  
8. `detail` 查不到 → `ROLE_NOT_FOUND`  

示例骨架：

```java
@ExtendWith(MockitoExtension.class)
class SysRoleServiceTest {
    @Mock SysRoleMapper mapper;
    @InjectMocks SysRoleService service;

    @Test
    void create_duplicateCode_throws() {
        when(mapper.selectCount(any())).thenReturn(1L);
        BizException ex = assertThrows(BizException.class,
            () -> service.create(new RoleCreateRequest("ADMIN", "管理员", null, 0)));
        assertEquals(ErrorCode.ROLE_CODE_DUPLICATE, ex.getErrorCode());
    }
    // ... 其余用例
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./mvnw -q -Dtest=SysRoleServiceTest test`  
Expected: 失败（类/方法不存在）

- [ ] **Step 3: 实现 DTO + Service**

关键实现要点：
- code 唯一：`Wrappers.<SysRole>lambdaQuery().eq(SysRole::getCode, code)`，更新时排除自身 id  
- 内置保护按 spec §6  
- `list`：用 MyBatis-Plus `Page`（`new Page<>(page, size)`）+ `like` keyword（code 或 name）+ 可选 status；`orderByAsc(sort, id)`；转 `PageResult`  
- `updatedAt`：更新/启停时设为 `Instant.now()`（若未用自动填充）  
- create 忽略任何「客户端想设 builtin」的可能——DTO 不包含 builtin 字段

- [ ] **Step 4: 运行测试确认通过**

Run: `./mvnw -q -Dtest=SysRoleServiceTest test`  
Expected: BUILD SUCCESS，用例全绿

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/survey/rbac/ src/test/java/com/survey/rbac/SysRoleServiceTest.java
git commit -m "$(cat <<'EOF'
feat: 实现角色 Service 与业务规则

覆盖编码唯一、内置保护与启停幂等，并附带单测。
EOF
)"
```

---

### Task 5: SysRoleController + 校验 + MockMvc

**Files:**
- Create: `src/main/java/com/survey/rbac/SysRoleController.java`
- Create: `src/main/java/com/survey/rbac/dto/RoleIdRequest.java`
- Create: `src/test/java/com/survey/rbac/SysRoleControllerTest.java`
- Modify: `pom.xml`（增加 validation starter，若无）
- Modify: `src/main/java/com/survey/common/web/GlobalExceptionHandler.java`（`MethodArgumentNotValidException` / 缺参 → `PARAM_INVALID`）

**Interfaces:**
- Consumes: `SysRoleService` 全部方法
- Produces: 下列映射（均返回 `ApiResult`）

| 方法 | 路径 |
|------|------|
| GET | `/api/rbac/role/detail?id=` |
| POST | `/api/rbac/role/list` |
| POST | `/api/rbac/role/create` |
| POST | `/api/rbac/role/update` |
| POST | `/api/rbac/role/delete` |
| POST | `/api/rbac/role/enable` |
| POST | `/api/rbac/role/disable` |

- [ ] **Step 1: 写 Controller 测试（`@WebMvcTest` + `@MockitoBean`/`@MockBean` Service）**

至少：
1. GET detail 成功 → `code=0` 且 body 含角色字段  
2. POST create 成功  
3. POST disable 内置时 Service 抛 `ROLE_BUILTIN_DISABLE_FORBIDDEN` → HTTP 400 且业务 code `B0005`  
4. GET detail 缺 id → 400 / `C0001`（若走校验）

- [ ] **Step 2: 运行确认失败**

Run: `./mvnw -q -Dtest=SysRoleControllerTest test`  
Expected: 失败

- [ ] **Step 3: 实现 Controller 与校验处理**

```java
@RestController
@RequestMapping("/api/rbac/role")
public class SysRoleController {
    private final SysRoleService roleService;
    // constructor

    @GetMapping("/detail")
    public ApiResult<RoleResponse> detail(@RequestParam Long id) { ... }

    @PostMapping("/list")
    public ApiResult<PageResult<RoleResponse>> list(@RequestBody RoleListRequest req) { ... }

    @PostMapping("/create")
    public ApiResult<RoleResponse> create(@Valid @RequestBody RoleCreateRequest req) { ... }

    @PostMapping("/update")
    public ApiResult<RoleResponse> update(@Valid @RequestBody RoleUpdateRequest req) { ... }

    @PostMapping("/delete")
    public ApiResult<Void> delete(@Valid @RequestBody RoleIdRequest req) {
        roleService.delete(req.id());
        return ApiResult.ok();
    }

    @PostMapping("/enable")
    public ApiResult<Void> enable(@Valid @RequestBody RoleIdRequest req) { ... }

    @PostMapping("/disable")
    public ApiResult<Void> disable(@Valid @RequestBody RoleIdRequest req) { ... }
}
```

`RoleIdRequest`：`@NotNull Long id`。  
`pom.xml` 增加：

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

`GlobalExceptionHandler` 增加对 `MethodArgumentNotValidException`、`MissingServletRequestParameterException` 的处理，返回 `ApiResult.fail(ErrorCode.PARAM_INVALID, ...)`，HTTP 400。

- [ ] **Step 4: 全量相关测试**

Run: `./mvnw -q -Dtest=PageQueryTest,SysRoleServiceTest,SysRoleControllerTest test`  
Expected: 全部 PASS

- [ ] **Step 5: 提交**

```bash
git add pom.xml \
  src/main/java/com/survey/rbac/SysRoleController.java \
  src/main/java/com/survey/rbac/dto/RoleIdRequest.java \
  src/main/java/com/survey/common/web/GlobalExceptionHandler.java \
  src/test/java/com/survey/rbac/SysRoleControllerTest.java
git commit -m "$(cat <<'EOF'
feat: 暴露角色动作式 API 并完善参数校验处理

提供 detail/list/create/update/delete/enable/disable 接口。
EOF
)"
```

---

### Task 6: 本地验收清单（人工）

**Files:** 无代码变更（除非发现缺陷回修）

- [ ] **Step 1: 启动应用并手工打点**

确认 DB 已执行 `sys_role.sql`，启动 `SurveyApplication`，用 curl/httpie：

1. `GET /api/rbac/role/detail?id=<SUPER_ADMIN的id>` 成功  
2. `POST /api/rbac/role/update` 改 SUPER_ADMIN 的 name 成功；改 code 返回 `B0003`  
3. `POST /api/rbac/role/disable` SUPER_ADMIN 返回 `B0005`  
4. `POST /api/rbac/role/create` 普通角色 → list/detail → disable/enable → delete 全流程成功  

- [ ] **Step 2: 若有缺陷，修测后提交 `fix: ...`；无缺陷则跳过提交**

---

## Spec Coverage Self-Check

| Spec 项 | Task |
|---------|------|
| PageQuery/PageResult | Task 1 |
| ErrorCode B0001–B0005 + 对照表 | Task 2 |
| sys_role 实体/Mapper/DDL | Task 3 |
| Service 规则 + 单测 | Task 4 |
| 动作式 API + Controller 测 | Task 5 |
| SUPER_ADMIN / 普通角色验收 | Task 6 |
| 不做鉴权/权限绑定 | 全任务均不引入 |
| 简单读 GET / 写与复杂查 POST | Task 5 路径表 |

## Placeholder / Consistency Notes

- Service 方法名与 Controller 调用保持一致：`create/detail/list/update/delete/enable/disable`
- 启停与删除入参统一为 `RoleIdRequest.id`（detail 用 query `id`）
- 提交文案一律中文简述
