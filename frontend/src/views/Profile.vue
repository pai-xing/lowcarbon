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
          <el-avatar :src="userStore.userInfo?.avatar || userInfo.avatar" :key="userStore.userInfo?.avatar || userInfo.avatar" :size="100">
            {{ (userStore.userInfo?.nickname || userInfo.nickname)?.[0] || (userStore.userInfo?.username || userInfo.username)?.[0] }}
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

        <div class="tabs-section">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="我的文章" name="articles">
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
                @current-change="handleArticlePageChange"
                style="margin-top: 15px; justify-content: flex-end"
              />
            </el-tab-pane>

            <el-tab-pane label="我的收藏" name="favorites">
              <el-table :data="favoriteArticles" v-loading="favoriteLoading" style="width: 100%">
                <el-table-column prop="title" label="标题">
                  <template #default="{ row }">
                    <el-link type="primary" @click="router.push(`/article/${row.id}`)">
                      {{ row.title }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="authorName" label="作者" width="120" />
                <el-table-column prop="category" label="分类" width="100" />
                <el-table-column prop="views" label="浏览" width="80" />
                <el-table-column prop="createTime" label="发布时间" width="120">
                  <template #default="{ row }">
                    {{ formatDate(row.createTime) }}
                  </template>
                </el-table-column>
              </el-table>
              
              <el-pagination
                v-if="totalFavorites > 0"
                v-model:current-page="favoriteQuery.pageNum"
                :page-size="favoriteQuery.pageSize"
                :total="totalFavorites"
                layout="prev, pager, next"
                @current-change="handleFavoritePageChange"
                style="margin-top: 15px; justify-content: flex-end"
              />
              
              <el-empty v-if="favoriteArticles.length === 0 && !favoriteLoading" description="暂无收藏" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getArticleList, deleteArticle, getFavoriteArticles } from '../api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Star, Sunny, Trophy, Edit, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const articleLoading = ref(false)
const favoriteLoading = ref(false)
const activeTab = ref('articles')

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

const favoriteArticles = ref([])
const totalFavorites = ref(0)
const favoriteQuery = ref({
  pageNum: 1,
  pageSize: 5
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

const fetchFavoriteArticles = async () => {
  favoriteLoading.value = true
  try {
    const res = await getFavoriteArticles(favoriteQuery.value)
    favoriteArticles.value = res.data.records || []
    totalFavorites.value = res.data.total || 0
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    favoriteLoading.value = false
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'favorites' && favoriteArticles.value.length === 0) {
    fetchFavoriteArticles()
  }
}

const handleArticlePageChange = () => {
  fetchMyArticles()
}

const handleFavoritePageChange = () => {
  fetchFavoriteArticles()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchUserInfo()
})

// 监听全局 store 的头像变化，及时同步到本页的 userInfo
watch(
  () => userStore.userInfo?.avatar,
  (newVal) => {
    if (newVal) {
      userInfo.value.avatar = newVal
    }
  }
)
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

.achievements-section {
  padding: 20px 0;
}

.achievements-section h3 {
  margin-bottom: 16px;
  color: #303133;
}

.tabs-section {
  padding: 20px 0;
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
