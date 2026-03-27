<!-- src/components/ImageCropperModal.vue -->
<template>
  <!-- 模态框背景 -->
  <Transition name="fade">
    <div 
      v-if="visible" 
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/70"
      @click.self="handleBackdropClick"
    >
      <!-- 模态框主体 -->
      <Transition name="slide-up">
        <div 
          v-if="visible"
          class="bg-white rounded-lg shadow-xl w-[90vw] max-w-6xl max-h-[90vh] overflow-hidden flex flex-col"
        >
          <!-- 头部 -->
          <div class="flex items-center justify-between p-6 border-b border-gray-200">
            <div>
              <h3 class="text-xl font-semibold text-gray-800">{{ titleText }}</h3>
              <p class="text-sm text-gray-600 mt-1">请确保重要内容在{{ aspectRatioText }}比例框内</p>
            </div>
            <button 
              @click="close"
              class="text-gray-400 hover:text-gray-600 transition-colors text-2xl w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100"
            >
              &times;
            </button>
          </div>
          
          <!-- 主要内容区域 -->
          <div class="flex-1 flex overflow-hidden">
            <!-- 左侧：裁剪操作区 -->
            <div class="w-3/5 border-r border-gray-200 p-6 flex flex-col">
              <!-- 裁剪容器 -->
              <div class="flex-1 mb-6">
                <div class="mb-4">
                  <div class="text-sm text-gray-600 mb-2">拖拽图片调整位置，缩放调整大小</div>
                  <div 
                    ref="cropContainer"
                    class="relative bg-gray-100 rounded-lg overflow-hidden"
                    style="height: 400px;">
                    <img
                      ref="imageElement"
                      :src="imageSrc"
                      class="absolute inset-0 w-full h-full"
                      alt="裁剪预览"
                      crossorigin="anonymous"
                    />
                  </div>
                </div>
              </div>
              
              <!-- 操作按钮组 -->
              <div class="flex flex-wrap gap-3 pt-4 border-t border-gray-200">
                <button 
                  @click="resetCrop"
                  class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors flex items-center gap-2"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                  重置
                </button>
                
                <button 
                  @click="zoomIn"
                  class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors flex items-center gap-2"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7" />
                  </svg>
                  放大
                </button>
                
                <button 
                  @click="zoomOut"
                  class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors flex items-center gap-2"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM13 10H7" />
                  </svg>
                  缩小
                </button>
                
                <button 
                  @click="rotateLeft"
                  class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors flex items-center gap-2"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
                  </svg>
                  左旋转
                </button>
                
                <button 
                  @click="rotateRight"
                  class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors flex items-center gap-2"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
                  </svg>
                  右旋转
                </button>
              </div>
            </div>
            
            <!-- 右侧：预览和确认区 -->
