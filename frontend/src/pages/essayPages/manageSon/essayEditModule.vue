<!-- src/pages/essayPages/manageSon/essayEditModule.vue -->
<script setup>
import { computed, onMounted, watch, ref } from 'vue'
import { useEssayStore } from '@/stores/essayStore'
import { useUserStore } from '@/stores/user'
import ImageCropperModal from '@/components/ImageCropperModal.vue'

const userStore = useUserStore()

const props = defineProps({
  isVisible: Boolean,
  editId: [String, Number]
})

const emit = defineEmits(['close'])

const essayStore = useEssayStore()

const essayToEdit = computed(() => {
  return essayStore.getEssayById(props.editId)
})

// 显示文章名
const essayTitle = computed(() => {
  return essayToEdit.value?.title || '未知文章'
})

watch(() => props.isVisible, (isVisible) => {
  if (isVisible && props.editId) {
    console.log('📌 弹窗打开，当前编辑ID:', props.editId)
    console.log('📝 对应文章数据:', essayToEdit.value)
  }
})

// 监听ID变化
watch(() => props.editId, (newId) => {
  if (newId && props.isVisible) {
    console.log('🔄 ID变化为:', newId)
    console.log('📝 新ID对应文章:', essayToEdit.value)
  }
})

onMounted(async () => {
  if (!essayStore.hasLoaded) {
    await essayStore.fetchEssayList()
  }
})

// 关闭弹窗
const closeDialog = () => {
  emit('close')
}

//////////////coverImage背景图获取方法///////////////
const getCoverImage = (essay) => {
  if (essay?.coverUrl && essay.coverUrl.trim() !== '') {
    if (essay.coverUrl.startsWith('http')) {
      return essay.coverUrl
    }
    return `http://localhost:8080/uploads/${essay.coverUrl}`
  }
  return defaultCoverImage
}
//////////////coverImage(ED)/////////////////////////

///////////////////图片选择方法///////////////////////
const fileInput = ref(null)
const previewImageUrl = ref(null)
const selectedFile = ref(null)
const isUploading = ref(false)

const triggerFileInput = () => {
  fileInput.value.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file && file.type.startsWith('image/')) {
    checkImageRatio(file)
  } else {
    alert('请选择有效的图片文件')
  }
}

const checkImageRatio = (file) => {
  const img = new Image()
  img.onload = () => {
    const ratio = img.width / img.height
    const targetRatio = 16/9
    const tolerance = 0.1

    imageInfo.value = {
      width: img.width,
      height: img.height,
      ratio: ratio
    }

    console.log('📏 图片原始尺寸:', img.width, '×', img.height)
    console.log('📐 原始比例:', ratio.toFixed(3), '目标比例:', targetRatio.toFixed(3))

    if (Math.abs(ratio - targetRatio) < tolerance) {
      console.log('✅ 图片比例接近16:9，直接使用')
      processSelectedFile(file)
    } else {
      console.log('✂️ 图片比例不是16:9，需要裁剪')
      imageToCrop.value = file
      showCropper.value = true
    }
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
    previewImageUrl.value = e.target.result
  }
  reader.readAsDataURL(file)
}

const removeImage = () => {
  if (previewImageUrl.value && previewImageUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(previewImageUrl.value)
  }
  previewImageUrl.value = null
  selectedFile.value = null
  imageInfo.value = { width: 0, height: 0, ratio: 0 }
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  formData.value.coverUrl = ''
}
/////////////图片裁剪/////////////////
const showCropper = ref(false)
const imageToCrop = ref(null)
const imageInfo = ref({
  width: 0,
  height: 0,
  ratio: 0
})

const handleCropComplete = async (croppedFile, previewUrl) => {
  console.log('✅ 裁剪完成，新文件:', croppedFile.name, croppedFile.size)

  try {
    isUploading.value = true
    console.log('📤 开始上传裁剪后的图片...')
    const newCoverUrl = await uploadCoverImage(croppedFile)
    console.log('✅ 裁剪图片上传成功，URL:', newCoverUrl)

    previewImageUrl.value = previewUrl
    selectedFile.value = croppedFile
    formData.value.coverUrl = newCoverUrl

    imageInfo.value = {
      width: 1600,
      height: 900,
      ratio: 16/9
    }
  } catch (error) {
    console.error('💥 裁剪图片上传失败:', error)
    alert('裁剪图片上传失败: ' + error.message)

    previewImageUrl.value = previewUrl
    selectedFile.value = croppedFile
    formData.value.coverUrl = ''

    const shouldContinue = confirm('裁剪图片上传失败，是否继续使用裁剪后的图片？\n（注意：如果继续，提交时会尝试重新上传）')
    if (!shouldContinue) {
      removeImage()
    }
  } finally {
    isUploading.value = false
  }
}
/////////////图片裁剪(ED)/////////////////

