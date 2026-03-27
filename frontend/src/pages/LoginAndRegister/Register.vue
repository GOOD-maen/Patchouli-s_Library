<script setup>
import { ref, onMounted,watch,onUnmounted,computed} from 'vue'
import { useRouter } from 'vue-router'
import { Eye,EyeClosed} from 'lucide-vue-next';

const router = useRouter()
const videoSrc = ref('/src/resources/pictures/background/afternoonCity.mp4') 
const videoPlayer = ref(null)


onMounted(() => {
  const video = videoPlayer.value
  if (video) {
    video.play().catch(e => {
      console.log('自动播放被阻止:', e)
    })
  }
})
/////////////额外功能按钮hover事件逻辑////////////

const registerButtonIsHovered = ref(false)
const forgetPasswordButtonIsHovered = ref(false)

/////////////额外功能按钮hover事件逻辑(ED////////////
//////////////////路由导航//////////////////

const toHome = () => {
  router.push('/')
}

const toLogin = () => {
  router.push('/login')
}

//////////////////路由导航(ED//////////////////


////////////////////注册逻辑//////////////////////
import axios from 'axios'

// 表单数据
const formData = ref({
  username: '',
  password: '',
  email: ''
})

// 表单验证错误
const formErrors = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  verificationCode: ''
})

// 加载状态
const isLoading = ref(false)



// 表单验证
const validateForm = () => {
  let isValid = true
  formErrors.value = { username: '', password: '', email: '', verificationCode: ''  };
  
  // 验证用户名
  if (!formData.value.username.trim()) {
    formErrors.value.username = '用户名不能为空'
    isValid = false
  }else if (formData.value.username.length > 20) {
  formErrors.value.username = '用户名不能超过20个字符'
  isValid = false
  }
  
  // 验证密码
  if (!formData.value.password) {
    formErrors.value.password = '密码不能为空'
    isValid = false
  } else if (formData.value.password.length < 6) {
    formErrors.value.password = '密码至少6个字符'
    isValid = false
  }
  
  // 验证邮箱
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!formData.value.email) {
    formErrors.value.email = '邮箱不能为空'
    isValid = false
  } else if (!emailRegex.test(formData.value.email)) {
    formErrors.value.email = '邮箱格式不正确'
    isValid = false
  }
  // 验证码验证
  if (showCodeInput.value && !verificationCode.value) {
    formErrors.value.verificationCode = '验证码不能为空';
    isValid = false;
  }
  
  // 确认密码验证
  if (showConfirmPwd.value && formData.value.password !== confirmPassword.value) {
    formErrors.value.confirmPassword = '两次输入的密码不一致';
    isValid = false;
  }
  
  return isValid
}

// 注册函数
const handleRegister = async () => {

  console.log('=== handleRegister start ===');
  console.log('formData:', {...formData.value});
  console.log('confirmPassword:', confirmPassword.value);
  console.log('verificationCode:', verificationCode.value);
  console.log('showConfirmPwd:', showConfirmPwd.value);
  console.log('showCodeInput:', showCodeInput.value);
  // 获取所有验证失败的字段
  const invalidFields = getInvalidFields()
  
  // 如果有错误字段，全部抖动并阻止提交
  if (invalidFields.length > 0) {
    shakeFields(invalidFields)
    console.error('请正确填写以下字段:', invalidFields.join(', '))
    return
  }
  
  // 防止重复提交
  if (isLoading.value) return
  
  isLoading.value = true
  
  try {
    const response = await axios.post('/api/user/register', {
      username: formData.value.username,
      password: formData.value.password,
      email: formData.value.email,
      verificationCode: verificationCode.value
    })
    
if (response.data.success) {
  console.log('注册成功！')
  setTimeout(() => {
    router.push('/login')
  }, 1500)
} else {
  console.error('注册失败:', response.data.message)
  const msg = response.data.message

  if (msg.includes('验证码')) {
    formErrors.value.email = msg  
    shakeField('verificationCode')
  } else if (msg.includes('用户名')) {
    formErrors.value.username = msg
    shakeField('username')
  } else if (msg.includes('邮箱')) {
    formErrors.value.email = msg
    shakeField('email')
  } else {
    alert(msg)
  }
}
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    isLoading.value = false
  }
}
////////////////////注册逻辑(ED)//////////////////////

