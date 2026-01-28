# 问题修复总结

## 问题描述
1. 地图足迹界面点击后显示空白界面
2. 点击低碳活动显示"请求失败，加载活动列表失败"（500错误）

## 问题原因分析

### 1. 地图足迹界面空白问题
**根本原因**：路由配置存在重复定义
- 在 `frontend/src/router/index.js` 中，`MapFootprint` 路由被定义了两次：
  - 第一次：`path: 'map'`
  - 第二次：`path: 'map-footprint'`
- 导航菜单使用的路径是 `/map`，但由于路由冲突，页面无法正常加载

### 2. 活动列表加载失败问题
**可能原因**：
1. 数据库表 `tb_activity` 和 `tb_activity_join` 未创建
2. 后端服务未启动或数据库连接失败

## 已实施的修复

### 1. 修复路由配置
**文件**：`frontend/src/router/index.js`
- 删除了重复的路由定义
- 保留唯一路径：`path: 'map-footprint'`
- 确保路由名称 `MapFootprint` 唯一

### 2. 修复导航菜单
**文件**：`frontend/src/layouts/MainLayout.vue`
- 将地图足迹菜单项的路径从 `/map` 改为 `/map-footprint`
- 更新 `activeMenu` 计算逻辑，将 `/map-footprint` 检查放在 `/footprint` 之前，避免路径前缀匹配问题

## 待执行的步骤

### 1. 数据库初始化（必须）
执行以下SQL脚本创建活动相关表：

```bash
# 连接到MySQL数据库后执行
mysql -u root -p lowcarbon < database_update_activity.sql
```

或在MySQL客户端中执行：
```sql
-- 执行 database_update_activity.sql 文件中的所有SQL语句
```

### 2. 重启前端开发服务器
```bash
cd frontend
npm run dev
```

### 3. 确认后端服务正在运行
```bash
# 在项目根目录执行
mvn spring-boot:run
```

## 验证步骤

1. **验证地图足迹功能**：
   - 登录系统
   - 点击导航栏的"地图足迹"
   - 确认页面能正常显示，不再是空白

2. **验证活动列表功能**：
   - 点击导航栏的"低碳活动"
   - 确认能看到活动列表
   - 确认没有500错误

## 修复后的路由结构

```
/map-footprint → MapFootprint 组件（地图足迹页面）
/activity → ActivityList 组件（活动列表页面）
/activity/:id → ActivityDetail 组件（活动详情页面）
```

## 注意事项

1. 如果活动列表仍然加载失败，请检查：
   - 数据库连接配置（application.yml）
   - 数据库表是否已创建
   - 后端控制台是否有错误日志

2. 如果地图足迹页面仍然空白，请：
   - 清除浏览器缓存
   - 检查浏览器控制台的错误信息
   - 确认前端服务已重启

## 相关文件清单

### 已修改的文件：
1. `frontend/src/router/index.js` - 修复路由重复问题
2. `frontend/src/layouts/MainLayout.vue` - 修复导航路径和菜单激活逻辑

### 需要执行的脚本：
1. `database_update_activity.sql` - 创建活动相关数据表

### 相关代码文件（无需修改）：
1. `frontend/src/views/MapFootprint.vue` - 地图足迹页面
2. `frontend/src/views/ActivityList.vue` - 活动列表页面
3. `src/main/java/com/lowcarbon/controller/ActivityController.java` - 活动控制器
4. `src/main/java/com/lowcarbon/service/impl/ActivityServiceImpl.java` - 活动服务实现
