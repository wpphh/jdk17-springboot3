-- 初始数据
-- 注意: SB3 中 spring.sql.init.mode 需要显式设为 always (SB2 默认是 always)

-- 角色
INSERT INTO roles (name, description, created_at, updated_at) VALUES ('ADMIN', '管理员', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO roles (name, description, created_at, updated_at) VALUES ('USER', '普通用户', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 默认管理员用户 (密码: admin123, BCrypt 加密)
-- 密码由 BCryptPasswordEncoder.encode("admin123") 生成
INSERT INTO users (username, email, password, status, created_at, updated_at)
VALUES ('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 分配管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ADMIN';
