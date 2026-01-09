-- ========================================
-- 文章互动功能数据库更新脚本
-- 包含：点赞、评论、收藏功能
-- ========================================

-- 1. 创建文章点赞表
CREATE TABLE IF NOT EXISTS `tb_article_like` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` BIGINT(20) NOT NULL COMMENT '文章ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`) COMMENT '一个用户对一篇文章只能点赞一次',
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞表';

-- 2. 创建文章评论表
CREATE TABLE IF NOT EXISTS `tb_article_comment` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` BIGINT(20) NOT NULL COMMENT '文章ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '评论用户ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';

-- 3. 创建文章收藏表
CREATE TABLE IF NOT EXISTS `tb_article_favorite` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` BIGINT(20) NOT NULL COMMENT '文章ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`) COMMENT '一个用户对一篇文章只能收藏一次',
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章收藏表';

-- 4. 为文章表添加计数字段
ALTER TABLE `tb_article` 
ADD COLUMN `likes_count` INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER `views`,
ADD COLUMN `comments_count` INT(11) NOT NULL DEFAULT 0 COMMENT '评论数' AFTER `likes_count`,
ADD COLUMN `favorites_count` INT(11) NOT NULL DEFAULT 0 COMMENT '收藏数' AFTER `comments_count`;

-- 5. 为计数字段添加索引（可选，用于排序查询）
ALTER TABLE `tb_article`
ADD INDEX `idx_likes_count` (`likes_count`),
ADD INDEX `idx_comments_count` (`comments_count`),
ADD INDEX `idx_favorites_count` (`favorites_count`);

-- ========================================
-- 执行完成后的验证查询
-- ========================================

-- 验证表是否创建成功
SELECT 
  TABLE_NAME, 
  TABLE_COMMENT 
FROM 
  information_schema.TABLES 
WHERE 
  TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('tb_article_like', 'tb_article_comment', 'tb_article_favorite');

-- 验证文章表字段是否添加成功
SELECT 
  COLUMN_NAME,
  COLUMN_TYPE,
  COLUMN_DEFAULT,
  COLUMN_COMMENT
FROM 
  information_schema.COLUMNS
WHERE 
  TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'tb_article'
  AND COLUMN_NAME IN ('likes_count', 'comments_count', 'favorites_count');

-- ========================================
-- 完成提示
-- ========================================
-- 数据库更新脚本执行完成！
-- 已创建3个新表：
-- 1. tb_article_like (文章点赞表)
-- 2. tb_article_comment (文章评论表)
-- 3. tb_article_favorite (文章收藏表)
-- 
-- 已为tb_article表添加3个计数字段：
-- 1. likes_count (点赞数)
-- 2. comments_count (评论数)
-- 3. favorites_count (收藏数)
