<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Eye, EyeClosed } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const videoSrc = ref('/src/resources/pictures/background/afternoonCity.mp4')
const videoPlayer = ref(null)

// 输入框 ref
const emailInput = ref(null)
const codeInput = ref(null)
const passwordInput = ref(null)
const confirmPasswordInput = ref(null)

// 路由跳转
const toHome = () => router.push('/')
const toLogin = () => router.push('/login')

// 视频播放
onMounted(() => {
  const video = videoPlayer.value
  if (video) {
    video.play().catch(e => console.log('自动播放被阻止:', e))
  }
})

// 按钮悬停状态（模板中使用）
const forgetPasswordButtonIsHovered = ref(false)

// 步骤控制：true = 邮箱验证，false = 密码重置
const changeboolean = ref(true)

// 表单数据
const formData = ref({
  email: '',
  password: '',
  confirmPassword: ''
})

// 验证码（独立，因为逻辑中单独使用）
const verificationCode = ref('')

// 错误信息
const formErrors = ref({
  email: '',
  password: '',
  confirmPassword: '',
  verificationCode: ''
})

// 抖动状态
const shakingFields = ref({
  email: false,
  password: false,
  confirmPassword: false,
  verificationCode: false
})

// 加载状态
const isLoading = ref(false)

// 密码可见
const showPassword = ref(false)

// 发送验证码相关
const isSending = ref(false)
const countdown = ref(0)
let timer = null

// 邮箱格式验证
const isEmailValid = computed(() => {
  const email = formData.value.email
  if (!email) return false
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
})

// 表单整体有效性（根据当前步骤）
const isFormValid = computed(() => {
  if (changeboolean.value) {
    return formData.value.email && !formErrors.value.email && verificationCode.value
  } else {
    return formData.value.password &&
           formData.value.confirmPassword &&
           !formErrors.value.password &&
           !formErrors.value.confirmPassword &&
           formData.value.password === formData.value.confirmPassword
  }
})

// 字段验证
const validateField = (field, value) => {
  let errorMsg = ''
  switch (field) {
    case 'email':
      if (!value) errorMsg = '邮箱不能为空'
      else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(value)) errorMsg = '邮箱格式不正确'
      }
      formErrors.value.email = errorMsg
      return !errorMsg
    case 'password':
      if (!value) errorMsg = '密码不能为空'
      else if (value.length < 6) errorMsg = '密码至少6个字符'
      formErrors.value.password = errorMsg
      return !errorMsg
    case 'confirmPassword':
      if (!value) errorMsg = '请再次输入密码'
      else if (formData.value.password !== value) errorMsg = '两次密码不一致'
      formErrors.value.confirmPassword = errorMsg
      return !errorMsg
    case 'verificationCode':
      if (!value) errorMsg = '验证码不能为空'
      formErrors.value.verificationCode = errorMsg
      return !errorMsg
    default: return true
  }
}

// 抖动单个字段
const shakeField = (field) => {
  if (!shakingFields.value[field]) return
  shakingFields.value[field] = true
  setTimeout(() => {
    shakingFields.value[field] = false
  }, 400)
}

// 失焦处理
const handleBlur = (field) => {
  const value = field === 'verificationCode' ? verificationCode.value : formData.value[field]
  const isValid = validateField(field, value)
  if (!isValid) shakeField(field)
}

// 切换密码可见
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

// 回车处理（空实现，保留原有）
const handleEnterKey = (currentField) => {}

// 清理定时器
onUnmounted(() => {
  if (timer) clearInterval(timer)
})

// 发送找回密码验证码
const handleSendCode = async () => {
  if (!validateField('email', formData.value.email)) {
    shakeField('email')
    return
  }
  if (isSending.value || countdown.value > 0) return

  isSending.value = true
  try {
    const response = await axios.post('/api/user/send-reset-code', {
      email: formData.value.email
    })
    if (response.data.success) {
      countdown.value = 60
      if (timer) clearInterval(timer)
      timer = setInterval(() => {
        countdown.value -= 1
        if (countdown.value <= 0) {
          clearInterval(timer)
          timer = null
        }
      }, 1000)
    } else {
      formErrors.value.email = response.data.message || '发送失败'
      shakeField('email')
    }
  } catch (error) {
    console.error('发送验证码失败', error)
    formErrors.value.email = '网络错误，请稍后重试'
    shakeField('email')
  } finally {
    isSending.value = false
  }
}

