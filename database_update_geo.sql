-- ========================================
-- 地图足迹模块数据库更新脚本
-- 为 tb_footprint 增加地理位置相关字段
-- ========================================

USE `lc`;

-- 1) 为碳足迹记录表增加经纬度、地址与定位来源字段
ALTER TABLE `tb_footprint`
  ADD COLUMN `latitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '纬度',
  ADD COLUMN `longitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '经度',
  ADD COLUMN `address` VARCHAR(255) DEFAULT NULL COMMENT '地址(可选，人类可读)',
  ADD COLUMN `geo_source` VARCHAR(30) DEFAULT NULL COMMENT '定位来源：manual(手动)、gps(设备)、reverse_geocoding(反向地理编码)';

-- 2) 为经纬度添加复合索引（便于地图范围查询）
ALTER TABLE `tb_footprint`
  ADD INDEX `idx_user_date` (`user_id`, `record_date`),
  ADD INDEX `idx_lat_lng` (`latitude`, `longitude`);

-- ========================================
-- 验证字段是否添加成功
-- ========================================
SELECT 
  COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_footprint'
  AND COLUMN_NAME IN ('latitude','longitude','address','geo_source');

-- ========================================
-- 完成提示
-- ========================================
-- 已为 tb_footprint 表新增位置字段：
-- - latitude(纬度), longitude(经度), address(地址), geo_source(定位来源)
-- 并添加了必要索引以支持按用户+日期与经纬度范围的查询。
