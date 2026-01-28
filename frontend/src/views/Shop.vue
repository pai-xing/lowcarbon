<template>
  <div class="shop-container">
    <el-page-header @back="$router.back()" content="积分商城" style="margin-bottom: 20px;" />
    
    <!-- 用户积分信息 -->
    <el-card class="points-card" shadow="hover">
      <div class="points-info">
        <el-icon :size="40" color="#67C23A"><Coin /></el-icon>
        <div class="points-text">
          <div class="current-points">当前积分：{{ userPoints }}</div>
          <div class="points-tip">通过记录碳足迹和低碳行为获得积分</div>
        </div>
      </div>
    </el-card>

    <!-- 商品列表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in products" :key="product.id">
        <el-card class="product-card" shadow="hover">
          <template #header>
            <div class="product-header">
              <span class="product-name">{{ product.productName }}</span>
              <el-tag :type="getProductTagType(product.productType)">
                {{ getProductTypeText(product.productType) }}
              </el-tag>
            </div>
          </template>
          
          <div class="product-content">
            <div class="product-icon">
              <el-icon :size="80" :color="getProductIconColor(product.productType)">
                <component :is="getProductIcon(product.productType)" />
              </el-icon>
            </div>
            
            <div class="product-desc">{{ product.description }}</div>
            
            <div class="product-footer">
              <div class="product-price">
                <el-icon><Coin /></el-icon>
                <span>{{ product.price }} 积分</span>
              </div>
              <el-button 
                type="primary" 
                :disabled="isProductOwned(product) || userPoints < product.price"
                @click="handleExchange(product)">
                {{ getButtonText(product) }}
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 兑换记录 -->
    <el-card class="records-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>兑换记录</span>
          <el-icon><List /></el-icon>
        </div>
      </template>
      
      <el-table :data="records" style="width: 100%" empty-text="暂无兑换记录">
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="消耗积分" width="120">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.price }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exchangeTime" label="兑换时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin, List, Picture, Cherry, Medal } from '@element-plus/icons-vue'
import { getProductList, exchangeProduct, getExchangeRecords } from '@/api/shop'
import { getUserInfo } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { applyTheme } from '@/utils/theme'

const userStore = useUserStore()
const products = ref([])
const records = ref([])
const userInfo = ref({})

const userPoints = computed(() => userInfo.value.points || 0)

// 获取商品列表
const loadProducts = async () => {
  try {
    const res = await getProductList()
    products.value = res.data
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  }
}

// 获取兑换记录
const loadRecords = async () => {
  try {
    const res = await getExchangeRecords()
    records.value = res.data
  } catch (error) {
    console.error('加载兑换记录失败', error)
  }
}

// 获取用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

// 判断商品是否已拥有
const isProductOwned = (product) => {
  if (product.productType === 'tree') {
    return userInfo.value.hasVirtualTree === 1
  }
  if (product.productType === 'badge') {
    return userInfo.value.vipBadge === 1
  }
  return false
}

// 获取按钮文字
const getButtonText = (product) => {
  if (isProductOwned(product)) {
    return '已拥有'
  }
  if (userPoints.value < product.price) {
    return '积分不足'
  }
  return '立即兑换'
}

// 获取商品类型文本
const getProductTypeText = (type) => {
  const map = {
    'theme': '主题皮肤',
    'tree': '虚拟道具',
    'badge': '尊贵标识'
  }
  return map[type] || '未知'
}

// 获取商品标签类型
const getProductTagType = (type) => {
  const map = {
    'theme': 'primary',
    'tree': 'success',
    'badge': 'warning'
  }
  return map[type] || ''
}

// 获取商品图标
const getProductIcon = (type) => {
  const map = {
    'theme': 'Picture',
    'tree': 'Cherry',
    'badge': 'Medal'
  }
  return map[type] || 'Picture'
}

// 获取商品图标颜色
const getProductIconColor = (type) => {
  const map = {
    'theme': '#409EFF',
    'tree': '#67C23A',
    'badge': '#E6A23C'
  }
  return map[type] || '#909399'
}

// 兑换商品
const handleExchange = async (product) => {
  try {
    await ElMessageBox.confirm(
      `确认花费 ${product.price} 积分兑换【${product.productName}】吗？`,
      '确认兑换',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await exchangeProduct(product.id)
    ElMessage.success('兑换成功！')
    
    // 刷新数据
    await loadUserInfo()
    await loadRecords()
    
    // 如果是主题商品，自动切换主题
    if (product.productType === 'theme') {
      // 从商品配置中提取主题标识（如：清新绿主题 -> green）
      let themeStyle = 'default'
      if (product.productName.includes('清新绿')) {
        themeStyle = 'green'
      } else if (product.productName.includes('暗夜黑')) {
        themeStyle = 'dark'
      }
      
      // 应用主题
      applyTheme(themeStyle)
      ElMessage.success('主题已自动切换！')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '兑换失败')
    }
  }
}

onMounted(() => {
  loadProducts()
  loadRecords()
  loadUserInfo()
})
</script>

<style scoped>
.shop-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.points-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.points-card :deep(.el-card__body) {
  padding: 30px;
}

.points-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.points-text {
  flex: 1;
}

.current-points {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
}

.points-tip {
  font-size: 14px;
  opacity: 0.9;
}

.product-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-name {
  font-weight: bold;
  font-size: 16px;
}

.product-content {
  text-align: center;
}

.product-icon {
  margin: 20px 0;
}

.product-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  min-height: 60px;
  margin: 20px 0;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 18px;
  font-weight: bold;
  color: #E6A23C;
}

.records-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 768px) {
  .shop-container {
    padding: 10px;
  }
  
  .current-points {
    font-size: 22px;
  }
}
</style>
