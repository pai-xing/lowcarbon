<template>
  <div class="footprint-container">
    <el-card class="calculator-card">
      <template #header>
        <div class="card-header">
          <span>碳足迹计算器</span>
          <el-button type="primary" @click="showHistory = !showHistory">
            {{ showHistory ? '返回计算器' : '查看历史记录' }}
          </el-button>
        </div>
      </template>

      <!-- 计算器表单 -->
      <div v-if="!showHistory">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
          <el-form-item label="行为类型" prop="behaviorType">
            <el-select v-model="form.behaviorType" placeholder="请选择行为类型" @change="onBehaviorTypeChange">
              <el-option
                v-for="type in behaviorTypes"
                :key="type.type"
                :label="type.name"
                :value="type.type"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="具体行为" prop="behaviorName">
            <el-input v-model="form.behaviorName" placeholder="请输入具体行为描述" />
          </el-form-item>

          <el-form-item label="数据值" prop="dataValue">
            <el-input-number
              v-model="form.dataValue"
              :min="0.01"
              :precision="2"
              placeholder="请输入数据值"
            />
            <span class="unit-hint" v-if="selectedBehavior">
              {{ selectedBehavior.unit }}
            </span>
          </el-form-item>

          <el-form-item label="记录日期" prop="recordDate">
            <el-date-picker
              v-model="form.recordDate"
              type="date"
              placeholder="请选择记录日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>

          <el-form-item label="备注">
            <el-input
              v-model="form.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注信息（可选）"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm">计算并保存</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 计算结果预览 -->
        <el-alert
          v-if="calculationResult"
          :title="calculationResult.title"
          type="success"
          :description="calculationResult.description"
          show-icon
          :closable="false"
          style="margin-top: 20px"
        />

        <!-- 统计数据展示 -->
        <div class="statistics-section" v-show="statistics">
          <h3>我的碳足迹统计（最近30天）</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-statistic title="累计减排量(kg CO₂)" :value="statistics.totalReduction || 0">
                <template #suffix>kg</template>
              </el-statistic>
            </el-col>
            <el-col :span="8">
              <el-statistic title="累计获得积分" :value="statistics.totalPoints || 0">
                <template #suffix>分</template>
              </el-statistic>
            </el-col>
            <el-col :span="8">
              <el-statistic title="记录次数" :value="statistics.totalRecords || 0">
                <template #suffix>次</template>
              </el-statistic>
            </el-col>
          </el-row>
          <!-- 趋势图：每日减排与积分 -->
          <div ref="dailyChartRef" style="height: 320px; margin-top: 20px;"></div>
          <!-- 按行为类型统计图 -->
          <div ref="behaviorChartRef" style="height: 320px; margin-top: 20px;"></div>
        </div>
      </div>

      <!-- 历史记录 -->
      <div v-else>
        <div class="filter-bar">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="loadHistoryList"
          />
          <el-button type="primary" @click="loadHistoryList">查询</el-button>
        </div>

        <el-table :data="historyList" style="margin-top: 20px">
          <el-table-column prop="recordDate" label="记录日期" width="120" />
          <el-table-column prop="behaviorName" label="行为名称" width="150" />
          <el-table-column prop="dataValue" label="数据值" width="100">
            <template #default="scope">
              {{ scope.row.dataValue }}
            </template>
          </el-table-column>
          <el-table-column prop="coefficient" label="系数" width="100">
            <template #default="scope">
              {{ scope.row.coefficient }}
            </template>
          </el-table-column>
          <el-table-column prop="reductionAmount" label="减排量(kg)" width="120">
            <template #default="scope">
              <el-tag type="success">{{ scope.row.reductionAmount }} kg</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="pointsEarned" label="获得积分" width="100">
            <template #default="scope">
              <el-tag type="warning">{{ scope.row.pointsEarned }} 分</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="openEdit(scope.row)"
                style="margin-right: 8px"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(scope.row.id)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadHistoryList"
          @current-change="loadHistoryList"
          style="margin-top: 20px; justify-content: center"
        />

        <!-- 编辑记录对话框 -->
        <el-dialog v-model="editDialogVisible" title="编辑记录" width="520px">
          <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
            <el-form-item label="行为类型" prop="behaviorType">
              <el-select v-model="editForm.behaviorType" placeholder="请选择行为类型" @change="onEditBehaviorTypeChange">
                <el-option
                  v-for="type in behaviorTypes"
                  :key="type.type"
                  :label="type.name"
                  :value="type.type"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="具体行为" prop="behaviorName">
              <el-input v-model="editForm.behaviorName" placeholder="请输入具体行为描述" />
            </el-form-item>
            <el-form-item label="数据值" prop="dataValue">
              <el-input-number v-model="editForm.dataValue" :min="0.01" :precision="2" />
            </el-form-item>
            <el-form-item label="记录日期" prop="recordDate">
              <el-date-picker
                v-model="editForm.recordDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                placeholder="请选择记录日期"
              />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息（可选）" />
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="editDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="submitEdit">保存</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  getBehaviorTypes,
  createFootprint,
  getFootprintList,
  getStatistics,
  getStatisticsByBehavior,
  getDailyStatistics,
  deleteFootprint,
  updateFootprint
} from '@/api/footprint'

