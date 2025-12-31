<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </div>
          <el-button type="primary" @click="router.push('/profile/edit')">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
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
            <p class="bio" v-if="userInfo.bio">{{ userInfo.bio }}</p>
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

        <div class="articles-section">
          <h3>我的文章</h3>
          <el-table :data="myArticles" v-loading="articleLoading" style="width: 100%">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="views" label="浏览" width="80" />
            <el-table-column prop="createTime" label="发布时间" width="120">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEditArticle(row)">
                  编辑
                </el-button>
                <el-button link type="danger" @click="handleDeleteArticle(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-if="totalArticles > 0"
            v-model:current-page="articleQuery.current"
            :page-size="articleQuery.size"
            :total="totalArticles"
            layout="prev, pager, next"
            @current-change="handleCurrentChange"
            style="margin-top: 15px; justify-content: flex-end"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getArticleList, deleteArticle } from '../api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Star, Sunny, Trophy, Edit, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const articleLoading = ref(false)

const userInfo = ref({
  id: null,
  username: '',
  nickname: '',
  avatar: '',
  bio: '',
  points: 0,
  totalReduction: 0,
  achievements: []
})

const myArticles = ref([])
const totalArticles = ref(0)
const articleQuery = ref({
  current: 1,
  size: 5,
  authorId: null
})

const fetchUserInfo = async () => {
  loading.value = true
  try {
    const info = await userStore.fetchUserInfo()
    userInfo.value = info
    articleQuery.value.authorId = info.id
    fetchMyArticles()
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchMyArticles = async () => {
  if (!articleQuery.value.authorId) return
  articleLoading.value = true
  try {
    const res = await getArticleList(articleQuery.value)
    myArticles.value = res.data.records || []
    totalArticles.value = res.data.total || 0
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    articleLoading.value = false
  }
}

const handleEditArticle = (article) => {
  router.push(`/publish?id=${article.id}`)
}

const handleDeleteArticle = async (article) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteArticle(article.id)
    ElMessage.success('删除成功')
    fetchMyArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleCurrentChange = () => {
  fetchMyArticles()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
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
  justify-content: space-between;
}

.header-title {
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
  margin: 0 0 12px 0;
  color: #909399;
  font-size: 14px;
}

.bio {
  color: #606266;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

.stats-section {
  padding: 20px 0;
}

.achievements-section,
.articles-section {
  padding: 20px 0;
}

.achievements-section h3,
.articles-section h3 {
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
</style>