///////////////////////密码可视逻辑 /////////////////////////

// 密码显示控制
const showPassword = ref(false)

///////////////////////密码可视逻辑(ED) /////////////////////////

///////////////////控制 确认密码 验证码 框出现消失逻辑///////////////////

const showConfirmPwd = ref(false)
const showCodeInput = ref(false)

const confirmPassword = ref('')
const verificationCode = ref('')

// 监听密码输入 - 控制确认密码框
watch(() => formData.value.password, (newPassword) => {
  const hasPassword = newPassword && newPassword.length > 0
  const hasEmail = formData.value.email?.trim() ? true : false

  if (hasPassword && !hasEmail) {
   
    showConfirmPwd.value = true
    showCodeInput.value = false
  } else {
    
    showConfirmPwd.value = false
   
  }
})

// 监听邮箱输入 - 控制验证码框
watch(() => formData.value.email, (newEmail) => {
  const hasEmail = newEmail && newEmail.trim() !== ''
  
  if (hasEmail) {
   
    showConfirmPwd.value = false
    showCodeInput.value = true
  } else {
   showCodeInput.value = false
    if (formData.value.password?.length > 0) {
      showConfirmPwd.value = true
    } else {
      showConfirmPwd.value = false
    }
  }
})

// 监听确认密码框的变化，确保互斥
watch(showConfirmPwd, (newVal) => {
  if (newVal) {
    showCodeInput.value = false
  }
})



// 密码输入处理（带延迟隐藏）
const handlePasswordInput = () => {
  formErrors.value.password = ''
}

// 确认密码输入处理（带延迟隐藏）
const handleConfirmPasswordInput = () => {
  formErrors.value.password = ''
}

// 切换密码可见状态（点击眼睛图标）
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

// 初始化状态
onMounted(() => {
  const video = videoPlayer.value
  if (video) {
    video.play().catch(e => {
      console.log('自动播放被阻止:', e)
    })
  }
  
  // 根据已有数据初始化展开状态
  if (formData.value.password?.length > 0) {
    showConfirmPwd.value = true
    showCodeInput.value = false
  } else if (formData.value.email?.trim()) {
    showCodeInput.value = true
  }
})

///////////////////控制 确认密码 验证码 框出现消失逻辑(ED)///////////////////

////////////////验证码发送//////////////
// 发送按钮相关状态
const isSending = ref(false);  // 是否正在发送
const countdown = ref(0);      // 倒计时秒数
let timer = null;              // 定时器

// 修改 handleSendCode，添加倒计时逻辑
const handleSendCode = async () => {
  // 检查邮箱...
  if (!formData.value.email) {
  console.warn('请先输入邮箱');
  return;
}
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(formData.value.email)) {
  console.warn('邮箱格式不正确');
  return;
}
  
  // 防止重复点击和倒计时中再次发送
  if (isSending.value || countdown.value > 0) return;
  
  isSending.value = true;
  
  try {
    const response = await axios.post('/api/user/send-code', {
      email: formData.value.email
    });
    
    if (response.data.success) {
      console.log('验证码已发送，请查收邮件');
      
      // 开始60秒倒计时
      countdown.value = 60;
      if (timer) clearInterval(timer);
      timer = setInterval(() => {
        countdown.value -= 1;
        if (countdown.value <= 0) {
          clearInterval(timer);
          timer = null;
        }
      }, 1000);
    } else {
      console.error('发送失败:', response.data.message);
    }
  } catch (error) {
    console.error('发送失败:', error);
  } finally {
    isSending.value = false;
  }
};

// 组件卸载时清理定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});
////////////////验证码发送(ED)//////////////
/////////////////抖动相关//////////////////
// 记录每个输入框是否正在抖动
const shakingFields = ref({
  username: false,
  password: false,
  confirmPassword: false,
  email: false,
  verificationCode: false
})