////////////////////////时间展示方法/////////////////////////////
const formattedDates = computed(() => {
  const formatDate = (dateString) => {
    if (!dateString) return '--'
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return dateString
    return date.toISOString().split('T')[0]
  }

  return {
    createdAt: formatDate(essayToEdit.value?.createdAt),
    updatedAt: formatDate(essayToEdit.value?.updatedAt)
  }
})
////////////////////////时间展示方法(ED)/////////////////////////////

////////////////////////修改方法////////////////////////
const defaultCoverImage = '@/resources/pictures/test.png'

// 表单数据
const formData = ref({
  id: '',
  title: '',
  content: '',
  coverUrl: '',
  category: '技术讨论' // 保留但忽略
})

// 监控 essayToEdit 变化，初始化表单
watch(() => essayToEdit.value, (newEssay) => {
  if (newEssay) {
    formData.value = {
      id: newEssay.id,
      title: newEssay.title || '',
      content: newEssay.content || '',
      coverUrl: newEssay.coverUrl || '',
      category: newEssay.category || '技术讨论'
    }

    if (newEssay.coverUrl && newEssay.coverUrl.trim() !== '') {
      previewImageUrl.value = getCoverImage(newEssay)
    } else {
      previewImageUrl.value = null
    }

    selectedFile.value = null
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}, { immediate: true })

const uploadCoverImage = async (file = null) => {
  const uploadFile = file || selectedFile.value

  if (!uploadFile) {
    return formData.value.coverUrl || ''
  }

  isUploading.value = true

  console.log('🔄 开始上传文件:', uploadFile.name, uploadFile.size)

  const formDataToSend = new FormData()
  formDataToSend.append('file', uploadFile)

  try {
    console.log('🌐 发送请求到: http://localhost:8080/api/upload')

    const response = await fetch('http://localhost:8080/api/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      },
      body: formDataToSend
    })

    console.log('📡 收到响应，状态码:', response.status)

    if (!response.ok) {
      const errorText = await response.text()
      console.log('❌ 错误响应内容:', errorText)
      throw new Error(`上传失败: ${response.status}`)
    }

    const result = await response.json()
    console.log('✅ 上传成功，返回结果:', result)

    const uploadedUrl = result.fileUrl || result.url || ''
    return uploadedUrl
  } catch (error) {
    console.error('💥 上传请求异常:', error)

    const shouldContinue = window.confirm(`图片上传失败: ${error.message}\n是否继续更新文章（将保持原封面或无封面）？`)
    if (shouldContinue) {
      return formData.value.coverUrl || ''
    } else {
      throw new Error('用户取消了操作')
    }
  } finally {
    isUploading.value = false
  }
}

const updateEssay = async () => {
  if (!formData.value.title.trim()) {
    alert('标题不能为空')
    return
  }

  if (!formData.value.content.trim()) {
    alert('内容不能为空')
    return
  }

  try {
    console.log('🔄 开始更新流程...')
    console.log('📝 原始表单数据:', formData.value)

    let coverUrlToSend = formData.value.coverUrl || ''

    if (selectedFile.value) {
      console.log('📤 检测到新图片，开始上传...')
      const uploadedUrl = await uploadCoverImage()
      console.log('✅ 上传成功，新封面URL:', uploadedUrl)
      coverUrlToSend = uploadedUrl
    } else if (!formData.value.coverUrl && previewImageUrl.value) {
      console.log('🗑️ 用户移除了封面图片')
      coverUrlToSend = ''
    }

    const updatePayload = {
      title: formData.value.title.trim(),
      content: formData.value.content.trim(),
      coverUrl: coverUrlToSend,
      category: formData.value.category.trim() || '技术讨论'
    }

    console.log('📤 发送更新数据:', updatePayload)
    console.log('📤 更新URL:', `http://localhost:8080/api/essay/${formData.value.id}`)

    const response = await fetch(`http://localhost:8080/api/essay/${formData.value.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userStore.token}`  // 关键：添加 token
      },
      body: JSON.stringify(updatePayload)
    })

    if (!response.ok) {
      const errorText = await response.text()
      console.error('❌ 更新失败，响应内容:', errorText)
      throw new Error(`更新失败 (${response.status}): ${errorText}`)
    }

    const updatedEssay = await response.json()
    console.log('✅ 更新成功，返回数据:', updatedEssay)

    await essayStore.fetchEssayList()
    console.log('🔄 已刷新文章列表')

    alert('修改成功！')
    closeDialog()
  } catch (error) {
    console.error('💥 更新过程中出错:', error)

    if (error.message === '用户取消了操作') {
      alert('已取消更新操作')
    } else {
      alert(`更新失败: ${error.message}`)
    }
  }
}
////////////////////////修改方法(ED)////////////////////////
</script>

