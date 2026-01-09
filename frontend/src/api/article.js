import request from '../utils/request'

// 获取文章列表
export function getArticleList(params) {
  return request({
    url: '/article/list',
    method: 'get',
    params
  })
}

// 获取文章详情
export function getArticleDetail(id) {
  return request({
    url: `/article/${id}`,
    method: 'get'
  })
}

// 创建文章
export function createArticle(data) {
  return request({
    url: '/article',
    method: 'post',
    data
  })
}

// 更新文章
export function updateArticle(id, data) {
  return request({
    url: `/article/${id}`,
    method: 'put',
    data
  })
}

// 删除文章
export function deleteArticle(id) {
  return request({
    url: `/article/${id}`,
    method: 'delete'
  })
}

// 置顶/取消置顶
export function toggleTop(id) {
  return request({
    url: `/article/${id}/top`,
    method: 'put'
  })
}

// 点赞文章
export function likeArticle(id) {
  return request({
    url: `/article/${id}/like`,
    method: 'post'
  })
}

// 取消点赞
export function unlikeArticle(id) {
  return request({
    url: `/article/${id}/like`,
    method: 'delete'
  })
}

// 检查点赞状态
export function checkLikeStatus(id) {
  return request({
    url: `/article/${id}/like/status`,
    method: 'get'
  })
}

// 收藏文章
export function favoriteArticle(id) {
  return request({
    url: `/article/${id}/favorite`,
    method: 'post'
  })
}

// 取消收藏
export function unfavoriteArticle(id) {
  return request({
    url: `/article/${id}/favorite`,
    method: 'delete'
  })
}

// 检查收藏状态
export function checkFavoriteStatus(id) {
  return request({
    url: `/article/${id}/favorite/status`,
    method: 'get'
  })
}

// 获取收藏列表
export function getFavoriteArticles(params) {
  return request({
    url: '/article/favorites',
    method: 'get',
    params
  })
}

// 添加评论
export function addComment(id, content) {
  return request({
    url: `/article/${id}/comment`,
    method: 'post',
    data: { content }
  })
}

// 获取评论列表
export function getComments(id) {
  return request({
    url: `/article/${id}/comments`,
    method: 'get'
  })
}

// 删除评论
export function deleteComment(commentId) {
  return request({
    url: `/article/comment/${commentId}`,
    method: 'delete'
  })
}
