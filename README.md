# 低碳生活系统(lowcarbon-system)

低碳生活系统是一个集“碳足迹计算与记录、统计可视化、低碳行为打卡、社区互动、科普文章、活动报名和商城兑换”等功能于一体的全栈平台。系统支持游客浏览、注册用户交互以及管理员后台管理三种角色。

- 技术栈：
  - 后端：Java(Spring Boot)、MyBatis、MySQL、Knife4j(接口文档)、JWT
  - 前端：Vue3、Vite、Element Plus、ECharts
- 仓库地址：origin: https://github.com/pai-xing/lowcarbon.git

本文档包含项目概述、功能状态、目录结构、快速开始、接口说明、页面说明、开发优先级、数据库与部署指南、常见问题与贡献规范。

---

## 目录

- 项目状态概览
- 项目结构
- 快速开始
- 已完成功能
- 校验与幂等设计
- 打卡与统计/日历
- 接口说明
- 前端页面说明
- 开发优先级与完成顺序
- 数据库初始化与升级脚本
- 开发与部署指南
- 维护与常见问题
- 贡献与开发规范
- 版权与许可

---

## 项目状态概览

### ✅ 已完成的模块功能

#### 1. 核心基础功能
- 用户认证系统：注册、登录、JWT Token认证
- 用户角色管理：用户(user)和管理员(admin)角色权限
- 导航布局系统：响应式布局和路由守卫

#### 2. 碳足迹系统 ✅
- 碳足迹计算器：基于出行、用电、饮食等行为计算碳排放
- 个人碳足迹记录：历史记录查看、编辑、删除
- 统计可视化：减排总量、积分累计、趋势图表、分类统计
- 数据校验增强：前后端完整校验机制

#### 3. 打卡系统 ✅
- 标准打卡功能：预设“次”类行为一键打卡
- 打卡统计：连续打卡天数、累计打卡数、月度达成率
- 打卡日历：月度打卡记录可视化
- 幂等校验：同用户/同行为/同日重复打卡限制

#### 4. 管理员后台基础功能 ✅
- 用户管理界面：用户列表查看、启用/禁用、分页搜索
- 文章管理基础：科普文章发布、编辑、删除
- 权限控制：管理员权限校验与路由守卫

#### 5. 地图足迹(MapFootprint)基础 ✅
- 地图可视化页面：MapFootprint.vue
- 地理数据更新脚本：database_update_geo.sql

### 🔥 急缺功能（优先开发）

#### 1. 社区动态功能（紧急）
- [ ] 社区帖子发布页面（对应 tb_post 表）
- [ ] 社区帖子列表展示（带点赞评论）
- [x] 社区评论回复功能（已完成）
- [x] 帖子审核机制后台（已完成）

#### 2. 管理员后台功能完善（紧急）
- [x] 管理员社区内容审核界面（已完成）
- [ ] 数据统计可视化仪表盘
- [x] 帖子批量操作功能（已完成）

#### 3. 积分与成就系统（核心完善）
- [ ] 成就勋章详情页面展示
- [ ] 积分排行榜功能
- [ ] 勋章解锁通知与展示

#### 4. 缺失的功能页面
- [ ] 社区动态页面（游客浏览）
- [x] 低碳活动页面（挑战参与、报名）（已完成）

#### 5. 互动功能增强
- [ ] 帖子点赞评论计数显示
- [ ] 社区内容审核状态管理

---

## 一、项目结构

- 后端(根目录 `src/main/java/com/lowcarbon/...`)
  - `controller/`：接口层
    - `FootprintController.java`：碳足迹记录、统计、删除更新；标准打卡、打卡统计与日历接口
    - `PostController.java`、`ArticleController.java`、`UserController.java` 等
  - `service/`：业务层
    - `FootprintService.java`、`FootprintServiceImpl.java`：碳足迹核心逻辑与校验；标准打卡、统计与日历实现
    - 规划：`AchievementService`(勋章授予)、`LeaderboardService`(排行榜)、`RuleCenterService`(规则中心)
  - `mapper/`：持久层
    - `FootprintMapper.java`：分页与统计查询；打卡幂等校验、打卡日历与统计查询
  - `entity/`：实体模型
    - `Footprint.java`、`User.java`、`Post.java`、`Comment.java` 等
  - `dto/vo/`：入参与返回模型
    - `FootprintCreateDTO.java`、`FootprintVO.java`、`ActivityVO.java` 等
