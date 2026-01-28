import request from '../utils/request'

/**
 * 活动相关API
 */

// 创建活动(管理员)
export const createActivity = (data) => {
  return request.post('/api/activity/create', data)
}

// 更新活动(管理员)
export const updateActivity = (data) => {
  return request.put('/api/activity/update', data)
}

// 删除活动(管理员)
export const deleteActivity = (id) => {
  return request.delete(`/api/activity/${id}`)
}

// 获取活动列表
export const getActivityList = (status) => {
  return request.get('/api/activity/list', { params: { status } })
}

// 获取活动详情
export const getActivityDetail = (id) => {
  return request.get(`/api/activity/${id}`)
}

// 参加活动
export const joinActivity = (activityId) => {
  return request.post(`/api/activity/join/${activityId}`)
}

// 获取活动排行榜
export const getActivityRanking = (activityId, limit = 100) => {
  return request.get(`/api/activity/ranking/${activityId}`, { params: { limit } })
}

// 获取我的活动排名
export const getMyRanking = (activityId) => {
  return request.get(`/api/activity/my-ranking/${activityId}`)
}