const formRef = ref(null)
const showHistory = ref(false)
const behaviorTypes = ref([])
const statistics = ref({ totalReduction: 0, totalPoints: 0, totalRecords: 0 })
const calculationResult = ref(null)
const historyList = ref([])
const dateRange = ref([])

// 编辑对话框与表单
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: null,
  behaviorType: '',
  behaviorName: '',
  dataValue: null,
  recordDate: '',
  remark: ''
})

const form = reactive({
  behaviorType: '',
  behaviorName: '',
  dataValue: null,
  recordDate: new Date().toISOString().split('T')[0],
  remark: ''
})

/** 根据行为类型获取单位（km/度/吨/次） */
const getUnitByType = (type) => {
  const cfg = behaviorTypes.value.find(b => b.type === type)
  return cfg ? cfg.unit : null
}

/** 创建表单的数据值校验：‘次’必须为≥1的整数，其余≥0.01 */
const validateDataValueCreate = (rule, value, callback) => {
  const unit = selectedBehavior.value?.unit
  if (value === null || value === undefined || value === '') {
    return callback(new Error('请输入数据值'))
  }
  const num = Number(value)
  if (Number.isNaN(num)) {
    return callback(new Error('数据值必须为数字'))
  }
  if (unit === '次') {
    if (!Number.isInteger(num) || num < 1) {
      return callback(new Error('“次”必须为≥1的整数'))
    }
  } else {
    if (num < 0.01) {
      return callback(new Error('数据值必须≥0.01'))
    }
  }
  return callback()
}

/** 编辑表单的数据值校验：按编辑时选择的行为类型单位校验 */
const validateDataValueEdit = (rule, value, callback) => {
  const unit = getUnitByType(editForm.behaviorType)
  if (value === null || value === undefined || value === '') {
    return callback(new Error('请输入数据值'))
  }
  const num = Number(value)
  if (Number.isNaN(num)) {
    return callback(new Error('数据值必须为数字'))
  }
  if (unit === '次') {
    if (!Number.isInteger(num) || num < 1) {
      return callback(new Error('“次”必须为≥1的整数'))
    }
  } else {
    if (num < 0.01) {
      return callback(new Error('数据值必须≥0.01'))
    }
  }
  return callback()
}

const rules = {
  behaviorType: [
    { required: true, message: '请选择行为类型', trigger: 'change' }
  ],
  behaviorName: [
    { required: true, message: '请输入具体行为描述', trigger: 'blur' }
  ],
  dataValue: [
    { validator: validateDataValueCreate, trigger: ['blur', 'change'] }
  ],
  recordDate: [
    { required: true, message: '请选择记录日期', trigger: 'change' }
  ]
}

const editRules = {
  behaviorType: [
    { required: true, message: '请选择行为类型', trigger: 'change' }
  ],
  behaviorName: [
    { required: true, message: '请输入具体行为描述', trigger: 'blur' }
  ],
  dataValue: [
    { validator: validateDataValueEdit, trigger: ['blur', 'change'] }
  ],
  recordDate: [
    { required: true, message: '请选择记录日期', trigger: 'change' }
  ]
}

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 图表容器与实例
const dailyChartRef = ref(null)
const behaviorChartRef = ref(null)
let dailyChart = null
let behaviorChart = null

// 加载每日趋势统计并渲染图表
const loadDailyStatistics = async () => {
  try {
    const res = await getDailyStatistics({})
    if (res.code === 200) {
      const list = res.data || []
      const dates = list.map(item => item.recordDate)
      const reductions = list.map(item => Number(item.dailyReduction || 0))
      const points = list.map(item => Number(item.dailyPoints || 0))

      if (!dailyChart && dailyChartRef.value) {
        dailyChart = echarts.init(dailyChartRef.value)
      }
      if (dailyChart) {
        dailyChart.setOption({
          title: { text: '每日减排与积分趋势' },
          tooltip: { trigger: 'axis' },
          legend: { data: ['减排(kg)', '积分'] },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'category', data: dates },
          yAxis: [
            { type: 'value', name: '减排(kg)' },
            { type: 'value', name: '积分' }
          ],
          series: [
            { name: '减排(kg)', type: 'line', yAxisIndex: 0, data: reductions, smooth: true },
            { name: '积分', type: 'line', yAxisIndex: 1, data: points, smooth: true }
          ]
        })
      }
    }
  } catch (e) {
    console.error('加载每日统计失败', e)
  }
}

