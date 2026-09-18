package com.survey.rbac;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.survey.common.api.PageQuery;
import com.survey.common.api.PageResult;
import com.survey.common.error.BizException;
import com.survey.common.error.ErrorCode;
import com.survey.rbac.dto.RoleCreateRequest;
import com.survey.rbac.dto.RoleListRequest;
import com.survey.rbac.dto.RoleResponse;
import com.survey.rbac.dto.RoleUpdateRequest;

@Service
public class SysRoleService {

    private final SysRoleMapper mapper;

    public SysRoleService(SysRoleMapper mapper) {
        this.mapper = mapper;
    }

    public RoleResponse create(RoleCreateRequest req) {
        assertCodeUnique(req.code(), null);

        Instant now = Instant.now();
        SysRole role = new SysRole();
        role.setCode(req.code());
        role.setName(req.name());
        role.setDescription(req.description());
        role.setSort(req.sort() != null ? req.sort() : 0);
        role.setBuiltin(false);
        role.setStatus(1);
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        mapper.insert(role);
        return toResponse(role);
    }

    public RoleResponse detail(Long id) {
        return toResponse(requireRole(id));
    }

    public PageResult<RoleResponse> list(RoleListRequest req) {
        PageQuery pageQuery = new PageQuery(req.page(), req.size());
        Page<SysRole> page = new Page<>(pageQuery.pageOrDefault(), pageQuery.sizeOrDefault());

        LambdaQueryWrapper<SysRole> qw = Wrappers.lambdaQuery();
        if (req.status() != null) {
            qw.eq(SysRole::getStatus, req.status());
        }
        if (StringUtils.hasText(req.keyword())) {
            String keyword = req.keyword().trim();
            qw.and(w -> w.like(SysRole::getCode, keyword).or().like(SysRole::getName, keyword));
        }
        qw.orderByAsc(SysRole::getSort).orderByAsc(SysRole::getId);

        Page<SysRole> result = mapper.selectPage(page, qw);
        List<RoleResponse> records = result.getRecords().stream().map(this::toResponse).toList();
        return PageResult.of(result.getTotal(), records);
    }

    public RoleResponse update(RoleUpdateRequest req) {
        SysRole role = requireRole(req.id());

        if (StringUtils.hasText(req.code()) && !req.code().equals(role.getCode())) {
            if (Boolean.TRUE.equals(role.getBuiltin())) {
                throw new BizException(ErrorCode.ROLE_BUILTIN_CODE_IMMUTABLE);
            }
            assertCodeUnique(req.code(), role.getId());
            role.setCode(req.code());
        }

        if (req.name() != null) {
            role.setName(req.name());
        }
        if (req.description() != null) {
            role.setDescription(req.description());
        }
        if (req.sort() != null) {
            role.setSort(req.sort());
        }
        role.setUpdatedAt(Instant.now());
        mapper.updateById(role);
        return toResponse(role);
    }

    public void delete(Long id) {
        SysRole role = requireRole(id);
        if (Boolean.TRUE.equals(role.getBuiltin())) {
            throw new BizException(ErrorCode.ROLE_BUILTIN_DELETE_FORBIDDEN);
        }
        mapper.deleteById(id);
    }

    public void enable(Long id) {
        setStatus(id, 1, false);
    }

    public void disable(Long id) {
        setStatus(id, 0, true);
    }

    private void setStatus(Long id, int targetStatus, boolean forbidBuiltin) {
        SysRole role = requireRole(id);
        if (forbidBuiltin && Boolean.TRUE.equals(role.getBuiltin())) {
            throw new BizException(ErrorCode.ROLE_BUILTIN_DISABLE_FORBIDDEN);
        }
        if (role.getStatus() != null && role.getStatus() == targetStatus) {
            return;
        }
        role.setStatus(targetStatus);
        role.setUpdatedAt(Instant.now());
        mapper.updateById(role);
    }

    private SysRole requireRole(Long id) {
        SysRole role = mapper.selectById(id);
        if (role == null) {
            throw new BizException(ErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    private void assertCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<SysRole> qw = Wrappers.<SysRole>lambdaQuery().eq(SysRole::getCode, code);
        if (excludeId != null) {
            qw.ne(SysRole::getId, excludeId);
        }
        Long count = mapper.selectCount(qw);
        if (count != null && count > 0) {
            throw new BizException(ErrorCode.ROLE_CODE_DUPLICATE);
        }
    }

    private RoleResponse toResponse(SysRole role) {
        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getBuiltin(),
                role.getStatus(),
                role.getSort(),
                role.getCreatedAt(),
                role.getUpdatedAt());
    }
}
