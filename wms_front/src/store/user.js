import { defineStore } from 'pinia'

// 用户状态存储（Pinia）
export const useUserStore = defineStore('user', {
  // state：数据
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),

  // getters：派生数据
  getters: {
    isLogin: state => !!state.token,
    role: state => state.userInfo.role || ''
  },

  // actions：方法
  actions: {
    // 登录成功后保存用户信息
    setLogin(data) {
      this.token = data.token
      this.userInfo = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        role: data.role
      }
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})