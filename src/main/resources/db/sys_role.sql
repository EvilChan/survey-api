-- sys_role: 系统角色（RBAC 第一刀：角色本体）
-- PostgreSQL

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    description VARCHAR(255),
    builtin     BOOLEAN      NOT NULL DEFAULT FALSE,
    status      SMALLINT     NOT NULL DEFAULT 1,
    sort        INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted     SMALLINT     NOT NULL DEFAULT 0
);

COMMENT ON TABLE  sys_role             IS '系统角色';
COMMENT ON COLUMN sys_role.code        IS '角色编码，业务唯一键';
COMMENT ON COLUMN sys_role.name        IS '展示名称';
COMMENT ON COLUMN sys_role.description IS '描述';
COMMENT ON COLUMN sys_role.builtin     IS '是否内置角色（内置不可删、不可改 code）';
COMMENT ON COLUMN sys_role.status      IS '状态：1 启用 / 0 停用';
COMMENT ON COLUMN sys_role.sort        IS '排序，升序';
COMMENT ON COLUMN sys_role.deleted     IS '逻辑删除：0 未删 / 1 已删';

-- 未删除范围内 code 唯一，软删后可重建同名编码
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code_active
    ON sys_role (code)
    WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_sys_role_status
    ON sys_role (status)
    WHERE deleted = 0;

-- 种子：超级管理员（可重复执行）
INSERT INTO sys_role (code, name, description, builtin, status, sort)
SELECT 'SUPER_ADMIN', '超级管理员', '系统内置超级管理员', TRUE, 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE code = 'SUPER_ADMIN' AND deleted = 0
);
