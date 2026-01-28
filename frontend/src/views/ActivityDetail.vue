<template>
  <div class="activity-detail">
    <!-- 活动信息卡片 -->
    <el-card class="activity-info-card" shadow="hover">
      <div class="activity-header">
        <div class="activity-title-section">
          <h1 class="activity-title">{{ activity.title }}</h1>
          <el-tag :type="getStatusType(activity.status)" size="large">
            {{ activity.statusText }}
          </el-tag>
        </div>
        <el-button
          v-if="activity.status !== 2 && !activity.hasJoined"
          type="primary"
          size="large"
          @click="handleJoin"
          :loading="joining"
        >
          参加活动
        </el-button>
        <el-tag v-else-if="activity.hasJoined" type="success" size="large">
          已参加
        </el-tag>
      </div>

      <el-divider />

      <div class="activity-content">
        <div class="info-item">
          <el-icon><Calendar /></el-icon>
          <span class="label">活动时间：</span>
          <span class="value">{{ formatTime(activity.startTime) }} 至 {{ formatTime(activity.endTime) }}</span>
        </div>
        <div class="info-item">
          <el-icon><User /></el-icon>
          <span class="label">参与人数：</span>
          <span class="value">{{ activity.participantCount }} 人</span>
        </div>
        <div class="info-item">
          <el-icon><TrendCharts /></el-icon>
          <span class="label">目标减排：</span>
          <span class="value">{{ activity.targetReduction }} kg CO₂</span>
        </div>
      </div>

      <el-divider />

      <div class="activity-description">
        <h3>活动说明</h3>
        <p>{{ activity.description }}</p>
      </div>
    </el-card>

    <!-- 个人战绩卡片 -->
    <el-card v-if="activity.hasJoined" class="my-ranking-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Trophy /></el-icon>
          <span>我的战绩</span>
        </div>
      </template>

      <div v-if="myRanking" class="my-ranking-content">
        <div class="ranking-badge">
          <div class="rank-number">
            <span v-if="myRanking.rank <= 3" class="medal">
              {{ myRanking.rank === 1 ? '🥇' : myRanking.rank === 2 ? '🥈' : '🥉' }}
            </span>
            <span v-else class="rank-text">第 {{ myRanking.rank }} 名</span>
          </div>
        </div>

        <div class="ranking-stats">
          <div class="stat-item">
            <div class="stat-value">{{ myRanking.totalReduction }} kg</div>
            <div class="stat-label">累计减排</div>
          </div>
          <el-divider direction="vertical" />
          <div class="stat-item">
            <div class="stat-value">{{ myRanking.totalPoints }}</div>
            <div class="stat-label">获得积分</div>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无战绩数据，快去记录碳足迹吧！" :image-size="100" />
    </el-card>

    <!-- 排行榜卡片 -->
    <el-card class="ranking-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Medal /></el-icon>
          <span>排行榜 TOP 100</span>
        </div>
      </template>

      <div v-if="rankings.length > 0" class="ranking-list">
        <div
          v-for="(item, index) in rankings"
          :key="item.userId"
          class="ranking-item"
          :class="{ 'is-current-user': item.userId === currentUserId, 'top-three': index < 3 }"
        >
          <div class="rank-badge">
            <span v-if="index === 0" class="medal">🥇</span>
            <span v-else-if="index === 1" class="medal">🥈</span>
            <span v-else-if="index === 2" class="medal">🥉</span>
            <span v-else class="rank-number">{{ index + 1 }}</span>
          </div>

          <el-avatar :src="item.avatar" :size="40">
            {{ item.nickname?.charAt(0) || item.username?.charAt(0) }}
          </el-avatar>

          <div class="user-info">
            <div class="nickname">{{ item.nickname || item.username }}</div>
            <div class="username">@{{ item.username }}</div>
          </div>

          <div class="stats">
            <div class="stat-item">
              <el-icon><Odometer /></el-icon>
              <span>{{ item.totalReduction }} kg</span>
            </div>
            <div class="stat-item points">
              <el-icon><Coin /></el-icon>
              <span>{{ item.totalPoints }} 积分</span>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无排行数据" :image-size="150" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  User,
  TrendCharts,
  Trophy,
  Medal,
  Odometer,
  Coin
} from '@element-plus/icons-vue'
import {
  getActivityDetail,
  joinActivity,
  getActivityRanking,
  getMyRanking
} from '@/api/activity'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activityId = ref(route.params.id)
const activity = ref({})
const rankings = ref([])
const myRanking = ref(null)
const joining = ref(false)

