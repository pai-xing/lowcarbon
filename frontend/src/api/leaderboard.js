import request from '@/utils/request'

/**
 * 获取排行榜
 * @param {Object} params - 查询参数
 * @param {string} params.type - 排行榜类型：total(总榜)、week(周榜)、month(月榜)、activity(活动榜)
 * @param {number} params.activityId - 活动ID（仅活动榜需要）
 * @param {number} params.limit - 返回数量，默认50
 * @returns {Promise}
 */
export function getLeaderboard(params) {
  return request({
    url: '/api/leaderboard',
    method: 'get',
    params
  })
}
