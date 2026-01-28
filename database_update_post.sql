USE `lc`;

-- ----------------------------
-- 1. 修改 tb_post 表，添加审核状态字段
-- ----------------------------
ALTER TABLE `tb_post` 
ADD COLUMN `status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核, 1-审核通过, 2-审核不通过' 
AFTER `comments_count`;

-- ----------------------------
-- 2. 创建帖子点赞表 (tb_post_like)
-- ----------------------------
DROP TABLE IF EXISTS `tb_post_like`;
CREATE TABLE `tb_post_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';
