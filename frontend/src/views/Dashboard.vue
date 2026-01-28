<template>
  <div class="dashboard-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据统计</span>
          <el-button type="primary" size="small" @click="fetchStatistics">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" v-loading="loading">
        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409EFF;">
              <el-icon size="40"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon size="40"><UserFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.activeUsers || 0 }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card">
            <div class="stat-icon" style="background: #E6A23C;">
              <el-icon size="40"><TrophyBase /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalPoints || 0 }}</div>
              <div class="stat-label">总积分</div>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon size="40"><Sunrise /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatReduction(statistics.totalReduction) }}</div>
              <div class="stat-label">总减排量(kg)</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <div class="info-panel">
            <h3>平台概览</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户总数">
                {{ statistics.totalUsers || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="活跃用户数">
                {{ statistics.activeUsers || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="用户活跃率">
                {{ calculateActiveRate() }}%
              </el-descriptions-item>
              <el-descriptions-item label="平均积分">
                {{ calculateAvgPoints() }}
              </el-descriptions-item>
              <el-descriptions-item label="总积分">
                {{ statistics.totalPoints || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="总减排量">
                {{ formatReduction(statistics.totalReduction) }} kg
              </el-descriptions-item>
              <el-descriptions-item label="人均减排量">
                {{ calculateAvgReduction() }} kg
              </el-descriptions-item>
              <el-descriptions-item label="今日新增用户">
                {{ statistics.todayNewUsers || 0 }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, UserFilled, TrophyBase, Sunrise, Refresh } from '@element-plus/icons-vue'
import { getStatistics } from '../api/user'

const loading = ref(false)
const statistics = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalPoints: 0,
  totalReduction: 0,
  todayNewUsers: 0
})

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    const res = await getStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    } else {
      ElMessage.error(res.msg || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 计算活跃率
const calculateActiveRate = () => {
  if (!statistics.value.totalUsers || statistics.value.totalUsers === 0) {
    return 0
  }
  return ((statistics.value.activeUsers / statistics.value.totalUsers) * 100).toFixed(2)
}

// 计算平均积分
const calculateAvgPoints = () => {
  if (!statistics.value.totalUsers || statistics.value.totalUsers === 0) {
    return 0
  }
  return Math.round(statistics.value.totalPoints / statistics.value.totalUsers)
}

// 计算人均减排量
const calculateAvgReduction = () => {
  if (!statistics.value.totalUsers || statistics.value.totalUsers === 0) {
    return '0.00'
  }
  return (statistics.value.totalReduction / statistics.value.totalUsers).toFixed(2)
}

// 格式化减排量
const formatReduction = (value) => {
  if (!value) return '0.00'
  return parseFloat(value).toFixed(2)
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 20px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.info-panel {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.info-panel h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 16px;
  color: #303133;
}
</style>
