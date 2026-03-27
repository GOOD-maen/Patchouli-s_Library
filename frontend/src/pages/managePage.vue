<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Folders } from 'lucide-vue-next'
import ImageCropperModal from '@/components/ImageCropperModal.vue' 

const router = useRouter()
const route = useRoute()

// ---------- 表单数据 ----------
const formData = ref({
  title: '',
  content: '',
  coverUrl: '',
  summary: '' // 新增简介字段
})

// ---------- 图片上传相关 ----------
const fileInput = ref(null)
const selectedFile = ref(null)
const imagePreview = ref('')
const isUploading = ref(false)
const isDragging = ref(false) // 可保留用于拖拽样式，目前未使用

// 裁剪相关
const showCropper = ref(false)
const imageToCrop = ref(null)
const imageInfo = ref({
  width: 0,
  height: 0,
  ratio: 0
})

// ---------- 方法 ----------
const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    checkImageRatio(file)
  }
}

// 检查图片比例，决定是否需要裁剪
const checkImageRatio = (file) => {
  const img = new Image()
  img.onload = () => {
    const ratio = img.width / img.height
    const targetRatio = 16 / 9
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
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

// 裁剪完成回调
const handleCropComplete = async (croppedFile, previewUrl) => {
  console.log('✅ 裁剪完成，新文件:', croppedFile.name, croppedFile.size)

  try {
    isUploading.value = true
    console.log('📤 开始上传裁剪后的图片...')
    const newCoverUrl = await uploadImage(croppedFile)
    console.log('✅ 裁剪图片上传成功，URL:', newCoverUrl)

    imagePreview.value = previewUrl
    selectedFile.value = croppedFile
    formData.value.coverUrl = newCoverUrl

    imageInfo.value = {
      width: 1600,
      height: 900,
      ratio: 16 / 9
    }
  } catch (error) {
    console.error('💥 裁剪图片上传失败:', error)
    alert('裁剪图片上传失败: ' + error.message)

    imagePreview.value = previewUrl
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

// 移除图片
const removeImage = () => {
  selectedFile.value = null
  imagePreview.value = ''
  imageInfo.value = { width: 0, height: 0, ratio: 0 }
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 上传图片到服务器
const uploadImage = async (file) => {
  console.log('🔄 开始上传文件:', file)

  const formDataToSend = new FormData()
  formDataToSend.append('file', file)

  try {
    console.log('🌐 发送请求到: http://localhost:8080/api/upload')

    const response = await fetch('http://localhost:8080/api/upload', {
      method: 'POST',
      headers: {
       'Authorization': `Bearer ${localStorage.getItem('token')}`
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
    console.log('✅ 上传成功:', result)
    return result.fileUrl
  } catch (error) {
    console.error('💥 请求异常:', error)
    throw error
  }
}

// 提交表单
const submitForm = async () => {
  if (!formData.value.title.trim() || !formData.value.content.trim()) {
    alert('请填写标题和内容！')
    return
  }

  isUploading.value = true

  try {
    let coverUrl = formData.value.coverUrl

    // 如果有选择的文件但还没上传（比如裁剪后未上传）
    if (selectedFile.value && !coverUrl) {
      coverUrl = await uploadImage(selectedFile.value)
    }

    const response = await fetch('http://localhost:8080/api/essay', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
       
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        title: formData.value.title,
        content: formData.value.content,
        coverUrl: coverUrl || '',
        summary: formData.value.summary 
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const newEssay = await response.json()
    alert('创建成功！')
    resetForm()
    
  } catch (error) {
    console.error('创建帖子失败:', error)
    alert('创建失败，请重试！')
  } finally {
    isUploading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    title: '',
    content: '',
    coverUrl: '',
    summary: ''
  }
  removeImage()
  imageInfo.value = { width: 0, height: 0, ratio: 0 }
}

</script>

<template>
  <!--总框-->
  <div style="background-color: #F5F5F5;" class="w-full h-[calc(100%-100px)]">
    <!--内框-->
    <div class="w-[88%] ml-[6%] h-full flex">
      <!--左侧区域-->
      <div class="w-[75%] h-full p-[20px]">
        <div class="classic_shadow bg-white w-full h-full rounded-[5px] pt-[20px] pl-[20px] pr-[20px] pb-[20px]">
          <!--title输入框-->
          <div class="w-full h-[50px] relative">
            <input
              class="title_input"
              type="text"
              placeholder="请输入标题"
              v-model="formData.title"
              required
            />
            <span class="bar"></span>
          </div>
          <!--title输入框(ED)-->

          <!--文章文本区-->
          <div class="w-full h-[calc(100%-50px)]">
            <textarea
              class="essay_content_textarea"
              placeholder="请输入文本"
              v-model="formData.content"
              required
            ></textarea>
          </div>
          <!--文章文本区(ED)-->
        </div>
      </div>
      <!--左侧区域(ED)-->

      <!--右侧区域-->
      <div class="w-[25%] h-full pt-[20px]">
        <!--封面上传（替换原有空白盒子）-->
        <div class="classic_shadow w-full h-[257px] mb-[20px] p-[20px] bg-white rounded-[5px]">
          <!--title-->
          <div class="w-full h-[20px] flex items-center">
            <div class="stripe"></div>
            <div class="little_title">封面</div>
          </div>
          <!--title(ED)-->

          <!-- 封面上传内容 -->
          <div class="mt-2">
            <div class="mb-2 text-xs text-gray-500">
              建议使用16:9比例的图片（如1600×900、800×450）
            </div>
            <div
              @click="triggerFileInput"
              class="h-[150px] border-2 border-dashed border-gray-300 rounded-lg  flex items-center justify-center text-center cursor-pointer hover:border-blue-400 transition-all duration-200"
              :class="{ 'border-blue-400 bg-blue-50': isDragging }"
            >
              <input
                type="file"
                ref="fileInput"
                @change="handleFileSelect"
                accept="image/*"
                class="hidden"
              />

              <div v-if="!selectedFile" class="text-gray-500 ">
                <svg class="mx-auto h-8 w-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 48 48">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M28 8H12a4 4 0 00-4 4v20a4 4 0 004 4h16a4 4 0 004-4V20L28 8z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 20h8M16 28h6" />
                </svg>
                <p class="mt-1 text-xs">点击选择图片</p>
              </div>

              <div v-else class="relative w-full h-full">
              <img
                :src="imagePreview"
                alt="封面预览"
                class="w-full h-full object-contain rounded"
                />
                <button
                @click.stop="removeImage"
                class="absolute left-1/2 -translate-x-1/2 bottom-1 px-2 py-0.5 text-xs text-white/0  hover:bg-white/70 hover:text-gray-700 rounded transition-colors"
                >
                删除图片
                </button>
              </div>
            </div>
          </div>
        </div>
        <!--封面上传(ED)-->

        <!--分类和标签区域-->
        <div class="classic_shadow w-full p-[20px] rounded-[5px] bg-white">
          <!--作品类型（忽略功能，仅保留UI）-->
          <div class="w-full h-[70px]">
            <div class="w-full h-[20px] flex">
              <div class="stripe"></div>
              <div class="little_title">作品类型</div>
            </div>
            <div class="w-full h-[40px] mt-[10px]">
              <div class="w-[70%] max-w-[250px] h-[35px] bg-slate-200 rounded-[5px] flex items-center classify">
                <Folders class="w-[15px] text-slate-800 ml-[15px]" />
                <div style="font-weight: 100; font-size: 14px;" class="ml-[10px] mb-[2px] text-slate-800">分类</div>
                <div class="ml-auto mr-[10px] text-slate-800">></div>
              </div>
            </div>
          </div>
          <!--作品类型(ED)-->

          <!--简介-->
          <div class="w-full pt-[10px] pb-[10px]">
            <div class="w-full h-[20px] flex mb-[10px]">
              <div class="stripe"></div>
              <div class="little_title">简介</div>
            </div>
            <div
              class="w-full h-[25px] text-slate-700 opacity-70 overflow-hidden text-xs italic"
              style="white-space: nowrap; text-overflow: ellipsis;"
            >
              #为你的作品做介绍，让别人更好的了解它。
            </div>

            <!--简介文本区（绑定 summary）-->
            <textarea
              class="introduction_textarea w-full min-h-[100px] max-h-[200px] rounded-[5px] bg-slate-200"
              v-model="formData.summary"
            ></textarea>
          </div>
          <!--简介(ED)-->
        </div>
        <!--分类和标签区域(ED)-->

        <!--保存草稿和提交发布-->
        <div class="classic_shadow w-full h-[100px] rounded-[5px] bg-white mt-[20px] pt-[1px]">
          <div style="font-weight: 300; font-size: 17px;" class="w-full h-[20px] mt-[5px] flex justify-center text-slate-600">
            ———— Are You Ready? ————
          </div>
          <div class="w-full h-[50px] flex justify-center items-center">
            <button
              style="background-color: #00D7A3; font-family: 'squareChinese';"
              class="w-[150px] h-[30px] mr-[5px] border-2 border-teal-200 text-slate-100 postbtn-action"
              @click="submitForm"
              :disabled="isUploading"
            >
              {{ isUploading ? '处理中...' : '保存草稿' }}
            </button>
            <button
              style="background-color:#353DD3; font-family: 'squareChinese';"
              class="w-[150px] h-[30px] ml-[5px] border-2 border-blue-400 text-slate-100 postbtn-action"
              @click="submitForm"
              :disabled="isUploading"
            >
              {{ isUploading ? '处理中...' : '发布作品' }}
            </button>
          </div>
        </div>
        <!--保存草稿和提交发布(ED)-->
      </div>
      <!--右侧区域(ED)-->
    </div>
    <!--内框(ED)-->
  </div>
  <!--总框(ED)-->

  <!--图片裁剪模态框-->
  <ImageCropperModal
    v-model:visible="showCropper"
    :image-file="imageToCrop"
    @crop-complete="handleCropComplete"
  />
</template>

<style scoped>

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

.classify {
  transition: box-shadow ease 0.2s;
  cursor: pointer;
}
.classify:hover {
  box-shadow: 0 0 0 1px #353DD3;
}

.introduction_textarea {
  border: none;
  outline: none;
  appearance: none;
  padding: 10px;
  font-family: 'squareChinese';
  font-size: 14px;
  font-weight: 100;
  border: 2px solid #e2e8f0;
  transition: box-shadow 0.2s ease;
  resize: none;
}
.introduction_textarea:hover {
  box-shadow: 0 0 0 1px #353DD3;
}
.introduction_textarea::-webkit-scrollbar {
  width: 5px;
}
.introduction_textarea::-webkit-scrollbar-track {
  background: #f8fafc;
}
.introduction_textarea::-webkit-scrollbar-thumb {
  background: #5b73ff;
  border-radius: 4px;
  cursor: pointer;
}
.introduction_textarea::-webkit-scrollbar-thumb:hover {
  background: #289ef3;
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