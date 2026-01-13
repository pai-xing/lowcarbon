<template>
  <div class="edit-profile-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>编辑个人资料</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 600px; margin: 0 auto"
      >
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :key="form.avatar"
            action=""
            :show-file-list="false"
            :http-request="handleUpload"
            :before-upload="beforeAvatarUpload"
            :on-success="(_, file) => { /* 兜底：成功后强制使用本地或远端URL */ if (form.avatar) { /* 保持预览 */ } }"
            :on-error="(err) => { console.error('上传组件错误:', err) }"
            accept="image/jpeg,image/png"
          >
            <img v-if="form.avatar" :src="form.avatar" :key="form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="form-tip">点击上方区域上传头像</div>
        </el-form-item>

        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
          <div class="form-tip">用户名不可修改</div>
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="form.bio"
            type="textarea"
            :rows="4"
            placeholder="介绍一下自己吧"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存修改</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { updateUserInfo } from '../api/user'
import { uploadFile } from '../api/file'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)

const form = ref({
  username: '',
  nickname: '',
  avatar: '',
  bio: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

onMounted(() => {
  if (userStore.userInfo) {
    form.value.username = userStore.userInfo.username
    form.value.nickname = userStore.userInfo.nickname || ''
    form.value.avatar = userStore.userInfo.avatar || ''
    form.value.bio = userStore.userInfo.bio || ''
  } else {
    // 如果没有用户信息，先获取
    userStore.fetchUserInfo().then(info => {
      form.value.username = info.username
      form.value.nickname = info.nickname || ''
      form.value.avatar = info.avatar || ''
      form.value.bio = info.bio || ''
    })
  }
})

const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('头像图片必须是 JPG 或 PNG 格式!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  try {
    // 先用本地预览，确保加号区域立即切换为图片
    // 使用 FileReader 生成本地 dataURL 预览，避免 blob URL 在某些环境下出现 ERR_FILE_NOT_FOUND
    const reader = new FileReader()
    reader.onload = () => {
      form.value.avatar = reader.result
    }
    reader.readAsDataURL(options.file)

    const res = await uploadFile(options.file)
    console.debug('[upload] server response =', res)
    // 后端返回URL，可能是相对路径（/api/uploads/**）或完整URL
    const remoteUrl = typeof res.data === 'string' ? res.data : ''
    let finalUrl = remoteUrl
      ? (remoteUrl.startsWith('http') ? remoteUrl : (window.location.origin + remoteUrl))
      : (typeof form.value.avatar === 'string' ? form.value.avatar : '')

    // 加版本参数破坏缓存，确保所有组件立即刷新
    if (finalUrl) {
      const sep = finalUrl.includes('?') ? '&' : '?'
      finalUrl = `${finalUrl}${sep}v=${Date.now()}`
    }
    console.debug('[upload] final url =', finalUrl)

    // 用最终URL替换本地预览（不使用 blob 对象URL，避免跨端口或释放导致的加载问题）
    if (finalUrl) {
      form.value.avatar = finalUrl
    }

    // 通知 el-upload 成功
    if (typeof options.onSuccess === 'function') {
      options.onSuccess({ url: finalUrl }, options.file)
    }
    // 兜底：没有 onSuccess 也强制组件内部完成态
    if (!options.onSuccess) {
      console.debug('[upload] no onSuccess provided by el-upload, forcing preview state')
    }

    // 同步到 store 以便其它页面显示
    if (userStore.userInfo && finalUrl) {
      userStore.userInfo.avatar = finalUrl
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    }

    ElMessage.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
    if (typeof options.onError === 'function') {
      options.onError(error)
    }
    ElMessage.error('图片上传失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await updateUserInfo({
          nickname: form.value.nickname,
          avatar: form.value.avatar,
          bio: form.value.bio
        })
        ElMessage.success('修改成功')
        // 刷新 Store 中的用户信息并确保头像与后端一致
        const latest = await userStore.fetchUserInfo()
        if (latest?.avatar) {
          userStore.userInfo.avatar = latest.avatar
          localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
        }
        router.push('/profile')
      } catch (error) {
        console.error('修改失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped>
.edit-profile-container {
  max-width: 800px;
  margin: 0 auto;
}

.avatar-uploader {
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1;
}
</style>
