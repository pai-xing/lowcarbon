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
      
      <!-- 互动操作栏 -->
      <div class="interaction-bar" v-if="userStore.token">
        <el-button 
          :type="hasLiked ? 'primary' : 'default'"
          :icon="hasLiked ? 'Star' : 'StarFilled'"
          @click="handleLike"
        >
          {{ hasLiked ? '已点赞' : '点赞' }} ({{ article.likesCount || 0 }})
        </el-button>
        <el-button 
          :type="hasFavorited ? 'warning' : 'default'"
          :icon="hasFavorited ? 'Star' : 'StarFilled'"
          @click="handleFavorite"
        >
          {{ hasFavorited ? '已收藏' : '收藏' }} ({{ article.favoritesCount || 0 }})
        </el-button>
        <el-button icon="ChatDotRound">
          评论 ({{ article.commentsCount || 0 }})
        </el-button>
      </div>
      
      <!-- 评论区 -->
      <div class="comments-section" v-if="userStore.token">
        <el-divider content-position="left">
          <h3>评论区 ({{ comments.length }})</h3>
        </el-divider>
        
        <!-- 发表评论 -->
        <div class="comment-input">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            placeholder="发表你的看法..."
            maxlength="500"
            show-word-limit
          />
          <el-button 
            type="primary" 
            @click="handleAddComment"
            :disabled="!newComment.trim()"
            style="margin-top: 10px"
          >
            发表评论
          </el-button>
        </div>
        
        <!-- 评论列表 -->
        <div class="comments-list">
          <div 
            v-for="comment in comments" 
            :key="comment.id"
            class="comment-item"
          >
            <div class="comment-header">
              <span class="comment-author">{{ comment.userName }}</span>
              <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-actions">
              <el-button 
                v-if="userStore.userId === comment.userId || userStore.isAdmin"
                type="danger" 
                link 
                size="small"
                @click="handleDeleteComment(comment.id)"
              >
                删除
              </el-button>
            </div>
          </div>
          <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发吧！" />
        </div>
      </div>
      
      <!-- 未登录提示 -->
      <div class="login-prompt" v-else>
        <el-alert
          title="登录后可点赞、收藏和评论"
          type="info"
          center
          :closable="false"
        >
          <template #default>
            <el-button type="primary" link @click="router.push('/login')">立即登录</el-button>
          </template>
        </el-alert>
      </div>
      
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
import { 
  getArticleDetail, 
  deleteArticle, 
  toggleTop,
  likeArticle,
  unlikeArticle,
  checkLikeStatus,
  favoriteArticle,
  unfavoriteArticle,
  checkFavoriteStatus,
  addComment,
  getComments,
  deleteComment
} from '../api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, View, Clock } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const article = ref({})
const hasLiked = ref(false)
const hasFavorited = ref(false)
const comments = ref([])
const newComment = ref('')

const fetchArticleDetail = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
    
    // 如果用户已登录，检查点赞和收藏状态
    if (userStore.token) {
      await checkInteractionStatus()
      await fetchComments()
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('文章不存在或已被删除')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const checkInteractionStatus = async () => {
  try {
    const [likeRes, favoriteRes] = await Promise.all([
      checkLikeStatus(route.params.id),
      checkFavoriteStatus(route.params.id)
    ])
    hasLiked.value = likeRes.data.hasLiked
    hasFavorited.value = likeRes.data.hasFavorited
  } catch (error) {
    console.error('检查状态失败:', error)
  }
}

const handleLike = async () => {
  try {
    if (hasLiked.value) {
      await unlikeArticle(route.params.id)
      hasLiked.value = false
      article.value.likesCount = (article.value.likesCount || 1) - 1
      ElMessage.success('已取消点赞')
    } else {
      await likeArticle(route.params.id)
      hasLiked.value = true
      article.value.likesCount = (article.value.likesCount || 0) + 1
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const handleFavorite = async () => {
  try {
    if (hasFavorited.value) {
      await unfavoriteArticle(route.params.id)
      hasFavorited.value = false
      article.value.favoritesCount = (article.value.favoritesCount || 1) - 1
      ElMessage.success('已取消收藏')
    } else {
      await favoriteArticle(route.params.id)
      hasFavorited.value = true
      article.value.favoritesCount = (article.value.favoritesCount || 0) + 1
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const fetchComments = async () => {
  try {
    const res = await getComments(route.params.id)
    comments.value = res.data
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const handleAddComment = async () => {
  if (!newComment.value.trim()) {
    return
  }
  
  try {
    await addComment(route.params.id, newComment.value)
    ElMessage.success('评论成功')
    newComment.value = ''
    await fetchComments()
    article.value.commentsCount = (article.value.commentsCount || 0) + 1
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error(error.message || '评论失败')
  }
}

const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteComment(commentId)
    ElMessage.success('删除成功')
    await fetchComments()
    article.value.commentsCount = Math.max((article.value.commentsCount || 1) - 1, 0)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
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

.interaction-bar {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
}

.comments-section {
  margin-top: 32px;
}

.comment-input {
  margin-bottom: 24px;
}

.comments-list {
  margin-top: 20px;
}

.comment-item {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

.login-prompt {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
</style>
