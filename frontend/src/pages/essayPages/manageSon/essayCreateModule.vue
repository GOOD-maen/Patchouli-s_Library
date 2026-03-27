<!-- src/components/EssayCreateModal.vue -->
<template>
  <Transition name="fade">
    <div 
      v-if="isVisible" 
      class="fixed inset-0 bg-slate-500/30 flex items-center justify-center z-50"
      @click="handleBackdropClick"
    >
      <Transition name="slide-up">
        <div 
          v-if="isVisible"
          class="bg-white rounded-lg shadow-xl w-[90vw] max-w-2xl max-h-[90vh] overflow-hidden flex flex-col"
          @click.stop
        >
          <div class="flex justify-between items-center p-6 border-b border-gray-200">
            <h3 class="text-xl font-semibold text-gray-800">创建新帖子</h3>
            <button 
              @click="close"
              class="text-gray-400 hover:text-gray-600 transition-colors text-2xl w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100"
            >
              &times;
            </button>
          </div>
          
          <div class="flex-1 overflow-y-auto p-6">
            <form @submit.prevent="submitForm" class="space-y-6">
              <div>
                <label for="title" class="block text-sm font-medium text-gray-700 mb-2">
                  帖子标题 *
                </label>
                <input
                  id="title"
                  v-model="formData.title"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                  placeholder="请输入帖子标题"
                />
              </div>

              <div>
                <label for="content" class="block text-sm font-medium text-gray-700 mb-2">
                  帖子内容 *
                </label>
                <textarea
                  id="content"
                  v-model="formData.content"
                  required
                  rows="6"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-vertical transition-all duration-200"
                  placeholder="请输入帖子内容..."
                ></textarea>
              </div>

              <!-- 文件上传区域 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  封面图片
                </label>
                <div class="mb-2 text-xs text-gray-500">
                  建议使用16:9比例的图片（如1600×900、800×450）
                </div>
                <div 
                  @click="triggerFileInput"
                  class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center cursor-pointer hover:border-blue-400 transition-all duration-200"
                  :class="{ 'border-blue-400 bg-blue-50': isDragging }"
                >
                  <input
                    type="file"
                    ref="fileInput"
                    @change="handleFileSelect"
                    accept="image/*"
                    class="hidden"
                  />
                  
                  <div v-if="!selectedFile" class="text-gray-500">
                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 48 48">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M28 8H12a4 4 0 00-4 4v20a4 4 0 004 4h16a4 4 0 004-4V20L28 8z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 20h8M16 28h6" />
                    </svg>
                    <p class="mt-2">点击选择图片或拖拽到此处</p>
                    <p class="text-xs text-gray-400 mt-1">支持 JPG, PNG, GIF 格式，最大5MB</p>
                  </div>

                  <div v-else class="flex flex-col items-center">
                    <div class="relative">
                      <img 
                        :src="imagePreview" 
                        alt="封面预览" 
                        class="max-h-48 rounded-lg shadow-md mx-auto"
                      />
                      <button
                        @click.stop="removeImage"
                        class="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-sm hover:bg-red-600 transition-colors"
                      >
                        &times;
                      </button>
                    </div>
                    <p class="text-sm text-gray-600 mt-2">{{ selectedFile.name }}</p>
                    <p class="text-xs text-gray-500 mt-1">
                      尺寸: {{ imageInfo.width }} × {{ imageInfo.height }} | 
                      比例: {{ imageInfo.ratio.toFixed(2) }}:1
                    </p>
                  </div>
                </div>
              </div>

              <div>
                <label for="category" class="block text-sm font-medium text-gray-700 mb-2">
                  分类
                </label>
                <select
                  id="category"
                  v-model="formData.category"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                >
                  <option value="技术讨论">技术讨论</option>
                  <option value="生活随笔">生活随笔</option>
                  <option value="问题求助">问题求助</option>
                  <option value="其他">其他</option>
                </select>
              </div>
            </form>
          </div>
          
          <div class="flex justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50">
            <button 
              @click="close"
              class="px-6 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-100 transition-all duration-200"
            >
              取消
            </button>
            <button 
              @click="submitForm"
              :disabled="!formData.title || !formData.content || isUploading"
              class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-all duration-200 flex items-center gap-2"
            >
              <svg v-if="isUploading" class="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ isUploading ? '上传中...' : '创建帖子' }}
            </button>
          </div>
        </div>
      </Transition>
    </div>
  </Transition>

  <!-- 图片裁剪模态框 -->
  <ImageCropperModal
    v-model:visible="showCropper"
    :image-file="imageToCrop"
    @crop-complete="handleCropComplete"
  />
</template>

<script setup>
import { ref, watch } from 'vue'
import ImageCropperModal from '@/components/ImageCropperModal.vue'

const props = defineProps({
  isVisible: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['close', 'essay-created'])

const fileInput = ref(null)
const selectedFile = ref(null)
const imagePreview = ref('')
const isUploading = ref(false)
const isDragging = ref(false)

// 裁剪相关状态
const showCropper = ref(false)
const imageToCrop = ref(null)
const imageInfo = ref({
  width: 0,
  height: 0,
  ratio: 0
})

const formData = ref({
  title: '',
  content: '',
  coverUrl: '',
  category: '技术讨论'
})

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 检查图片比例，决定是否需要裁剪
    checkImageRatio(file)
  }
}

