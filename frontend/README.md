# 低碳生活系统 - 前端

基于 Vue 3 + Vite + Element Plus 的前端项目

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Vue Router** - 官方路由管理器
- **Pinia** - Vue 状态管理
- **Element Plus** - Vue 3 组件库
- **Axios** - HTTP 客户端

## 功能特性

### 用户功能
- ✅ 用户注册/登录
- ✅ 个人中心（查看积分、减排量、勋章）
- ✅ 编辑个人资料（昵称、头像、个性签名）

### 文章功能
- ✅ 文章列表浏览（支持关键词搜索、分类筛选）
- ✅ 文章详情查看
- ✅ 分页功能

### 管理员功能
- ✅ 文章管理（发布、编辑、删除）
- ✅ 文章置顶/取消置顶

## 安装和运行

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

### 3. 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist` 目录

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口
│   │   ├── user.js       # 用户相关API
│   │   └── article.js    # 文章相关API
│   ├── assets/           # 静态资源
│   ├── layouts/          # 布局组件
│   │   └── MainLayout.vue
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # 状态管理
│   │   └── user.js
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios 配置
│   ├── views/            # 页面组件
│   │   ├── Home.vue              # 首页（文章列表）
│   │   ├── ArticleDetail.vue     # 文章详情
│   │   ├── Login.vue             # 登录页
│   │   ├── Register.vue          # 注册页
│   │   ├── Profile.vue           # 个人中心
│   │   └── ArticleManage.vue     # 文章管理
│   ├── App.vue           # 根组件
│   ├── main.js           # 入口文件
│   └── style.css         # 全局样式
├── index.html            # HTML 模板
├── package.json          # 项目配置
├── vite.config.js        # Vite 配置
└── README.md            # 项目说明
```

## API 配置

前端通过代理访问后端API，配置在 `vite.config.js` 中：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

确保后端服务运行在 `http://localhost:8080`

## 路由说明

- `/` - 首页（文章列表）
- `/login` - 登录页
- `/register` - 注册页
- `/article/:id` - 文章详情页
- `/profile` - 个人中心（需登录）
- `/admin/articles` - 文章管理（需管理员权限）

## 认证说明

- 登录/注册后，Token 会存储在 `localStorage` 中
- 请求时会自动在请求头中添加 `Authorization: Bearer {token}`
- Token 过期或无效时会自动跳转到登录页

## 注意事项

1. 确保后端服务已启动并运行在 `http://localhost:8080`
2. 管理员功能需要用户角色为 `admin`
3. 文章内容支持 HTML 格式
4. 头像和封面图需要提供完整的 URL 地址

