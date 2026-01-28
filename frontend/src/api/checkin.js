import request from '@/utils/request'

/**
 * 标准打卡（单位为"次"的行为，一天仅一次）
 * @param {Object} data
 * @param {string} data.behaviorType 行为类型（需为单位"次"的类型，如 VEGETARIAN/RECYCLE 等）
 * @param {string} [data.remark] 备注（可选）
 * @param {number} [data.latitude] 纬度（可选）
 * @param {number} [data.longitude] 经度（可选）
 * @param {string} [data.address] 地址（可选）
 * @param {string} [data.geoSource] 位置来源（可选，如 'GPS', 'MANUAL', 'IP'）
 */
export const checkin = (data) => {
  return request.post('/footprint/checkin', data)
}

/**
 * 获取指定月份的打卡日历
 * @param {Object} params
 * @param {string} params.month 月份(YYYY-MM)，例如 '2026-01'
 * @returns {Promise} [{ date, count, types }]
 */
export const getCalendar = (params) => {
  return request.get('/footprint/checkin/calendar', { params })
}

/**
 * 获取用户打卡统计（streak、totalCheckins、monthlyRate）
 * @returns {Promise} { streak, totalCheckins, monthlyRate }
 */
export const getCheckinStats = () => {
  return request.get('/footprint/checkin/stats')
}
