package com.survey.rbac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.survey.common.error.BizException;
import com.survey.common.error.ErrorCode;
import com.survey.rbac.dto.RoleCreateRequest;
import com.survey.rbac.dto.RoleResponse;
import com.survey.rbac.dto.RoleUpdateRequest;

@ExtendWith(MockitoExtension.class)
class SysRoleServiceTest {

    @Mock
    SysRoleMapper mapper;

    @InjectMocks
    SysRoleService service;

    @Test
    void create_success_setsBuiltinFalseAndStatusEnabled() {
        when(mapper.selectCount(any())).thenReturn(0L);
        when(mapper.insert(any(SysRole.class))).thenAnswer(invocation -> {
            SysRole role = invocation.getArgument(0);
            role.setId(10L);
            return 1;
        });

        RoleResponse resp = service.create(new RoleCreateRequest("EDITOR", "编辑", "desc", 5));

        assertEquals(10L, resp.id());
        assertEquals("EDITOR", resp.code());
        assertEquals("编辑", resp.name());
        assertFalse(resp.builtin());
        assertEquals(1, resp.status());

        ArgumentCaptor<SysRole> captor = ArgumentCaptor.forClass(SysRole.class);
        verify(mapper).insert(captor.capture());
        SysRole inserted = captor.getValue();
        assertFalse(inserted.getBuiltin());
        assertEquals(1, inserted.getStatus());
        assertEquals(5, inserted.getSort());
    }

    @Test
    void create_duplicateCode_throws() {
        when(mapper.selectCount(any())).thenReturn(1L);

        BizException ex = assertThrows(BizException.class,
                () -> service.create(new RoleCreateRequest("ADMIN", "管理员", null, 0)));

        assertEquals(ErrorCode.ROLE_CODE_DUPLICATE, ex.getErrorCode());
        verify(mapper, never()).insert(any(SysRole.class));
    }

    @Test
    void update_normalRole_canChangeCode() {
        SysRole existing = normalRole(1L, "OLD");
        when(mapper.selectById(1L)).thenReturn(existing);
        when(mapper.selectCount(any())).thenReturn(0L);
        when(mapper.updateById(any(SysRole.class))).thenReturn(1);

        RoleResponse resp = service.update(new RoleUpdateRequest(1L, "NEW", "新名称", "d", 2));

        assertEquals("NEW", resp.code());
        assertEquals("新名称", resp.name());
        verify(mapper).updateById(any(SysRole.class));
    }

    @Test
    void update_builtin_changeCode_throws() {
        SysRole existing = builtinRole(1L, "SUPER_ADMIN");
        when(mapper.selectById(1L)).thenReturn(existing);

        BizException ex = assertThrows(BizException.class,
                () -> service.update(new RoleUpdateRequest(1L, "OTHER", "超管", null, 0)));

        assertEquals(ErrorCode.ROLE_BUILTIN_CODE_IMMUTABLE, ex.getErrorCode());
        verify(mapper, never()).updateById(any(SysRole.class));
    }

    @Test
    void delete_builtin_throws() {
        when(mapper.selectById(1L)).thenReturn(builtinRole(1L, "SUPER_ADMIN"));

        BizException ex = assertThrows(BizException.class, () -> service.delete(1L));

        assertEquals(ErrorCode.ROLE_BUILTIN_DELETE_FORBIDDEN, ex.getErrorCode());
        verify(mapper, never()).deleteById(any(Long.class));
    }

    @Test
    void disable_builtin_throws() {
        when(mapper.selectById(1L)).thenReturn(builtinRole(1L, "SUPER_ADMIN"));

        BizException ex = assertThrows(BizException.class, () -> service.disable(1L));

        assertEquals(ErrorCode.ROLE_BUILTIN_DISABLE_FORBIDDEN, ex.getErrorCode());
        verify(mapper, never()).updateById(any(SysRole.class));
    }

    @Test
    void enable_alreadyEnabled_isIdempotent() {
        SysRole existing = normalRole(2L, "EDITOR");
        existing.setStatus(1);
        when(mapper.selectById(2L)).thenReturn(existing);

        service.enable(2L);

        verify(mapper, never()).updateById(any(SysRole.class));
    }

    @Test
    void disable_alreadyDisabled_isIdempotent() {
        SysRole existing = normalRole(2L, "EDITOR");
        existing.setStatus(0);
        when(mapper.selectById(2L)).thenReturn(existing);

        service.disable(2L);

        verify(mapper, never()).updateById(any(SysRole.class));
    }

    @Test
    void detail_notFound_throws() {
        when(mapper.selectById(99L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> service.detail(99L));

        assertEquals(ErrorCode.ROLE_NOT_FOUND, ex.getErrorCode());
    }

    private static SysRole normalRole(Long id, String code) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setCode(code);
        role.setName(code);
        role.setBuiltin(false);
        role.setStatus(1);
        role.setSort(0);
        return role;
    }

    private static SysRole builtinRole(Long id, String code) {
        SysRole role = normalRole(id, code);
        role.setBuiltin(true);
        return role;
    }
}
