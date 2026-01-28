<template>
  <div class="activity-list-container">
    <el-card class="header-card">
      <div class="header">
        <h2>低碳活动</h2>
        <p class="subtitle">参加活动，积攒积分，冲击排行榜！</p>
      </div>
    </el-card>

    <!-- 状态筛选 -->
    <el-card class="filter-card">
      <el-radio-group v-model="statusFilter" @change="loadActivities">
        <el-radio-button :value="null">全部活动</el-radio-button>
        <el-radio-button :value="1">进行中</el-radio-button>
        <el-radio-button :value="0">未开始</el-radio-button>
        <el-radio-button :value="2">已结束</el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 活动列表 -->
    <div v-loading="loading" class="activity-grid">
      <el-empty v-if="!loading && activities.length === 0" description="暂无活动" />
      
      <el-card 
        v-for="activity in activities" 
        :key="activity.id" 
        class="activity-card"
        shadow="hover"
        @click="goToDetail(activity.id)"
      >
        <div class="activity-content">
          <div class="activity-header">
            <h3>{{ activity.title }}</h3>
            <el-tag 
              :type="getStatusType(activity.status)" 
              size="small"
            >
              {{ activity.statusText }}
            </el-tag>
          </div>

          <p class="activity-desc">{{ activity.description }}</p>

          <div class="activity-info">
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(activity.startTime) }} - {{ formatDate(activity.endTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>{{ activity.participantCount }} 人参与</span>
            </div>
            <div v-if="activity.targetReduction > 0" class="info-item">
              <el-icon><TrophyBase /></el-icon>
              <span>目标: {{ activity.targetReduction }}kg</span>
            </div>
          </div>

          <div class="activity-footer">
            <el-button 
              v-if="!activity.hasJoined && activity.status !== 2" 
              type="primary" 
              size="small"
              @click.stop="handleJoin(activity)"
            >
              参加活动
            </el-button>
            <el-tag v-else-if="activity.hasJoined" type="success" size="small">
              已参加
            </el-tag>
            <el-button type="info" size="small" plain>
              查看详情
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, User, TrophyBase } from '@element-plus/icons-vue'
import { getActivityList, joinActivity } from '../api/activity'

const router = useRouter()
const loading = ref(false)
const statusFilter = ref(null)
const activities = ref([])

// 加载活动列表
const loadActivities = async () => {
  loading.value = true
  try {
    const res = await getActivityList(statusFilter.value)
    activities.value = res.data
  } catch (error) {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

// 参加活动
const handleJoin = async (activity) => {
  try {
    await joinActivity(activity.id)
    ElMessage.success('参加成功！')
    loadActivities()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '参加失败')
  }
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/activity/${id}`)
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'info'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.activity-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header {
  text-align: center;
}

.header h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.activity-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.activity-card:hover {
  transform: translateY(-5px);
}

.activity-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  flex: 1;
}

.activity-desc {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 13px;
}

.activity-footer {
  display: flex;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #EBEEF5;
}

.activity-footer .el-button {
  flex: 1;
}
</style>
