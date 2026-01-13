<template>
  <div class="checkin-page">
    <el-row :gutter="16">
      <!-- 预设“次”类行为按钮区 -->
      <el-col :xs="24" :md="12">
        <el-card class="section-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日打卡</span>
              <el-tooltip content="仅支持单位为“次”的行为；同日同行为仅一次" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <div class="behavior-buttons">
            <el-button
              v-for="b in presetBehaviors"
              :key="b.type"
              type="primary"
              :icon="b.icon"
              plain
              :disabled="todayTypes.has(b.type) || checkinLoading[b.type]"
              :loading="checkinLoading[b.type]"
              @click="doCheckin(b.type)"
            >
              {{ b.name }}
            </el-button>
          </div>
          <div class="remark-input">
            <el-input
              v-model="remark"
              placeholder="备注(可选)"
              clearable
              maxlength="100"
              show-word-limit
            />
          </div>
          <div v-if="todayTypes.size > 0" class="today-types">
            <el-tag v-for="t in Array.from(todayTypes)" :key="t" type="success" effect="plain" class="type-tag">
              今日已打卡：{{ typeNameMap[t] || t }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 打卡统计卡片 -->
      <el-col :xs="24" :md="12">
        <el-card class="section-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>打卡统计</span>
              <el-button size="small" :icon="Refresh" @click="loadStats" />
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="连续打卡天数(streak)">
              <el-tag type="warning">{{ stats.streak }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="累计打卡数(totalCheckins)">
              <el-tag type="info">{{ stats.totalCheckins }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="本月达成率(monthlyRate)">
              <el-progress :percentage="Math.round(stats.monthlyRate * 100)" :stroke-width="16" />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 打卡日历 -->
    <el-card class="section-card calendar-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>打卡日历</span>
          <div class="calendar-controls">
            <el-date-picker
              v-model="monthPicker"
              type="month"
              placeholder="选择月份"
              value-format="YYYY-MM"
              @change="handleMonthChange"
              style="width: 160px"
            />
            <el-button size="small" :icon="Refresh" @click="refreshCalendar" />
          </div>
        </div>
      </template>

      <div v-loading="calendarLoading">
        <div class="calendar-grid">
          <div class="calendar-weekday" v-for="w in weekdays" :key="w">{{ w }}</div>

          <!-- 空白占位（当月第一天的星期偏移） -->
          <div v-for="i in firstWeekdayOffset" :key="'offset-' + i" class="calendar-cell empty"></div>

          <!-- 每日格子 -->
          <div v-for="day in daysInMonth" :key="'day-' + day" class="calendar-cell">
            <div class="cell-header">
              <span class="day-number">{{ day }}</span>
              <el-tag
                v-if="calendarMap.has(day)"
                size="small"
                type="success"
                effect="plain"
              >已打卡 × {{ calendarMap.get(day).count }}</el-tag>
            </div>
            <div class="cell-body">
              <el-space wrap size="4">
                <el-tag
                  v-for="t in (calendarMap.get(day)?.types || [])"
                  :key="day + '-' + t"
                  size="small"
                  effect="dark"
                >{{ typeNameMap[t] || t }}</el-tag>
              </el-space>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 打卡页面
 * - 支持单位为“次”的行为一键打卡；幂等：同日同行为仅一次
 * - 展示打卡统计(streak/totalCheckins/monthlyRate)
 * - 展示按月打卡日历(每日次数、行为类型列表)
 */
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, Refresh, ForkSpoon, Delete, TakeawayBox, ShoppingBag } from '@element-plus/icons-vue'
import { checkin, getCalendar, getCheckinStats } from '../api/checkin'

// 预设单位为“次”的行为
const typeNameMap = {
  VEGETARIAN: '素食',
  REDUCE_WASTE: '减少浪费',
  RECYCLE: '垃圾分类',
  REUSE_BAG: '环保袋'
}
const presetBehaviors = [
  { type: 'VEGETARIAN', name: typeNameMap.VEGETARIAN, icon: ForkSpoon },
  { type: 'REDUCE_WASTE', name: typeNameMap.REDUCE_WASTE, icon: Delete },
  { type: 'RECYCLE', name: typeNameMap.RECYCLE, icon: TakeawayBox },
  { type: 'REUSE_BAG', name: typeNameMap.REUSE_BAG, icon: ShoppingBag }
]

// 备注
const remark = ref('')
// 今日已打卡的行为类型集合（从当月日历中取今日）
const todayTypes = reactive(new Set())

// 统计
const stats = reactive({
  streak: 0,
  totalCheckins: 0,
  monthlyRate: 0
})

// 月份选择
const monthPicker = ref('')
const current = new Date()
const yyyyMM = `${current.getFullYear()}-${String(current.getMonth() + 1).padStart(2, '0')}`
monthPicker.value = yyyyMM

// 日历数据
const calendarLoading = ref(false)
const calendarData = ref([]) // [{ record_date: 'YYYY-MM-DD', count: n, types: 'A,B' }]
const calendarMap = computed(() => {
  const map = new Map()
  calendarData.value.forEach(d => {
    const day = Number(d.record_date.split('-')[2])
    const types = (d.types || '').split(',').filter(Boolean)
    map.set(day, { count: d.count, types })
  })
  // 更新今日类型集合
  todayTypes.clear()
  const todayStr = getTodayStr()
  const today = calendarData.value.find(d => d.record_date === todayStr)
  if (today) {
    (today.types || '').split(',').filter(Boolean).forEach(t => todayTypes.add(t))
  }
  return map
})

// 加载状态
const checkinLoading = reactive({
  VEGETARIAN: false,
  REDUCE_WASTE: false,
  RECYCLE: false,
  REUSE_BAG: false
})

// 星期标题
const weekdays = ['一', '二', '三', '四', '五', '六', '日']

// 当月天数与第一天星期偏移
const daysInMonth = computed(() => {
  const [y, m] = monthPicker.value.split('-').map(Number)
  return new Date(y, m, 0).getDate()
})
const firstWeekdayOffset = computed(() => {
  const [y, m] = monthPicker.value.split('-').map(Number)
  const firstDay = new Date(y, m - 1, 1).getDay() // 0(日)~6(六)
  // 我们的列头从周一开始，因此需要将周日视为偏移 6
  const offset = firstDay === 0 ? 6 : firstDay - 1
  return offset
})

function getTodayStr () {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

async function loadCalendar () {
  try {
    calendarLoading.value = true
    const res = await getCalendar({ month: monthPicker.value })
    // 期望结构：[{ record_date, count, types }]
    calendarData.value = Array.isArray(res?.data) ? res.data : res
  } catch (e) {
    console.error('加载日历失败', e)
    ElMessage.error(e?.message || '加载日历失败')
  } finally {
    calendarLoading.value = false
  }
}

async function loadStats () {
  try {
    const res = await getCheckinStats()
    const data = res?.data || res
    stats.streak = data?.streak ?? 0
    stats.totalCheckins = data?.totalCheckins ?? 0
    stats.monthlyRate = data?.monthlyRate ?? 0
  } catch (e) {
    console.error('加载统计失败', e)
    ElMessage.error(e?.message || '加载统计失败')
  }
}

function handleMonthChange () {
  refreshCalendar()
}

async function refreshCalendar () {
  await loadCalendar()
}

async function doCheckin (behaviorType) {
  if (todayTypes.has(behaviorType)) {
    ElMessage.warning('今日已完成该行为打卡')
    return
  }
  checkinLoading[behaviorType] = true
  try {
    await checkin({ behaviorType, remark: remark.value || undefined })
    ElMessage.success('打卡成功')
    // 刷新日历与统计
    await Promise.all([loadStats(), loadCalendar()])
  } catch (e) {
    console.error('打卡失败', e)
    ElMessage.error(e?.message || '打卡失败')
  } finally {
    checkinLoading[behaviorType] = false
  }
}

onMounted(async () => {
  await Promise.all([loadStats(), loadCalendar()])
})
</script>

<style scoped>
.checkin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  width: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.behavior-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.remark-input {
  margin-top: 12px;
}

.today-types {
  margin-top: 12px;
}

.type-tag {
  margin-right: 6px;
}

.calendar-card {
  margin-top: 8px;
}

.calendar-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-weekday {
  text-align: center;
  font-weight: 600;
  color: #606266;
}

.calendar-cell {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  min-height: 80px;
  padding: 8px;
  display: flex;
  flex-direction: column;
}

.calendar-cell.empty {
  border: none;
  min-height: 0;
  padding: 0;
}

.cell-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.day-number {
  font-weight: 600;
}

.cell-body {
  margin-top: 8px;
}
</style>