<div class="w-2/5 flex flex-col h-full">
  <!-- 滚动内容区 -->
  <div class="flex-1 overflow-y-auto px-6 pt-6 scrollbar-hidden">
    <div class="space-y-6">
      <!-- 预览区域 -->
      <div>
        <div class="flex items-center justify-between mb-3">
          <h4 class="text-sm font-semibold text-gray-700">预览</h4>
          <span class="text-xs text-gray-500">{{ aspectRatioText }}比例</span>
        </div>
        <div 
          class="bg-gray-50 rounded-lg overflow-hidden border-2 border-gray-300"
          :style="{ aspectRatio: previewAspectRatioStyle, height: '200px' }"
        >
          <div 
            v-if="croppedPreview"
            :style="{
              backgroundImage: `url(${croppedPreview})`,
              backgroundSize: 'cover',
              backgroundPosition: 'center',
              width: '100%',
              height: '100%'
            }"
          ></div>
          <div v-else class="flex flex-col items-center justify-center h-full text-gray-400 p-4">
            <svg class="w-12 h-12 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <p class="text-sm text-center">调整图片后，预览将在这里显示</p>
          </div>
        </div>
      </div>
      
      <!-- 操作说明 -->
      <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h5 class="text-sm font-medium text-blue-800 mb-2">操作说明</h5>
        <ul class="text-xs text-blue-700 space-y-1">
          <li class="flex items-start gap-2"><span class="text-blue-500">•</span>拖拽图片调整显示区域</li>
          <li class="flex items-start gap-2"><span class="text-blue-500">•</span>放大/缩小：调整图片大小</li>
          <li class="flex items-start gap-2"><span class="text-blue-500">•</span>旋转：调整图片方向</li>
          <li class="flex items-start gap-2"><span class="text-blue-500">•</span>确保重要内容在灰色边框内</li>
        </ul>
      </div>
      
      <!-- 图片信息 -->
      <div class="bg-gray-50 rounded-lg p-4" v-if="originalWidth > 0">
        <h5 class="text-sm font-medium text-gray-700 mb-2">图片信息</h5>
        <div class="text-xs text-gray-600 space-y-1">
          <div class="flex justify-between"><span>原始尺寸：</span><span class="font-mono">{{ originalWidth }} × {{ originalHeight }}</span></div>
          <div class="flex justify-between"><span>原始比例：</span><span class="font-mono">{{ originalRatio.toFixed(2) }}:1</span></div>
          <div class="flex justify-between"><span>目标比例：</span><span class="font-mono text-blue-600">{{ aspectRatioText }} ({{ props.aspectRatio.toFixed(2) }}:1)</span></div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 确认按钮（固定在底部） -->
  <div class="px-6 pb-6 pt-4 border-t border-gray-200">
    <div class="flex gap-3">
      <button @click="close" class="flex-1 py-3 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors">取消</button>
      <button @click="confirmCrop" :disabled="!croppedPreview || isProcessing" class="flex-1 py-3 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2">
        <svg v-if="isProcessing" class="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        {{ isProcessing ? '处理中...' : '确认裁剪' }}
      </button>
    </div>
  </div>
</div>
          </div>
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick ,computed } from 'vue'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'

const aspectRatioText = computed(() => {
  if (props.aspectRatio === 1) return '1:1'
  if (Math.abs(props.aspectRatio - 16/9) < 0.01) return '16:9'
  return `${props.aspectRatio.toFixed(2)}:1`
})

const previewAspectRatioStyle = computed(() => props.aspectRatio)

const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  },
  imageFile: {
    type: File,
    required: true
  },
  aspectRatio: {
    type: Number,
    default: 16 / 9
  }
})

const titleText = computed(() => {
  if (props.aspectRatio === 1) return '调整头像图片'
  if (Math.abs(props.aspectRatio - 16/9) < 0.01) return '调整封面图片'
  return '调整图片'
})

const emit = defineEmits(['update:visible', 'crop-complete'])

// DOM 引用
const imageElement = ref(null)
const cropContainer = ref(null)

// 裁剪器实例
let cropper = null

// 状态
const imageSrc = ref('')
const croppedPreview = ref(null)
const isProcessing = ref(false)

// 图片信息
const originalWidth = ref(0)
const originalHeight = ref(0)
const originalRatio = ref(0)

// 初始化裁剪器
const initCropper = () => {
  if (!imageElement.value) return
  
  // 销毁旧的裁剪器
  if (cropper) {
    cropper.destroy()
    cropper = null
  }
  
  console.log('🔄 初始化裁剪器...')
  
  try {
    cropper = new Cropper(imageElement.value, {
      aspectRatio: props.aspectRatio,
      viewMode: 1,
      dragMode: 'move',
      autoCropArea: 0.8,
      restore: false,
      guides: true,
      center: true,
      highlight: true,
      cropBoxMovable: true,
      cropBoxResizable: true,
      toggleDragModeOnDblclick: false,
      ready() {
        console.log('✅ 裁剪器准备就绪')
        updatePreview()
      },
      crop() {
        updatePreview()
      },
      error(event) {
        console.error('裁剪器错误:', event)
        alert('图片加载失败，请检查图片格式')
      }
    })
  } catch (error) {
    console.error('裁剪器初始化失败:', error)
    alert('裁剪器初始化失败，请刷新页面重试')
  }
}

