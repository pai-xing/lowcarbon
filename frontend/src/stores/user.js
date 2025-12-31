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
        this.userInfo = res.data
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        return res.data
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

