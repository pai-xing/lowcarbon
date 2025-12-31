# 低碳生活系统

## 项目简介

基于 Spring Boot 3.2.0 + Vue 3 的前后端分离架构的低碳生活管理系统，支持用户注册登录、碳足迹计算、科普文章管理、社区互动等功能。

## 技术栈

### 后端
- **核心框架**: Spring Boot 3.2.0 (Java 17)
- **数据持久层**: MyBatis-Plus 3.5.9
- **数据库**: MySQL 8.0
- **接口文档**: Knife4j 4.3.0 (Swagger增强版)
- **工具库**: Lombok、Hutool
- **认证**: JWT + BCrypt密码加密

### 前端
- **框架**: Vue 3 + Vite
- **UI组件库**: Element Plus
- **HTTP客户端**: Axios

## 项目结构

```
lowcarbon-system/
├── src/main/java/com/lowcarbon/
│   ├── common/          # 通用类（Result、Constants）
│   ├── config/          # 配置类（WebConfig、Knife4jConfig、InterceptorConfig）
│   ├── controller/      # 控制器层
│   │   ├── UserController.java      # 用户与认证模块
│   │   └── ArticleController.java   # 科普内容管理模块
│   ├── dto/             # 数据传输对象
│   ├── entity/          # 实体类
│   ├── interceptor/     # 拦截器（认证拦截）
│   ├── mapper/          # MyBatis Mapper接口
│   ├── service/         # 服务层接口
│   │   └── impl/        # 服务层实现
│   └── util/            # 工具类（JwtUtil）
└── src/main/resources/
    └── application.yml   # 配置文件
```

## 数据库配置

1. 执行提供的 SQL 脚本创建数据库和表结构
2. 修改 `application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lc?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

## 核心功能模块

### 1. 用户与认证模块 (tb_user)

#### 功能特性
- ✅ 用户注册（BCrypt密码加密）
- ✅ 用户登录（JWT Token认证）
- ✅ 个人中心（修改昵称、头像上传、个性签名）
- ✅ 用户信息展示（积分、累计碳减排量、获得的勋章）

#### API接口
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/info` - 获取用户信息（需登录）
- `PUT /api/user/info` - 更新用户信息（需登录）
- `POST /api/file/upload` - 文件上传（头像/图片）

### 2. 科普内容管理模块 (tb_article)

#### 功能特性
- ✅ 文章列表展示（游客可访问，支持分类和关键词搜索）
- ✅ 文章详情查看（自动记录浏览量）
- ✅ 用户发布/管理文章（支持富文本编辑、封面上传、编辑、删除）
- ✅ 后台管理（管理员可编辑、删除、置顶文章）

#### API接口
- `GET /api/article/list` - 文章列表（游客可访问）
- `GET /api/article/{id}` - 文章详情（游客可访问）
- `POST /api/article` - 创建文章（登录用户）
- `PUT /api/article/{id}` - 更新文章（作者/管理员）
- `DELETE /api/article/{id}` - 删除文章（管理员）
- `PUT /api/article/{id}/top` - 置顶/取消置顶（管理员）

## 启动说明

1. **环境要求**
   - JDK 17+
   - Maven 3.6+
   - MySQL 8.0+

2. **启动步骤**
   ```bash
   # 1. 执行数据库脚本创建表结构
   # 2. 修改 application.yml 中的数据库配置
   # 3. 编译项目
   mvn clean install
   
   # 4. 启动项目
   mvn spring-boot:run
   ```

3. **访问接口文档**
   - 启动后访问：http://localhost:8080/doc.html
   - Swagger UI：http://localhost:8080/swagger-ui/index.html

## 接口文档说明

项目集成了 Knife4j，启动后可通过以下地址访问：
- **Knife4j文档**: http://localhost:8080/doc.html
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

## 认证说明

### JWT Token使用方式

1. **登录/注册后获取Token**
   ```json
   {
     "code": 200,
     "message": "登录成功",
     "data": {
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     }
   }
   ```

2. **请求头携带Token**
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

### 游客可访问接口
- 用户注册、登录
- 文章列表、文章详情

### 需要登录的接口
- 获取用户信息
- 更新用户信息
- 所有管理员操作接口

## 开发规范

1. **统一响应格式**
   ```json
   {
     "code": 200,
     "message": "操作成功",
     "data": {}
   }
   ```

2. **异常处理**
   - 业务异常通过 `Result.error()` 返回
   - 认证异常返回 401 状态码

3. **密码加密**
   - 使用 BCrypt 算法加密存储
   - 注册和登录时自动处理加密/验证

## 后续开发计划

- [ ] 碳足迹计算与激励模块
- [ ] 社区互动模块（动态、评论、点赞）
- [ ] 管理员数据统计模块
- [x] 文件上传功能
- [ ] 权限管理增强

## 许可证

Apache 2.0

