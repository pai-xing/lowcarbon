-- 低碳活动功能数据库更新脚本
-- 核心思路：活动就是"带时间范围的排行榜"，复用现有的碳足迹数据

-- 1. 创建活动表
CREATE TABLE IF NOT EXISTS tb_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    title VARCHAR(100) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动描述',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
    target_reduction DECIMAL(10,2) DEFAULT 0 COMMENT '目标减排量(kg)，0表示无目标',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低碳活动表';

-- 2. 创建活动参与记录表
CREATE TABLE IF NOT EXISTS tb_activity_join (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '参加时间',
    UNIQUE KEY uk_user_activity (user_id, activity_id) COMMENT '用户只能参加一次同一活动',
    KEY idx_activity_id (activity_id) COMMENT '活动ID索引',
    KEY idx_user_id (user_id) COMMENT '用户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动参与记录表';

-- 3. 插入示例活动数据
INSERT INTO tb_activity (title, description, start_time, end_time, status, target_reduction) VALUES
('新春减碳挑战', '在春节期间，让我们一起践行低碳生活，看谁减排最多！', '2026-01-28 00:00:00', '2026-02-05 23:59:59', 1, 100.0),
('绿色出行月', '一个月的绿色出行挑战，骑行、步行、公交都算数！', '2026-02-01 00:00:00', '2026-02-28 23:59:59', 0, 200.0),
('节能达人赛', '看谁能在一周内通过节约用电、用水等行为减排最多', '2026-01-20 00:00:00', '2026-01-27 23:59:59', 2, 50.0);

-- 注意：活动排行榜数据通过以下SQL实时计算，不需要额外存储
-- SELECT 
--   u.id, u.username, u.nickname, u.avatar,
--   COALESCE(SUM(f.reduction_amount), 0) as total_reduction,
--   COALESCE(SUM(f.points_earned), 0) as total_points
-- FROM tb_user u
-- LEFT JOIN tb_footprint f ON u.id = f.user_id 
--   AND f.record_time BETWEEN :startTime AND :endTime
-- WHERE u.id IN (SELECT user_id FROM tb_activity_join WHERE activity_id = :activityId)
-- GROUP BY u.id
-- ORDER BY total_reduction DESC;
