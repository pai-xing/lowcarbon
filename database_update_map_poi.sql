-- 地图预设点（POI - Point of Interest）管理表
CREATE TABLE IF NOT EXISTS `tb_map_poi` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '地点名称',
  `latitude` decimal(10, 7) NOT NULL COMMENT '纬度',
  `longitude` decimal(10, 7) NOT NULL COMMENT '经度',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `poi_type` varchar(50) DEFAULT 'custom' COMMENT 'POI类型：custom-自定义, library-图书馆, canteen-食堂, gym-体育馆等',
  `description` varchar(500) DEFAULT NULL COMMENT '描述信息',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标标识',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_poi_type` (`poi_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地图预设点管理表';

-- 插入示例数据
INSERT INTO `tb_map_poi` (`name`, `latitude`, `longitude`, `address`, `poi_type`, `description`) VALUES
('图书馆', 39.909300, 116.397400, '北京市东城区天安门广场', 'library', '学校图书馆'),
('第一食堂', 39.908500, 116.398200, '北京市东城区', 'canteen', '学生第一食堂'),
('体育馆', 39.910100, 116.396800, '北京市东城区', 'gym', '综合体育馆');