// 验证验证码并切换到密码重置阶段
const handleVerifyCode = async () => {
  if (!validateField('email', formData.value.email)) {
    shakeField('email')
    return
  }
  if (!validateField('verificationCode', verificationCode.value)) {
    shakeField('verificationCode')
    return
  }

  isLoading.value = true
  try {
    const response = await axios.post('/api/user/verify-reset-code', {
      email: formData.value.email,
      code: verificationCode.value
    })
    if (response.data.success) {
      changeboolean.value = false
    } else {
      formErrors.value.verificationCode = response.data.message || '验证码错误'
      shakeField('verificationCode')
    }
  } catch (error) {
    console.error('验证失败', error)
    formErrors.value.verificationCode = '网络错误，请稍后重试'
    shakeField('verificationCode')
  } finally {
    isLoading.value = false
  }
}

// 重置密码
const handleResetPassword = async () => {
  if (!validateField('password', formData.value.password)) {
    shakeField('password')
    return
  }
  if (!validateField('confirmPassword', formData.value.confirmPassword)) {
    shakeField('confirmPassword')
    return
  }

  isLoading.value = true
  try {
    const response = await axios.post('/api/user/reset-password', {
      email: formData.value.email,
      password: formData.value.password
    })
    if (response.data.success) {
      router.push('/login')
    } else {
      formErrors.value.password = response.data.message || '重置失败'
      shakeField('password')
    }
  } catch (error) {
    console.error('重置密码失败', error)
    formErrors.value.password = '网络错误，请稍后重试'
    shakeField('password')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="fullscreen-container">
    <video
      ref="videoPlayer"
      class="fullscreen-video"
      :src="videoSrc"
      autoplay
      muted
      loop
      playsinline
    ></video>

    <div class="content-layer">
      <div class="w-[345px] h-[415px] mb-[100px] bg-indigo-800/70 p-[-10px]">
        <div
          style="
            display: flex;
            flex-direction: column;
            gap: 10px;
            border: 2px dashed;
            background-color: #ffffff;
            box-shadow: -2px -5px 0 8px #ffffff, 2px 2px 4px 2px rgba(0, 0, 0, 0.5);
            transition: 0.4s ease-in-out;
          "
          class="w-[330px] h-[400px]"
        >
          <!-- LOGO区域 -->
          <div class="w-full h-[90px] relative flex justify-center border-b-2">
            <button
              @click="toHome"
              :style="{
                fontFamily: 'connectionEnglish',
                fontWeight: 300,
                transform: 'scaleX(0.6)',
                borderColor: '#000000',
                background: 'linear-gradient(to right, #000000, #4a4a4a, #000000)',
                WebkitBackgroundClip: 'text',
                backgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                color: 'transparent'
              }"
              style="transition: color 0.5s ease"
              class="mt-[25px] h-[45px] inline-block text-2xl border-3 pl-[15px] pr-[15px] transition-all duration-500"
            >
              Patchouli's Library
            </button>
          </div>

          <!-- 邮箱验证步骤 -->
          <div v-if="changeboolean" class="w-full h-[240px] border-b-2 flex justify-center flex-col">
            <!-- 邮箱输入 -->
            <div class="w-full h-[50px] flex justify-center mb-[30px]">
              <div class="w-[230px] h-[50px] relative bg-blue-600/50 flex justify-center items-center">
                <div class="group">
                  <input
                    ref="emailInput"
                    v-model="formData.email"
                    type="text"
                    class="input w-[200px]"
                    :class="{
                      shake: shakingFields.email,
                      'input-error': shakingFields.email,
                      'input-error-bg': shakingFields.email
                    }"
                    @keyup.enter="handleEnterKey('email')"
                    @input="formErrors.email = ''"
                    @blur="handleBlur('email')"
                    required
                  />
                  <span class="highlight"></span>
                  <span class="bar"></span>
                  <label :class="{ 'shake-label': shakingFields.email, 'label-error': shakingFields.email }">邮箱</label>
                </div>
                <div
                  v-if="formErrors.email"
                  style="color: #E04848; font-family: 'squareChinese'; font-size: 12px;"
                  class="absolute top-0 right-1"
                >
                  {{ formErrors.email }}
                </div>
              </div>
            </div>

            <!-- 验证码输入 + 发送按钮 -->
            <div class="w-full flex justify-center h-[40px] transition-all duration-300 ease-in-out" style="overflow: hidden;">
              <div
                style="box-shadow: inset 0 2px 4px 4px rgba(0, 0, 0, 0.1);"
                class="w-[210px] h-[40px] flex justify-center items-center overflow-hidden"
              >
                <div style="width: 140px; max-width: 140px;" class="form__group confirm-pwd-group">
                  <input
                    ref="codeInput"
                    v-model="verificationCode"
                    type="text"
                    class="form__field confirm-pwd-input"
                    :class="{
                      shake: shakingFields.verificationCode,
                      'input-error': shakingFields.verificationCode,
                      'input-error-bg': shakingFields.verificationCode
                    }"
                    placeholder="confirmPwd"
                    @keyup.enter="handleEnterKey('verificationCode')"
                    @input="formErrors.verificationCode = ''"
                    required
                  />
                  <label for="confirmPwd" class="form__label confirm-pwd-label">验证码</label>
                </div>
                <button
                  @click="handleSendCode"
                  :disabled="!isEmailValid || countdown > 0 || isSending"
                  :class="['sendcodebutton', { disabled: countdown > 0 }]"
                  style="font-family: 'squareChinese'; transform: scaleX(0.9);"
                  class="sendcodebutton w-[35px] h-[25px] ml-[10px] flex items-center justify-center rounded-[4px]"
                >
                  {{ countdown > 0 ? countdown + 's' : 'send' }}
                </button>
              </div>
            </div>

            <!-- Confirm 按钮 -->
            <div class="w-full h-[30px] flex justify-center items-center">
              <button
                class="register-btn"
                @click="handleVerifyCode"
                :disabled="!isFormValid || isLoading"
              >
                {{ isLoading ? 'Loading...' : 'Confirm' }}
              </button>
            </div>
          </div>

          <!-- 密码重置步骤 -->
          <div v-else class="w-full h-[240px] border-b-2 flex justify-center items-center flex-col">
            <!-- 新密码输入 -->
            <div style="transition: all 0.3s ease;" class="w-full h-[50px] flex justify-center">
              <div class="w-[250px] h-[50px] relative bg-blue-600/50 flex justify-center items-center">
                <div class="group">
                  <input
                    ref="passwordInput"
                    v-model="formData.password"
                    :type="showPassword ? 'text' : 'password'"
                    class="input w-[170px]"
                    :class="{
                      shake: shakingFields.password,
                      'input-error': shakingFields.password,
                      'input-error-bg': shakingFields.password
                    }"
                    @keyup.enter="handleEnterKey('password')"
                    @blur="handleBlur('password')"
                    required
                  />
                  <span class="highlight"></span>
                  <span class="bar"></span>
                  <label :class="{ 'shake-label': shakingFields.password, 'label-error': shakingFields.password }">密码</label>
                </div>
                <button
                  type="button"
                  @click="togglePasswordVisibility"
                  class="focus:outline-none mt-[15px] ml-[10px]"
                >
                  <Eye v-if="showPassword" style="color: #F5F9FF; cursor: pointer;" class="w-[17px] h-[20px]" />
                  <EyeClosed v-else style="color: #F5F9FF; cursor: pointer;" class="w-[17px] h-[20px]" />
                </button>
                <div
                  v-if="formErrors.password"
                  style="color: #E04848; font-family: 'squareChinese'; font-size: 12px;"
                  class="absolute top-0 right-1"
                >
                  {{ formErrors.password }}
                </div>
              </div>
            </div>

            <!-- 确认密码输入 -->
            <div class="w-full flex h-[40px] overflow-hidden justify-center mt-[30px] transition-all duration-300 ease-in-out">
              <div
                style="box-shadow: inset 0 2px 4px 4px rgba(0, 0, 0, 0.1);"
                class="w-[210px] h-[40px] flex justify-center items-center"
              >
                <div style="width: 180px; max-width: 180px;" class="form__group confirm-pwd-group">
                  <input
                    ref="confirmPasswordInput"
                    v-model="formData.confirmPassword"
                    type="password"
                    class="form__field confirm-pwd-input"
                    :class="{
                      shake: shakingFields.confirmPassword,
                      'input-error': shakingFields.confirmPassword,
                      'input-error-bg': shakingFields.confirmPassword
                    }"
                    placeholder="confirmPwd"
                    @keyup.enter="handleEnterKey('confirmPassword')"
                    @blur="handleBlur('confirmPassword')"
                    required
                  />
                  <label for="confirmPwd" class="form__label confirm-pwd-label">确认密码</label>
                </div>
              </div>
            </div>

            <!-- Confirm 按钮 -->
            <div class="w-full h-[30px] flex justify-center items-center">
              <button
                class="register-btn"
                @click="handleResetPassword"
                :disabled="!isFormValid || isLoading"
              >
                {{ isLoading ? 'Loading...' : 'Confirm' }}
              </button>
            </div>
          </div>

          <!-- 返回登录链接 -->
          <div class="w-full h-[45px] flex items-center justify-center">
            <button
              @click="toLogin"
              @mouseenter="forgetPasswordButtonIsHovered = true"
              @mouseleave="forgetPasswordButtonIsHovered = false"
              class="text-btn hover:cursor-pointer relative w-[100px] h-[30px] overflow-hidden"
            >
              <div
                :style="{
                  transform: forgetPasswordButtonIsHovered ? 'translateY(-100%)' : 'translateY(0)',
                  opacity: forgetPasswordButtonIsHovered ? 0 : 1,
                  transition: 'all 0.3s ease'
                }"
                class="absolute inset-0 flex items-center justify-center"
              >
                想起来了?
              </div>
              <div
                :style="{
                  transform: forgetPasswordButtonIsHovered ? 'translateY(0)' : 'translateY(100%)',
                  opacity: forgetPasswordButtonIsHovered ? 1 : 0,
                  transition: 'all 0.3s ease'
                }"
                class="absolute inset-0 flex items-center justify-center text-base"
              >
                Go Back~
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 全屏容器 */
.fullscreen-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.fullscreen-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  min-width: 100%;
  min-height: 100%;
  transform: translate(-50%, -50%);
  left: 50%;
  top: 50%;
  z-index: 0;
  filter: brightness(0.8);
}

