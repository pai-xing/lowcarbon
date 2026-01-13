# 低碳生活系统(lowcarbon-system)

低碳生活系统包含前后端两部分，提供碳足迹计算与记录、统计与可视化、行为管理、用户积分与成就(勋章)以及低碳行为打卡与日历等功能。当前已完成碳足迹主流程、趋势图/行为统计图、记录编辑与前后端校验增强，并已完成标准打卡、打卡统计与打卡日历前后端接入。下一阶段将实现位置加成与勋章联动、排行榜与规则中心配置化。

## 一、项目结构

- 后端(根目录 `src/main/java/com/lowcarbon/...`)
  - `controller/`：对外接口层
    - `FootprintController.java`：碳足迹记录、统计与删除、更新等接口；标准打卡、打卡统计与打卡日历接口
  - `service/`：业务逻辑层
    - `FootprintService.java`、`FootprintServiceImpl.java`：碳足迹核心逻辑与校验；标准打卡、打卡统计与打卡日历实现
    - 计划新增：`AchievementService`(勋章评估与授予)、`LeaderboardService`(排行榜)、`RuleCenterService`(规则中心)
  - `mapper/`：数据持久层
    - `FootprintMapper.java`：分页与统计查询，标准打卡幂等校验、打卡日历与打卡统计相关查询
  - `entity/`：实体模型
    - `Footprint.java`、`User.java` 等
  - `dto/vo/`：入参与返回模型
    - `FootprintCreateDTO.java`、`FootprintVO.java` 等
- 前端(`frontend/`)
  - `src/views/Footprint.vue`：碳足迹计算器与历史记录、统计卡片、趋势图与行为统计图
  - `src/views/Checkin.vue`：标准打卡页(预设“次”类行为)、打卡统计卡片、打卡日历组件
  - `src/api/footprint.js`：足迹相关 API
  - `src/api/checkin.js`：打卡相关 API
  - 路由：`src/router/index.js` 已配置 `/footprint` 与 `/checkin`
  - 布局：`src/layouts/MainLayout.vue` 顶部导航已接入“碳足迹”与“打卡”入口

## 二、快速开始

### 后端

- 环境要求：
  - JDK 17+ (或项目 `pom.xml` 要求的版本)
  - Maven 3.8+
  - MySQL/对应数据库，表 `tb_footprint` 等已初始化
- 启动与编译：
  - 编译：`mvn clean compile -DskipTests`
  - 启动：运行 `LowCarbonApplication` (Spring Boot)
- 配置：
  - 查看 `src/main/resources/application.yml`，配置数据库连接等

### 前端

- 环境要求：
  - Node.js 16+ (或项目 `frontend/package.json` 要求的版本)
- 安装依赖：
  - `cd frontend && npm install`
- 开发模式：
  - `npm run dev`，默认运行于 `http://localhost:3000`，若端口占用自动改为其他端口（如 3001）
- 构建：
  - `npm run build`
- 访问入口：
  - 顶部导航“碳足迹”：`/footprint`
  - 顶部导航“打卡”：`/checkin`（需登录）

## 三、已完成功能

### 1. 碳足迹计算与记录系统

- 行为类型配置
  - 后端：`GET /footprint/behavior-types`
  - 来源：`FootprintServiceImpl.getBehaviorTypes()`，包含类型、单位、说明与系数
- 创建记录
  - 后端：`POST /footprint`
  - 前端：`Footprint.vue` 的表单提交，计算减排量与积分，写入 `tb_footprint` 并更新用户累计
- 列表与删除
  - 后端：`GET /footprint/list`(分页/日期范围)、`DELETE /footprint/{id}`
  - 前端：历史记录页支持日期筛选、分页、删除联动统计刷新
- 统计汇总
  - 后端：`GET /footprint/statistics`(默认最近30天，总减排/总积分/记录次数)
  - 前端：统计卡片展示
- 趋势图与分类统计图
  - 后端：`GET /footprint/statistics/daily`、`GET /footprint/statistics/by-behavior`
  - 前端：接入 ECharts，显示每日减排/积分折线图与行为类型总减排/次数柱状图
  - 渲染修复：使用 `v-show` 保持容器存在、`watch + nextTick` 初始化与自适应、`onUnmounted` 释放实例
- 记录编辑
  - 后端：`PUT /footprint/{id}`
  - 服务层：按新数据重新计算系数/减排/积分，并按差值修正用户累计(不为负保护)
  - 前端：列表中新增“编辑”对话框，保存后刷新列表、统计与图表

### 2. 校验增强

- 前端表单校验(创建/编辑)：
  - 单位“次”：数据值必须为 ≥1 的整数
  - 其他单位（km/度/吨等）：数据值必须 ≥0.01
- 后端校验：
  - `FootprintCreateDTO.dataValue` 加 `@DecimalMin("0.01")`
  - 服务层单位/类型校验：非法类型直接拒绝；“次”要求整数且 ≥1；其他单位 ≥0.01

### 3. 标准打卡与统计/日历（新增）

- 接口
  - 标准打卡：`POST /footprint/checkin`（单位为“次”的行为，一天仅一次，幂等保障）
  - 打卡日历（按月）：`GET /footprint/checkin/calendar?month=YYYY-MM`（返回每日次数与行为类型列表）
  - 打卡统计：`GET /footprint/checkin/stats`（返回 `streak`(连续打卡天数)、`totalCheckins`(累计打卡数)、`monthlyRate`(本月达成率)）
- 服务层实现（`FootprintServiceImpl`）
  - `checkin`：校验行为单位为“次”、同日幂等；复用创建流程计算减排与积分；`dataValue=1`
  - `getCheckinCalendar`：返回当月每日打卡次数与行为类型列表(使用 `GROUP_CONCAT`)
  - `getCheckinStats`：计算连续打卡天数、累计打卡数与本月达成率
