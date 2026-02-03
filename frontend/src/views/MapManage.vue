<template>
  <div class="map-manage-container">
    <!-- 控制面板 -->
    <el-card class="control-panel">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- Tab 1: 足迹监控 -->
        <el-tab-pane label="足迹监控" name="footprint">
          <div class="tab-content">
            <el-space wrap>
              <el-button 
                type="primary" 
                :icon="Location" 
                @click="loadAllFootprints"
                :loading="footprintLoading"
              >
                加载最近7天足迹
              </el-button>
              <el-button 
                :icon="Delete" 
                @click="clearFootprintMarkers"
              >
                清除足迹标记
              </el-button>
              <el-tag v-if="footprintCount > 0" type="success">
                已加载 {{ footprintCount }} 个足迹点
              </el-tag>
            </el-space>
            <el-divider />
            <el-alert
              title="使用说明"
              type="info"
              :closable="false"
            >
              <ul>
                <li>点击"加载最近7天足迹"查看全校用户的低碳行为分布</li>
                <li>地图会自动缩放以显示所有足迹点</li>
                <li>点击标记可查看详细信息（用户名、行为类型、减排量等）</li>
                <li>足迹点数量较多时会自动聚合显示</li>
              </ul>
            </el-alert>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 地点维护 -->
        <el-tab-pane label="地点维护" name="poi">
          <div class="tab-content">
            <el-form :model="poiForm" label-width="100px" size="default">
              <el-form-item label="地点名称">
                <el-input 
                  v-model="poiForm.name" 
                  placeholder="例如：图书馆、食堂、体育馆"
                  clearable
                />
              </el-form-item>
              <el-form-item label="经度">
                <el-input 
                  v-model="poiForm.longitude" 
                  placeholder="点击地图自动填充"
                  readonly
                />
              </el-form-item>
              <el-form-item label="纬度">
                <el-input 
                  v-model="poiForm.latitude" 
                  placeholder="点击地图自动填充"
                  readonly
                />
              </el-form-item>
              <el-form-item label="地址">
                <el-input 
                  v-model="poiForm.address" 
                  placeholder="详细地址（可选）"
                  clearable
                />
              </el-form-item>
              <el-form-item label="地点类型">
                <el-select v-model="poiForm.poiType" placeholder="请选择" clearable>
                  <el-option label="教学楼" value="teaching" />
                  <el-option label="食堂" value="canteen" />
                  <el-option label="图书馆" value="library" />
                  <el-option label="体育馆" value="gym" />
                  <el-option label="宿舍" value="dormitory" />
                  <el-option label="其他" value="other" />
                </el-select>
              </el-form-item>
              <el-form-item label="描述">
                <el-input 
                  v-model="poiForm.description" 
                  type="textarea"
                  :rows="2"
                  placeholder="地点描述（可选）"
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-space>
                  <el-button 
                    type="primary" 
                    :icon="Plus"
                    @click="savePoi"
                    :loading="poiSaving"
                  >
                    保存地点
                  </el-button>
                  <el-button @click="resetPoiForm">
                    重置
                  </el-button>
                  <el-button 
                    type="success" 
                    :icon="poiPickMode ? 'Select' : 'Pointer'"
                    @click="togglePickMode"
                  >
                    {{ poiPickMode ? '点击地图中...' : '点击地图拾取坐标' }}
                  </el-button>
                </el-space>
              </el-form-item>
            </el-form>

            <el-divider />

            <!-- 已添加的地点列表 -->
            <div class="poi-list">
              <div class="list-header">
                <span class="title">已添加的地点</span>
                <el-button 
                  :icon="Refresh" 
                  size="small" 
                  @click="loadPois"
                  :loading="poisLoading"
                >
                  刷新
                </el-button>
              </div>
              <el-table 
                :data="poisList" 
                stripe 
                v-loading="poisLoading"
                max-height="300"
              >
                <el-table-column prop="name" label="地点名称" width="120" />
                <el-table-column prop="poiType" label="类型" width="80">
                  <template #default="{ row }">
                    <el-tag size="small">{{ getPoiTypeName(row.poiType) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="longitude" label="经度" width="100" />
                <el-table-column prop="latitude" label="纬度" width="100" />
                <el-table-column prop="address" label="地址" show-overflow-tooltip />
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="{ row }">
                    <el-button 
                      link 
                      type="primary" 
                      size="small"
                      @click="locatePoi(row)"
                    >
                      定位
                    </el-button>
                    <el-button 
                      link 
                      type="danger" 
                      size="small"
                      @click="handleDeletePoi(row)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 地图容器 -->
    <div class="map-container">
      <div id="map-manage" class="map"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Delete, Plus, Refresh } from '@element-plus/icons-vue'
import { getAllUsersFootprints, getAllPois, createPoi, deletePoi } from '@/api/mapManage'
import AMapLoader from '@amap/amap-jsapi-loader'

// 状态变量
const activeTab = ref('footprint')
const map = ref(null)
const footprintLoading = ref(false)
const footprintCount = ref(0)
const footprintMarkers = ref([])
const poiMarkers = ref([])
const tempMarker = ref(null) // 临时标记（拾取坐标时）
const markerCluster = ref(null) // 聚合实例

// POI相关
const poisLoading = ref(false)
const poiSaving = ref(false)
const poiPickMode = ref(false)
const poisList = ref([])
const poiForm = ref({
  name: '',
  longitude: '',
  latitude: '',
  address: '',
  poiType: '',
  description: ''
})

// 地图初始化
const initMap = () => {
  AMapLoader.load({
    key: '1eef1f213f29b72ff7c89dca6c05e9f4',
    version: '2.0',
    plugins: ['AMap.MarkerCluster']
  }).then((AMap) => {
    map.value = new AMap.Map('map-manage', {
      zoom: 13,
      center: [116.397428, 39.90923],
      viewMode: '2D'
    })

    // 绑定地图点击事件（用于拾取坐标）
    map.value.on('click', handleMapClick)

    // 加载预设地点
    loadPois()

    ElMessage.success('地图加载成功')
  }).catch((error) => {
    console.error('地图加载失败:', error)
    ElMessage.error('地图加载失败，请刷新页面重试')
  })
}

// 处理地图点击（拾取坐标）
const handleMapClick = (e) => {
  if (!poiPickMode.value) return

  const lng = e.lnglat.getLng()
  const lat = e.lnglat.getLat()

  // 移除之前的临时标记
  if (tempMarker.value) {
    map.value.remove(tempMarker.value)
  }

  // 创建临时标记
  const AMap = window.AMap
  tempMarker.value = new AMap.Marker({
    position: [lng, lat],
    icon: new AMap.Icon({
      size: new AMap.Size(32, 32),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
      imageSize: new AMap.Size(32, 32)
    }),
    map: map.value
  })

  // 填充表单
  poiForm.value.longitude = lng.toFixed(6)
  poiForm.value.latitude = lat.toFixed(6)

  ElMessage.success(`已选中坐标：${lng.toFixed(6)}, ${lat.toFixed(6)}`)
}

// 切换拾取模式
const togglePickMode = () => {
  poiPickMode.value = !poiPickMode.value
  if (poiPickMode.value) {
    ElMessage.info('请在地图上点击选择位置')
  } else {
    // 移除临时标记
    if (tempMarker.value) {
      map.value.remove(tempMarker.value)
      tempMarker.value = null
    }
  }
}

// 加载所有用户足迹
const loadAllFootprints = async () => {
  if (!map.value) {
    ElMessage.warning('地图未初始化')
    return
  }

  footprintLoading.value = true
  try {
    const response = await getAllUsersFootprints({ days: 7 })
    const footprints = response.data || []

    if (footprints.length === 0) {
      ElMessage.info('最近7天暂无足迹数据')
      return
    }

    // 清除旧的足迹标记
    clearFootprintMarkers()

    const AMap = window.AMap
    const markers = []

    // 创建标记数据
    footprints.forEach((footprint) => {
      if (footprint.longitude && footprint.latitude) {
        const marker = {
          position: [footprint.longitude, footprint.latitude],
          title: footprint.userName || '未知用户',
          extData: footprint
        }
        markers.push(marker)
      }
    })

    // 使用聚合点显示
    if (markerCluster.value) {
      markerCluster.value.setMap(null)
    }

    markerCluster.value = new AMap.MarkerCluster(map.value, markers, {
      gridSize: 80,
      renderClusterMarker: renderClusterMarker,
      renderMarker: renderFootprintMarker
    })

    // 绑定点击事件
    markerCluster.value.on('click', (e) => {
      if (e.clusterData && e.clusterData.length === 1) {
        showFootprintInfo(e.clusterData[0])
      }
    })

    footprintCount.value = footprints.length
    footprintMarkers.value = markers

    // 自动适配视图
    if (markers.length > 0) {
      map.value.setFitView(null, false, [50, 50, 50, 50])
    }

    ElMessage.success(`成功加载 ${footprints.length} 个足迹点`)
  } catch (error) {
    console.error('加载足迹失败:', error)
    ElMessage.error('加载足迹失败')
  } finally {
    footprintLoading.value = false
  }
}

// 自定义聚合点样式
const renderClusterMarker = (context) => {
  const AMap = window.AMap
  const count = context.count
  const factor = Math.pow(count / 100, 1 / 5)
  const div = document.createElement('div')
  const size = Math.round(30 + Math.pow(count / 100, 1 / 5) * 20)
  
  div.style.cssText = `
    background-color: rgba(76, 175, 80, 0.7);
    border: 2px solid rgba(76, 175, 80, 1);
    border-radius: 50%;
    color: white;
    font-size: ${size > 40 ? 14 : 12}px;
    font-weight: bold;
    text-align: center;
    line-height: ${size}px;
    width: ${size}px;
    height: ${size}px;
  `
  div.innerHTML = count
  context.marker.setContent(div)
  context.marker.setOffset(new AMap.Pixel(-size / 2, -size / 2))
}

// 自定义足迹标记样式
const renderFootprintMarker = (context) => {
  const AMap = window.AMap
  const marker = new AMap.Marker({
    position: context.data.position,
    icon: new AMap.Icon({
      size: new AMap.Size(25, 34),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
      imageSize: new AMap.Size(25, 34)
    }),
    offset: new AMap.Pixel(-13, -30),
    extData: context.data.extData
  })

  marker.on('click', () => {
    showFootprintInfo(context.data)
  })

  context.marker = marker
}

// 显示足迹详情
const showFootprintInfo = (data) => {
  const AMap = window.AMap
  const footprint = data.extData || data
  
  const content = `
    <div style="padding: 10px; min-width: 200px;">
      <h4 style="margin: 0 0 10px 0; color: #409EFF;">足迹详情</h4>
      <p><strong>用户：</strong>${footprint.userName || '未知'}</p>
      <p><strong>行为：</strong>${footprint.behaviorType || '未知'}</p>
      <p><strong>时间：</strong>${footprint.createdAt || '未知'}</p>
      <p><strong>减排量：</strong>${footprint.carbonReduction || 0} kg</p>
    </div>
  `

  const infoWindow = new AMap.InfoWindow({
    content: content,
    offset: new AMap.Pixel(0, -30)
  })

  infoWindow.open(map.value, [data.position[0], data.position[1]])
}

// 清除足迹标记
const clearFootprintMarkers = () => {
  if (markerCluster.value) {
    markerCluster.value.setMap(null)
    markerCluster.value = null
  }
  footprintMarkers.value = []
  footprintCount.value = 0
  ElMessage.info('已清除足迹标记')
}

// 加载预设地点
const loadPois = async () => {
  if (!map.value) return

  poisLoading.value = true
  try {
    const response = await getAllPois()
    poisList.value = response.data || []

    // 清除旧的POI标记
    poiMarkers.value.forEach(marker => map.value.remove(marker))
    poiMarkers.value = []

    const AMap = window.AMap

    // 添加POI标记
    poisList.value.forEach(poi => {
      if (poi.longitude && poi.latitude) {
        const marker = new AMap.Marker({
          position: [poi.longitude, poi.latitude],
          title: poi.name,
          icon: new AMap.Icon({
            size: new AMap.Size(25, 34),
            image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
            imageSize: new AMap.Size(25, 34)
          }),
          offset: new AMap.Pixel(-13, -30),
          map: map.value,
          extData: poi
        })

        // 绑定点击事件
        marker.on('click', () => {
          const content = `
            <div style="padding: 10px; min-width: 200px;">
              <h4 style="margin: 0 0 10px 0; color: #F56C6C;">预设地点</h4>
              <p><strong>名称：</strong>${poi.name}</p>
              <p><strong>类型：</strong>${getPoiTypeName(poi.poiType)}</p>
              <p><strong>地址：</strong>${poi.address || '未填写'}</p>
              <p><strong>描述：</strong>${poi.description || '无'}</p>
            </div>
          `
          const infoWindow = new AMap.InfoWindow({
            content: content,
            offset: new AMap.Pixel(0, -30)
          })
          infoWindow.open(map.value, marker.getPosition())
        })

        poiMarkers.value.push(marker)
      }
    })

    if (poisList.value.length > 0 && activeTab.value === 'poi') {
      ElMessage.success(`已加载 ${poisList.value.length} 个预设地点`)
    }
  } catch (error) {
    console.error('加载预设地点失败:', error)
    ElMessage.error('加载预设地点失败')
  } finally {
    poisLoading.value = false
  }
}

// 保存预设地点
const savePoi = async () => {
  if (!poiForm.value.name) {
    ElMessage.warning('请输入地点名称')
    return
  }
  if (!poiForm.value.longitude || !poiForm.value.latitude) {
    ElMessage.warning('请点击地图选择位置')
    return
  }

  poiSaving.value = true
  try {
    await createPoi(poiForm.value)
    ElMessage.success('地点保存成功')
    resetPoiForm()
    loadPois()
  } catch (error) {
    console.error('保存地点失败:', error)
    ElMessage.error('保存地点失败')
  } finally {
    poiSaving.value = false
  }
}

// 删除预设地点
const handleDeletePoi = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除地点"${row.name}"吗？`, '确认删除', {
      type: 'warning'
    })

    await deletePoi(row.id)
    ElMessage.success('删除成功')
    loadPois()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地点失败:', error)
      ElMessage.error('删除地点失败')
    }
  }
}

// 定位到指定POI
const locatePoi = (poi) => {
  if (map.value && poi.longitude && poi.latitude) {
    map.value.setZoomAndCenter(16, [poi.longitude, poi.latitude])
    
    // 显示信息窗体
    const AMap = window.AMap
    const content = `
      <div style="padding: 10px; min-width: 200px;">
        <h4 style="margin: 0 0 10px 0; color: #F56C6C;">预设地点</h4>
        <p><strong>名称：</strong>${poi.name}</p>
        <p><strong>类型：</strong>${getPoiTypeName(poi.poiType)}</p>
        <p><strong>地址：</strong>${poi.address || '未填写'}</p>
        <p><strong>描述：</strong>${poi.description || '无'}</p>
      </div>
    `
    const infoWindow = new AMap.InfoWindow({
      content: content,
      offset: new AMap.Pixel(0, -30)
    })
    infoWindow.open(map.value, [poi.longitude, poi.latitude])
  }
}

// 重置POI表单
const resetPoiForm = () => {
  poiForm.value = {
    name: '',
    longitude: '',
    latitude: '',
    address: '',
    poiType: '',
    description: ''
  }
  poiPickMode.value = false
  if (tempMarker.value) {
    map.value.remove(tempMarker.value)
    tempMarker.value = null
  }
}

// 获取POI类型名称
const getPoiTypeName = (type) => {
  const typeMap = {
    teaching: '教学楼',
    canteen: '食堂',
    library: '图书馆',
    gym: '体育馆',
    dormitory: '宿舍',
    other: '其他'
  }
  return typeMap[type] || type || '未知'
}

// 生命周期
onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map.value) {
    map.value.destroy()
  }
})
</script>

<style scoped>
.map-manage-container {
  display: flex;
  height: calc(100vh - 60px);
  gap: 16px;
  padding: 16px;
  background-color: #f5f5f5;
}

.control-panel {
  width: 450px;
  flex-shrink: 0;
  overflow-y: auto;
}

.tab-content {
  padding: 16px;
}

.tab-content ul {
  margin: 8px 0;
  padding-left: 20px;
}

.tab-content ul li {
  margin: 4px 0;
}

.poi-list {
  margin-top: 16px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.list-header .title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.map-container {
  flex: 1;
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.map {
  width: 100%;
  height: 100%;
}

:deep(.el-tabs__content) {
  padding: 0;
}

:deep(.amap-marker-label) {
  border: none;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
}
</style>