// 更新裁剪预览
const updatePreview = () => {
  if (!cropper) return
  try {
    const previewWidth = 400
    const previewHeight = previewWidth / props.aspectRatio
    const maxSize = 1600
    const canvas = cropper.getCroppedCanvas({
      width: maxSize,
      height: maxSize / props.aspectRatio,
      fillColor: '#ffffff',
      imageSmoothingEnabled: true,
      imageSmoothingQuality: 'high'
    })
    croppedPreview.value = canvas.toDataURL('image/jpeg', 0.9)
  } catch (error) {
    console.error('预览更新失败:', error)
  }
}

// 操作函数
const zoomIn = () => {
  if (cropper) cropper.zoom(0.1)
}

const zoomOut = () => {
  if (cropper) cropper.zoom(-0.1)
}

const rotateLeft = () => {
  if (cropper) cropper.rotate(-90)
}

const rotateRight = () => {
  if (cropper) cropper.rotate(90)
}

const resetCrop = () => {
  if (cropper) {
    cropper.reset()
    updatePreview()
  }
}

// 确认裁剪
const confirmCrop = async () => {
  if (!cropper || !croppedPreview.value || isProcessing.value) return
  isProcessing.value = true
  try {
    const maxWidth = 1600
    const maxHeight = maxWidth / props.aspectRatio
    const canvas = cropper.getCroppedCanvas({
      width: maxWidth,
      height: maxHeight,
      fillColor: '#ffffff',
      imageSmoothingEnabled: true,
      imageSmoothingQuality: 'high'
    })
    canvas.toBlob((blob) => {
      try {
        const timestamp = Date.now()
        const originalName = props.imageFile.name
        const nameWithoutExt = originalName.replace(/\.[^/.]+$/, "")
        const croppedFileName = `${nameWithoutExt}_cropped_${timestamp}.jpg`
        const croppedFile = new File([blob], croppedFileName, {
          type: 'image/jpeg',
          lastModified: Date.now()
        })
        emit('crop-complete', croppedFile, croppedPreview.value)
        close()
      } catch (error) {
        console.error('裁剪文件创建失败:', error)
        alert('裁剪文件创建失败，请重试')
      } finally {
        isProcessing.value = false
      }
    }, 'image/jpeg', 0.9)
  } catch (error) {
    console.error('裁剪处理失败:', error)
    alert('裁剪失败，请重试')
    isProcessing.value = false
  }
}

// 关闭模态框
const close = () => {
  if (imageSrc.value && imageSrc.value.startsWith('blob:')) {
    URL.revokeObjectURL(imageSrc.value)
  }
  
  if (cropper) {
    cropper.destroy()
    cropper = null
  }
  
  imageSrc.value = ''
  croppedPreview.value = null
  isProcessing.value = false
  originalWidth.value = 0
  originalHeight.value = 0
  originalRatio.value = 0
  
  emit('update:visible', false)
}

// 点击背景关闭
const handleBackdropClick = () => {
  close()
}

// 监听visible变化
watch(() => props.visible, async (newVisible) => {
  if (newVisible && props.imageFile) {
    console.log('🖼️ 打开裁剪器，文件:', props.imageFile.name)
    
    const reader = new FileReader()
    reader.onload = (e) => {
      imageSrc.value = e.target.result
      
      const img = new Image()
      img.onload = () => {
        originalWidth.value = img.width
        originalHeight.value = img.height
        originalRatio.value = img.width / img.height
        
        console.log('📏 原始图片信息:', {
          width: img.width,
          height: img.height,
          ratio: originalRatio.value.toFixed(2)
        })
        
        nextTick(() => {
          if (imageElement.value) {
            setTimeout(() => {
              initCropper()
            }, 100)
          } else {
            console.error('图片元素未找到')
          }
        })
        
        URL.revokeObjectURL(img.src)
      }
      img.onerror = () => {
        console.error('图片加载失败')
        alert('图片加载失败，请重试')
      }
      img.src = e.target.result
    }
    
    reader.onerror = () => {
      console.error('文件读取失败')
      alert('文件读取失败，请重试')
    }
    
    reader.readAsDataURL(props.imageFile)
  }
})

// 组件卸载时清理
onUnmounted(() => {
  if (cropper) {
    cropper.destroy()
  }
  if (imageSrc.value && imageSrc.value.startsWith('blob:')) {
    URL.revokeObjectURL(imageSrc.value)
  }
})
</script>
<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
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
</style>