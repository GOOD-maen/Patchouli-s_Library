<script setup>
import { onMounted, ref ,computed,watch } from 'vue'
import { useEssayStore } from '@/stores/essayStore'
import { useUserStore } from '@/stores/user'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'
import defaultCoverImage from '@/resources/pictures/test.png'
import EssayCreateModule from '@/pages/essayPages/manageSon/essayCreateModule.vue'
import EssayEditModule from '@/pages/essayPages/manageSon/essayEditModule.vue'
import { PencilLine, Trash ,PencilRuler } from 'lucide-vue-next'
import defaultAvatar from '@/resources/pictures/heads/mc_head.png';
import defaultBackground from '@/resources/pictures/background/background.png';
import ImageCropperModal from '@/components/ImageCropperModal.vue' 


const router = useRouter()
const essayStore = useEssayStore()
const userStore = useUserStore() 

////////////获取预览图片///////////
// 本地预览（dataURL）
const avatarPreview = ref('')
const bgPreview = ref('')

const baseUrl = 'http://localhost:8080' 

const getFullImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/')) return `${baseUrl}${url}`
  const cleanUrl = url.replace(/^\/+/, '')
  return `${baseUrl}/${cleanUrl}`
}
////////////获取预览图片(ED)///////////
/////////////////////用户信息获取//////////////////////

const displayUUID = computed(() => userStore.userInfo?.uuid || '------')

/////////////////////用户信息获取(ED)//////////////////////

////////////// router ///////////////
const openEssayDetail = (essayId) => {
  router.push(`/essay/${essayId}`)
}
////////////// router(ED) ///////////////

////////////////////功能切换/////////////////

const route = useRoute()

// 将 nowFunction 改为响应式，并从路由 query 中获取初始值
const nowFunction = ref(route.query.tab === 'userWorks' ? 'userWorks' : 'setting')

watch(
  () => route.query.tab,
  (newTab) => {
    if (newTab === 'userWorks' || newTab === 'setting') {
      nowFunction.value = newTab
    } else {
      nowFunction.value = 'userWorks'
    }
  }
)

////////////////////功能切换(ED)/////////////////

////////////// 封面图片处理 /////////////
const getCoverImage = (essay) => {
  if (essay.coverUrl && essay.coverUrl.trim() !== '') {
    return `http://localhost:8080/uploads/${essay.coverUrl}`
  }
  return defaultCoverImage
}
////////////// 封面图片处理(ED) //////////

///////////////// 创建弹窗 ////////////////
const showCreateModule = ref(false)
const openCreateModule = () => {
  showCreateModule.value = true
}
const closeCreateModule = () => {
  showCreateModule.value = false
}
const handleEssayCreated = (newEssay) => {
  console.log('新帖子创建成功:', newEssay)
  if (userStore.userInfo?.id) {
    essayStore.fetchUserEssays(userStore.userInfo.id)
  }
  closeCreateModule()
}
///////////////// 创建弹窗(ED) ////////////

/////////////// 删除弹窗 /////////////////
const showDeleteModule = ref(false)
const essayToDelete = ref(null)