// 抖动方法
const shakeField = (field) => {
  // 如果字段不存在，直接返回
  if (!(field in shakingFields.value)) return

  console.log('Shaking field:', field);

  
  // 设置为true，触发抖动动画
  shakingFields.value[field] = true
  
  // 400ms后移除抖动
  setTimeout(() => {
    shakingFields.value[field] = false
  }, 400)
}

// 判断邮箱是否有效
const isEmailValid = computed(() => {
  const email = formData.value.email
  if (!email) return false
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
})

// 判断所有字段是否有效
const isFormValid = computed(() => {
  // 基础字段错误检查
  if (formErrors.value.username) return false
  if (formErrors.value.password) return false
  if (formErrors.value.email) return false
  
  // 检查基础字段是否有值
  if (!formData.value.username.trim()) return false
  if (!formData.value.password) return false
  if (!formData.value.email) return false
  
  // 如果显示确认密码框，检查其值
  if (showConfirmPwd.value && !confirmPassword.value) return false
  
  // 如果显示验证码框，检查其值
  if (showCodeInput.value && !verificationCode.value) return false
  
  return true
})

// 批量抖动多个字段
const shakeFields = (fields) => {
  fields.forEach(field => {
    shakeField(field)
  })
}

// 获取所有验证失败的字段
const getInvalidFields = () => {
  const invalidFields = []
  
  // 检查用户名
  if (!validateField('username', formData.value.username)) {
    invalidFields.push('username')
  }
  
  // 检查密码
  if (!validateField('password', formData.value.password)) {
    invalidFields.push('password')
  }
  
  // 检查确认密码（如果显示）
  if (showConfirmPwd.value && !validateField('confirmPassword', confirmPassword.value)) {
    invalidFields.push('confirmPassword')
  }
  
  // 检查邮箱
  if (!validateField('email', formData.value.email)) {
    invalidFields.push('email')
  }
  
  // 检查验证码（如果显示）
  if (showCodeInput.value && !validateField('verificationCode', verificationCode.value)) {
    invalidFields.push('verificationCode')
  }
  
  return invalidFields
}

// 验证规则函数
const validateField = (field, value) => {
  let errorMsg = ''
  
  switch(field) {
    case 'username':
      if (!value.trim()) {
        errorMsg = '用户名不能为空'
      } else if (value.length > 20) {
        errorMsg = '用户名不能超过20个字符'
      }
      formErrors.value.username = errorMsg
      return !errorMsg
      
    case 'password':
      if (!value) {
        errorMsg = '密码不能为空'
      } else if (value.length < 6) {
        errorMsg = '密码至少6个字符'
      }
      formErrors.value.password = errorMsg
      return !errorMsg
      
    case 'confirmPassword':
      if (!showConfirmPwd.value) return true
      if (!value) {
        errorMsg = '请再次输入密码'
      } else if (formData.value.password !== value) {
        errorMsg = '两次输入的密码不一致'
      }
      formErrors.value.password = errorMsg
      return !errorMsg
      
    case 'email':
      if (!value) {
        errorMsg = '邮箱不能为空'
      } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(value)) {
          errorMsg = '邮箱格式不正确'
        }
      }
      formErrors.value.email = errorMsg
      return !errorMsg
      
    case 'verificationCode':
      if (!showCodeInput.value) return true
      if (!value) {
        errorMsg = '验证码不能为空'
      }
      formErrors.value.email = errorMsg
      return !errorMsg
      
    default:
      return true
  }
}



// 失焦处理函数
const handleBlur = (field) => {
  let value = ''
  switch(field) {
    case 'username':
      value = formData.value.username
      break
    case 'password':
      value = formData.value.password
      break
    case 'confirmPassword':
      value = confirmPassword.value
      break
    case 'email':
      value = formData.value.email
      break
    case 'verificationCode':
      value = verificationCode.value
      break
  }
  
  const isValid = validateField(field, value)
  if (!isValid) {
    shakeField(field)
  }
}

