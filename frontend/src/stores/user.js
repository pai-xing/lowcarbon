import { defineStore } from 'pinia'
import { login, register, getUserInfo } from '../api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.userInfo?.role === 'admin'
  },
  
  actions: {
    // 登录
    async login(loginData) {
      const res = await login(loginData)
      this.token = res.data.token
      localStorage.setItem('token', this.token)
      await this.fetchUserInfo()
      return res
    },
    
    // 注册
    async register(registerData) {
      const res = await register(registerData)
      this.token = res.data.token
      localStorage.setItem('token', this.token)
      await this.fetchUserInfo()
      return res
    },
    
    // 获取用户信息
    async fetchUserInfo() {
      try {
        const res = await getUserInfo()
        // 统一头像URL并追加缓存破坏参数，避免各处组件因浏览器缓存显示旧头像
        const data = { ...res.data }
        if (data && typeof data.avatar === 'string' && data.avatar) {
          let avatarUrl = data.avatar
          if (!avatarUrl.startsWith('http')) {
            avatarUrl = window.location.origin + avatarUrl
          }
          const sep = avatarUrl.includes('?') ? '&' : '?'
          // 使用时间戳作为版本参数，确保每次拉取后的头像都能刷新
          avatarUrl = `${avatarUrl}${sep}v=${Date.now()}`
          data.avatar = avatarUrl
        }
        this.userInfo = data
        localStorage.setItem('userInfo', JSON.stringify(data))
        return data
      } catch (error) {
        this.logout()
        throw error
      }
    },
    
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