const openDeleteModule = (essayId) => {
  essayToDelete.value = essayId
  showDeleteModule.value = true
}
const closeDeleteModule = () => {
  showDeleteModule.value = false
  essayToDelete.value = null
}
const deleteEssay = async () => {
  if (!essayToDelete.value) return

  try {
    const response = await fetch(`http://localhost:8080/api/essay/${essayToDelete.value}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })

    console.log('响应状态:', response.status)
    const contentType = response.headers.get('content-type')
    let result = null

    if (contentType && contentType.includes('application/json')) {
      try {
        result = await response.json()
      } catch (jsonError) {
        console.error('JSON解析失败:', jsonError)
        alert('服务器响应格式错误')
        closeDeleteModule()
        return
      }
    }

    if (response.ok) {
      console.log('删除成功')

      if (userStore.userInfo?.id) {
        essayStore.fetchUserEssays(userStore.userInfo.id)
      } else {

        essayStore.fetchEssayList()
      }
    } else {
      console.error('删除失败:', result)
      alert(result?.message || `删除失败: HTTP ${response.status}`)
    }
  } catch (error) {
    console.error('删除请求失败:', error)
    alert('网络错误，请重试')
  } finally {
    closeDeleteModule()
  }
}
/////////////// 删除弹窗(ED) /////////////

/////////////// 编辑弹窗 /////////////////
const showEditModule = ref(false)
const currentEditid = ref(null)

const openEditModule = (essayId) => {
  currentEditid.value = essayId
  showEditModule.value = true
}
const closeEditModule = () => {
  showEditModule.value = false
  currentEditid.value = null
}
/////////////// 编辑弹窗(ED) /////////////

// 初始化加载文章列表
onMounted(() => {

  if (userStore.isLoggedIn && userStore.userInfo?.id) {
    essayStore.fetchUserEssays(userStore.userInfo.id)
  } else {
    console.log('用户未登录，不加载文章')
  }
})

//////////////////日期格式//////////////////

const formatDate = (dateString) => {
  if (!dateString) return '--'
  const date = new Date(dateString)

  if (isNaN(date.getTime())) return dateString

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false 
  }).format(date).replace(/\//g, '-') 
}

//////////////////日期格式(ED)//////////////////
//////////////////////编辑用户信息方法//////////////////////
const isEditing = ref(false)

// 切换编辑模式
const toggleEditMode = async () => {
  if (isEditing.value) {
    await saveSettings()
  } else {
    isEditing.value = true
  }
}

// 表单数据（绑定到界面）
const editForm = ref({
  username: userStore.userInfo?.username || '',
  avatar: userStore.userInfo?.avatar || '',
  signature: userStore.userInfo?.signature || '',
  backgroundImage: userStore.userInfo?.backgroundImage || ''
})

// 头像上传相关
const avatarFileInput = ref(null)
const avatarFileToCrop = ref(null)
const showAvatarCropper = ref(false)
const isUploading = ref(false)

// 背景上传相关
const bgFileInput = ref(null)
const isUploadingBg = ref(false)

// 触发头像选择
const triggerAvatarUpload = () => {
  avatarFileInput.value?.click()
}

// 头像文件选择
const onAvatarSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }
  avatarFileToCrop.value = file
  showAvatarCropper.value = true
  avatarFileInput.value.value = '' // 清空以便再次选择同一文件
}

// 裁剪完成回调
const handleAvatarCropComplete = async (croppedFile, previewUrl) => {
  try {
    isUploading.value = true
    avatarPreview.value = previewUrl
    const url = await uploadImage(croppedFile, 'avatar')
    editForm.value.avatar = url
    console.log('头像上传成功:', url)
  } catch (error) {
    console.error('头像上传失败', error)
    alert('头像上传失败：' + error.message)
  } finally {
    isUploading.value = false
    showAvatarCropper.value = false
  }
}

// 背景图片上传（直接上传，无需裁剪）
const triggerBgUpload = () => {
  bgFileInput.value?.click()
}

const onBgSelect = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }
  
  const reader = new FileReader()
  reader.onload = (e) => {
    bgPreview.value = e.target.result
  }
  reader.readAsDataURL(file)

  try {
    isUploadingBg.value = true
    const url = await uploadImage(file, 'background')
    editForm.value.backgroundImage = url
    console.log('背景上传成功:', url)
  } catch (error) {
    console.error('背景上传失败', error)
    alert('背景上传失败：' + error.message)
  } finally {
    isUploadingBg.value = false
    bgFileInput.value.value = ''
  }
}

// 通用图片上传函数（复用）
const uploadImage = async (file, type) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type) 

  const token = userStore.token
  const response = await fetch('http://localhost:8080/api/user/uploads', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`
    },
    body: formData
  })
  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(`上传失败: ${response.status}`)
  }
  const result = await response.json()
  return result.fileUrl
}

// 保存设置
const saveSettings = async () => {
  if (isUploading.value || isUploadingBg.value) {
    alert('请等待图片上传完成')
    return
  }
  try {
    await userStore.updateProfile({
      username: editForm.value.username, 
      avatar: editForm.value.avatar,
      backgroundImage: editForm.value.backgroundImage,
      signature: editForm.value.signature
    })
    alert('保存成功')
  } catch (error) {
    console.error('保存失败', error)
    alert('保存失败：' + error.message)
  }
}

// 初始化表单数据（监听 store 变化）
onMounted(async () => {
  if (userStore.isLoggedIn) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败，使用本地存储的数据')
    }
    if (userStore.userInfo) {
      editForm.value.username = userStore.userInfo.username || ''
      editForm.value.avatar = userStore.userInfo.avatar || ''
      editForm.value.signature = userStore.userInfo.signature || ''
      editForm.value.backgroundImage = userStore.userInfo.backgroundImage || ''
    }
    if (userStore.userInfo?.id) {
      essayStore.fetchUserEssays(userStore.userInfo.id)
    }
  } else {
    console.log('用户未登录，不加载文章')
  }
})

