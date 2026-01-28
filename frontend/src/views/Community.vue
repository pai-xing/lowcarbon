<template>
  <div class="community-container">
    <div class="community-header">
      <h1>社区动态</h1>
      <el-button v-if="userStore.token" type="primary" @click="goToPublish">
        <el-icon><EditPen /></el-icon>
        发布动态
      </el-button>
    </div>

    <!-- 帖子列表 -->
    <div class="post-list">
      <el-empty v-if="postList.length === 0" description="暂无动态" />
      
      <div v-for="post in postList" :key="post.id" class="post-card">
        <!-- 帖子头部 -->
        <div class="post-header">
          <div class="user-info">
            <el-avatar :src="post.avatar || '/default-avatar.png'" :size="40" />
            <div class="user-detail">
              <span class="nickname">{{ post.nickname }}</span>
              <span class="time">{{ formatTime(post.createTime) }}</span>
            </div>
          </div>
          <el-dropdown v-if="userStore.userId === post.userId" trigger="click">
            <el-icon class="more-icon"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleDelete(post.id)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 帖子内容 -->
        <div class="post-content">
          <p class="content-text">{{ post.content }}</p>
          
          <!-- 图片展示 -->
          <div v-if="post.images && post.images.length > 0" class="images-grid">
            <el-image
              v-for="(img, index) in post.images"
              :key="index"
              :src="img"
              :preview-src-list="post.images"
              :initial-index="index"
              fit="cover"
              class="post-image"
            />
          </div>
        </div>

        <!-- 帖子底部操作栏 -->
        <div class="post-footer">
          <div class="action-item" @click="handleLike(post)">
            <el-icon :class="{ liked: post.isLiked }">
              <component :is="post.isLiked ? 'StarFilled' : 'Star'" />
            </el-icon>
            <span>{{ post.likesCount || 0 }}</span>
          </div>
          <div class="action-item" @click="showComments(post)">
            <el-icon><ChatLineRound /></el-icon>
            <span>{{ post.commentsCount || 0 }}</span>
          </div>
        </div>

        <!-- 评论区域 -->
        <div v-if="activePostId === post.id" class="comments-section">
          <!-- 评论输入框 -->
          <div v-if="userStore.token" class="comment-input">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="2"
              :placeholder="replyToUser ? `回复 @${replyToUser}` : '发表评论...'"
              maxlength="500"
              show-word-limit
            />
            <div class="comment-actions">
              <el-button v-if="replyToUser" size="small" @click="cancelReply">取消回复</el-button>
              <el-button type="primary" size="small" @click="submitComment(post.id)">发送</el-button>
            </div>
          </div>
          <div v-else class="login-tip">
            <span>登录后可参与评论</span>
            <el-button type="text" @click="$router.push('/login')">去登录</el-button>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :src="comment.avatar || '/default-avatar.png'" :size="32" />
              <div class="comment-detail">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.nickname }}</span>
                  <span v-if="comment.targetNickname" class="reply-to">
                    回复 @{{ comment.targetNickname }}
                  </span>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                </div>
                <p class="comment-content">{{ comment.content }}</p>
                <div class="comment-actions">
                  <el-button
                    v-if="userStore.token"
                    type="text"
                    size="small"
                    @click="replyComment(comment)"
                  >
                    回复
                  </el-button>
                  <el-button
                    v-if="userStore.userId === comment.userId"
                    type="text"
                    size="small"
                    @click="deleteComment(comment.id, post.id)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
            <el-empty v-if="comments.length === 0" description="暂无评论" :image-size="60" />
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  EditPen,
  MoreFilled,
  Star,
  StarFilled,
  ChatLineRound
} from '@element-plus/icons-vue'
import {
  getPostList,
  deletePost,
  toggleLike,
  addComment,
  getComments,
  deleteComment as deleteCommentApi
} from '@/api/post'

const router = useRouter()
const userStore = useUserStore()

const postList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

const activePostId = ref(null)
const comments = ref([])
const commentContent = ref('')
const replyToUser = ref('')
const replyToUserId = ref(null)