.content-layer {
  position: relative;
  z-index: 10;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 字体 */
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

/* 输入框样式 */
.group {
  position: relative;
}

.input {
  font-size: 16px;
  font-family: 'squareChinese';
  padding: 15px 10px 0px 5px;
  display: block;
  width: 200px;
  border: none;
  border-bottom: 1px solid #515151;
  background: transparent;
}

.input:focus {
  outline: none;
}

label {
  color: #343434;
  font-size: 18px;
  font-weight: normal;
  font-family: 'squareChinese';
  position: absolute;
  pointer-events: none;
  left: 0px;
  top: 10px;
  transition: 0.2s ease all;
}

.input:focus ~ label,
.input:valid ~ label {
  top: -5px;
  left: 0px;
  font-size: 14px;
  color: #f4f4f4;
}

.bar {
  position: relative;
  display: block;
  width: 200px;
}

.bar:before,
.bar:after {
  content: '';
  height: 2px;
  width: 0;
  bottom: 1px;
  position: absolute;
  background: #5264AE;
  transition: 0.2s ease all;
}

.bar:before {
  left: 50%;
}

.bar:after {
  right: 50%;
}

.input:focus ~ .bar:before,
.input:focus ~ .bar:after {
  width: 50%;
}

.highlight {
  position: absolute;
  height: 60%;
  width: 100%;
  top: 35%;
  left: 0;
  pointer-events: none;
  opacity: 0.5;
}

.input:focus ~ .highlight,
.input:valid ~ .highlight {
  background: #5264AE;
  animation: all 0.6s ease;
}

@keyframes inputHighlighter {
  from { background: transparent; }
  to { background: #5264AE; }
}

/* 文字按钮 */
.text-btn {
  font-size: 15px;
  font-family: 'squareChinese';
  transition: all 0.2s;
}

.text-btn:hover {
  color: #2418ff;
}

.text-btn:active {
  color: #000000;
}

/* 抖动动画 */
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-3px); }
  20%, 40%, 60%, 80% { transform: translateX(3px); }
}

