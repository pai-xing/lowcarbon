<template>
  <div class="publish-container">
    <div class="publish-header">
      <h1>发布动态</h1>
      <el-button @click="goBack">取消</el-button>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
      <el-form-item prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="8"
          placeholder="分享你的低碳生活心得..."
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item>
        <div class="upload-section">
          <div class="upload-tip">
            <el-icon><Picture /></el-icon>
            <span>添加图片（最多9张）</span>
          </div>
          
          <div class="image-list">
            <div
              v-for="(image, index) in imageList"
              :key="index"
              class="image-item"
            >
              <el-image :src="image" fit="cover" />
              <div class="image-mask">
                <el-icon class="delete-icon" @click="removeImage(index)">
                  <Delete />
                </el-icon>
              </div>
            </div>
            
            <el-upload
              v-if="imageList.length < 9"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              accept="image/*"
              class="image-uploader"
            >
              <div class="upload-trigger">
                <el-icon><Plus /></el-icon>
              </div>
            </el-upload>
          </div>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="handleSubmit"
          style="width: 100%"
        >
          发布
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Plus, Delete } from '@element-plus/icons-vue'
import { createPost } from '@/api/post'

const router = useRouter()

const form = ref({
  content: '',
  images: []
})

const imageList = ref([])
const submitting = ref(false)
const formRef = ref(null)

const rules = {
  content: [
    { required: true, message: '请输入动态内容', trigger: 'blur' },
    { min: 1, max: 1000, message: '内容长度在1到1000个字符', trigger: 'blur' }
  ]
}

// 上传配置
const uploadUrl = computed(() => {
  return import.meta.env.VITE_API_BASE_URL + '/api/file/upload'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

// 返回
const goBack = () => {
  router.back()
}

// 上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    imageList.value.push(response.data)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

// 移除图片
const removeImage = (index) => {
  imageList.value.splice(index, 1)
}

// 提交发布
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (!form.value.content.trim()) {
      ElMessage.warning('请输入动态内容')
      return
    }

    submitting.value = true
    
    const postData = {
      content: form.value.content,
      images: imageList.value.length > 0 ? JSON.stringify(imageList.value) : null
    }

    await createPost(postData)
    
    ElMessage.success('发布成功，等待审核')
    router.push('/community')
  } catch (error) {
    if (error !== 'validation failed') {
      ElMessage.error('发布失败')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.publish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.publish-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.upload-section {
  width: 100%;
}

.upload-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}

.image-item {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.image-item .el-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.image-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-mask {
  opacity: 1;
}

.delete-icon {
  font-size: 24px;
  color: white;
  cursor: pointer;
}

.delete-icon:hover {
  color: #f56c6c;
}

.image-uploader {
  width: 100%;
}

.upload-trigger {
  width: 150px;
  height: 150px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-trigger:hover {
  border-color: #409eff;
}

.upload-trigger .el-icon {
  font-size: 32px;
  color: #8c939d;
}

.upload-trigger:hover .el-icon {
  color: #409eff;
}
</style>
