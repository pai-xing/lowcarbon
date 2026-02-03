<template>
  <div class="leaderboard-container">
    <div class="header">
      <h2>🏆 低碳排行榜</h2>
      <p class="subtitle">一起为地球减负，成为低碳达人</p>
    </div>

    <!-- Tab切换 -->
    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        :class="['tab-btn', { active: activeTab === tab.value }]"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 活动选择（仅活动榜显示） -->
    <div v-if="activeTab === 'activity'" class="activity-selector">
      <select v-model="selectedActivityId" @change="loadLeaderboard" class="activity-select">
        <option value="">请选择活动</option>
        <option v-for="activity in activities" :key="activity.id" :value="activity.id">
          {{ activity.title }}
        </option>
      </select>
    </div>

    <!-- 我的排名卡片 -->
    <div v-if="myRank" class="my-rank-card">
      <div class="my-rank-content">
        <div class="rank-info">
          <span class="label">我的排名</span>
          <span class="rank-number">第 {{ myRank.rank }} 名</span>
        </div>
        <div class="stats">
          <div class="stat-item">
            <span class="stat-label">积分</span>
            <span class="stat-value">{{ myRank.points }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">减排量</span>
            <span class="stat-value">{{ myRank.totalReduction }} kg</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 排行榜列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="leaderboardList.length === 0" class="empty">
      暂无数据
    </div>

    <div v-else class="leaderboard-list">
      <!-- 前三名特殊展示 -->
      <div v-if="topThree.length > 0" class="top-three">
        <div 
          v-for="(user, index) in topThree" 
          :key="user.userId"
          :class="['top-item', `rank-${index + 1}`]"
        >
          <div class="medal">
            <span v-if="index === 0">🥇</span>
            <span v-else-if="index === 1">🥈</span>
            <span v-else>🥉</span>
          </div>
          <img :src="user.avatar || '/default-avatar.png'" :alt="user.nickname" class="avatar" />
          <div class="user-info">
            <div class="nickname">{{ user.nickname || user.username }}</div>
            <div class="badge" v-if="user.vipBadge === 1">👑 VIP</div>
          </div>
          <div class="stats">
            <div class="points">{{ user.points }} 积分</div>
            <div class="reduction">{{ user.totalReduction }} kg</div>
          </div>
        </div>
      </div>

      <!-- 其他排名 -->
      <div v-if="otherRanks.length > 0" class="other-ranks">
        <div 
          v-for="user in otherRanks" 
          :key="user.userId"
          class="rank-item"
        >
          <div class="rank-number">{{ user.rank }}</div>
          <img :src="user.avatar || '/default-avatar.png'" :alt="user.nickname" class="avatar" />
          <div class="user-info">
            <div class="nickname">
              {{ user.nickname || user.username }}
              <span class="badge" v-if="user.vipBadge === 1">👑</span>
            </div>
          </div>
          <div class="stats">
            <div class="points">{{ user.points }} 积分</div>
            <div class="reduction">{{ user.totalReduction }} kg</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getLeaderboard } from '../api/leaderboard'
import { getActivityList } from '../api/activity'

// Tab配置
const tabs = [
  { label: '总榜', value: 'total' },
  { label: '周榜', value: 'week' },
  { label: '月榜', value: 'month' },
  { label: '活动榜', value: 'activity' }
]

const activeTab = ref('total')
const leaderboardList = ref([])
const myRank = ref(null)
const loading = ref(false)

// 活动相关
const activities = ref([])
const selectedActivityId = ref('')

// 前三名
const topThree = computed(() => {
  return leaderboardList.value.slice(0, 3)
})

// 其他排名
const otherRanks = computed(() => {
  return leaderboardList.value.slice(3)
})

// 切换Tab
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'activity') {
    loadActivities()
  } else {
    loadLeaderboard()
  }
}