.shake {
  animation: shake 0.4s ease-in-out;
}

.input-error {
  border-color: #E04848 !important;
  border-bottom-width: 2px !important;
}

.input,
.confirm-pwd-input {
  transition: border-color 0.2s ease, border-bottom-width 0.2s ease;
  border-bottom: 1px solid #515151;
}

.confirm-pwd-input.input-error {
  border-bottom-color: #E04848 !important;
}

.input-error-bg {
  color: #E04848 !important;
}

.confirm-pwd-input.input-error-bg {
  color: #E04848 !important;
}

.shake-label {
  animation: shake 0.4s ease-in-out;
}

/* 确认密码输入框样式 */
.confirm-pwd-group {
  position: relative;
  padding: 10px 0 0;
  border-bottom: 2px solid #5264AE !important;
  margin-bottom: 7px;
}

.confirm-pwd-input {
  font-family: 'squareChinese' !important;
  width: 120px !important;
  border: none !important;
  outline: 0 !important;
  font-size: 17px !important;
  color: #5081ec !important;
  padding: 0px 0 !important;
  background: transparent !important;
  transition: all 0.2s !important;
  margin-left: 35px;
}

.confirm-pwd-input::placeholder {
  color: transparent !important;
}

.confirm-pwd-input:focus {
  margin-left: 35px;
}

