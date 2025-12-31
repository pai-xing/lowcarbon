<template>
  <div class="publish-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><EditPen /></el-icon>
          <span>发布科普文章</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="top"
      >
        <el-form-item label="文章标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入文章标题（建议20字以内）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="文章分类" prop="category">
          <el-select
            v-model="form.category"
            placeholder="请选择文章分类"
            style="width: 100%"
          >
            <el-option label="环保知识" value="环保知识">
              <el-icon><Document /></el-icon>
              <span style="margin-left: 8px">环保知识</span>
            </el-option>
            <el-option label="低碳生活" value="低碳生活">
              <el-icon><House /></el-icon>
              <span style="margin-left: 8px">低碳生活</span>
            </el-option>
            <el-option label="节能减排" value="节能减排">
              <el-icon><Sunny /></el-icon>
              <span style="margin-left: 8px">节能减排</span>
            </el-option>
            <el-option label="绿色出行" value="绿色出行">
              <el-icon><Van /></el-icon>
              <span style="margin-left: 8px">绿色出行</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="封面图片">
          <el-input
            v-model="form.coverImg"
            placeholder="请输入封面图片URL（选填）"
          >
            <template #prepend>
              <el-icon><Picture /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>建议尺寸：800x450，支持jpg、png格式</span>
          </div>
          <div v-if="form.coverImg" class="image-preview">
            <img :src="form.coverImg" alt="封面预览" @error="handleImageError" />
          </div>
        </el-form-item>

        <el-form-item label="文章内容" prop="content">
          <div ref="editorRef" class="editor-container"></div>
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>支持富文本编辑，可插入图片、设置格式等</span>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="button-group">
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
              <el-icon><Upload /></el-icon>
              <span>发布文章</span>
            </el-button>
            <el-button size="large" @click="handleReset">
              <el-icon><RefreshLeft /></el-icon>
              <span>重置</span>
            </el-button>
            <el-button size="large" @click="handlePreview">
              <el-icon><View /></el-icon>
              <span>预览</span>
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="文章预览"
      width="900px"
      top="5vh"
    >
      <div class="preview-content">
        <h2 class="preview-title">{{ form.title }}</h2>
        <div class="preview-meta">
          <el-tag v-if="form.category" type="primary" size="small">{{ form.category }}</el-tag>
          <span class="preview-date">{{ new Date().toLocaleDateString('zh-CN') }}</span>
        </div>
        <div v-if="form.coverImg" class="preview-cover">
          <img :src="form.coverImg" alt="封面" />
        </div>
        <div class="preview-body" v-html="form.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  EditPen, Upload, RefreshLeft, View, Picture, InfoFilled,
  Document, House, Sunny, Van
} from '@element-plus/icons-vue'
import { createArticle } from '../api/article'
import { createEditor, createToolbar } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'

const router = useRouter()
const formRef = ref(null)
const editorRef = ref(null)
const submitting = ref(false)
const previewVisible = ref(false)

let editor = null

const form = ref({
  title: '',
  category: '',
  coverImg: '',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度应在5-100字符之间', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择文章分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' },
    { min: 50, message: '文章内容至少需要50个字符', trigger: 'blur' }
  ]
}

const initEditor = () => {
  if (!editorRef.value) return

  // 编辑器配置
  const editorConfig = {
    placeholder: '请输入文章内容...',
    MENU_CONF: {
      uploadImage: {
        // 自定义上传图片
        customUpload: async (file, insertFn) => {
          // 这里可以实现真实的图片上传
          // 暂时使用base64
          const reader = new FileReader()
          reader.onload = (e) => {
            insertFn(e.target.result, file.name, '')
          }
          reader.readAsDataURL(file)
        }
      }
    },
    onChange: (editor) => {
      form.value.content = editor.getHtml()
    }
  }

  // 工具栏配置
  const toolbarConfig = {
    excludeKeys: ['group-video', 'fullScreen']
  }

  // 创建编辑器
  editor = createEditor({
    selector: editorRef.value,
    html: form.value.content,
    config: editorConfig,
    mode: 'default'
  })

  // 创建工具栏
  createToolbar({
    editor,
    selector: editorRef.value,
    config: toolbarConfig,
    mode: 'default'
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    if (!form.value.content || form.value.content.trim() === '<p><br></p>') {
      ElMessage.warning('请输入文章内容')
      return
    }

    try {
      await ElMessageBox.confirm(
        '确认发布这篇文章吗？发布后其他用户将可以看到。',
        '确认发布',
        {
          confirmButtonText: '确定发布',
          cancelButtonText: '取消',
          type: 'info'
        }
      )

      submitting.value = true
      await createArticle({
        title: form.value.title,
        category: form.value.category,
        coverImg: form.value.coverImg || null,
        content: form.value.content,
        isTop: 0
      })

      ElMessage.success('发布成功！')
      
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push('/')
      }, 1500)
    } catch (error) {
      if (error !== 'cancel') {
        console.error('发布失败:', error)
        ElMessage.error('发布失败，请重试')
      }
    } finally {
      submitting.value = false
    }
  })
}

const handleReset = () => {
  ElMessageBox.confirm('确定要重置表单吗？所有内容将被清空。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    form.value = {
      title: '',
      category: '',
      coverImg: '',
      content: ''
    }
    if (editor) {
      editor.setHtml('')
    }
    formRef.value?.clearValidate()
    ElMessage.success('已重置')
  }).catch(() => {})
}

const handlePreview = () => {
  if (!form.value.title) {
    ElMessage.warning('请先输入文章标题')
    return
  }
  if (!form.value.content) {
    ElMessage.warning('请先输入文章内容')
    return
  }
  previewVisible.value = true
}

const handleImageError = () => {
  ElMessage.warning('封面图片加载失败，请检查URL是否正确')
}

onMounted(() => {
  // 延迟初始化编辑器，确保DOM已渲染
  setTimeout(() => {
    initEditor()
  }, 100)
})

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy()
  }
})
</script>

<style scoped>
.publish-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.form-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.image-preview {
  margin-top: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  max-width: 400px;
}

.image-preview img {
  width: 100%;
  display: block;
}

.editor-container {
  border: 1px solid #ccc;
  border-radius: 4px;
  min-height: 400px;
}

.button-group {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.preview-content {
  padding: 20px;
}

.preview-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #303133;
}

.preview-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.preview-date {
  color: #909399;
  font-size: 14px;
}

.preview-cover {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.preview-cover img {
  width: 100%;
  display: block;
}

.preview-body {
  line-height: 1.8;
  color: #606266;
  font-size: 16px;
}

.preview-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 12px 0;
}

.preview-body :deep(p) {
  margin: 12px 0;
}

.preview-body :deep(h1),
.preview-body :deep(h2),
.preview-body :deep(h3) {
  margin: 20px 0 12px;
  font-weight: 600;
}
</style>