// 加载按行为类型统计并渲染图表
const loadBehaviorStatistics = async () => {
  try {
    const res = await getStatisticsByBehavior({})
    if (res.code === 200) {
      const list = res.data || []
      const labels = list.map(item => item.behaviorName || item.behavior_type)
      const reductions = list.map(item => Number(item.totalReduction || 0))
      const counts = list.map(item => Number(item.count || 0))

      if (!behaviorChart && behaviorChartRef.value) {
        behaviorChart = echarts.init(behaviorChartRef.value)
      }
      if (behaviorChart) {
        behaviorChart.setOption({
          title: { text: '按行为类型统计' },
          tooltip: { trigger: 'axis' },
          legend: { data: ['总减排(kg)', '次数'] },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'category', data: labels },
          yAxis: [
            { type: 'value', name: '总减排(kg)' },
            { type: 'value', name: '次数' }
          ],
          series: [
            { name: '总减排(kg)', type: 'bar', yAxisIndex: 0, data: reductions },
            { name: '次数', type: 'bar', yAxisIndex: 1, data: counts }
          ]
        })
      }
    }
  } catch (e) {
    console.error('加载行为统计失败', e)
  }
}

// 窗口自适应
const handleResize = () => {
  if (dailyChart) dailyChart.resize()
  if (behaviorChart) behaviorChart.resize()
}
window.addEventListener('resize', handleResize)

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (dailyChart) {
    dailyChart.dispose()
    dailyChart = null
  }
  if (behaviorChart) {
    behaviorChart.dispose()
    behaviorChart = null
  }
})

// 统计对象加载后，确保图表容器可见再初始化/调整尺寸
watch(statistics, (val) => {
  if (val) {
    nextTick(() => {
      if (dailyChartRef.value) {
        if (!dailyChart) dailyChart = echarts.init(dailyChartRef.value)
        dailyChart.resize()
      }
      if (behaviorChartRef.value) {
        if (!behaviorChart) behaviorChart = echarts.init(behaviorChartRef.value)
        behaviorChart.resize()
      }
    })
  }
})

// 当前选中的行为类型配置
const selectedBehavior = computed(() => {
  return behaviorTypes.value.find(b => b.type === form.behaviorType)
})

// 加载行为类型配置
const loadBehaviorTypes = async () => {
  try {
    const res = await getBehaviorTypes()
    if (res.code === 200) {
      // 将Map格式转换为数组格式
      behaviorTypes.value = Object.entries(res.data).map(([type, config]) => ({
        type,
        name: config.name,
        unit: config.unit,
        description: config.description,
        coefficient: config.coefficient
      }))
    }
  } catch (error) {
    ElMessage.error('加载行为类型失败')
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getStatistics({})
    if (res.code === 200) {
      statistics.value = res.data || { totalReduction: 0, totalPoints: 0, totalRecords: 0 }
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
    statistics.value = { totalReduction: 0, totalPoints: 0, totalRecords: 0 }
  }
}

// 加载历史记录
const loadHistoryList = async () => {
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const res = await getFootprintList(params)
    if (res.code === 200) {
      historyList.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载历史记录失败')
  }
}

// 行为类型改变时
const onBehaviorTypeChange = () => {
  if (selectedBehavior.value) {
    form.behaviorName = selectedBehavior.value.name
  }
}

// 打开编辑对话框
const openEdit = (row) => {
  editForm.id = row.id
  editForm.behaviorType = row.behaviorType
  editForm.behaviorName = row.behaviorName
  editForm.dataValue = Number(row.dataValue)
  editForm.recordDate = row.recordDate
  editForm.remark = row.remark || ''
  editDialogVisible.value = true
}

// 编辑时行为类型改变，同步名称为默认名称（可手动再改）
const onEditBehaviorTypeChange = () => {
  const cfg = behaviorTypes.value.find(b => b.type === editForm.behaviorType)
  if (cfg) editForm.behaviorName = cfg.name
}

// 提交编辑
const submitEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const payload = {
        behaviorType: editForm.behaviorType,
        behaviorName: editForm.behaviorName,
        dataValue: editForm.dataValue,
        recordDate: editForm.recordDate,
        remark: editForm.remark
      }
      const res = await updateFootprint(editForm.id, payload)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        // 刷新列表、统计与图表
        loadHistoryList()
        loadStatistics()
        loadDailyStatistics()
        loadBehaviorStatistics()
      }
    } catch (e) {
      ElMessage.error(e?.response?.data?.msg || '更新失败')
    }
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await createFootprint(form)
        if (res.code === 200) {
          const data = res.data
          calculationResult.value = {
            title: '计算成功！',
            description: `本次行为减排 ${data.reductionAmount} kg CO₂，获得 ${data.pointsEarned} 积分`
          }
          ElMessage.success('保存成功')
          
          // 重置表单并刷新统计数据
          resetForm()
          loadStatistics()
          
          // 3秒后清除计算结果提示
          setTimeout(() => {
            calculationResult.value = null
          }, 3000)
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.msg || '保存失败')
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.recordDate = new Date().toISOString().split('T')[0]
  calculationResult.value = null
}

// 删除记录
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？删除后将扣除对应的积分。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteFootprint(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadHistoryList()
      loadStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.msg || '删除失败')
    }
  }
}

onMounted(() => {
  loadBehaviorTypes()
  loadStatistics()
  loadDailyStatistics()
  loadBehaviorStatistics()
})
</script>

<style scoped>
.footprint-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.calculator-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unit-hint {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.statistics-section {
  margin-top: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.statistics-section h3 {
  margin-bottom: 20px;
  color: #303133;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.el-statistic {
  text-align: center;
}
</style>