<template>
  <div v-if="isVisible" class="fixed inset-0 bg-slate-500/30 flex items-center justify-center z-50">
    <!-- 编辑框 - 尺寸缩小，采用创建组件的样式 -->
    <div class="bg-white rounded-lg shadow-xl w-[70vw] max-w-[1000px] h-[80vh] overflow-hidden flex flex-col classic_shadow">
      <!-- 头部（可保留简单标题，或移除） -->
      <div class="w-full min-h-[40px] border-b-2 border-slate-300 flex items-center px-4 text-sm text-gray-600">
        <span class="font-semibold">编辑文章：</span>
        <span class="text-blue-500 truncate max-w-[300px]">{{ formData.title || essayTitle }}</span>
        <span class="ml-2 text-xs text-gray-400">ID: {{ formData.id }}</span>
      </div>

      <!-- 主体内容 - 左右两栏，类似创建组件 -->
      <div class="flex-1 flex overflow-hidden">
        <!-- 左侧区域（标题+内容） -->
        <div class="w-[75%] h-full p-4 overflow-y-auto">
          <div class="bg-white w-full h-full flex flex-col">
            <!-- 标题输入 -->
            <div class="w-full relative mb-4">
              <input
                class="title_input"
                type="text"
                placeholder="请输入标题"
                v-model="formData.title"
                required
              />
              <span class="bar"></span>
            </div>

            <!-- 内容输入 -->
            <div class="flex-1 min-h-0">
              <textarea
                class="essay_content_textarea w-full h-full"
                placeholder="请输入文本"
                v-model="formData.content"
                required
              ></textarea>
            </div>
          </div>
        </div>

        <!-- 右侧区域（封面上传、时间、按钮） -->
        <div class="w-[25%] h-full p-4 overflow-y-auto border-l-2 border-slate-200">
          <!-- 封面上传 -->
          <div class="classic_shadow w-full p-4 bg-white rounded-lg mb-4">
            <div class="w-full h-[20px] flex items-center mb-2">
              <div class="stripe"></div>
              <div class="little_title">封面</div>
            </div>
            <div class="mb-2 text-xs text-gray-500">
              建议使用16:9比例的图片（如1600×900、800×450）
            </div>
            <div
              @click="triggerFileInput"
              class="h-[150px] border-2 border-dashed border-gray-300 rounded-lg flex items-center justify-center text-center cursor-pointer hover:border-blue-400 transition-all duration-200"
            >
              <input
                type="file"
                ref="fileInput"
                @change="handleFileSelect"
                accept="image/*"
                class="hidden"
              />

              <div v-if="!previewImageUrl" class="text-gray-500">
                <svg class="mx-auto h-8 w-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 48 48">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M28 8H12a4 4 0 00-4 4v20a4 4 0 004 4h16a4 4 0 004-4V20L28 8z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 20h8M16 28h6" />
                </svg>
                <p class="mt-1 text-xs">点击选择图片</p>
              </div>

              <div v-else class="relative w-full h-full">
                <img
                  :src="previewImageUrl"
                  alt="封面预览"
                  class="w-full h-full object-contain rounded"
                />
                <button
                  @click.stop="removeImage"
                  class="absolute left-1/2 -translate-x-1/2 bottom-1 px-2 py-0.5 text-xs bg-white/80 hover:bg-white text-gray-700 rounded transition-colors"
                >
                  删除图片
                </button>
              </div>
            </div>
          </div>

          <!-- 时间展示（创建/修改时间） -->
          <div class="classic_shadow w-full p-4 bg-white rounded-lg mb-4">
            <div class="w-full h-[20px] flex items-center mb-2">
              <div class="stripe"></div>
              <div class="little_title">时间信息</div>
            </div>
            <div class="text-sm text-gray-600 mb-1">创建时间：{{ formattedDates.createdAt }}</div>
            <div class="text-sm text-gray-600">修改时间：{{ formattedDates.updatedAt }}</div>
          </div>

          <!-- 按钮区域（确认修改 + 关闭） -->
          <div class="classic_shadow w-full p-4 bg-white rounded-lg">
            <button
              @click="updateEssay"
              :disabled="isUploading"
              class="postbtn-action w-full mb-2 py-2 text-white font-medium rounded transition-all duration-300"
              style="background-color: #353DD3;"
            >
              {{ isUploading ? '处理中...' : '确 认 修 改' }}
            </button>
            <button
              @click="closeDialog"
              :disabled="isUploading"
              class="postbtn-action w-full py-2 text-white font-medium rounded transition-all duration-300"
              style="background-color: #ef4444;"
            >
              关 闭
            </button>
          </div>

          <!-- 底部提示（可保留） -->
          <div class="mt-4 text-xs text-gray-400 italic">
            Tip: 修改前请确认内容无误。
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 图片裁剪模态框 -->
  <ImageCropperModal
    v-model:visible="showCropper"
    :image-file="imageToCrop"
    @crop-complete="handleCropComplete"
  />
