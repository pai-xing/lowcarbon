<template>
  <div class="user-manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名或昵称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户列表 -->
      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'">
              {{ row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column prop="totalEmissionReduction" label="减排量(kg)" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'admin'"
              :type="row.status === 1 ? 'danger' : 'success'"
              size="small"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-tag v-else type="info" size="small">管理员</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus } from '../api/user'

const loading = ref(false)
const userList = ref([])
const total = ref(0)

const queryForm = ref({
  username: '',
  status: null,
  page: 1,
  pageSize: 10
})

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: queryForm.value.page,
      pageSize: queryForm.value.pageSize
    }
    if (queryForm.value.username) {
      params.username = queryForm.value.username
    }
    if (queryForm.value.status !== null && queryForm.value.status !== '') {
      params.status = queryForm.value.status
    }

    const res = await getUserList(params)
    if (res.code === 200) {
      userList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryForm.value.page = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  queryForm.value = {
    username: '',
    status: null,
    page: 1,
    pageSize: 10
  }
  fetchUserList()
}

// 分页变化
const handlePageChange = () => {
  fetchUserList()
}

// 更改用户状态
const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户"${row.nickname || row.username}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const res = await updateUserStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      fetchUserList()
    } else {
      ElMessage.error(res.msg || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更改用户状态失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-manage-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
