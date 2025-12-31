# 白屏问题排查指南

## 常见原因和解决方案

### 1. 检查浏览器控制台错误

**最重要的一步**：打开浏览器开发者工具（F12），查看 Console 标签页是否有错误信息。

常见错误：
- `Cannot find module` - 模块导入错误
- `Uncaught TypeError` - 类型错误
- `Failed to resolve` - 路径解析错误

### 2. 检查网络请求

在浏览器开发者工具的 Network 标签页中：
- 检查是否有请求失败（红色）
- 检查静态资源是否加载成功
- 检查 API 请求是否正常

### 3. 常见问题修复

#### 问题1：Element Plus 图标未正确导入

如果看到图标相关的错误，检查：
```bash
npm list @element-plus/icons-vue
```

如果未安装，执行：
```bash
npm install @element-plus/icons-vue
```

#### 问题2：路由配置错误

检查 `src/router/index.js` 文件是否正确

#### 问题3：组件导入错误

检查各个组件文件是否存在：
- `src/views/Home.vue`
- `src/views/Login.vue`
- `src/layouts/MainLayout.vue`
- 等等

### 4. 清除缓存重新安装

```bash
# 删除 node_modules 和锁文件
rm -rf node_modules package-lock.json

# 重新安装
npm install

# 重新启动
npm run dev
```

### 5. 检查端口占用

如果 3000 端口被占用，Vite 会自动使用其他端口，检查终端输出的实际端口号。

### 6. 检查 Vite 配置

确保 `vite.config.js` 配置正确，特别是：
- Vue 插件已配置
- 路径别名配置正确

### 7. 简化测试

如果还是白屏，可以创建一个最简单的测试页面：

创建 `src/views/Test.vue`:
```vue
<template>
  <div>
    <h1>测试页面</h1>
    <p>如果你能看到这个，说明 Vue 正常工作</p>
  </div>
</template>
```

修改路由，将首页指向这个测试页面，看是否能显示。

### 8. 检查浏览器兼容性

确保浏览器支持 ES6+ 语法：
- Chrome 60+
- Firefox 60+
- Safari 12+
- Edge 79+

### 9. 查看 Vite 终端输出

检查运行 `npm run dev` 的终端窗口，看是否有编译错误或警告。

## 快速诊断步骤

1. **打开浏览器控制台**（F12）
2. **查看 Console 标签**，记录所有错误信息
3. **查看 Network 标签**，检查资源加载情况
4. **检查终端输出**，看 Vite 是否有错误提示
5. **尝试访问** `http://localhost:3000`，看是否能显示任何内容

## 获取帮助

如果以上方法都无法解决，请提供：
1. 浏览器控制台的完整错误信息
2. Vite 终端的输出信息
3. 浏览器 Network 标签的截图
4. 使用的浏览器版本

