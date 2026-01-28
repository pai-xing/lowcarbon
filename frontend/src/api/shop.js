import request from '@/utils/request'

/**
 * 获取商品列表
 */
export function getProductList() {
  return request({
    url: '/shop/products',
    method: 'get'
  })
}

/**
 * 兑换商品
 */
export function exchangeProduct(productId) {
  return request({
    url: `/shop/exchange/${productId}`,
    method: 'post'
  })
}

/**
 * 获取兑换记录
 */
export function getExchangeRecords() {
  return request({
    url: '/shop/records',
    method: 'get'
  })
}

/**
 * 切换主题
 */
export function changeTheme(themeStyle) {
  return request({
    url: `/shop/theme/${themeStyle}`,
    method: 'post'
  })
}
