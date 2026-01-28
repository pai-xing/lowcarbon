-- 更新用户角色字段：将大写改为小写
-- 执行时间：2026-01-25
-- 说明：修复角色常量大小写不一致问题

USE `lc`;

-- 将所有用户角色从大写改为小写
UPDATE `tb_user` SET `role` = 'user' WHERE `role` = 'USER';
UPDATE `tb_user` SET `role` = 'admin' WHERE `role` = 'ADMIN';

-- 验证更新结果
SELECT id, username, role, status FROM `tb_user`;
