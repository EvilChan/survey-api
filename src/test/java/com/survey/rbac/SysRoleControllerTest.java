package com.survey.rbac;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.survey.common.error.BizException;
import com.survey.common.error.ErrorCode;
import com.survey.common.web.GlobalExceptionHandler;
import com.survey.rbac.dto.RoleCreateRequest;
import com.survey.rbac.dto.RoleResponse;

@WebMvcTest(controllers = SysRoleController.class)
@Import(GlobalExceptionHandler.class)
class SysRoleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SysRoleService roleService;

    /** 避免 @WebMvcTest 继承 SurveyApplication 的 @MapperScan 时创建真实 Mapper */
    @MockitoBean
    SysRoleMapper sysRoleMapper;

    @Test
    void detail_success_returnsRoleFields() throws Exception {
        Instant now = Instant.parse("2026-09-18T08:00:00Z");
        when(roleService.detail(1L)).thenReturn(new RoleResponse(
                1L, "SUPER_ADMIN", "超级管理员", "desc", true, 1, 0, now, now));

        mockMvc.perform(get("/api/rbac/role/detail").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$.data.name").value("超级管理员"));
    }

    @Test
    void create_success() throws Exception {
        Instant now = Instant.parse("2026-09-18T08:00:00Z");
        when(roleService.create(any(RoleCreateRequest.class))).thenReturn(new RoleResponse(
                10L, "EDITOR", "编辑", "d", false, 1, 5, now, now));

        mockMvc.perform(post("/api/rbac/role/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"EDITOR","name":"编辑","description":"d","sort":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.code").value("EDITOR"));

        verify(roleService).create(any(RoleCreateRequest.class));
    }

    @Test
    void disable_builtin_returnsB0005() throws Exception {
        doThrow(new BizException(ErrorCode.ROLE_BUILTIN_DISABLE_FORBIDDEN))
                .when(roleService).disable(eq(1L));

        mockMvc.perform(post("/api/rbac/role/disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("B0005"));
    }

    @Test
    void detail_missingId_returnsC0001() throws Exception {
        mockMvc.perform(get("/api/rbac/role/detail"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C0001"));
    }

    @Test
    void create_blankCode_returnsC0001() throws Exception {
        mockMvc.perform(post("/api/rbac/role/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"","name":"编辑","description":"d","sort":5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C0001"));
    }
}
