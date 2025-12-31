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