/////////////////抖动相关(ED)//////////////////

/////////////////上下游相关////////////////

// 重置下游字段
const resetDownstreamFields = (changedField) => {
  switch (changedField) {
    case 'username':
      // 清空密码、确认密码、邮箱、验证码的值
      formData.value.password = ''
      confirmPassword.value = ''
      formData.value.email = ''
      verificationCode.value = ''
      // 重置状态（如果后续有字段状态也可以清空，但目前我们通过抖动类，无需额外状态）
      // 重置验证码相关
      countdown.value = 0
      isSending.value = false
      if (timer) {
        clearInterval(timer)
        timer = null
      }
      console.log('用户名已修改，已重置后续字段')
      break

    case 'password':
      // 清空确认密码、邮箱、验证码
      confirmPassword.value = ''
      formData.value.email = ''
      verificationCode.value = ''
      formErrors.value.password = ''
      formErrors.value.email = ''
      countdown.value = 0
      isSending.value = false
      if (timer) {
        clearInterval(timer)
        timer = null
      }
      console.log('密码已修改，已重置后续字段')
      break

    case 'confirmPassword':
      // 清空邮箱、验证码
      formErrors.value.password = '';
      formData.value.email = ''
      verificationCode.value = ''
      countdown.value = 0
      isSending.value = false
      if (timer) {
        clearInterval(timer)
        timer = null
      }
      console.log('确认密码已修改，已重置后续字段')
      break

    case 'email':
      // 清空验证码
      verificationCode.value = ''
      formErrors.value.email = ''
      countdown.value = 0
      isSending.value = false
      if (timer) {
        clearInterval(timer)
        timer = null
      }
      console.log('邮箱已修改，已重置验证码')
      break
  }
}

// 监听用户名变化，重置下游
watch(() => formData.value.username, (newVal, oldVal) => {
  if (oldVal !== undefined && newVal !== oldVal) {
    resetDownstreamFields('username')
  }
})

// 监听密码变化，重置下游
watch(() => formData.value.password, (newVal, oldVal) => {
  if (oldVal !== undefined && newVal !== oldVal) {
    resetDownstreamFields('password')
  }
})

// 监听确认密码变化，重置下游
watch(confirmPassword, (newVal, oldVal) => {
  if (oldVal !== undefined && newVal !== oldVal) {
    resetDownstreamFields('confirmPassword')
  }
})

// 监听邮箱变化，重置下游
watch(() => formData.value.email, (newVal, oldVal) => {
  if (oldVal !== undefined && newVal !== oldVal) {
    resetDownstreamFields('email')
  }
})

/////////////////上下游相关(ED)////////////////

//////////////////////“回车”切换逻辑///////////////////////

const usernameInput = ref(null)
const passwordInput = ref(null)
const confirmPasswordInput = ref(null)
const emailInput = ref(null)
const codeInput = ref(null) 

const handleEnterKey = (currentField) => {
  const focusOrder = ['username', 'password', 'confirmPassword', 'email', 'verificationCode', 'register']
  const currentIndex = focusOrder.indexOf(currentField)
  if (currentIndex === -1) return

  for (let i = currentIndex + 1; i < focusOrder.length; i++) {
    const nextField = focusOrder[i]
    
    // 跳过隐藏的字段
    if (nextField === 'confirmPassword' && !showConfirmPwd.value) continue
    if (nextField === 'verificationCode' && !showCodeInput.value) continue
    
    if (nextField === 'register') {
      // 最后一个，如果表单有效则提交
      if (isFormValid.value && !isLoading.value) {
        handleRegister()
      }
      return
    }
    
    // 聚焦到下一个输入框
    const refMap = {
      username: usernameInput,
      password: passwordInput,
      confirmPassword: confirmPasswordInput,
      email: emailInput,
      verificationCode: codeInput
    }
    const nextInput = refMap[nextField]?.value
    if (nextInput) {
      nextInput.focus()
      break
    }
  }
}

