<template>
  <div class="article-detail-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="article-header">
          <div class="article-title-section">
            <el-tag v-if="article.isTop" type="danger" size="small">置顶</el-tag>
            <h1 class="article-title">{{ article.title }}</h1>
          </div>
          <div class="article-meta">
            <span><el-icon><User /></el-icon> {{ article.authorName }}</span>
            <span><el-icon><View /></el-icon> {{ article.views }}</span>
            <span><el-icon><Clock /></el-icon> {{ formatDate(article.createTime) }}</span>
            <el-tag v-if="article.category" size="small">{{ article.category }}</el-tag>
          </div>
        </div>
      </template>
      
      <div class="article-content" v-if="article.content" v-html="article.content"></div>
      
      <div class="article-actions" v-if="userStore.isAdmin">
        <el-button type="primary" @click="goToManage">前往管理</el-button>
        <el-button type="danger" @click="handleDelete">删除</el-button>
        <el-button
          :type="article.isTop ? 'warning' : 'info'"
          @click="handleToggleTop"
        >
          {{ article.isTop ? '取消置顶' : '置顶' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getArticleDetail, deleteArticle, toggleTop } from '../api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, View, Clock } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const article = ref({})

const fetchArticleDetail = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('文章不存在或已被删除')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const goToManage = () => {
  router.push('/admin/articles')
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteArticle(article.value.id)
    ElMessage.success('删除成功')
    router.push('/')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleToggleTop = async () => {
  try {
    await toggleTop(article.value.id)
    ElMessage.success('操作成功')
    fetchArticleDetail()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchArticleDetail()
})
</script>

<style scoped>
.article-detail-container {
  max-width: 900px;
  margin: 0 auto;
}

.article-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.article-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.article-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-content {
  padding: 24px 0;
  line-height: 1.8;
  color: #303133;
  font-size: 16px;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
}

.article-content :deep(p) {
  margin-bottom: 16px;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 16px 0;
}

.article-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
}
</style>

