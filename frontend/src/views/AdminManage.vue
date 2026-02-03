<template>
  <div class="admin-manage">
    <el-page-header @back="$router.push('/')" title="返回首页">
      <template #content>
        <span class="page-title">管理员管理</span>
      </template>
    </el-page-header>

    <div class="manage-container">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in menuItems" :key="item.path">
          <el-card class="manage-card" shadow="hover" @click="navigateTo(item.path)">
            <div class="card-content">
              <el-icon :size="48" :color="item.color">
                <component :is="item.icon" />
              </el-icon>
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { 
  User, 
  Document, 
  ChatDotRound, 
  DataAnalysis,
  Calendar,
  LocationInformation
} from '@element-plus/icons-vue'

const router = useRouter()

const menuItems = [
  {
    path: '/admin/users',
    title: '用户管理',
    description: '管理注册用户信息，查询、禁用等操作',
    icon: User,
    color: '#409EFF'
  },
  {
    path: '/admin/articles',
    title: '文章管理',
    description: '发布、编辑、删除和置顶科普文章',
    icon: Document,
    color: '#67C23A'
  },
  {
    path: '/admin/posts',
    title: '社区管理',
    description: '审核用户发布的动态和评论',
    icon: ChatDotRound,
    color: '#E6A23C'
  },
  {
    path: '/admin/dashboard',
    title: '数据统计',
    description: '查看平台关键数据和可视化图表',
    icon: DataAnalysis,
    color: '#F56C6C'
  },
  {
    path: '/admin/activities',
    title: '活动管理',
    description: '创建和管理线上低碳挑战活动',
    icon: Calendar,
    color: '#9C27B0'
  },
  {
    path: '/admin/map',
    title: '地图管理',
    description: '查看用户足迹分布，维护预设打卡地点',
    icon: LocationInformation,
    color: '#00BCD4'
  }
]

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.admin-manage {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
}

.manage-container {
  margin-top: 30px;
}

.manage-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.manage-card:hover {
  transform: translateY(-5px);
}

.card-content {
  text-align: center;
  padding: 20px;
}

.card-content h3 {
  margin: 15px 0 10px;
  font-size: 18px;
  color: #303133;
}

.card-content p {
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .admin-manage {
    padding: 10px;
  }
  
  .page-title {
    font-size: 20px;
  }
}
</style>