- 前端(`frontend/`)
  - `src/views/Footprint.vue`：计算器与历史记录、统计卡片、趋势与分类图
  - `src/views/Checkin.vue`：标准打卡页、打卡统计与日历
  - `src/views/MapFootprint.vue`：地图足迹可视化
  - `src/views/ActivityList.vue` / `ActivityDetail.vue`：活动模块页面框架
  - `src/views/Shop.vue`：兑换商城页面框架
  - `src/api/*.js`：`footprint.js`、`checkin.js`、`activity.js`、`post.js` 等
  - 路由：`src/router/index.js` 已配置 `/footprint`、`/checkin` 等
  - 布局：`src/layouts/MainLayout.vue` 顶部导航接入“碳足迹”“打卡”等入口
- 文档与脚本
  - 根目录 `README.md`(本文档)
  - `BUGFIX_SUMMARY.md`：近期修复摘要
  - 前端文档：`frontend/README.md`、`frontend/QUICKSTART.md`、`frontend/TROUBLESHOOTING.md`、`frontend/INSTALL_NODEJS.md`
  - 数据库：`database.sql` 初始结构；`database_update_*.sql` 增量升级脚本

---

## 二、快速开始

### 后端

- 环境要求：
  - JDK 17+（或见 `pom.xml` 中 `maven.compiler.target`）
  - Maven 3.8+
  - MySQL(或兼容数据库)，表如 `tb_footprint` 已初始化
- 启动与编译：
  - 编译：`mvn clean compile -DskipTests`
  - 启动：运行 `LowCarbonApplication`（Spring Boot）
- 配置：
  - 编辑 `src/main/resources/application.yml` 配置数据库与JWT等
- 接口文档(Knife4j)：
  - 默认访问：`http://localhost:8080/doc.html`（或 `http://localhost:8080/swagger-ui/index.html`，视配置而定）

### 前端

- 环境要求：
  - Node.js 16+（参考 `frontend/package.json`）
- 安装依赖：
  - `cd frontend && npm install`
- 开发模式：
  - `npm run dev`，默认 `http://localhost:3000`，端口占用将自动切换(如 3001)
- 构建：
  - `npm run build`（产物位于 `frontend/dist/`）

### 访问入口

- 顶部导航“碳足迹”：`/footprint`
- 顶部导航“打卡”：`/checkin`（需登录）
- 地图足迹：`/map-footprint`（如已在路由中配置）

---

## 三、已完成功能

### 1. 碳足迹计算与记录系统

- 行为类型配置
  - 后端：`GET /footprint/behavior-types`
  - 来源：`FootprintServiceImpl.getBehaviorTypes()`（类型、单位、说明、系数）
- 创建记录
  - 后端：`POST /footprint`
  - 前端：`Footprint.vue` 表单提交；计算减排与积分，写入 `tb_footprint` 并更新用户累计
- **位置信息自动获取**（已修复）
  - 支持HTML5 Geolocation API获取用户GPS坐标
  - 可选配置高德地图Key实现反向地理编码（自动填充地址）
  - 位置来源标识：manual(手动)、gps(设备GPS)、reverse_geocoding(反向地理编码)
  - 数据流修复：经纬度以字符串形式存储和显示，提交时转换为数值类型
  - 支持在创建记录和编辑记录时获取位置
- 列表与删除
  - 后端：`GET /footprint/list`(分页/日期范围)、`DELETE /footprint/{id}`
  - 前端：历史记录页支持筛选、分页、删除联动统计刷新
- 统计汇总
  - 后端：`GET /footprint/statistics`（最近30天：总减排/总积分/记录次数）
  - 前端：统计卡片展示
- 趋势与分类统计图
  - 后端：`GET /footprint/statistics/daily`、`GET /footprint/statistics/by-behavior`
  - 前端：ECharts 折线/柱状图；`v-show` 容器保留，`watch + nextTick` 初始化与自适应，`onUnmounted` 释放实例
- 记录编辑
  - 后端：`PUT /footprint/{id}`（按差值修正用户累计，保护不为负）
  - 前端：编辑对话框，保存后刷新列表、统计与图表