- 数据层（`FootprintMapper`）
  - `existsCheckin`、`existsAnyOnDate`、`getCheckinCalendar`、`getTotalCheckins`、`getMonthlyCheckedDays` 等
- 前端页面（`frontend/src/views/Checkin.vue`）
  - 今日打卡按钮区：预设“次”类行为（`VEGETARIAN`/`REDUCE_WASTE`/`RECYCLE`/`REUSE_BAG`），同日相同行为禁用
  - 打卡统计卡片：展示 `streak`/`totalCheckins`/`monthlyRate`，支持刷新
  - 打卡日历：月份选择器、每日次数与行为类型标签展示、加载态与错误提示
- 前端路由与导航
  - 路由：`/checkin` 已在 `frontend/src/router/index.js` 配置，需登录态
  - 导航：`frontend/src/layouts/MainLayout.vue` 顶部菜单已添加“打卡”入口与正确高亮

## 四、后端接口说明

- `GET /footprint/behavior-types`
  - 获取行为类型配置（名称/单位/说明/系数）
- `POST /footprint`
  - 创建足迹记录；请求体 `FootprintCreateDTO`：`behaviorType`、`behaviorName`、`dataValue`、`recordDate`、`remark`
- `GET /footprint/list`
  - 查询分页列表：`pageNum`、`pageSize`、`startDate`、`endDate`
- `GET /footprint/statistics`
  - 获取统计汇总：默认最近30天，返回 `totalReduction`、`totalPoints`、`totalRecords`
- `GET /footprint/statistics/daily`
  - 获取每日统计：返回每日减排/积分列表
- `GET /footprint/statistics/by-behavior`
  - 获取按行为类型统计：总减排、次数
- `PUT /footprint/{id}`
  - 更新记录（重新计算并按差值修正用户累计）
- `DELETE /footprint/{id}`
  - 删除记录（扣减用户累计）
- 打卡相关（新增）
  - `POST /footprint/checkin`：标准打卡（单位为“次”的行为，一天仅一次）
  - `GET /footprint/checkin/calendar?month=YYYY-MM`：打卡日历（每日次数与行为类型列表）
  - `GET /footprint/checkin/stats`：打卡统计（连续天数/累计次数/本月达成率）

## 五、前端页面说明

- `Footprint.vue`
  - 计算器表单：行为类型、行为名称、数据值、日期与备注
  - 历史记录：分页/日期筛选、编辑与删除
  - 统计与图表：总览卡片、每日趋势折线图、行为统计柱状图
- `Checkin.vue`（新增）
  - 今日打卡：预设“次”类行为一键打卡，幂等提示与按钮禁用
  - 打卡统计：连续打卡天数、累计打卡数、本月达成率
  - 打卡日历：月份选择器、每日次数与行为类型标签展示
- 路由入口：
  - 在 `frontend/src/router/index.js` 中配置，进入“碳足迹”与“打卡”视图
- 导航入口：
  - 在 `frontend/src/layouts/MainLayout.vue` 顶部菜单中接入“碳足迹”与“打卡”入口（需登录态）

## 六、下一步计划

### 1) 位置加成与勋章联动（服务端为主）
- 位置加成：基于地理位置或场景（如绿色出行区域、节能场所）配置积分倍率加成
- 勋章联动：
  - 连续打卡勋章：如 `streak ≥ 7`、`streak ≥ 30`
  - 月度达成勋章：本月打卡天数达到阈值
  - 多行为达成勋章：某类行为累计次数达标
- 接口：在 `checkin` 成功后调用 `AchievementService.evaluateAndGrant(userId)` 授予勋章；前端展示解锁提示

### 2) 排行榜与规则中心
- 排行榜模块：
  - 用户积分排行、减排排行、连续打卡排行
  - 性能优化：Redis ZSet 存储排行，定时回写数据库
- 规则中心(配置化)：
  - 将“积分倍率/每日上限/等级阈值”等规则配置化
  - 管理员可在活动或节日灵活调整激励力度
  - 缓存：规则加载至缓存，动态生效

### 3) 性能与规则配置（可选）
- 规则阈值持久化，后台管理界面配置
- Redis 做今日打卡状态与排行缓存，定时回写数据库
- 接口幂等与限流完善

## 七、维护与常见问题

- 前端 Vite 端口占用时将自动尝试其他端口（例如从 3000 切换到 3001）
- 若前端图表为空：
  - 确认最近30天有记录，或尝试创建一条记录
  - 图表容器使用 `v-show` 保持在 DOM 中，统计加载后触发初始化/resize
- 打卡提示：
  - 同用户/同行为/同日仅允许一次，重复打卡会提示“今日已完成该行为打卡”
  - 月份选择器使用 `YYYY-MM` 格式，月历展示每日次数与行为类型
- 编译报红：
  - 如遇报错，请执行 `mvn clean compile`；确认数据库表结构与应用配置正确

## 八、贡献与开发规范

- 分支建议：`feat/*`(新功能)、`fix/*`(修复)、`docs/*`(文档)
- 提交信息：使用动词前缀与模块标识，例如 `feat(footprint): add edit dialog`、`feat(checkin): add calendar view`
- 代码风格：
  - 后端：遵循 Java/Spring 规范，方法命名清晰、事务与幂等校验完善
  - 前端：Vue3 + Element Plus，组件化清晰、校验与提示友好
- 安全与隐私：
  - 接口需要用户登录态；使用 `AuthInterceptor` 与 `JwtUtil` 解析用户ID

## 九、版权与许可

- 本项目仅用于毕业设计与学习参考，若需商业使用或二次开发请自行评估风险并遵循相关法律法规。
