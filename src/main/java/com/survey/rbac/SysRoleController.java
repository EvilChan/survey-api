package com.survey.rbac;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.survey.common.api.ApiResult;
import com.survey.common.api.PageResult;
import com.survey.rbac.dto.RoleCreateRequest;
import com.survey.rbac.dto.RoleIdRequest;
import com.survey.rbac.dto.RoleListRequest;
import com.survey.rbac.dto.RoleResponse;
import com.survey.rbac.dto.RoleUpdateRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rbac/role")
public class SysRoleController {

    private final SysRoleService roleService;

    public SysRoleController(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/detail")
    public ApiResult<RoleResponse> detail(@RequestParam Long id) {
        return ApiResult.ok(roleService.detail(id));
    }

    @PostMapping("/list")
    public ApiResult<PageResult<RoleResponse>> list(@RequestBody RoleListRequest req) {
        return ApiResult.ok(roleService.list(req));
    }

    @PostMapping("/create")
    public ApiResult<RoleResponse> create(@Valid @RequestBody RoleCreateRequest req) {
        return ApiResult.ok(roleService.create(req));
    }

    @PostMapping("/update")
    public ApiResult<RoleResponse> update(@Valid @RequestBody RoleUpdateRequest req) {
        return ApiResult.ok(roleService.update(req));
    }

    @PostMapping("/delete")
    public ApiResult<Void> delete(@Valid @RequestBody RoleIdRequest req) {
        roleService.delete(req.id());
        return ApiResult.ok();
    }

    @PostMapping("/enable")
    public ApiResult<Void> enable(@Valid @RequestBody RoleIdRequest req) {
        roleService.enable(req.id());
        return ApiResult.ok();
    }

    @PostMapping("/disable")
    public ApiResult<Void> disable(@Valid @RequestBody RoleIdRequest req) {
        roleService.disable(req.id());
        return ApiResult.ok();
    }
}