### 2. 校验增强

- 前端表单校验：
  - 单位“次”：数据值必须为 ≥1 的整数
  - 其他单位（km/度/吨等）：数据值必须 ≥0.01
- 后端校验：
  - `FootprintCreateDTO.dataValue`：`@DecimalMin("0.01")`
  - 服务层单位/类型校验：非法类型拒绝；“次”要求整数且 ≥1；其他单位 ≥0.01

### 3. 标准打卡与统计/日历

- 接口
  - 标准打卡：`POST /footprint/checkin`（单位为“次”的行为，一天仅一次）
  - 打卡日历：`GET /footprint/checkin/calendar?month=YYYY-MM`
  - 打卡统计：`GET /footprint/checkin/stats`（`streak`、`totalCheckins`、`monthlyRate`）
- 服务层实现（`FootprintServiceImpl`）
  - `checkin`：单位校验、幂等校验；复用创建流程，`dataValue=1`
  - `getCheckinCalendar`：返回当月每日打卡次数与行为类型列表(`GROUP_CONCAT`)
  - `getCheckinStats`：计算连续天数、累计次数、本月达成率
- 数据层（`FootprintMapper`）
  - `existsCheckin`、`existsAnyOnDate`、`getCheckinCalendar`、`getTotalCheckins`、`getMonthlyCheckedDays` 等
- 前端（`frontend/src/views/Checkin.vue`）
  - 今日打卡按钮区：预设“次”类行为（`VEGETARIAN`/`REDUCE_WASTE`/`RECYCLE`/`REUSE_BAG`），同日相同行为禁用
  - 打卡统计卡片：展示 `streak`/`totalCheckins`/`monthlyRate`
  - 打卡日历：月份选择器、每日次数与行为类型标签，含加载态与错误提示

---

## 四、后端接口说明

- `GET /footprint/behavior-types`
  - 获取行为类型配置（名称/单位/说明/系数）
- `POST /footprint`
  - 创建足迹记录；请求体 `FootprintCreateDTO`：`behaviorType`、`behaviorName`、`dataValue`、`recordDate`、`remark`、`latitude`、`longitude`、`address`、`geoSource`
- `GET /footprint/list`
  - 查询分页列表：`pageNum`、`pageSize`、`startDate`、`endDate`
- `GET /footprint/statistics`
  - 获取统计汇总：默认最近30天；返回 `totalReduction`、`totalPoints`、`totalRecords`
- `GET /footprint/statistics/daily`
  - 获取每日统计：返回每日减排/积分列表
- `GET /footprint/statistics/by-behavior`
  - 获取按行为类型统计：总减排、次数
- `PUT /footprint/{id}`
  - 更新记录（重新计算并按差值修正用户累计）
- `DELETE /footprint/{id}`
  - 删除记录（扣减用户累计）
- 打卡相关
  - `POST /footprint/checkin`：标准打卡（单位为“次”的行为，一天仅一次）
  - `GET /footprint/checkin/calendar?month=YYYY-MM`：打卡日历（每日次数与行为类型）
  - `GET /footprint/checkin/stats`：打卡统计（连续天数/累计次数/本月达成率）
- 地图足迹
  - `GET /footprint/map/points`：按日期范围获取用户地图足迹点位列表（含经纬度、地址与来源），用于 MapFootprint 页面渲染

---

## 五、前端页面说明

- `Footprint.vue`
  - 计算器表单：行为类型、名称、数据值、日期与备注；支持“获取当前位置(GPS)”按钮自动填充经纬度与地址（需浏览器定位授权，地址可在配置高德地图Key后通过反向地理编码填充）
  - 历史记录：分页/日期筛选、编辑与删除；显示“地址”和“坐标(latitude, longitude)”列
  - 统计与图表：总览卡片、每日趋势折线图、行为统计柱状图
- `Checkin.vue`
  - 今日打卡：预设“次”类行为一键打卡，幂等提示与按钮禁用
  - 打卡统计：连续打卡天数、累计打卡数、本月达成率
  - 打卡日历：月份选择器、每日次数与行为类型标签展示