</template>

<style scoped>
/* 完全复制创建组件的样式 */
.classic_shadow {
  box-shadow: 2px 4px 6px -1px rgba(0, 0, 0, 0.1), 0 -2px 4px -1px rgba(0, 0, 0, 0.06);
}

@font-face {
  font-family: 'connectionEnglish';
  src: url('../resources/font/Bitcount_Single/BitcountSingle-VariableFont.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

@font-face {
  font-family: 'squareChinese';
  src: url('../resources/font/WDXL_Lubrifont_SC/WDXLLubrifontSC-Regular.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

.postbtn-action {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  cursor: pointer;
  border-radius: 4px;
  outline: none;
}

.postbtn-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  opacity: 1.4;
}

.postbtn-action:active {
  transform: translateY(0);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.stripe {
  background-color: #353DD3;
  width: 5px;
  height: 100%;
  position: relative;
}

.stripe::after {
  content: '';
  position: absolute;
  width: 5px;
  height: 100%;
  background: #353DD3;
  opacity: 0.4;
  left: 2px;
}

.little_title {
  margin-left: 10px;
  font-size: 15px;
  font-weight: 100;
  font-family: 'squareChinese';
  transform: scaleX(1.2);
}

.title_input {
  font-family: 'squareChinese';
  font-size: 25px;
  color: #434343;
  display: block;
  width: 100%;
  height: 45px;
  border: none;
  border-bottom: 1px solid #515151;
  position: relative;
  outline: none;
  background: transparent;
  padding-bottom: 2px;
  transition: all ease 0.2s;
  z-index: 1;
}
.title_input::placeholder {
  color: #999;
  font-size: 20px;
}
.bar {
  position: absolute;
  display: block;
  width: 100%;
  bottom: 5px;
  left: 0;
}
.bar:before,
.bar:after {
  content: '';
  height: 2px;
  width: 0;
  bottom: 0;
  position: absolute;
  background: #5b73ff;
  transition: 1s ease all;
}
.bar:before {
  left: 0%;
}
.title_input:hover ~ .bar:before {
  width: 100%;
}
.title_input:focus ~ .bar:before {
  box-shadow: 0 0 8px 1px rgba(91, 115, 255, 0.6);
  width: 100%;
}

.essay_content_textarea {
  width: 100%;
  height: 100%;
  padding: 15px;
  box-sizing: border-box;
  font-family: 'squareChinese';
  font-size: 16px;
  font-weight: 100;
  color: #434343;
  background-color: #fff;
  border: 2px solid #e2e8f0;
  border-radius: 5px;
  outline: none;
  resize: none;
  appearance: none;
  transition: all 0.2s ease;
  line-height: 1.6;
  letter-spacing: 0.5px;
}
.essay_content_textarea:hover {
  border: #353DD3 1.5px solid;
  box-shadow: 0 0 0 1px #353DD3;
}
.essay_content_textarea:focus {
  border-color: #5b73ff;
  box-shadow: 0 0 0 2px rgba(91, 115, 255, 0.3), 0 0 8px 1px rgba(91, 115, 255, 0.2);
}
.essay_content_textarea::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.essay_content_textarea::-webkit-scrollbar-track {
  background: #f8fafc;
  border-radius: 3px;
}
.essay_content_textarea::-webkit-scrollbar-thumb {
  background: #5b73ff;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.essay_content_textarea::-webkit-scrollbar-thumb:hover {
  background: #353DD3;
}
.essay_content_textarea::placeholder {
  color: #999;
  font-size: 16px;
  opacity: 0.7;
}
</style>