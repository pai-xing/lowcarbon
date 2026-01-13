import request from '@/utils/request'

/**
 * 获取行为类型配置列表
 */
export const getBehaviorTypes = () => {
  return request.get('/footprint/behavior-types')
}

/**
 * 创建碳足迹记录
 * @param {Object} data - 碳足迹数据
 * @param {string} data.behaviorType - 行为类型
 * @param {string} data.behaviorName - 行为名称
 * @param {number} data.dataValue - 数据值
 * @param {string} data.recordDate - 记录日期(YYYY-MM-DD)
 * @param {string} data.remark - 备注
 */
export const createFootprint = (data) => {
  return request.post('/footprint', data)
}

/**
 * 获取碳足迹记录列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.startDate - 开始日期(YYYY-MM-DD)
 * @param {string} params.endDate - 结束日期(YYYY-MM-DD)
 */
export const getFootprintList = (params) => {
  return request.get('/footprint/list', { params })
}

/**
 * 获取碳足迹统计数据
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期(YYYY-MM-DD)
 * @param {string} params.endDate - 结束日期(YYYY-MM-DD)
 */
export const getStatistics = (params) => {
  return request.get('/footprint/statistics', { params })
}

/**
 * 获取按行为类型分组的统计数据
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期(YYYY-MM-DD)
 * @param {string} params.endDate - 结束日期(YYYY-MM-DD)
 */
export const getStatisticsByBehavior = (params) => {
  return request.get('/footprint/statistics/by-behavior', { params })
}

/**
 * 获取每日统计数据
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期(YYYY-MM-DD)
 * @param {string} params.endDate - 结束日期(YYYY-MM-DD)
 */
export const getDailyStatistics = (params) => {
  return request.get('/footprint/statistics/daily', { params })
}

/**
 * 删除碳足迹记录
 * @param {number} id - 记录ID
 */
export const deleteFootprint = (id) => {
  return request.delete(`/footprint/${id}`)
}

export const updateFootprint = (id, data) => {
  return request.put(`/footprint/${id}`, data)
}