// 获取帖子列表
const fetchPosts = async (page = 1) => {
  try {
    loading.value = true
    const res = await getPostList({
      page,
      pageSize: pageSize.value,
      status: 1 // 只显示已发布的帖子
    })
    
    if (page === 1) {
      postList.value = res.data.records || []
    } else {
      postList.value.push(...(res.data.records || []))
    }
    
    hasMore.value = postList.value.length < res.data.total
    currentPage.value = page
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  fetchPosts(currentPage.value + 1)
}

// 跳转到发布页面
const goToPublish = () => {
  router.push('/publish-post')
}

// 点赞/取消点赞
const handleLike = async (post) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await toggleLike(post.id)
    post.isLiked = !post.isLiked
    post.likesCount += post.isLiked ? 1 : -1
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 显示/隐藏评论区
const showComments = async (post) => {
  if (activePostId.value === post.id) {
    activePostId.value = null
    comments.value = []
  } else {
    activePostId.value = post.id
    await fetchComments(post.id)
  }
}

// 获取评论列表
const fetchComments = async (postId) => {
  try {
    const res = await getComments(postId)
    comments.value = res.data || []
  } catch (error) {
    ElMessage.error('获取评论失败')
  }
}

// 回复评论
const replyComment = (comment) => {
  replyToUser.value = comment.nickname
  replyToUserId.value = comment.userId
  commentContent.value = ''
}

// 取消回复
const cancelReply = () => {
  replyToUser.value = ''
  replyToUserId.value = null
}

// 提交评论
const submitComment = async (postId) => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    await addComment({
      postId,
      targetUserId: replyToUserId.value,
      content: commentContent.value
    })
    
    ElMessage.success('评论成功')
    commentContent.value = ''
    cancelReply()
    
    // 刷新评论列表
    await fetchComments(postId)
    
    // 更新帖子评论数
    const post = postList.value.find(p => p.id === postId)
    if (post) {
      post.commentsCount++
    }
  } catch (error) {
    ElMessage.error('评论失败')
  }
}

// 删除评论
const deleteComment = async (commentId, postId) => {
  try {
    await ElMessageBox.confirm('确定删除此评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteCommentApi(commentId)
    ElMessage.success('删除成功')
    
    // 刷新评论列表
    await fetchComments(postId)
    
    // 更新帖子评论数
    const post = postList.value.find(p => p.id === postId)
    if (post && post.commentsCount > 0) {
      post.commentsCount--
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 删除帖子
const handleDelete = async (postId) => {
  try {
    await ElMessageBox.confirm('确定删除此动态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePost(postId)
    ElMessage.success('删除成功')
    
    // 从列表中移除
    const index = postList.value.findIndex(p => p.id === postId)
    if (index > -1) {
      postList.value.splice(index, 1)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else if (diff < 7 * day) {
    return Math.floor(diff / day) + '天前'
  } else {
    return date.toLocaleDateString()
  }
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.community-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.community-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.community-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nickname {
  font-weight: 500;
  font-size: 14px;
}

.time {
  font-size: 12px;
  color: #999;
}

.more-icon {
  cursor: pointer;
  font-size: 20px;
  color: #999;
}

.more-icon:hover {
  color: #333;
}

.post-content {
  margin-bottom: 12px;
}

.content-text {
  margin: 0 0 12px 0;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.post-image {
  width: 100%;
  height: 200px;
  border-radius: 4px;
  cursor: pointer;
}

.post-footer {
  display: flex;
  gap: 24px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  transition: color 0.3s;
}

.action-item:hover {
  color: #409eff;
}

.action-item .liked {
  color: #f56c6c;
}

.comments-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.comment-input {
  margin-bottom: 16px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.login-tip {
  text-align: center;
  padding: 16px;
  color: #999;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-detail {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.comment-author {
  font-weight: 500;
  font-size: 14px;
}

.reply-to {
  font-size: 12px;
  color: #409eff;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  margin: 0 0 6px 0;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.load-more {
  text-align: center;
  margin-top: 24px;
}
</style>
