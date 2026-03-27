// stores/essayStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'

export const useEssayStore = defineStore('essay', () => {
  // 所有文章（主页用）
  const essays = ref([])
  const loading = ref(false)
  const error = ref(null)
  const hasLoaded = ref(false) 

  // 用户文章（个人主页用）
  const userEssays = ref([])
  const userLoading = ref(false)
  const userError = ref(null)

  // 获取所有文章
  const fetchEssayList = async () => {
    console.log('🚀 开始调用 fetchEssayList()')
    loading.value = true
    error.value = null
    try {
      const apiUrl = 'http://localhost:8080/api/essay'
      console.log('🌐 发送请求到:', apiUrl)
      const response = await fetch(apiUrl)
      console.log('📡 响应状态:', response.status)
      const responseText = await response.text()
      console.log('📄 原始响应文本:', responseText)
      if (!response.ok) throw new Error(`网络请求失败: ${response.status}`)
      const data = JSON.parse(responseText)
      essays.value = data
      hasLoaded.value = true
    } catch (err) {
      error.value = err.message
      console.error('❌ 获取数据失败:', err)
      essays.value = []
    } finally {
      loading.value = false
    }
  }

  // 获取指定用户的文章
  const fetchUserEssays = async (userId) => {
    console.log('🚀 开始调用 fetchUserEssays()，用户ID:', userId)
    userLoading.value = true
    userError.value = null
    try {
      const userStore = useUserStore()
      const url = `http://localhost:8080/api/essay/user/${userId}`
      const headers = {}
      if (userStore.token) headers['Authorization'] = `Bearer ${userStore.token}`
      const response = await fetch(url, { headers })
      const responseText = await response.text()
      if (!response.ok) throw new Error(`网络请求失败: ${response.status}`)
      const data = JSON.parse(responseText)
      userEssays.value = data 
    } catch (err) {
      userError.value = err.message
      console.error('❌ 获取用户文章失败:', err)
      userEssays.value = []
    } finally {
      userLoading.value = false
    }
  }

  // 确保所有文章已加载
  const ensureDataLoaded = async () => {
    console.log('🔍 ensureDataLoaded 被调用，当前状态: hasLoaded=', hasLoaded.value, 'loading=', loading.value)
    if (!hasLoaded.value && !loading.value) {
      await fetchEssayList()
    }
  }

  const essayCount = computed(() => essays.value.length)
  const essayIds = computed(() => essays.value.map(item => item.id))
  const getEssayById = (id) => essays.value.find(item => item.id == id)
  const getAllEssay = () => essays.value

  // 用户文章相关的计算属性
  const userEssayCount = computed(() => userEssays.value.length)
  const getUserEssayById = (id) => userEssays.value.find(item => item.id == id)

  return {
    // 所有文章
    essays,
    loading,
    error,
    hasLoaded,
    essayCount,
    essayIds,
    fetchEssayList,
    ensureDataLoaded,
    getEssayById,
    getAllEssay,
    // 用户文章
    userEssays,
    userLoading,
    userError,
    userEssayCount,
    fetchUserEssays,
    getUserEssayById
  }
})