//////////////////////编辑用户信息方法(ED)//////////////////////

</script>

<template>
  <!--设置-->
  <div v-if="nowFunction==='setting'" class="w-full h-full bg-white classic_shadow">
    <div class="w-full h-[50px] bottom_shadow">
      <div style="font-size: 25px; font-family:'squareChinese';" class="w-full h-[45px] border-b-[1px] flex pt-[5px] items-center justify-center">
        ————— Setting —————
      </div>
    </div>
    <div class="w-full h-[calc(100%-50px)] overflow-y-auto">
      <div class="relative">
        <div
        v-if="!isEditing"
        class="absolute inset-0 bg-black/10  z-10  cursor-not-allowed"
      ></div>
      <!--头像，昵称，签名-->
      <div class="user_card w-full h-[170px] border-b-[1.5px] border-slate-500/20 flex relative">
          
        <!--头像-->
        <div class="w-[170px] h-[170px] border-r-[1.5px] p-[20px] border-slate-500/20">
          <div class="w-[130px] h-[130px] classic_shadow p-[5px] bg-slate-400/90 relative group cursor-pointer" @click="isEditing && triggerAvatarUpload()">
  <div
    :style="{ backgroundImage: `url(${avatarPreview || getFullImageUrl(editForm.avatar) || defaultAvatar})` }"
    class="w-full h-full inset_shadow bg-cover bg-center"
  ></div>
        <div v-if="isEditing" class="absolute inset-0 flex items-center justify-center bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity rounded-md">
          <div style="border-radius: 5px; font-family: 'squareChinese';font-size: 16px; padding: 5px 10px;" class="border-[2px] border-white text-white">编辑头像</div>
          </div>
        </div>
          <input type="file" ref="avatarFileInput" @change="onAvatarSelect" accept="image/*" hidden />
        </div>
        <!--头像(ED)-->

        <!--昵称和签名-->
        <div class="h-[170px] w-[calc(100%-170px)]">
          <div class="w-full h-[60px] border-b-[1.5px] border-slate-500/20 flex items-center pl-4 relative">
            <input
              v-model="editForm.username"
              type="text"
              placeholder="昵称"
              :disabled="!isEditing"
              class="w-[200px] h-[40px] px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition-all duration-200 text-gray-700 font-['squareChinese'] shadow-sm"
              :class="{ 'bg-gray-100 text-gray-500 cursor-not-allowed': !isEditing, 'bg-white': isEditing }"
            />
            <div style="font-size: 10px;" class="mt-[20px] ml-[10px]">UUID: {{ displayUUID }}</div>
          
          </div>
          <div class="w-full h-[calc(100%-60px)] flex items-center justify-center p-3">
            <textarea
              v-model="editForm.signature"
              placeholder="签名"
              rows="3"
              class="w-full h-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition-all duration-200 bg-white/80 text-gray-700 font-['squareChinese'] resize-none shadow-sm"
            ></textarea>
          </div>
        </div>
        <!--昵称和签名(ED)-->
      </div>

      <!--头像，昵称，签名(ED)-->

      <!--背景图片-->
      <div class="w-full h-[240px] border-b-[1.5px] border-slate-500/20 relative">
        <div style="font-size: 20px; font-family: 'squareChinese';" class="ml-[15px] mt-[10px] mb-[5px]">背景图片</div>
        <div
  :style="{ backgroundImage: `url(${bgPreview || getFullImageUrl(editForm.backgroundImage) || defaultBackground})` }"
  class="w-[calc(100%-30px)] h-[170px] ml-[15px] classic_shadow bg-cover bg-center relative group cursor-pointer"
  @click="isEditing && triggerBgUpload()"
>
  <div v-if="isEditing" class="absolute inset-0 flex items-center justify-center bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity rounded-md">
    <div style="border-radius: 5px; font-family: 'squareChinese';font-size: 16px; padding: 5px 10px;" class="border-[2px] border-white text-white">编辑背景</div>
  </div>
</div>
        <input type="file" ref="bgFileInput" @change="onBgSelect" accept="image/*" hidden />
      </div>
      <!--背景图片(ED)-->
