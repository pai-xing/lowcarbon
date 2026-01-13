<template>
  <el-container class="main-layout">
    <el-header class="header">
      <div class="header-content">
        <div class="user-actions">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :src="userStore.userInfo?.avatar" :key="userStore.userInfo?.avatar" :size="32">
                  {{ userStore.userInfo?.nickname?.[0] || userStore.userInfo?.username?.[0] }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
        <div class="logo" @click="$router.push('/')">
          <el-icon><Sunny /></el-icon>
          <span>低碳生活系统</span>
        </div>
        <div class="nav-menu">
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            router
            class="nav-menu-item"
          >
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/category/huanbao">环保知识</el-menu-item>
            <el-menu-item index="/category/ditan">低碳生活</el-menu-item>
            <el-menu-item index="/category/jieneng">节能减排</el-menu-item>
            <el-menu-item index="/category/lvxing">绿色出行</el-menu-item>
            <el-menu-item v-if="userStore.isLoggedIn" index="/footprint">碳足迹</el-menu-item>
            <el-menu-item v-if="userStore.isLoggedIn" index="/checkin">打卡</el-menu-item>
            <el-menu-item v-if="userStore.isLoggedIn" index="/publish">
              <el-icon><EditPen /></el-icon>
              <span>发布科普</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAdmin" index="/admin/articles">文章管理</el-menu-item>
          </el-menu>
        </div>
        <div class="header-search">
          <el-input
            v-model="headerKeyword"
            placeholder="搜索文章标题或内容"
            clearable
            size="small"
            @keyup.enter="handleHeaderSearch"
            style="width: 260px"
          >
            <template #append>
              <el-button :icon="Search" @click="handleHeaderSearch" />
            </template>
          </el-input>
        </div>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Sunny, ArrowDown, EditPen, Search } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 头部搜索框状态与事件
const headerKeyword = ref('')
const handleHeaderSearch = () => {
  // 跳转到首页并带上关键词参数，由 Home 页面根据路由查询参数执行搜索
  router.push({ name: 'Home', query: { keyword: headerKeyword.value } })
}

const activeMenu = computed(() => {
  if (route.path.startsWith('/admin')) return '/admin/articles'
  if (route.path.startsWith('/profile')) return '/profile'
  if (route.path.startsWith('/category')) return route.path
  if (route.path.startsWith('/footprint')) return '/footprint'
  if (route.path.startsWith('/checkin')) return '/checkin'
  return '/'
})

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  }
}

onMounted(() => {
  if (userStore.isLoggedIn && !userStore.userInfo) {
    userStore.fetchUserInfo()
  }
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 60px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu-item {
  border-bottom: none;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  width: 100%;
}
.header-search {
  display: flex;
  align-items: center;
  margin-right: 12px;
}

</style>
