import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin } from '@/api/auth'
import { updateUserProfile as apiUpdateProfile } from '@/api/user'
import { getUserProfile } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 从存储中读取 token 和 userInfo
  const token = ref(localStorage.getItem('token') || sessionStorage.getItem('token') || '')
  
  // 尝试从两个存储中读取 userInfo（优先 localStorage，再 sessionStorage）
  let savedUserInfo = null
  try {
    savedUserInfo = JSON.parse(localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo') || 'null')
  } catch (e) {
    console.warn('解析 userInfo 失败', e)
  }
  const userInfo = ref(savedUserInfo)

  const isLoggedIn = computed(() => !!token.value)

  // 登录
  async function login(account, password, remember) {
    const res = await apiLogin({ account, password })
    const newToken = res.token
    const newUser = res.user

    token.value = newToken
    userInfo.value = newUser

    // 根据 remember 选择存储位置
    if (remember) {
      localStorage.setItem('token', newToken)
      localStorage.setItem('userInfo', JSON.stringify(newUser))
    } else {
      sessionStorage.setItem('token', newToken)
      sessionStorage.setItem('userInfo', JSON.stringify(newUser))
    }
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
  }


  async function updateProfile(updateData) {
    const res = await apiUpdateProfile(updateData)
    // 假设后端返回 data 包含更新后的完整用户信息
    const newUserInfo = res.data
    userInfo.value = { ...userInfo.value, ...newUserInfo }

    // 同步到存储（根据之前登录时 remember 的选择）
    const remember = localStorage.getItem('token') !== null
    if (remember) {
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } else {
      sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }

    return res
  }

  async function fetchUserInfo() {
  if (!token.value) return
  try {
    // 调用后端接口获取当前用户信息
    const res = await getUserProfile()  // GET /api/user/profile
    const newUserInfo = res.data
    userInfo.value = { ...userInfo.value, ...newUserInfo }

    // 同步存储
    const remember = localStorage.getItem('token') !== null
    if (remember) {
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } else {
      sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

  return { token, userInfo, isLoggedIn, login, logout, updateProfile, fetchUserInfo }
})