</div>

      <!--保存按钮-->
      <div class="flex justify-center p-4">
  <button
    @click="toggleEditMode"
    :disabled="isUploading || isUploadingBg"
    class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 transition-colors"
  >
    {{ isEditing ? '保存信息' : '编辑信息' }}
  </button>
      </div>
    </div>

    <!-- 头像裁剪模态框 -->
    <ImageCropperModal
      v-model:visible="showAvatarCropper"
      :image-file="avatarFileToCrop"
      :aspect-ratio="1"
      @crop-complete="handleAvatarCropComplete"
    />
  </div>
  <!--设置(ED)-->

  <!-- 作品列表 -->
  <div v-if="nowFunction==='userWorks'" class="w-full h-full bg-white classic_shadow">
    <div class="w-full h-[50px] bottom_shadow"></div>

    <!-- 展示列表（根据登录状态条件渲染） -->
    <div class="w-full h-[calc(100%-50px)]">
      <!-- 已登录状态 -->
      <template v-if="userStore.isLoggedIn">
        <!-- 有文章时显示列表 -->
        <div
          v-if="essayStore.userEssays.length > 0"
          id="essayList"
          class="bg-white/50 pt-[20px] pl-[4px] pb-[20px] grid grid-cols-1 auto-rows-min gap-1 overflow-y-auto z-0 w-full h-full"
        >
          <!-- 简要信息卡 -->
          <div
            v-for="essay in essayStore.userEssays"
            :key="essay.id"
            style="aspect-ratio: 16 / 9 ; font-family: 'Gill Sans', 'Gill Sans MT', Calibri, 'Trebuchet MS', sans-serif;"
            class="bg-slate-50 m-1 rounded-[10px] border-slate-300 border-2 grid grid-rows-[50%_1fr_30px]"
          >
            <!-- 信息卡上部 -->
            <div
              class="row-span-1 p-[10px] grid grid-cols-[50%_50%]"
              @click="openEssayDetail(essay.id)"
            >
              <!-- 封面 -->
              <div
                style="aspect-ratio: 16 / 9"
                :style="{
                  backgroundImage: `url(${getCoverImage(essay)})`,
                  backgroundSize: 'cover',
                  backgroundRepeat: 'no-repeat',
                  backgroundPosition: 'center',
                }"
                class="w-[95%]"
              ></div>
              <!-- 上部右侧内容区 -->
              <div  class="col-span-1">
                <div style="font-size: 15px;" class="mb-2 ">{{ essay.title }}</div>
                <div 
                 class="text-slate-500 font-light text-xs mt-[-5px]">
                  创建时间:{{ formatDate(essay.createdAt) }}
                </div>
                <div class="text-slate-500 font-light text-xs">
                  更新时间:{{ formatDate(essay.updatedAt) }}
                </div>
                <!--简介-->
                <div class="w-[270px] h-[90px] bg-slate-400/60 p-[3px]">

                  <div style="border-color: aliceblue;" class="p-[2px] w-full h-full border-2 border-dashed">

                    <textarea style="border: none;
                    outline: none;
                    appearance: none;
                    font-family: 'squareChinese';
                    color: #3e3e3e;
                    font-size: 13px;
                    scrollbar-width: none;
                    resize: none;
                    padding: 0 10px;" 
                    readonly class=" w-full h-full">{{ essay ? essay.summary : '暂无简介' }}</textarea>

                  </div>
                </div>
                <!--简介(ED)-->
                
              </div>
            </div>
            <!-- 信息卡中部 -->
            <div class="row-span-1 border-b-2 border-t-2 border-slate-300 pr-2 pl-2">
              <div
                class="max-h-full overflow-y-auto line-clamp-6 break-words cursor-pointer bg-slate-200 pt-1 h-[100%] text-sm font-normal leading-snug text-gray-800 hide-scrollbar"
                @click="openEssayDetail(essay.id)"
              >
                {{ essay.content }}
              </div>
            </div>
            <!-- 信息卡下部 -->
            <div class="row-span-1 grid grid-cols-[1fr_30px_40px]">
              <div class="col-span-1"></div>
              <button class="px-[10px] py-1 text-slate-400 rounded text-sm hover:text-slate-600 transition-colors ml-auto">
                <PencilLine @click="openEditModule(essay.id)" class="w-[20px]" />
              </button>
              <button class="px-1 py-1 text-slate-400 rounded text-sm hover:text-slate-600 transition-colors mr-[20px]">
                <Trash @click="openDeleteModule(essay.id)" class="w-[20px]" />
              </button>
            </div>
          </div>
          <!-- 简要信息卡(ED) -->
        </div>
        <!-- 已登录但无文章时的提示 -->
        <div v-else class="w-full h-full flex items-center justify-center text-gray-500">
          暂无文章
        </div>
      </template>

      <!-- 未登录状态提示 -->
      <template v-else>
        <div class="w-full h-full flex items-center justify-center text-gray-500">
          暂未登录，请先登录后查看您的文章。
        </div>
      </template>
    </div>

    <!-- 创建帖子弹窗（仅在已登录时显示？但保留按钮已在父组件，这里弹窗仍可显示，但创建后刷新会失败，建议在父组件控制“创建新帖子”按钮显示，不过暂且保留） -->
    <EssayCreateModule
      :isVisible="showCreateModule"
      @close="closeCreateModule"
      @essay-created="handleEssayCreated"
    />

    <!-- 编辑帖子弹窗 -->
    <EssayEditModule
      :isVisible="showEditModule"
      :edit-id="currentEditid"
      @close="closeEditModule"
    />

    <!-- 删除帖子询问弹窗 -->
