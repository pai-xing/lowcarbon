import request from '../utils/request'

/**
 * 创建帖子
 */
export function createPost(data) {
  return request({
    url: '/posts',
    method: 'post',
    data
  })
}

/**
 * 获取帖子列表
 */
export function getPostList(params) {
  return request({
    url: '/posts',
    method: 'get',
    params
  })
}

/**
 * 获取帖子详情
 */
export function getPostById(id) {
  return request({
    url: `/posts/${id}`,
    method: 'get'
  })
}

/**
 * 获取用户发布的帖子列表
 */
export function getUserPosts(userId) {
  return request({
    url: `/posts/user/${userId}`,
    method: 'get'
  })
}

/**
 * 删除帖子
 */
export function deletePost(id) {
  return request({
    url: `/posts/${id}`,
    method: 'delete'
  })
}

/**
 * 点赞/取消点赞帖子
 */
export function toggleLike(id) {
  return request({
    url: `/posts/${id}/like`,
    method: 'post'
  })
}

/**
 * 添加评论
 */
export function addComment(data) {
  return request({
    url: '/posts/comments',
    method: 'post',
    data
  })
}

/**
 * 获取帖子的评论列表
 */
export function getComments(postId) {
  return request({
    url: `/posts/${postId}/comments`,
    method: 'get'
  })
}

/**
 * 删除评论
 */
export function deleteComment(id) {
  return request({
    url: `/posts/comments/${id}`,
    method: 'delete'
  })
}

/**
 * 审核帖子（管理员）
 */
export function auditPost(id, status) {
  return request({
    url: `/posts/${id}/audit`,
    method: 'put',
    params: { status }
  })
}