const currentUserId = computed(() => userStore.userInfo?.id)

// 获取活动详情
const loadActivityDetail = async () => {
  try {
    const res = await getActivityDetail(activityId.value)
    activity.value = res.data
  } catch (error) {
    ElMessage.error('加载活动详情失败')
    router.push('/activity')
  }
}

// 获取排行榜
const loadRanking = async () => {
  try {
    const res = await getActivityRanking(activityId.value)
    rankings.value = res.data
  } catch (error) {
    console.error('加载排行榜失败:', error)
  }
}

// 获取我的排名
const loadMyRanking = async () => {
  if (!activity.value.hasJoined) return
  
  try {
    const res = await getMyRanking(activityId.value)
    myRanking.value = res.data
  } catch (error) {
    console.error('加载个人排名失败:', error)
  }
}

// 参加活动
const handleJoin = async () => {
  joining.value = true
  try {
    await joinActivity(activityId.value)
    ElMessage.success('参加成功！')
    await loadActivityDetail()
    await loadMyRanking()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '参加活动失败')
  } finally {
    joining.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取状态类型
const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning' }
  return types[status] || 'info'
}

onMounted(async () => {
  await loadActivityDetail()
  await loadRanking()
  await loadMyRanking()
})
</script>

<style scoped>
.activity-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 活动信息卡片 */
.activity-info-card {
  margin-bottom: 20px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.activity-title-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-title {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.4;
}

.activity-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 20px 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #606266;
}

.info-item .el-icon {
  font-size: 20px;
  color: #67c23a;
}

.info-item .label {
  font-weight: 500;
}

.info-item .value {
  color: #303133;
  font-weight: 600;
}

.activity-description {
  margin-top: 20px;
}

.activity-description h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #303133;
}

.activity-description p {
  margin: 0;
  font-size: 15px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

/* 个人战绩卡片 */
.my-ranking-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.card-header .el-icon {
  font-size: 22px;
  color: #67c23a;
}

.my-ranking-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 20px 0;
}

.ranking-badge {
  display: flex;
  justify-content: center;
}

.rank-number {
  font-size: 64px;
  font-weight: bold;
}

.rank-number .medal {
  font-size: 80px;
}

.rank-number .rank-text {
  color: #409eff;
}

.ranking-stats {
  display: flex;
  align-items: center;
  gap: 40px;
  width: 100%;
  max-width: 500px;
  justify-content: center;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #67c23a;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

/* 排行榜卡片 */
.ranking-card {
  margin-bottom: 20px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
  transition: all 0.3s;
}

.ranking-item:hover {
  background: #e8f4fd;
  transform: translateX(4px);
}

.ranking-item.top-three {
  background: linear-gradient(135deg, #fff9e6 0%, #fff 100%);
  border: 2px solid #ffd700;
}

.ranking-item.is-current-user {
  background: linear-gradient(135deg, #e6f7ff 0%, #fff 100%);
  border: 2px solid #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.rank-badge {
  min-width: 50px;
  text-align: center;
}

.rank-badge .medal {
  font-size: 32px;
}

.rank-badge .rank-number {
  font-size: 20px;
  font-weight: bold;
  color: #909399;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-info .nickname {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.user-info .username {
  font-size: 13px;
  color: #909399;
}

.stats {
  display: flex;
  gap: 24px;
  align-items: center;
}

.stats .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.stats .stat-item .el-icon {
  font-size: 18px;
  color: #67c23a;
}

.stats .stat-item.points .el-icon {
  color: #e6a23c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activity-detail {
    padding: 12px;
  }

  .activity-header {
    flex-direction: column;
  }

  .activity-title {
    font-size: 22px;
  }

  .ranking-stats {
    gap: 20px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stats {
    flex-direction: column;
    gap: 8px;
    align-items: flex-end;
  }

  .ranking-item {
    padding: 12px;
  }
}
</style>
