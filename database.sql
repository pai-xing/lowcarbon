SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 创建并使用数据库 lc
-- ----------------------------
DROP DATABASE IF EXISTS `lc`;
CREATE DATABASE `lc` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `lc`;

-- ----------------------------
-- 2. 表结构：用户表 (tb_user)
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(加密)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `bio` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
  `points` INT DEFAULT 0 COMMENT '当前积分',
  `total_reduction` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '累计碳减排总量',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：USER-普通用户, ADMIN-管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用, 1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 3. 表结构：科普文章表 (tb_article)
-- ----------------------------
DROP TABLE IF EXISTS `tb_article`;
CREATE TABLE `tb_article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
  `cover_img` VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
  `content` LONGTEXT COMMENT '文章内容(富文本)',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类(如：低碳技巧, 政策解读)',
  `views` INT DEFAULT 0 COMMENT '浏览量',
  `author_id` BIGINT DEFAULT NULL COMMENT '发布人ID',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0-否, 1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科普文章表';

-- ----------------------------
-- 4. 表结构：碳足迹记录表 (tb_footprint)
-- ----------------------------
DROP TABLE IF EXISTS `tb_footprint`;
CREATE TABLE `tb_footprint` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `behavior_type` VARCHAR(50) NOT NULL COMMENT '行为类型(如：BUS-公交, SUBWAY-地铁, WALK-步行, SAVE_ELEC-节电)',
  `behavior_name` VARCHAR(50) DEFAULT NULL COMMENT '行为中文名称(冗余字段，方便展示)',
  `coefficient` DECIMAL(10, 4) NOT NULL COMMENT '碳排放计算系数(kg/unit)',
  `data_value` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '行为数值(如：公里数、度数)',
  `reduction_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '产生的碳减排量',
  `points_earned` INT DEFAULT 0 COMMENT '该行为获得的积分',
  `record_date` DATE DEFAULT NULL COMMENT '行为发生日期',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='碳足迹记录表';

-- ----------------------------
-- 5. 表结构：社区动态表 (tb_post)
-- ----------------------------
DROP TABLE IF EXISTS `tb_post`;
CREATE TABLE `tb_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '动态ID',
  `user_id` BIGINT NOT NULL COMMENT '发布用户ID',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '动态文字内容',
  `images` TEXT COMMENT '图片地址列表(JSON格式存储，如 ["url1","url2"])',
  `likes_count` INT DEFAULT 0 COMMENT '点赞数',
  `comments_count` INT DEFAULT 0 COMMENT '评论数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区动态表';

-- ----------------------------
-- 6. 表结构：社区评论表 (tb_comment)
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment`;
CREATE TABLE `tb_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` BIGINT NOT NULL COMMENT '关联的动态ID',
  `user_id` BIGINT NOT NULL COMMENT '评论者ID',
  `target_user_id` BIGINT DEFAULT NULL COMMENT '被回复者ID(如果是回复评论)',
  `content` VARCHAR(255) NOT NULL COMMENT '评论内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区评论表';

-- ----------------------------
-- 7. 表结构：积分兑换/成就记录表 (tb_achievement)
-- ----------------------------
DROP TABLE IF EXISTS `tb_achievement`;
CREATE TABLE `tb_achievement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` TINYINT NOT NULL COMMENT '类型：1-获得勋章, 2-积分兑换',
  `title` VARCHAR(50) NOT NULL COMMENT '标题/勋章名称/商品名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `points_change` INT DEFAULT 0 COMMENT '积分变化(正数为获得勋章奖励，负数为兑换消耗)',
  `icon_url` VARCHAR(255) DEFAULT NULL COMMENT '勋章图标URL',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '达成/兑换时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分兑换/成就记录表';

SET FOREIGN_KEY_CHECKS = 1;