.confirm-pwd-label {
  color: #343434 !important;
  font-size: 14px !important;
  font-family: 'squareChinese' !important;
  position: absolute !important;
  pointer-events: none !important;
  left: 0px !important;
  top: 10px !important;
  transition: 0.2s ease all !important;
}

.confirm-pwd-input:focus ~ .confirm-pwd-label,
.confirm-pwd-input:not(:placeholder-shown) ~ .confirm-pwd-label {
  top: 10px !important;
  left: -10px !important;
  color: #343434 !important;
}

/* 发送验证码按钮 */
.sendcodebutton {
  background-color: #07f3b4;
  cursor: pointer;
  box-shadow: inset 0 2px 3px rgba(255, 255, 255, 0.7),
              inset 0 -2px 3px rgba(0, 0, 0, 0.1),
              5px 10px 15px rgba(0, 0, 0, 0.15);
  transition: all 0.2s ease;
}

.sendcodebutton:hover {
  color: #fcfcfc;
}

.sendcodebutton:active {
  color: #2418ff;
  box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.2);
}

.sendcodebutton.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #ccc;
}

/* 注册/确认按钮 */
.register-btn {
  font-family: 'connectionEnglish';
  font-weight: 300;
  font-size: 20px;
  background: #7187ff;
  border: none;
  border-radius: 10px;
  color: #000;
  outline: none;
  cursor: pointer;
  height: 35px;
  width: 130px;
  box-shadow: 4px 6px 10px rgba(0, 0, 0, 0.25);
  transition: all 0.4s;
  margin-top: 50px;
}

.register-btn:active {
  margin-top: 57px;
  margin-right: 10px;
  transition: all 0.1s;
}

.register-btn:hover {
  box-shadow: inset 0 0 0 4px #ef476f,
              inset 0 0 0 8px #ffd166,
              inset 0 0 0 12px #06d6a0,
              inset 0 0 0 16px #118ab2;
  background: #073b4c;
  color: #fff;
  height: 45px;
  width: 140px;
}

.register-btn:disabled,
.register-btn.btn-disabled {
  opacity: 0.8;
  cursor: not-allowed;
  pointer-events: none;
}

/* 隐藏浏览器默认密码查看按钮 */
input::-ms-reveal,
input::-ms-clear {
  display: none;
}

input[type="password"]::-webkit-credentials-auto-fill-button,
input[type="password"]::-webkit-textfield-decoration-container {
  display: none !important;
}
</style>