//////////////////////“回车”切换逻辑(ED)///////////////////////

</script>

<template>
  <!-- 全屏容器 -->
  <div class="fullscreen-container">
    <!-- 视频背景 -->
    <video
      ref="videoPlayer"
      class="fullscreen-video"
      :src="videoSrc"
      autoplay
      muted
      loop
      playsinline
    ></video>
    
    <!-- 内容层 -->
    <div class="content-layer">
      
      <div class="w-[345px] h-[505px] mb-[100px] bg-indigo-800/70 p-[-10px]">
      
        <div style="
         display: flex;
        flex-direction: column;
        gap: 10px;
        border: 2px dashed ;
        background-color: #ffffff;
        box-shadow: -2px -5px 0 8px #ffffff, 2px 2px 4px 2px rgba(0, 0, 0, 0.5);
        transition: .4s ease-in-out;
          " class="w-[330px] h-[490px] ">
            <!--LOGE外层-->
          <div class="w-full h-[90px] relative flex justify-center border-b-2">
          <!--LOGE-->
          <button
          @click="toHome"
          :style="{
          fontFamily: 'connectionEnglish',
          fontWeight: 300,
          transform: 'scaleX(0.6)',
          borderColor: '#000000',
          background:'linear-gradient(to right, #000000, #4a4a4a, #000000)' ,
          WebkitBackgroundClip: 'text',
          backgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          color: 'transparent',
          
          }" 
          style=
          "transition: color  0.5s ease;
          cursor: pointer;"
          class="mt-[25px] h-[45px] inline-block text-2xl border-3 pl-[15px] pr-[15px] transition-all duration-500 ">
          Patchouli's Library
          </button>
          <!--LOGE(ED)-->
        </div>
        <!--LOGE外层(ED)-->
           <!--主操作层-->
        <div class="w-full h-[calc(100%-90px)] ">
          <div class=" w-full h-[250px] mb-[20px]  flex flex-col justify-between">
            <!--NAME层-->
          <div class="w-full h-[50px] flex justify-center">
            <!--用户名输入框外层-->
            <div class=" w-[230px] relative h-[50px] bg-blue-600/50 flex justify-center items-center">
              <div class="group">
                <input 
                  ref="usernameInput"
                  @keyup.enter="handleEnterKey('username')"
                  required="" 
                  type="text" 
                  class="input w-[200px]" 
                  :class="{ 'shake': shakingFields.username, 'input-error': shakingFields.username,'input-error-bg': shakingFields.username }"
                  v-model="formData.username"
                  @input="formErrors.username = ''"
                  @blur="handleBlur('username')" 
                >
                <span class="highlight"></span>
                <span class="bar"></span>
                <label :class="{ 'shake-label': shakingFields.username, 'label-error': shakingFields.username }">
                  用户名</label>
              </div>
              <div v-if="formErrors.username" 
            style="color: #E04848; font-family: 'squareChinese'; font-size: 12px;" 
            class="absolute top-0 right-1">
            {{ formErrors.username }}
            </div>
            </div>
            <!--用户名输入框外层(ED)-->
            
          </div>
          <!--NAME层(ED)-->
           <!--密码层-->
          <div style="transition: all 0.3s ease;" class="w-full h-[50px] flex justify-center ">
            <!--密码输入框外层-->
            <div class=" w-[230px] h-[50px] relative bg-blue-600/50 flex justify-center items-center">
              <div class="group">
                <input 
                ref="passwordInput"
                @keyup.enter="handleEnterKey('password')"
                required="" 
                :type="showPassword ? 'text' : 'password'"
                class="input w-[170px]" 
                :class="{ 
                  'shake': shakingFields.password, 
                  'input-error': shakingFields.password,
                  'input-error-bg': shakingFields.password 
                }"
                v-model="formData.password" 
                @input="handlePasswordInput" 
                @blur="handleBlur('password')"
                >
                <span class="highlight"></span>
                <span class="bar"></span>
                <label :class="{ 'shake-label': shakingFields.password, 'label-error': shakingFields.password }">密码</label>
              </div>
              <!-- 密码可见切换按钮 -->
              <button 
                type="button"
                @click="togglePasswordVisibility"
                class="focus:outline-none mt-[15px] ml-[10px]">
                <Eye v-if="showPassword" style="color: #F5F9FF; cursor: pointer;" class="w-[17px] h-[20px]"/>
                <EyeClosed v-else style="color: #F5F9FF; cursor: pointer;" class="w-[17px] h-[20px]"/>
                </button>

  <div v-if="formErrors.password" 
            style="color: #E04848; font-family: 'squareChinese'; font-size: 12px;" 
            class="absolute top-0 right-1">
            {{ formErrors.password }}
            </div>
            </div>
            <!--密码输入框外层(ED)-->
            

          </div>
          <!--密码层(ED)-->
          <!--确认密码层-->
          
          <div  class="w-full flex justify-center  transition-all duration-300 ease-in-out"
     :style="{ 
       height: showConfirmPwd ? '40px' : '0px',
       opacity: showConfirmPwd ? 1 : 0,
       overflow: 'hidden'
     }">
            <!--确认密码输入框外层-->
            <div style="box-shadow: inset 0 2px 4px 4px rgba(0, 0, 0, 0.1);" class=" w-[210px] h-[40px] flex justify-center items-center">
             
              <div style="width: 180px;
                max-width: 180px;"  class="form__group confirm-pwd-group">
                <input 
                  ref="confirmPasswordInput"
                  @keyup.enter="handleEnterKey('confirmPassword')"
                  type="password" 
                  class="form__field confirm-pwd-input" 
                  :class="{ 
                    'shake': shakingFields.confirmPassword, 
                    'input-error': shakingFields.confirmPassword,
                    'input-error-bg': shakingFields.confirmPassword 
                  }"
                  placeholder="confirmPwd" 
                  required="" 
                  v-model="confirmPassword"
                  @input="handleConfirmPasswordInput"
                  @blur="handleBlur('confirmPassword')"
                >
                <label for="confirmPwd" class="form__label confirm-pwd-label">确认密码</label>
              </div>

            </div>
            <!--确认密码输入框外层(ED)-->
          </div>
          <!--确认密码层(ED)-->
          <!--邮箱层-->
          <div class="w-full h-[50px] flex justify-center">
            <!--邮箱输入框外层-->
            <div class=" w-[230px] h-[50px] relative bg-blue-600/50 flex justify-center items-center">
              <div class="group">
                <input 
                  ref="emailInput"
                  @keyup.enter="handleEnterKey('email')"
                  required="" 
                  type="text" 
                  class="input w-[200px]" 
                  :class="{ 
                    'shake': shakingFields.email, 
                    'input-error': shakingFields.email,
                    'input-error-bg': shakingFields.email 
                  }"
                  v-model="formData.email"
                  @input="formErrors.email = ''"
                  @blur="handleBlur('email')"
                >
                <span class="highlight"></span>
                <span class="bar"></span>
                <label :class="{ 'shake-label': shakingFields.email, 'label-error': shakingFields.email }">邮箱</label>
              </div>

               <div v-if="formErrors.email" 
            style="color: #E04848; font-family: 'squareChinese'; font-size: 12px;" 
            class="absolute top-0 right-1">
            {{ formErrors.email }}
            </div>
            </div>
            <!--邮箱输入框外层(ED)-->
            
          </div>
          <!--邮箱层(ED)-->
            <!--验证码层-->
          
          <div  class="w-full flex justify-center h-[40px] mb-[20px] transition-all duration-300 ease-in-out"
          :style="{ 
       height: showCodeInput ? '40px' : '0px',
       marginBottom: showCodeInput ? '20px' : '0px',
       opacity: showCodeInput ? 1 : 0,
       overflow: 'hidden'
     }">
            <!--验证码输入框外层-->
            <div style="box-shadow: inset 0 2px 4px 4px rgba(0, 0, 0, 0.1);" class=" w-[210px] h-[40px] flex justify-center items-center overflow-hidden">
             
              <div style="width: 140px;
                max-width: 140px;" class="form__group confirm-pwd-group ">
                <input 
                ref="codeInput"
                  @keyup.enter="handleEnterKey('verificationCode')"
                  type="input" 
                  class="form__field confirm-pwd-input" 
                  :class="{ 
                    'shake': shakingFields.verificationCode, 
                    'input-error': shakingFields.verificationCode,
                    'input-error-bg': shakingFields.verificationCode 
                  }"
                  placeholder="confirmPwd" 
                  required="" 
                  v-model="verificationCode"
                  @input="formErrors.email = ''"
                >
                <label for="confirmPwd" class="form__label confirm-pwd-label">验证码</label>
              </div>
              <!--发送验证码按钮-->
              <button
              @click="handleSendCode"
              :disabled="!isEmailValid || countdown > 0 || isSending"
              :class="['sendcodebutton', { 'disabled': countdown > 0 }]"
              style="font-family: 'squareChinese';
              transform: scaleX(0.9);"
               class="sendcodebutton w-[35px] h-[25px] ml-[10px] flex items-center justify-center rounded-[4px]">
              {{ countdown > 0 ? countdown + 's' : 'send' }}</button>
              <!--发送验证码按钮(ED)-->
            </div>
            <!--验证码输入框外层(ED)-->
          </div>
          <!--验证码层(ED)-->
        </div>
          
              <div class="relative w-full flex justify-center">
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
          已有账号?
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
          <!--REGISTER按钮层-->

          <div class="w-full h-[30px] flex justify-center items-center ">
            <button 
  class="register-btn" 
  :disabled="!isFormValid || isLoading"
  :class="{ 'btn-disabled': !isFormValid || isLoading }"
  @click="handleRegister"