// 加载活动列表
const loadActivities = async () => {
  try {
    const res = await getActivityList({ status: 1 }) // 只加载进行中的活动
    activities.value = res.data || []
    if (activities.value.length > 0) {
      selectedActivityId.value = activities.value[0].id
      loadLeaderboard()
    }
  } catch (error) {
    console.error('加载活动列表失败', error)
  }
}

// 加载排行榜
const loadLeaderboard = async () => {
  if (activeTab.value === 'activity' && !selectedActivityId.value) {
    return
  }

  loading.value = true
  try {
    const params = {
      type: activeTab.value,
      limit: 50
    }
    
    if (activeTab.value === 'activity') {
      params.activityId = selectedActivityId.value
    }

    const res = await getLeaderboard(params)
    leaderboardList.value = res.data.list || []
    myRank.value = res.data.myRank
  } catch (error) {
    console.error('加载排行榜失败', error)
    leaderboardList.value = []
    myRank.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLeaderboard()
})
</script>

<style scoped>
.leaderboard-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h2 {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 10px;
}

.subtitle {
  color: #7f8c8d;
  font-size: 14px;
}

/* Tab切换 */
.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 12px;
}

.tab-btn {
  flex: 1;
  padding: 12px;
  border: none;
  background: transparent;
  color: #7f8c8d;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-btn.active {
  background: #4CAF50;
  color: white;
}

.tab-btn:hover:not(.active) {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

/* 活动选择器 */
.activity-selector {
  margin-bottom: 20px;
}

.activity-select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
}

/* 我的排名卡片 */
.my-rank-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  color: white;
}

.my-rank-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rank-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.rank-info .label {
  font-size: 14px;
  opacity: 0.9;
}

.rank-number {
  font-size: 24px;
  font-weight: bold;
}

.stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
}

/* 前三名 */
.top-three {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.top-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.top-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.rank-1 {
  border: 2px solid #FFD700;
  background: linear-gradient(to right, #FFF9E6, white);
}

.rank-2 {
  border: 2px solid #C0C0C0;
  background: linear-gradient(to right, #F5F5F5, white);
}

.rank-3 {
  border: 2px solid #CD7F32;
  background: linear-gradient(to right, #FFF0E6, white);
}

.medal {
  font-size: 32px;
}

.top-item .avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.top-item .user-info {
  flex: 1;
}

.top-item .nickname {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.badge {
  display: inline-block;
  font-size: 12px;
  color: #FFD700;
}

.top-item .stats {
  text-align: right;
}

.top-item .points {
  font-size: 16px;
  font-weight: bold;
  color: #4CAF50;
  margin-bottom: 5px;
}

.top-item .reduction {
  font-size: 14px;
  color: #7f8c8d;
}

/* 其他排名 */
.other-ranks {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.rank-item:hover {
  transform: translateX(5px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.rank-number {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 50%;
  font-weight: bold;
  color: #7f8c8d;
}

.rank-item .avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  object-fit: cover;
}

.rank-item .user-info {
  flex: 1;
}

.rank-item .nickname {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
}

.rank-item .badge {
  margin-left: 5px;
  font-size: 12px;
}

.rank-item .stats {
  text-align: right;
}

.rank-item .points {
  font-size: 14px;
  font-weight: bold;
  color: #4CAF50;
  margin-bottom: 3px;
}

.rank-item .reduction {
  font-size: 12px;
  color: #7f8c8d;
}

/* 加载和空状态 */
.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
}

/* 响应式 */
@media (max-width: 768px) {
  .leaderboard-container {
    padding: 15px;
  }

  .tabs {
    flex-wrap: wrap;
  }

  .tab-btn {
    flex: 1 1 45%;
  }

  .my-rank-content {
    flex-direction: column;
    gap: 15px;
  }

  .top-item {
    padding: 15px;
  }

  .top-item .avatar {
    width: 50px;
    height: 50px;
  }

  .medal {
    font-size: 24px;
  }
}
</style>