- `MapFootprint.vue`
  - 结合地理数据展示用户足迹地图（依赖 `database_update_geo.sql` 提供地理位数据）
  - 适用于按城市/区域聚合展示足迹分布、热力图等（具体图层实现可按需扩展）
- 路由入口：
  - 在 `frontend/src/router/index.js` 中配置进入“碳足迹”“打卡”“地图足迹”等视图
- 导航入口：
  - 在 `frontend/src/layouts/MainLayout.vue` 顶部菜单中接入上述入口（登录态控制）

---

## 六、开发优先级与推荐完成顺序

### 🔥 第一阶段：紧急补充（1-2周）
1. 社区动态功能
   - 帖子发布页面（`PublishPost.vue`）
   - 帖子列表展示（`Community.vue`）
   - 点赞评论功能实现
2. 管理员后台完善
   - 帖子审核机制（`PostManage.vue`）
   - 内容审核界面

### 📍 第二阶段：核心完善（2-3周）
3. 积分成就系统
   - 勋章展示与管理
   - 积分排行榜
   - 勋章解锁机制
4. 社区审核机制
   - 举报处理
   - 内容状态管理

### 🔧 第三阶段：功能增强（1-2周）
5. 数据统计仪表盘
   - 平台数据可视化、活跃度分析
6. 低碳活动模块
   - 主题活动创建、用户参与机制

### 完成状态追踪
- 已实现的实体与API基础 ✅
  - 数据库表结构：帖子(`tb_post`)、帖子点赞(`tb_post_like`)、评论(`tb_comment`)实体已创建
  - 后端基础结构：`PostController`、`PostService`、`PostMapper` 等基础文件已创建
  - 前端页面框架：`Community.vue`、`PublishPost.vue`、`PostManage.vue` 等已创建
- 待实现的核心交互功能 🚧
  - 帖子发布与展示的 API 对接
  - 点赞评论业务逻辑
  - 审核工作流逻辑

---

## 七、数据库初始化与升级脚本

- 初始结构：`database.sql`
- 增量升级脚本：
  - 文章/互动：`database_update_post.sql`、`database_update_interaction.sql`
  - 角色/权限：`database_update_role.sql`
  - 商城/活动：`database_update_shop.sql`、`database_update_activity.sql`
  - 地理信息：`database_update_geo.sql`
- 使用建议：
  - 在干净数据库上先执行 `database.sql`
  - 按需叠加对应的 `database_update_*.sql` 升级脚本（注意备份与事务）

---

## 八、开发与部署指南

### 后端本地开发
- 运行 `LowCarbonApplication`，本地默认端口 `8080`
- 接口文档：`http://localhost:8080/doc.html`（Knife4j）

### 前端本地开发
- `cd frontend && npm install && npm run dev`
- 访问：`http://localhost:3000`（端口占用自动调整）

### 生产部署示例

- 后端打包与运行：
  - `mvn clean package -DskipTests`
  - 运行：`java -jar target/lowcarbon-system-*.jar --spring.profiles.active=prod`
- 前端构建与部署：
  - `cd frontend && npm run build`
  - 将 `frontend/dist/` 部署到静态服务(Nginx/Apache)
- Nginx 反向代理示例：
  - 前端：`location / { root /var/www/lowcarbon/dist; try_files $uri $uri/ /index.html; }`
  - 后端：`location /api/ { proxy_pass http://127.0.0.1:8080/; }`
- 环境变量与配置：
  - 在 `application.yml` 中配置数据库/Redis/JWT等
  - 前端通过 `.env` 系列文件配置 API 基地址（按需）

---

## 九、维护与常见问题

- 前端 Vite 端口占用自动切换（如从 3000 到 3001）
- 图表为空的排查：
  - 确认最近30天有记录或创建一条测试记录
  - 确认容器使用 `v-show` 保持在 DOM 中，数据到达后初始化与 `resize`
- 打卡提示：
  - 同用户/同行为/同日仅一次；重复提示"今日已完成该行为打卡"
  - 月份选择器格式 `YYYY-MM`，月历展示每日次数与行为类型
- 编译报红：
  - 执行 `mvn clean compile`
  - 检查数据库结构与应用配置
- 位置自动填充问题：
  - 确保浏览器已授权定位权限
  - 点击"获取当前位置(GPS)"按钮后，经纬度应自动填充到输入框
  - 若地址未自动填充，可配置高德地图Key启用反向地理编码功能
