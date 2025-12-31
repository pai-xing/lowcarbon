<template>
  <div class="article-manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文章管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            发布文章
          </el-button>
        </div>
      </template>

      <el-table
        :data="articleList"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="views" label="浏览量" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isTop" type="danger" size="small">置顶</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.isTop ? 'warning' : 'info'"
              size="small"
              @click="handleToggleTop(row)"
            >
              {{ row.isTop ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 文章编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="环保知识" value="环保知识" />
            <el-option label="低碳生活" value="低碳生活" />
            <el-option label="节能减排" value="节能减排" />
            <el-option label="绿色出行" value="绿色出行" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图URL">
          <el-input v-model="form.coverImg" placeholder="请输入封面图URL" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="请输入文章内容（支持HTML）"
          />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getArticleList, createArticle, updateArticle, deleteArticle, toggleTop } from '../api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('发布文章')
const formRef = ref(null)

const articleList = ref([])
const total = ref(0)

const queryForm = reactive({
  current: 1,
  size: 10
})

const form = reactive({
  id: null,
  title: '',
  category: '',
  coverImg: '',
  content: '',
  isTop: false
})

const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' }
  ]
}

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

const handleCreate = () => {
  dialogTitle.value = '发布文章'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑文章'
  form.id = row.id
  form.title = row.title
  form.category = row.category || ''
  form.coverImg = row.coverImg || ''
  form.content = row.content || ''
  form.isTop = row.isTop === 1
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const data = {
          title: form.title,
          category: form.category,
          coverImg: form.coverImg,
          content: form.content,
          isTop: form.isTop ? 1 : 0
        }
        
        if (form.id) {
          await updateArticle(form.id, data)
          ElMessage.success('更新成功')
        } else {
          await createArticle(data)
          ElMessage.success('发布成功')
        }
        
        dialogVisible.value = false
        fetchArticleList()
      } catch (error) {
        console.error('操作失败:', error)
      } finally {
        saving.value = false
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteArticle(row.id)
    ElMessage.success('删除成功')
    fetchArticleList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleToggleTop = async (row) => {
  try {
    await toggleTop(row.id)
    ElMessage.success('操作成功')
    fetchArticleList()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleSizeChange = () => {
  fetchArticleList()
}

const handleCurrentChange = () => {
  fetchArticleList()
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  form.id = null
  form.title = ''
  form.category = ''
  form.coverImg = ''
  form.content = ''
  form.isTop = false
  formRef.value?.clearValidate()
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
  fetchArticleList()
})
</script>

<style scoped>
.article-manage-container {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

