<template>
  <div class="post-manage-container">
    <div class="manage-header">
      <h1>帖子管理</h1>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" @change="handleFilter" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已删除" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 帖子列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <div class="expand-section">
                <h4>帖子内容：</h4>
                <p class="post-content">{{ row.content }}</p>
              </div>
              <div v-if="row.images && row.images.length > 0" class="expand-section">
                <h4>图片：</h4>
                <div class="images-preview">
                  <el-image
                    v-for="(img, index) in row.images"
                    :key="index"
                    :src="img"
                    :preview-src-list="row.images"
                    :initial-index="index"
                    fit="cover"
                    class="preview-image"
                  />
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="用户信息" width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :src="row.avatar || '/default-avatar.png'" :size="32" />
              <span>{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="内容预览" min-width="300">
          <template #default="{ row }">
            <div class="content-preview">
              {{ row.content.length > 100 ? row.content.substring(0, 100) + '...' : row.content }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="互动数据" width="120">
          <template #default="{ row }">
            <div class="stats-cell">
              <div><el-icon><Star /></el-icon> {{ row.likesCount }}</div>
              <div><el-icon><ChatLineRound /></el-icon> {{ row.commentsCount }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'info'"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handleAudit(row.id, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="handleAudit(row.id, 2)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              @click="handleAudit(row.id, 2)"
            >
              下架
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, ChatLineRound } from '@element-plus/icons-vue'
import { getPostList, auditPost, deletePost } from '@/api/post'

const loading = ref(false)
const tableData = ref([])

const filterForm = ref({
  status: 0 // 默认显示待审核
})

const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

// 获取帖子列表
const fetchData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    }
    
    if (filterForm.value.status !== null && filterForm.value.status !== undefined) {
      params.status = filterForm.value.status
    }

    const res = await getPostList(params)
    tableData.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选
const handleFilter = () => {
  pagination.value.page = 1
  fetchData()
}

// 重置筛选
const resetFilter = () => {
  filterForm.value.status = 0
  handleFilter()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.value.page = page
  fetchData()
}

const handleSizeChange = (pageSize) => {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  fetchData()
}

// 审核帖子
const handleAudit = async (postId, status) => {
  const statusText = status === 1 ? '通过' : status === 2 ? '拒绝' : '下架'
  
  try {
    await ElMessageBox.confirm(
      `确定要${statusText}此帖子吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await auditPost(postId, status)
    ElMessage.success(`${statusText}成功`)
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${statusText}失败`)
    }
  }
}

// 删除帖子
const handleDelete = async (postId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除此帖子吗？删除后无法恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await deletePost(postId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已发布',
    2: '已删除'
  }
  return statusMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.post-manage-container {
  padding: 20px;
}

.manage-header {
  margin-bottom: 20px;
}

.manage-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-preview {
  line-height: 1.5;
  word-break: break-word;
}

.stats-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
}

.stats-cell > div {
  display: flex;
  align-items: center;
  gap: 4px;
}

.expand-content {
  padding: 16px;
}

.expand-section {
  margin-bottom: 16px;
}

.expand-section:last-child {
  margin-bottom: 0;
}

.expand-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.post-content {
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.images-preview {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
}

.preview-image {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  cursor: pointer;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
