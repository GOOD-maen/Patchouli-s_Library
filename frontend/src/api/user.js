// 封装用户相关接口
const API_BASE_URL = 'http://localhost:8080/api'

// 通用请求函数（带 token）
async function request(url, options = {}) {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  const headers = {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
    ...options.headers
  }

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '请求失败' }))
    throw new Error(error.message || `HTTP ${response.status}`)
  }

  return response.json()
}

// 更新用户信息
export function updateUserProfile(data) {
  return request('/user/profile', {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

export function getUserProfile() {
  return request('/user/profile', {
    method: 'GET'
  })
}