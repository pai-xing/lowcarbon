-- ====================================
-- 积分商城数据库更新脚本
-- ====================================

-- 1. 修改用户表，添加虚拟商品相关字段
ALTER TABLE tb_user
ADD COLUMN theme_style VARCHAR(20) DEFAULT 'default' COMMENT '主题皮肤样式：default/green/dark',
ADD COLUMN has_virtual_tree TINYINT(1) DEFAULT 0 COMMENT '是否拥有虚拟树：0-无，1-有',
ADD COLUMN vip_badge TINYINT(1) DEFAULT 0 COMMENT '是否拥有VIP标识：0-无，1-有';

-- 2. 创建虚拟商品表
CREATE TABLE IF NOT EXISTS tb_virtual_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    product_name VARCHAR(50) NOT NULL COMMENT '商品名称',
    product_type VARCHAR(20) NOT NULL COMMENT '商品类型：theme/tree/badge',
    description TEXT COMMENT '商品描述',
    price INT NOT NULL COMMENT '积分价格',
    icon_url VARCHAR(255) COMMENT '商品图标',
    status TINYINT(1) DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='虚拟商品表';

-- 3. 创建兑换记录表
CREATE TABLE IF NOT EXISTS tb_exchange_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(50) NOT NULL COMMENT '商品名称',
    price INT NOT NULL COMMENT '消耗积分',
    exchange_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换记录表';

-- 4. 插入初始商品数据
INSERT INTO tb_virtual_product (product_name, product_type, description, price, icon_url, status, sort_order) VALUES
('清新绿主题', 'theme', '为你的个人中心换上清新的绿色主题，让低碳理念融入视觉体验', 100, '/theme-green.png', 1, 1),
('暗夜黑主题', 'theme', '深邃的暗色主题，护眼省电，低碳又时尚', 150, '/theme-dark.png', 1, 2),
('虚拟碳汇树', 'tree', '认养一棵虚拟树，见证你的低碳贡献持续生长', 500, '/tree.png', 1, 3),
('低碳先锋勋章', 'badge', '专属尊贵身份标识，彰显你的低碳贡献与环保决心', 1000, '/badge-vip.png', 1, 4);

-- 5. 添加索引优化查询
ALTER TABLE tb_virtual_product ADD INDEX idx_status_sort (status, sort_order);