>
  {{ isLoading ? 'Loading...' : 'Register' }}
</button>
          </div>
          <!--REGISTER按钮层(ED)-->
         

        </div>
        <!--主操作层(ED)-->
       


        </div>
      </div>

    </div>
    <!-- 内容层(ED) -->

  </div>
  <!-- 全屏容器(ED) -->

</template>

<style scoped>
/* 关键：全屏容器，无滚动 */
.fullscreen-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden; /* 隐藏任何溢出 */
  margin: 0;
  padding: 0;
}

/* 视频样式：完美覆盖，不超出 */
.fullscreen-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  
  /* 关键属性：裁剪并覆盖整个区域 */
  object-fit: cover;
  
  /* 确保没有超出 */
  min-width: 100%;
  min-height: 100%;
  
  /* 居中（备用） */
  transform: translate(-50%, -50%);
  left: 50%;
  top: 50%;
  
  /* 在最底层 */
  z-index: 0;
  
  /* 可选效果 */
  filter: brightness(0.8);
}

/* 内容层 */
.content-layer {
  position: relative;
  z-index: 10;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/*字体规定 */
@font-face {
  font-family: 'connectionEnglish'; /* 给字体起一个你喜欢的名字，后面就用它来调用 */
  src: url('../resources/font/Bitcount_Single/BitcountSingle-VariableFont.ttf') format('truetype');/*像素英语 */
  font-weight: normal;
  font-style: normal;
}

@font-face {
  font-family: 'squareChinese'; /* 给字体起一个你喜欢的名字，后面就用它来调用 */
  src: url('../resources/font/WDXL_Lubrifont_SC/WDXLLubrifontSC-Regular.ttf') format('truetype');/* 方正中文 */ 
  font-weight: normal;
  font-style: normal;
}
/*字体规定(ED) */

/*输入框样式 */

.group {
 position: relative;
}

.input {
 font-size: 16px;
 font-family: 'squareChinese';
 padding: 15px 10px 0px 5px;
 display: block;
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
 -moz-transition: 0.2s ease all;
 -webkit-transition: 0.2s ease all;
}

.input:focus ~ label, .input:valid ~ label {
 top: -5px;
 left: 0px;
 font-size: 14px;
 color: #f4f4f4;
}

.bar {
 position: relative;
 display: block;
}

.bar:before, .bar:after {
 content: '';
 height: 2px;
 width: 0;
 bottom: 1px;
 position: absolute;
 background: #5264AE;
 transition: 0.2s ease all;
 -moz-transition: 0.2s ease all;
 -webkit-transition: 0.2s ease all;
}

.bar:before {
 left: 50%;
}

.bar:after {
 right: 50%;
}

.input:focus ~ .bar:before, .input:focus ~ .bar:after {
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

.input:focus ~ .highlight, .input:valid ~ .highlight  {
  background: #5264AE ;
 animation: all 0.6s ease;
}

@keyframes inputHighlighter {
 from {
  background: transparent;
 }

 to {
  background: #5264AE;
 }
}

/*输入框样式(ED) */

/*确认密码输入框样式 */

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
 
/*确认密码输入框样式(ED) */

/*LOGIN按钮样式 */

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
    box-shadow: 4px 6px 10px   rgba(0, 0, 0, 0.25);
    transition: all 0.4s;
    margin-top: 50PX;
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

/* 禁用按钮样式 */
.register-btn:disabled,
.register-btn.btn-disabled {
  opacity: 0.8;
  cursor: not-allowed;
  pointer-events: none;
}

/*LOGIN按钮样式(ED) */
/*文字按钮样式 */
.text-btn{
  font-size: 15px;
  font-family: 'squareChinese';  
  transition: all 0.2s;
}

.text-btn:hover{
  color: #2418ff;
}

.text-btn:active{
  color: #000000;
}
/*文字按钮样式(ED) */
/*发送验证码按钮样式 */
.sendcodebutton {
  background-color: #07f3b4;
  cursor: pointer;
  box-shadow: 
  inset 0 2px 3px rgba(255, 255, 255, 0.7),  /* 顶部内发光，增加饱满感 */
  inset 0 -2px 3px rgba(0, 0, 0, 0.1),      /* 底部微暗，强化立体 */
  5px 10px 15px rgba(0, 0, 0, 0.15);           /* 外部投影 */
  transition: all 0.2s ease; 
}
.sendcodebutton:hover{
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

/*发送验证码按钮样式(ED) */

/* ========== 抖动动画 ========== */

/* 抖动动画关键帧 */
@keyframes shake {
  0%, 100% { 
    transform: translateX(0); 
  }
  10%, 30%, 50%, 70%, 90% { 
    transform: translateX(-3px); 
  }
  20%, 40%, 60%, 80% { 
    transform: translateX(3px); 
  }
}

/* 抖动类名 - 应用到输入框上就会触发动画 */
.shake {
  animation: shake 0.4s ease-in-out;
}

/* 错误状态边框颜色 - 仅在抖动时显示红色 */
.input-error {
  border-color: #E04848 !important;
  border-bottom-width: 2px !important;
}

/* 确保输入框有过渡效果，让颜色变化更平滑 */
.input, .confirm-pwd-input {
  transition: border-color 0.2s ease, border-bottom-width 0.2s ease;
  border-bottom: 1px solid #515151;
}


.confirm-pwd-input.input-error {
  border-bottom-color: #E04848 !important; /* 错误时变红 */
}

.input-error-bg {
  color: #E04848 !important;  /* 文字也变红（可选） */
}


.confirm-pwd-input.input-error-bg {
  color: #E04848 !important;
}



/* 标签抖动动画（复用 shake 动画） */
.shake-label {
  animation: shake 0.4s ease-in-out;
}
/* ========== 抖动动画 ========== */

/* 隐藏 Edge/IE 的密码查看按钮 */
input::-ms-reveal,
input::-ms-clear {
  display: none;
}

/* 隐藏 Chrome 的密码查看按钮（较新版本可能不支持） */
input[type="password"]::-webkit-credentials-auto-fill-button,
input[type="password"]::-webkit-textfield-decoration-container {
  display: none !important;
}
</style>