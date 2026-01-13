<template>
  <div class="category-container">
    <div class="category-header">
      <h2 class="category-title">{{ categoryName }} 科普</h2>
      <el-tag type="success" effect="plain">分类</el-tag>
    </div>

    <div class="article-list" v-loading="loading">
      <el-card
        v-for="article in articleList"
        :key="article.id"
        class="article-card"
        shadow="hover"
        @click="goToDetail(article.id)"
      >
        <div class="article-header">
          <el-tag v-if="article.isTop" type="danger" size="small">置顶</el-tag>
          <h3 class="article-title">{{ article.title }}</h3>
        </div>
        <div class="article-content">
          <div class="article-cover" v-if="article.coverImg">
            <img :src="article.coverImg" alt="封面" />
          </div>
          <div class="article-info">
            <p class="article-summary">{{ getSummary(article.content) }}</p>
            <div class="article-meta">
              <span><el-icon><User /></el-icon> {{ article.authorName }}</span>
              <span><el-icon><View /></el-icon> {{ article.views }}</span>
              <span><el-icon><Clock /></el-icon> {{ formatDate(article.createTime) }}</span>
              <el-tag v-if="article.category" size="small">{{ article.category }}</el-tag>
            </div>
          </div>
        </div>
      </el-card>

      <el-empty v-if="!loading && articleList.length === 0" description="暂无文章" />

      <el-pagination
        v-if="total > 0"
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center"
      />
    </div>
  </div>
</template>

<script setup>
/* 分类页面：根据路由参数展示四个分类之一的独立界面 */
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleList } from '../api/article'
import { User, View, Clock } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 路由参数与分类中文名映射
const keyToCategory = {
  huanbao: '环保知识',
  ditan: '低碳生活',
  jieneng: '节能减排',
  lvxing: '绿色出行'
}

const categoryKey = ref(route.params.key)
const categoryName = computed(() => keyToCategory[categoryKey.value] || '科普')

// 列表查询条件
const loading = ref(false)
const articleList = ref([])
const total = ref(0)

const queryForm = reactive({
  keyword: '',
  category: keyToCategory[categoryKey.value] || '',
  current: 1,
  size: 10
})

const fetchArticleList = async () => {
  loading.value = true
  try {
    const res = await getArticleList(queryForm)
    articleList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  fetchArticleList()
}

const handleCurrentChange = () => {
  fetchArticleList()
}

const goToDetail = (id) => {
  router.push(`/article/${id}`)
}

const getSummary = (content) => {
  if (!content) return ''
  const text = content.replace(/<[^>]+>/g, '')
  return text.length > 150 ? text.substring(0, 150) + '...' : text
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 监听路由参数变化，切换分类时刷新列表
watch(
  () => route.params.key,
  (newKey) => {
    categoryKey.value = newKey
    queryForm.category = keyToCategory[newKey] || ''
    queryForm.current = 1
    fetchArticleList()
  }
)

onMounted(() => {
  fetchArticleList()
})
</script>

<style scoped>
.category-container {
  max-width: 1200px;
  margin: 0 auto;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.category-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.article-list {
  min-height: 400px;
}

.article-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s;
}

.article-card:hover {
  transform: translateY(-2px);
}

.article-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.article-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.article-content {
  display: flex;
  gap: 20px;
}

.article-cover {
  width: 200px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-info {
  flex: 1;
}

.article-summary {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
</style>
