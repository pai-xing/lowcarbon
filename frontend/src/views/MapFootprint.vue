<template>
  <div class="map-footprint">
    <el-card class="section-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>地图足迹</span>
          <div class="tools">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="loadPoints"
            />
            <el-button type="primary" :icon="Refresh" @click="loadPoints">刷新</el-button>
          </div>
        </div>
      </template>

      <div class="quick-filters">
        <el-button-group>
          <el-button 
            :type="isToday ? 'primary' : ''"
            @click="showToday"
          >
            今天
          </el-button>
          <el-button 
            :type="isWeek ? 'primary' : ''"
            @click="showWeek"
          >
            最近一周
          </el-button>
        </el-button-group>
      </div>

      <div class="map-container" v-loading="loading">
        <div v-if="amapReady" id="amap-container" class="map"></div>
        <el-empty v-else description="地图SDK未加载，显示列表视图">
          <template #description>
            <span>无法加载地图时，仍可查看位置列表。</span>
          </template>
        </el-empty>
      </div>

      <div class="list-container">
        <el-table :data="points" size="small" stripe>
          <el-table-column prop="recordDate" label="日期" width="120" />
          <el-table-column prop="behaviorName" label="行为" width="160" />
          <el-table-column prop="address" label="地址" min-width="200" />
          <el-table-column label="坐标" width="220">
            <template #default="scope">
              <span v-if="scope.row.latitude && scope.row.longitude">
                {{ scope.row.latitude }}, {{ scope.row.longitude }}
              </span>
              <el-tag v-else type="info" effect="plain">无坐标</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 地图足迹页面
 * - 加载用户足迹点位（带经纬度/地址/行为/日期）
 * - 使用高德地图绘制点标记，标记上显示行为文字
 * - 默认显示今天的数据，可查看最近一周
 */
import { onMounted, ref, onBeforeUnmount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getMapPoints } from '@/api/footprint'

// 高德地图API Key
const AMAP_KEY = '1eef1f213f29b72ff7c89dca6c05e9f4'
const AMAP_VERSION = '2.0'

const points = ref([])
const dateRange = ref([])
const loading = ref(false)
const amapReady = ref(false)
let map = null
let markers = []

// 判断当前是否显示今天
const isToday = computed(() => {
  if (!Array.isArray(dateRange.value) || dateRange.value.length !== 2) return false
  return dateRange.value[0] === dateRange.value[1] && dateRange.value[0] === getTodayStr()
})

// 判断当前是否显示最近一周
const isWeek = computed(() => {
  if (!Array.isArray(dateRange.value) || dateRange.value.length !== 2) return false
  const weekRange = getWeekRange()
  return dateRange.value[0] === weekRange[0] && dateRange.value[1] === weekRange[1]
})

function getTodayStr () {
  const today = new Date()
  const y = today.getFullYear()
  const m = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function getDefaultRange () {
  // 默认显示今天的数据
  const todayStr = getTodayStr()
  return [todayStr, todayStr]
}

function getWeekRange () {
  // 获取最近一周的日期范围（包括今天）
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 6) // 7天包括今天
  const fmt = (d) => {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }
  return [fmt(start), fmt(end)]
}

function showToday () {
  dateRange.value = getDefaultRange()
  loadPoints()
}

function showWeek () {
  dateRange.value = getWeekRange()
  loadPoints()
}

