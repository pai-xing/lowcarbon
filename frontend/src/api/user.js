import request from '../utils/request'

// 用户注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

// 管理员获取用户列表
export function getUserList(params) {
  return request({
    url: '/user/admin/list',
    method: 'get',
    params
  })
}

// 管理员更新用户状态
export function updateUserStatus(userId, status) {
  return request({
    url: `/user/admin/${userId}/status`,
    method: 'put',
    params: { status }
  })
}

// 管理员获取统计数据
export function getStatistics() {
  return request({
    url: '/user/admin/statistics',
    method: 'get'
  })
}