- 更多前端常见问题：
  - 参见 `frontend/TROUBLESHOOTING.md`
- 修复记录：
  - 参见 `BUGFIX_SUMMARY.md`

---

## 代码检测与一致性校验

- 路由与导航一致性
  - 依据 BUGFIX_SUMMARY.md，`frontend/src/router/index.js` 中已移除重复的 `MapFootprint` 路由定义，保留唯一路径：`path: 'map-footprint'`，名称 `name: 'MapFootprint'` 唯一。
  - `frontend/src/layouts/MainLayout.vue` 菜单项已更新为 `index="/map-footprint"`；并调整 `activeMenu` 计算顺序，确保不被 `/footprint` 前缀误匹配，导航高亮正确。
- 接口文档与接口存在性
  - 已存在 `src/main/java/com/lowcarbon/config/Knife4jConfig.java`，本地接口文档默认地址：`http://localhost:8080/doc.html`（如未能访问，请确认该配置在激活的 `profile` 下启用）。
  - 打卡相关接口在后端控制器中实现，README 已列出：`POST /footprint/checkin`、`GET /footprint/checkin/calendar`、`GET /footprint/checkin/stats`。建议通过 Knife4j 文档页面进行联调验证。
- 前端路由与守卫
  - 使用 `createRouter(createWebHistory)` 与 `useUserStore` 进行登录态路由守卫；`MapFootprint`、`Checkin` 等页面均设置 `meta: { requiresAuth: true }`，请在游客态下避免访问受限路由。
- 地图足迹页面与地理数据
  - `frontend/src/views/MapFootprint.vue` 已存在基础布局与样式（`.map-footprint` 容器等）；若需地图图层与热力图，请结合 `database_update_geo.sql` 提供的地理位数据进行后续扩展。
- 碳足迹位置自动填充功能
  - 已修复：`frontend/src/views/Footprint.vue` 中GPS位置获取后自动填充经纬度到输入框
  - 实现方式：经纬度字段使用字符串类型存储（`latitude: ''`, `longitude: ''`），输入框使用 `v-model`（不带.number修饰符），type为"text"
  - 提交时转换：在提交前通过 `parseFloat()` 将字符串转换为数值类型
  - 强制更新机制：使用 `locationUpdateKey` 和 `editLocationUpdateKey` 配合 `:key` 属性强制Vue重新渲染输入框组件
  - 支持场景：创建新记录和编辑现有记录时均可使用"获取当前位置(GPS)"功能
- 建议的本地一致性校验步骤
  - 后端：`mvn clean compile -DskipTests`（确认编译通过）→ 运行 `LowCarbonApplication` → 访问 `http://localhost:8080/doc.html`。
  - 前端：`cd frontend && npm install && npm run dev` → 登录后访问 `/map-footprint`、`/checkin` → 验证导航高亮与路由跳转。
  - 位置功能测试：访问 `/footprint` → 点击"获取当前位置(GPS)"按钮 → 授权浏览器定位 → 验证经纬度自动填充到输入框。
- 文档交叉引用
  - 变更与修复摘要：`BUGFIX_SUMMARY.md`
  - 前端说明与问题排查：`frontend/README.md`、`frontend/QUICKSTART.md`、`frontend/TROUBLESHOOTING.md`

## 十、贡献与开发规范

- 分支建议：`feat/*`(新功能)、`fix/*`(修复)、`docs/*`(文档)
- 提交信息：动词前缀 + 模块标识
  - 示例：`feat(footprint): add edit dialog`、`feat(checkin): add calendar view`
- 代码风格：
  - 后端：遵循 Java/Spring 规范，方法命名清晰，事务与幂等完善
  - 前端：Vue3 + Element Plus，组件划分合理，校验与提示友好
- 安全与隐私：
  - 接口需要登录态；使用 `AuthInterceptor` 与 `JwtUtil` 解析用户ID
  - 前端请求需在请求头携带 `Authorization: Bearer <token>`；后端通过 `JwtUtil` 从Token中提取用户ID

---

## 十一、版权与许可

- 本项目仅用于毕业设计与学习参考。若需商业使用或二次开发请自行评估风险并遵循相关法律法规。
