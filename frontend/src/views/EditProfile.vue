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
            action=""
            :show-file-list="false"
            :http-request="handleUpload"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
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
            rows="4"
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
    const res = await uploadFile(options.file)
    form.value.avatar = res.data
    ElMessage.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
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
        // 刷新 Store 中的用户信息
        await userStore.fetchUserInfo()
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