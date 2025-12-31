<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </div>
      </template>
      
      <div class="profile-content" v-loading="loading">
        <div class="profile-header">
          <el-avatar :src="userInfo.avatar" :size="100">
            {{ userInfo.nickname?.[0] || userInfo.username?.[0] }}
          </el-avatar>
          <div class="user-basic-info">
            <h2>{{ userInfo.nickname || userInfo.username }}</h2>
            <p class="username">@{{ userInfo.username }}</p>
          </div>
        </div>

        <el-divider />

        <div class="stats-section">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-statistic title="积分" :value="userInfo.points || 0">
                <template #prefix>
                  <el-icon><Star /></el-icon>
                </template>
              </el-statistic>
            </el-col>
            <el-col :span="8">
              <el-statistic title="累计减排量" :value="userInfo.totalReduction || 0" suffix="kg">
                <template #prefix>
                  <el-icon><Sunny /></el-icon>
                </template>
              </el-statistic>
            </el-col>
            <el-col :span="8">
              <el-statistic title="获得勋章" :value="userInfo.achievements?.length || 0">
                <template #prefix>
                  <el-icon><Trophy /></el-icon>
                </template>
              </el-statistic>
            </el-col>
          </el-row>
        </div>

        <el-divider />

        <div class="achievements-section" v-if="userInfo.achievements && userInfo.achievements.length > 0">
          <h3>我的勋章</h3>
          <div class="achievements-list">
            <el-card
              v-for="achievement in userInfo.achievements"
              :key="achievement.id"
              class="achievement-card"
              shadow="hover"
            >
              <div class="achievement-icon">
                <el-icon size="40"><Trophy /></el-icon>
              </div>
              <div class="achievement-info">
                <h4>{{ achievement.title }}</h4>
                <p>{{ achievement.description }}</p>
              </div>
            </el-card>
          </div>
        </div>

        <el-divider />

        <div class="edit-section">
          <h3>编辑资料</h3>
          <el-form
            ref="formRef"
            :model="form"
            label-width="100px"
            style="max-width: 600px"
          >
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="头像URL">
              <el-input v-model="form.avatar" placeholder="请输入头像URL" />
            </el-form-item>
            <el-form-item label="个性签名">
              <el-input
                v-model="form.bio"
                type="textarea"
                :rows="4"
                placeholder="请输入个性签名"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { updateUserInfo } from '../api/user'
import { ElMessage } from 'element-plus'
import { User, Star, Sunny, Trophy } from '@element-plus/icons-vue'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const formRef = ref(null)

const userInfo = ref({
  username: '',
  nickname: '',
  avatar: '',
  bio: '',
  points: 0,
  totalReduction: 0,
  achievements: []
})

const form = reactive({
  nickname: '',
  avatar: '',
  bio: ''
})

const fetchUserInfo = async () => {
  loading.value = true
  try {
    const info = await userStore.fetchUserInfo()
    userInfo.value = info
    form.nickname = info.nickname || ''
    form.avatar = info.avatar || ''
    form.bio = info.bio || ''
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateUserInfo(form)
    ElMessage.success('保存成功')
    await fetchUserInfo()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 0;
}

.user-basic-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.username {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.stats-section {
  padding: 20px 0;
}

.achievements-section {
  padding: 20px 0;
}

.achievements-section h3 {
  margin-bottom: 16px;
  color: #303133;
}

.achievements-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.achievement-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.achievement-icon {
  color: #f39c12;
}

.achievement-info h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.achievement-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.edit-section h3 {
  margin-bottom: 16px;
  color: #303133;
}
</style>

