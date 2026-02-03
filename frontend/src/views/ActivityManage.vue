<template>
  <div class="activity-manage-container">
    <div class="header">
      <h2>活动管理</h2>
      <button @click="showCreateDialog" class="create-btn">+ 创建活动</button>
    </div>

    <!-- 活动列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="activities.length === 0" class="empty">
      暂无活动，点击右上角创建新活动
    </div>

    <div v-else class="activity-list">
      <div v-for="activity in activities" :key="activity.id" class="activity-card">
        <div class="activity-header">
          <h3>{{ activity.title }}</h3>
          <div class="status-badge" :class="'status-' + activity.status">
            {{ getStatusText(activity.status) }}
          </div>
        </div>
        
        <p class="description">{{ activity.description }}</p>
        
        <div class="activity-info">
          <div class="info-item">
            <span class="label">开始时间：</span>
            <span>{{ formatDateTime(activity.startTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">结束时间：</span>
            <span>{{ formatDateTime(activity.endTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">目标减排：</span>
            <span>{{ activity.targetReduction }} kg</span>
          </div>
          <div class="info-item">
            <span class="label">参与人数：</span>
            <span>{{ activity.joinCount || 0 }} 人</span>
          </div>
        </div>

        <div class="actions">
          <button @click="editActivity(activity)" class="btn-edit">编辑</button>
          <button @click="deleteActivity(activity.id)" class="btn-delete">删除</button>
          <button @click="viewRanking(activity.id)" class="btn-view">查看排行</button>
        </div>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <div v-if="dialogVisible" class="dialog-overlay" @click="closeDialog">
      <div class="dialog" @click.stop>
        <div class="dialog-header">
          <h3>{{ editingActivity ? '编辑活动' : '创建活动' }}</h3>
          <button @click="closeDialog" class="close-btn">×</button>
        </div>

        <div class="dialog-body">
          <div class="form-group">
            <label>活动标题 *</label>
            <input 
              v-model="formData.title" 
              type="text" 
              placeholder="请输入活动标题"
              maxlength="100"
            />
          </div>

          <div class="form-group">
            <label>活动描述 *</label>
            <textarea 
              v-model="formData.description" 
              placeholder="请输入活动描述"
              rows="4"
              maxlength="500"
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>开始时间 *</label>
              <input v-model="formData.startTime" type="datetime-local" />
            </div>

            <div class="form-group">
              <label>结束时间 *</label>
              <input v-model="formData.endTime" type="datetime-local" />
            </div>
          </div>

          <div class="form-group">
            <label>目标减排量 (kg)</label>
            <input 
              v-model.number="formData.targetReduction" 
              type="number" 
              placeholder="0表示无目标"
              min="0"
              step="0.01"
            />
          </div>

          <div class="form-group">
            <label>活动状态</label>
            <select v-model.number="formData.status">
              <option :value="0">未开始</option>
              <option :value="1">进行中</option>
              <option :value="2">已结束</option>
            </select>
          </div>
        </div>

        <div class="dialog-footer">
          <button @click="closeDialog" class="btn-cancel">取消</button>
          <button @click="submitForm" class="btn-submit">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getActivityList, createActivity, updateActivity, deleteActivity as deleteActivityAPI } from '../api/activity'
import { useRouter } from 'vue-router'

const router = useRouter()

const activities = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingActivity = ref(null)

const formData = ref({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  targetReduction: 0,
  status: 0
})

// 加载活动列表
const loadActivities = async () => {
  loading.value = true
  try {
    const res = await getActivityList({})
    activities.value = res.data || []
  } catch (error) {
    console.error('加载活动列表失败', error)
    alert('加载失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  editingActivity.value = null
  formData.value = {
    title: '',
    description: '',
    startTime: '',
    endTime: '',
    targetReduction: 0,
    status: 0
  }
  dialogVisible.value = true
}

// 编辑活动
const editActivity = (activity) => {
  editingActivity.value = activity
  formData.value = {
    id: activity.id,
    title: activity.title,
    description: activity.description,
    startTime: formatDateTimeForInput(activity.startTime),
    endTime: formatDateTimeForInput(activity.endTime),
    targetReduction: activity.targetReduction,
    status: activity.status
  }
  dialogVisible.value = true
}

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false
  editingActivity.value = null
}

// 提交表单
const submitForm = async () => {
  // 验证
  if (!formData.value.title || !formData.value.description) {
    alert('请填写必填项')
    return
  }

  if (!formData.value.startTime || !formData.value.endTime) {
    alert('请选择活动时间')
    return
  }

  if (new Date(formData.value.startTime) >= new Date(formData.value.endTime)) {
    alert('结束时间必须大于开始时间')
    return
  }

  try {
    const data = {
      ...formData.value,
      startTime: formData.value.startTime,
      endTime: formData.value.endTime
    }

    if (editingActivity.value) {
      await updateActivity(data)
      alert('更新成功')
    } else {
      await createActivity(data)
      alert('创建成功')
    }

    closeDialog()
    loadActivities()
  } catch (error) {
    console.error('操作失败', error)
    alert('操作失败：' + (error.message || '未知错误'))
  }
}

// 删除活动
const deleteActivity = async (id) => {
  if (!confirm('确定要删除这个活动吗？')) {
    return
  }

  try {
    await deleteActivityAPI(id)
    alert('删除成功')
    loadActivities()
  } catch (error) {
    console.error('删除失败', error)
    alert('删除失败：' + (error.message || '未知错误'))
  }
}

// 查看排行榜
const viewRanking = (activityId) => {
  router.push({
    path: '/leaderboard',
    query: { type: 'activity', activityId }
  })
}

// 格式化时间显示
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化时间用于输入框
const formatDateTimeForInput = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return statusMap[status] || '未知'
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.activity-manage-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h2 {
  font-size: 24px;
  color: #2c3e50;
}

.create-btn {
  padding: 10px 20px;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.3s;
}

.create-btn:hover {
  background: #45a049;
}

/* 活动列表 */
.activity-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.activity-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.activity-header h3 {
  font-size: 18px;
  color: #2c3e50;
  flex: 1;
  margin-right: 10px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-0 {
  background: #e3f2fd;
  color: #1976d2;
}

.status-1 {
  background: #e8f5e9;
  color: #4caf50;
}

.status-2 {
  background: #fce4ec;
  color: #e91e63;
}

.description {
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-item {
  display: flex;
  font-size: 13px;
}

.info-item .label {
  color: #7f8c8d;
  min-width: 80px;
}

.actions {
  display: flex;
  gap: 10px;
}

.actions button {
  flex: 1;
  padding: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.btn-edit {
  background: #2196F3;
  color: white;
}

.btn-edit:hover {
  background: #1976D2;
}

.btn-delete {
  background: #f44336;
  color: white;
}

.btn-delete:hover {
  background: #d32f2f;
}

.btn-view {
  background: #FF9800;
  color: white;
}

.btn-view:hover {
  background: #F57C00;
}

/* 对话框 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.dialog-header h3 {
  font-size: 20px;
  color: #2c3e50;
}

.close-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #7f8c8d;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: #2c3e50;
}

.dialog-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 500;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #4CAF50;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #eee;
}

.btn-cancel,
.btn-submit {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-submit {
  background: #4CAF50;
  color: white;
}

.btn-submit:hover {
  background: #45a049;
}

/* 加载和空状态 */
.loading, .empty {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
  font-size: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .activity-list {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .dialog {
    width: 95%;
  }
}
</style>