function loadAmapScript () {
  return new Promise((resolve, reject) => {
    if (window.AMap) {
      amapReady.value = true
      resolve(true)
      return
    }

    window._AMapSecurityConfig = {
      securityJsCode: '' // 如果需要安全密钥，请在此配置
    }

    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=${AMAP_VERSION}&key=${AMAP_KEY}`
    script.async = true
    script.onload = () => {
      amapReady.value = true
      resolve(true)
    }
    script.onerror = () => {
      amapReady.value = false
      ElMessage.error('高德地图加载失败')
      reject(new Error('高德地图加载失败'))
    }
    document.head.appendChild(script)
  })
}

function initMap () {
  if (!amapReady.value || map) return

  try {
    map = new window.AMap.Map('amap-container', {
      zoom: 12, // 初始缩放级别（提高以便更好地查看标签）
      center: [116.397428, 39.90923], // 初始中心点（北京）
      viewMode: '2D', // 使用2D模式
      resizeEnable: true
    })
  } catch (error) {
    console.error('地图初始化失败', error)
    ElMessage.error('地图初始化失败')
  }
}

function clearMarkers () {
  if (markers.length > 0) {
    map.remove(markers)
    markers = []
  }
}

function renderMarkers () {
  if (!amapReady.value || !map) return

  clearMarkers()

  // 更严格的坐标校验，避免出现 NaN/越界经纬度
  const validPoints = points.value.filter(p => {
    const latOk = typeof p.latitude === 'number' && !isNaN(p.latitude) && Math.abs(p.latitude) <= 90
    const lngOk = typeof p.longitude === 'number' && !isNaN(p.longitude) && Math.abs(p.longitude) <= 180
    return latOk && lngOk
  })

  if (validPoints.length === 0) {
    ElMessage.info('暂无有效坐标点')
    return
  }

  validPoints.forEach(p => {
    const position = [p.longitude, p.latitude] // 高德地图使用 [lng, lat] 格式
    const behaviorText = p.behaviorName || p.behaviorType || '足迹'

    // 创建带文字标签的标记
    const marker = new window.AMap.Marker({
      position,
      title: behaviorText,
      map
    })

    // 单独设置标签以避免构造函数中的问题
    try {
      marker.setLabel({
        content: `<div class="marker-label">${behaviorText}</div>`,
        direction: 'top'
      })
    } catch (error) {
      console.error('设置标签失败:', error)
    }

    // 创建信息窗体内容
    const infoContent = `
      <div style="padding: 10px; min-width: 200px;">
        <div style="font-weight: bold; margin-bottom: 8px; font-size: 14px;">
          ${behaviorText}
        </div>
        <div style="margin-bottom: 5px; color: #666;">
          <span style="color: #999;">日期：</span>${p.recordDate || ''}
        </div>
        ${p.address ? `
          <div style="margin-bottom: 5px; color: #666;">
            <span style="color: #999;">地址：</span>${p.address}
          </div>
        ` : ''}
        <div style="color: #999; font-size: 12px;">
          坐标：${p.latitude}, ${p.longitude}
        </div>
      </div>
    `

    // 创建信息窗体（固定像素偏移值）
    const infoWindow = new window.AMap.InfoWindow({
      content: infoContent,
      offset: new window.AMap.Pixel(0, -30)
    })

    // 点击标记时显示信息窗体
    marker.on('click', () => {
      infoWindow.open(map, marker.getPosition())
    })

    markers.push(marker)
  })

  // 使用 setFitView 以覆盖所有标记，避免构造空/非法 Bounds 导致的 Pixel(NaN, NaN)
  try {
    if (markers.length > 0 && typeof map.setFitView === 'function') {
      // 参数: 覆盖物数组、是否立即过渡、四周留白像素、最大缩放级别
      map.setFitView(markers, true, [50, 50, 50, 50], 16)
    } else {
      // 兜底：将镜头移至第一个标记
      map.setZoomAndCenter(12, markers[0].getPosition())
    }
  } catch (err) {
    console.error('fitView 失败，使用备用方式', err)
    try {
      map.setZoomAndCenter(12, markers[0].getPosition())
    } catch (e2) {
      console.error('备用定位失败', e2)
    }
  }
}

async function loadPoints () {
  try {
    loading.value = true
    const params = {}
    if (Array.isArray(dateRange.value) && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getMapPoints(params)
    const data = res?.data || res || []
    
    console.log('API返回的原始数据:', data)
    
    // 标准化数据，确保坐标是有效的数字类型
    points.value = (Array.isArray(data) ? data : []).map(d => {
      // 处理可能是字符串或对象的情况
      let lat = d.latitude
      let lng = d.longitude
      
      console.log(`原始坐标类型 - lat类型: ${typeof lat}, lng类型: ${typeof lng}`)
      console.log(`原始坐标值 - lat:`, lat, `, lng:`, lng)
      
      // 如果是对象（BigDecimal），尝试获取其值
      if (lat && typeof lat === 'object') {
        console.log('lat是对象，尝试转换为字符串')
        lat = lat.toString ? lat.toString() : String(lat)
      }
      if (lng && typeof lng === 'object') {
        console.log('lng是对象，尝试转换为字符串')
        lng = lng.toString ? lng.toString() : String(lng)
      }
      
      // 转换为数字
      const latNum = parseFloat(lat)
      const lngNum = parseFloat(lng)
      
      console.log(`处理坐标 - 原始: lat=${lat}, lng=${lng}, 转换后: lat=${latNum}, lng=${lngNum}, 是否有效: ${!isNaN(latNum) && !isNaN(lngNum)}`)
      
      return {
        ...d,
        latitude: !isNaN(latNum) && latNum !== null ? latNum : null,
        longitude: !isNaN(lngNum) && lngNum !== null ? lngNum : null
      }
    })
    
    console.log('处理后的数据:', points.value)
    console.log('有效坐标点数量:', points.value.filter(p => 
      p.latitude !== null && 
      p.longitude !== null && 
      typeof p.latitude === 'number' && 
      typeof p.longitude === 'number'
    ).length)
    
    renderMarkers()
  } catch (e) {
    console.error('加载地图点位失败', e)
    ElMessage.error(e?.message || '加载地图点位失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (!Array.isArray(dateRange.value) || dateRange.value.length !== 2) {
    dateRange.value = getDefaultRange()
  }
  
  try {
    await loadAmapScript()
    initMap()
    await loadPoints()
  } catch (error) {
    console.error('初始化失败', error)
  }
})

onBeforeUnmount(() => {
  if (map) {
    map.destroy()
    map = null
  }
  markers = []
})
</script>

<style scoped>
.map-footprint {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-card {
  width: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tools {
  display: flex;
  gap: 8px;
  align-items: center;
}

.quick-filters {
  margin-top: 12px;
  padding: 0 0 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.map-container {
  margin-top: 12px;
}

.map {
  width: 100%;
  height: 500px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
}

.list-container {
  margin-top: 16px;
}

/* 高德地图信息窗体样式优化 */
:deep(.amap-info-content) {
  padding: 0;
}

/* 地图标签样式 */
:deep(.marker-label) {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #409EFF;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 13px;
  font-weight: bold;
  color: #409EFF;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

:deep(.amap-marker-label) {
  border: none;
  background: transparent;
}
</style>