// 检查图片比例
const checkImageRatio = (file) => {
  const img = new Image()
  img.onload = () => {
    const ratio = img.width / img.height
    const targetRatio = 16/9
    const tolerance = 0.1  // 允许10%的误差
    
    // 更新图片信息
    imageInfo.value = {
      width: img.width,
      height: img.height,
      ratio: ratio
    }
    
    console.log('📏 图片原始尺寸:', img.width, '×', img.height)
    console.log('📐 原始比例:', ratio.toFixed(3), '目标比例:', targetRatio.toFixed(3))
    
    // 如果比例接近16:9，直接使用
    if (Math.abs(ratio - targetRatio) < tolerance) {
      console.log('✅ 图片比例接近16:9，直接使用')
      processSelectedFile(file)
    } else {
      console.log('✂️ 图片比例不是16:9，需要裁剪')
      // 保存文件并打开裁剪器
      imageToCrop.value = file
      showCropper.value = true
    }
    
    // 释放临时URL
    URL.revokeObjectURL(img.src)
  }
  img.onerror = () => {
    console.error('❌ 图片加载失败')
    alert('图片加载失败，请重试')
  }
  img.src = URL.createObjectURL(file)
}

const processSelectedFile = (file) => {
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件！')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB！')
    return
  }

  selectedFile.value = file
  formData.value.coverUrl = ''
  
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

// 处理裁剪完成的回调
const handleCropComplete = async (croppedFile, previewUrl) => {
  console.log('✅ 裁剪完成，新文件:', croppedFile.name, croppedFile.size)
  
  try {
    // 显示上传状态
    isUploading.value = true
    
    // 上传裁剪后的图片
    console.log('📤 开始上传裁剪后的图片...')
    const newCoverUrl = await uploadImage(croppedFile)
    console.log('✅ 裁剪图片上传成功，URL:', newCoverUrl)
    
    // 更新预览和表单数据
    imagePreview.value = previewUrl
    selectedFile.value = croppedFile
    formData.value.coverUrl = newCoverUrl
    
    // 更新图片信息（裁剪后的图片是16:9）
    imageInfo.value = {
      width: 1600,  // 裁剪后的标准尺寸
      height: 900,
      ratio: 16/9
    }
    
  } catch (error) {
    console.error('💥 裁剪图片上传失败:', error)
    alert('裁剪图片上传失败: ' + error.message)
    
    // 如果上传失败，使用预览图片（但可能无法持久化）
    imagePreview.value = previewUrl
    selectedFile.value = croppedFile
    formData.value.coverUrl = '' // 清空URL，提交时会重新上传
    
    // 询问用户是否继续
    const shouldContinue = confirm('裁剪图片上传失败，是否继续使用裁剪后的图片？\n（注意：如果继续，提交时会尝试重新上传）')
    if (!shouldContinue) {
      removeImage()
    }
  } finally {
    isUploading.value = false
  }
}

const removeImage = () => {
  selectedFile.value = null
  imagePreview.value = ''
  imageInfo.value = { width: 0, height: 0, ratio: 0 }
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const uploadImage = async (file) => {
  console.log('🔄 开始上传文件:', file)
  
  const formDataToSend = new FormData()
  formDataToSend.append('file', file)
  
  try {
    console.log('🌐 发送请求到: http://localhost:8080/api/upload')
    
    const response = await fetch('http://localhost:8080/api/upload', {
      method: 'POST',
      body: formDataToSend
    })
    
    console.log('📡 收到响应，状态码:', response.status)
    
    if (!response.ok) {
      const errorText = await response.text()
      console.log('❌ 错误响应内容:', errorText)
      throw new Error(`上传失败: ${response.status}`)
    }
    
    const result = await response.json()
    console.log('✅ 上传成功:', result)
    return result.fileUrl
    
  } catch (error) {
    console.error('💥 请求异常:', error)
    throw error
  }
}

const close = () => {
  emit('close')
}

const handleBackdropClick = (event) => {
  if (event.target === event.currentTarget) {
    close()
  }
}

const submitForm = async () => {
  if (!formData.value.title.trim() || !formData.value.content.trim()) {
    alert('请填写标题和内容！')
    return
  }

  isUploading.value = true

  try {
    let coverUrl = formData.value.coverUrl
    
    // 如果有选择的文件但还没有上传（比如裁剪后的文件）
    if (selectedFile.value && !coverUrl) {
      coverUrl = await uploadImage(selectedFile.value)
    }

    const response = await fetch('http://localhost:8080/api/essay', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        ...formData.value,
        coverUrl: coverUrl || ''
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const newEssay = await response.json()
    resetForm()
    emit('essay-created', newEssay)
    
  } catch (error) {
    console.error('创建帖子失败:', error)
    alert('创建失败，请重试！')
  } finally {
    isUploading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    title: '',
    content: '',
    coverUrl: '',
    category: '技术讨论'
  }
  removeImage()
  imageInfo.value = { width: 0, height: 0, ratio: 0 }
}

watch(() => props.isVisible, (newVal) => {
  if (newVal) {
    resetForm()
  }
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

input:focus, textarea:focus, select:focus {
  transform: translateY(-1px);
  box-shadow: 0 10px 25px -5px rgba(59, 130, 246, 0.1), 0 10px 10px -5px rgba(59, 130, 246, 0.04);
}
</style>