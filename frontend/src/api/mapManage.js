import request from '@/utils/request'

/**
 * 获取所有用户足迹
 * @param {Object} params - 查询参数
 * @param {number} params.days - 最近天数，默认7天
 * @returns {Promise}
 */
export function getAllUsersFootprints(params) {
  return request({
    url: '/map-manage/footprints/all',
    method: 'get',
    params
  })
}

/**
 * 获取所有预设地点
 * @returns {Promise}
 */
export function getAllPois() {
  return request({
    url: '/map-manage/pois',
    method: 'get'
  })
}

/**
 * 创建预设地点
 * @param {Object} data - 地点数据
 * @param {string} data.name - 地点名称
 * @param {number} data.latitude - 纬度
 * @param {number} data.longitude - 经度
 * @param {string} data.address - 地址（可选）
 * @param {string} data.poiType - 地点类型（可选）
 * @param {string} data.description - 描述（可选）
 * @returns {Promise}
 */
export function createPoi(data) {
  return request({
    url: '/map-manage/pois',
    method: 'post',
    data
  })
}

/**
 * 更新预设地点
 * @param {number} id - 地点ID
 * @param {Object} data - 更新的地点数据
 * @returns {Promise}
 */
export function updatePoi(id, data) {
  return request({
    url: `/map-manage/pois/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除预设地点
 * @param {number} id - 地点ID
 * @returns {Promise}
 */
export function deletePoi(id) {
  return request({
    url: `/map-manage/pois/${id}`,
    method: 'delete'
  })
}