<Transition name="fade-scale">
  <div
    v-if="showDeleteModule"
    class="fixed inset-0 bg-gray-900/40 backdrop-blur-sm flex items-center justify-center z-50"
    @click.self="closeDeleteModule"
  >
    <div class="bg-white/95 backdrop-blur-sm rounded-2xl shadow-2xl p-8 w-[90%] max-w-md border border-gray-100 transform transition-all">
      <div class="flex items-center mb-5">
        <div class="w-12 h-12 bg-gradient-to-br from-red-400 to-red-600 rounded-full flex items-center justify-center mr-4 shadow-lg shadow-red-200">
          <Trash class="w-6 h-6 text-white" />
        </div>
        <h3 class="text-xl font-bold text-gray-800">删除确认</h3>
      </div>
      
      <div class="mb-7">
        <p class="text-gray-700 text-base leading-relaxed">
          确定要删除这篇帖子吗？
        </p>
        <p class="text-sm text-gray-500 mt-2 flex items-center">
          <svg class="w-4 h-4 mr-1 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          删除后无法恢复，请谨慎操作。
        </p>
      </div>
      
      <div class="flex justify-end gap-3">
        <button
          @click="closeDeleteModule"
          class="px-5 py-2.5 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-all duration-200 border border-gray-200 hover:border-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-300"
        >
          取消
        </button>
        <button
          @click="deleteEssay"
          class="px-5 py-2.5 text-sm font-medium text-white bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 rounded-lg shadow-md shadow-red-200 hover:shadow-lg transform hover:-translate-y-0.5 active:translate-y-0 transition-all duration-200 flex items-center focus:outline-none focus:ring-2 focus:ring-red-400"
        >
          <Trash class="w-4 h-4 mr-2" />
          确认删除
        </button>
      </div>
    </div>
  </div>
</Transition>
    <!-- 删除帖子询问弹窗(ED) -->
  </div>
  <!-- 作品列表(ED) -->
</template>

<style scoped>

.top_shadow{
    box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.1), 0 -2px 4px -1px rgba(0, 0, 0, 0.06);
}
.bottom_shadow{
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}
.classic_shadow {
  box-shadow: 2px 4px 10px 10px rgba(0, 0, 0, 0.1), 0 -2px 10px 8px rgba(0, 0, 0, 0.06);
}
.little_shadow {
  box-shadow: 2px 4px 10px 5px rgba(0, 0, 0, 0.1), 0 -2px 10px 8px rgba(0, 0, 0, 0.06);
}
.inset_shadow {
  box-shadow: inset 2px 4px 10px 5px rgba(0, 0, 0, 0.1), inset 0 -2px 10px 8px rgba(0, 0, 0, 0.06);
}

/* 样式保持不变 */
.classic_shadow {
  box-shadow: 2px 4px 6px 5px rgba(0, 0, 0, 0.1), 0 -2px 8px 8px rgba(0, 0, 0, 0.06);
}

#essayList::-webkit-scrollbar {
  width: 8px;
}
#essayList::-webkit-scrollbar-thumb {
  background: #858585;
  border: 2px solid transparent;
  background-clip: padding-box;
  min-height: 40px;
}

.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.2s ease;
}
.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
.fade-scale-enter-to,
.fade-scale-leave-from {
  opacity: 1;
  transform: scale(1);
}

.hide-scrollbar {
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.hide-scrollbar::-webkit-scrollbar {
  display: none;
}

.user_card{
  .classic_shadow {
  box-shadow: 2px 4px 10px 5px rgba(0, 0, 0, 0.05), 0 -2px 10px 4px rgba(0, 0, 0, 0.06);
  }
}
</style>