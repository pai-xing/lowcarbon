# Node.js 安装指南

## 问题说明
系统无法识别 `npm` 命令，说明 Node.js 未安装或未正确配置环境变量。

## 解决方案

### 方法一：安装 Node.js（推荐）

1. **下载 Node.js**
   - 访问官网：https://nodejs.org/zh-cn/
   - 下载 LTS 版本（长期支持版，推荐）
   - 选择 Windows Installer (.msi) 64-bit

2. **安装 Node.js**
   - 双击下载的 `.msi` 文件
   - 按照安装向导操作
   - **重要**：安装时勾选 "Add to PATH"（添加到环境变量）
   - 完成安装

3. **验证安装**
   - 关闭并重新打开 PowerShell 或命令提示符
   - 运行以下命令验证：
   ```bash
   node --version
   npm --version
   ```
   - 如果显示版本号，说明安装成功

### 方法二：使用包管理器安装（可选）

#### 使用 Chocolatey（如果已安装）
```powershell
choco install nodejs
```

#### 使用 Winget（Windows 10/11）
```powershell
winget install OpenJS.NodeJS.LTS
```

## 安装完成后

1. **重新打开终端**（重要！）
   - 关闭当前的 PowerShell 窗口
   - 重新打开一个新的 PowerShell 窗口

2. **进入前端目录**
   ```bash
   cd C:\Users\DELL\Desktop\毕设\lowcarbon-system\frontend
   ```

3. **安装依赖**
   ```bash
   npm install
   ```

4. **启动开发服务器**
   ```bash
   npm run dev
   ```

## 常见问题

### 问题1：安装后仍然无法识别 npm
**解决方案：**
- 确保安装时勾选了 "Add to PATH"
- 重新打开终端窗口
- 如果还是不行，手动添加到环境变量：
  1. 右键"此电脑" → "属性" → "高级系统设置"
  2. 点击"环境变量"
  3. 在"系统变量"中找到 `Path`，点击"编辑"
  4. 添加 Node.js 安装路径（通常是 `C:\Program Files\nodejs\`）

### 问题2：权限问题
**解决方案：**
- 以管理员身份运行 PowerShell
- 或者使用用户目录安装（不推荐）

## 版本要求

- Node.js: 16.0 或更高版本
- npm: 8.0 或更高版本（通常随 Node.js 一起安装）

## 快速下载链接

- **Node.js 18 LTS（推荐）**: https://nodejs.org/dist/v18.19.0/node-v18.19.0-x64.msi
- **Node.js 20 LTS**: https://nodejs.org/dist/v20.10.0/node-v20.10.0-x64.msi

## 安装完成后继续

安装完成后，请按照以下步骤操作：

```bash
# 1. 进入前端目录
cd C:\Users\DELL\Desktop\毕设\lowcarbon-system\frontend

# 2. 安装项目依赖
npm install

# 3. 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:3000` 启动。

