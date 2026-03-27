<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore =useUserStore()

const videoSrc = ref('/src/resources/pictures/background/afternoonCity.mp4') 
const videoPlayer = ref(null)



onMounted(() => {
  
  const video = videoPlayer.value
  if (video) {
    video.play().catch(e => {
      console.log('自动播放被阻止:', e)
    })
  }

 if (accountInput.value) {
    accountInput.value.focus()
    setTimeout(() => {
      if (passwordInput.value) {
        passwordInput.value.focus()
        setTimeout(() => {
          if (accountInput.value) {
            accountInput.value.focus()
            if (passwordInput.value) {
              passwordInput.value.dispatchEvent(new Event('input', { bubbles: true }))
            }
          }
        }, 200)
      }
    }, 200)
  }
})
/////////////额外功能按钮hover事件逻辑////////////

const registerButtonIsHovered = ref(false)
const forgetPasswordButtonIsHovered = ref(false)

/////////////额外功能按钮hover事件逻辑(ED////////////
////////////////////路由导航////////////////////
const toHome = () => {
  router.push('/')
}

const toRegister = () => {
  router.push('/register')
}

const toForgetPassword= () => {
  router.push('/forgetpassword')
}

////////////////////路由导航(ED)////////////////////

//////////////////登录逻辑/////////////////////
import axios from 'axios'

// 表单数据
const formData = ref({
  account: '',  // 用户名或邮箱
  password: ''
})

// 记住密码
const rememberPassword = ref(false)

// 加载状态
const isLoading = ref(false)

// 表单验证错误
const formErrors = ref({
  account: '',
  password: ''
})

// 表单验证
const validateForm = () => {
  let isValid = true
  formErrors.value = { account: '', password: '' }
  
  // 验证账号（用户名或邮箱）
  if (!formData.value.account.trim()) {
    formErrors.value.account = '账号不能为空'
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
  
  return isValid
}

// 登录函数
const handleLogin = async () => {
  if (!validateForm()) return
  if (isLoading.value) return

  isLoading.value = true
  try {
    await userStore.login(
      formData.value.account,
      formData.value.password,
      rememberPassword.value
    )

    if (rememberPassword.value) {
      localStorage.setItem('rememberedUsername', formData.value.account)
    } else {
      localStorage.removeItem('rememberedUsername')
    }

    // 登录成功，跳转到首页或之前的页面
    router.push('/')
  } catch (error) {
    // 错误已经在响应拦截器中提示，这里可忽略或额外处理
    console.error('登录失败', error)
  } finally {
    isLoading.value = false
  }
}


//////////////////登录逻辑(ED)/////////////////////
////////////////////切换焦点逻辑////////////////////

const accountInput = ref(null)
const passwordInput = ref(null)

// 回车键处理
const handleEnterKey = (currentField) => {
  if (currentField === 'account') {
    // 聚焦到密码框
    passwordInput.value?.focus()
  } else if (currentField === 'password') {
    // 触发登录
    handleLogin()
  }
}


////////////////////切换焦点逻辑(ED)////////////////////


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
          style="transition: color  0.5s ease;"
          class="mt-[25px] h-[45px] inline-block text-2xl border-3 pl-[15px] pr-[15px] transition-all duration-500 ">
          Patchouli's Library
          </button>
          <!--LOGE(ED)-->
        </div>
        <!--LOGE外层(ED)-->
        <!--主操作层（w-390px h-490px）-->
        <div class="w-full h-[calc(100%-90px)] pt-[20px]">
          <!--NAME层-->
          <div class="w-full h-[90px] flex justify-center items-end">
            <!--帐号输入框外层-->
            <div class=" w-[230px] h-[50px] bg-blue-600/50 flex justify-center items-center">
              <div class="group">
                <input 
                  ref="accountInput"
                  @keyup.enter="handleEnterKey('account')"
                  required="" 
                  type="text" 
                  class="input" 
                  v-model="formData.account"
                >
                <span class="highlight"></span>
                <span class="bar"></span>
                <label>邮箱/用户名</label>
              </div>
            </div>
            <!--帐号输入框外层(ED)-->
            
          </div>
          <!--NAME层(ED)-->
           <!--密码层-->
          <div class="w-full h-[70px] flex justify-center items-end">
            <!--密码输入框外层-->
            <div class=" w-[230px] h-[50px] bg-blue-600/50 flex justify-center items-center">
              <div class="group">
                <input 
                  ref="passwordInput"
                  @keyup.enter="handleEnterKey('password')"
                  required="" 
                  type="password" 
                  class="input" 
                  v-model="formData.password"
                >
                <span class="highlight"></span>
                <span class="bar"></span>
                <label>密码</label>
              </div>
            </div>
            <!--密码输入框外层(ED)-->
            
          </div>
          <!--密码层(ED)-->
          <!--记住我-->
            <div class="w-full h-[40px] relative flex pl-[45px]">
             
                <label for="remember" class="cyberpunk-checkbox-label">
              <input type="checkbox" id="remember" class="cyberpunk-checkbox" v-model="rememberPassword">
              记住我</label>  
           
             
            </div>
            <!--记住我(ED)-->
          <!--LOGIN按钮层-->

          <div class="w-full h-[100px] flex justify-center border-b-2 ">
            <button class="login-btn " @click="handleLogin" :disabled="isLoading">
               {{ isLoading ? 'LOGIN...' : 'LOGIN' }}
            </button>
          </div>
          <!--LOGIN按钮层(ED)-->
          <!--其他服务按钮层-->

          <div class="w-full h-[40px] flex justify-center gap-[60px]">
    <!-- 忘记密码按钮 -->
    <div class="relative">
      <button
      @click="toForgetPassword"
        @mouseenter="forgetPasswordButtonIsHovered = true"
        @mouseleave="forgetPasswordButtonIsHovered = false"
        class="text-btn hover:cursor-pointer relative w-[100px] h-[60px] overflow-hidden"
      >
        <div 
          :style="{
            transform: forgetPasswordButtonIsHovered ? 'translateY(-100%)' : 'translateY(0)',
            opacity: forgetPasswordButtonIsHovered ? 0 : 1,
            transition: 'all 0.3s ease'
          }"
          class="absolute inset-0 flex items-center justify-center"
        >
          忘记密码?
        </div>
        
        <div 
          :style="{
            transform: forgetPasswordButtonIsHovered ? 'translateY(0)' : 'translateY(100%)',
            opacity: forgetPasswordButtonIsHovered ? 1 : 0,
            transition: 'all 0.3s ease'
          }"
          class="absolute inset-0 flex items-center justify-center"
        >
          前去 找回
        </div>
      </button>
    </div>

    <!-- 注册按钮 -->
    <div class="relative">
      <button
      @click="toRegister"
        @mouseenter="registerButtonIsHovered = true"
        @mouseleave="registerButtonIsHovered = false"
        class="text-btn hover:cursor-pointer relative w-[100px] h-[60px] overflow-hidden"
      >
        <div 
          :style="{
            transform: registerButtonIsHovered ? 'translateY(-100%)' : 'translateY(0)',
            opacity: registerButtonIsHovered ? 0 : 1,
            transition: 'all 0.3s ease'
          }"
          class="absolute inset-0 flex items-center justify-center"
        >
          没有账号?
        </div>
        
        <div 
          :style="{
            transform: registerButtonIsHovered ? 'translateY(0)' : 'translateY(100%)',
            opacity: registerButtonIsHovered ? 1 : 0,
            transition: 'all 0.3s ease'
          }"
          class="absolute inset-0 flex items-center justify-center"
        >
          立即 注册
        </div>
      </button>
    </div>
  </div>
          <!--其他服务按钮层(ED)-->

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
 width: 200px;
 border: none;
 border-bottom: 1px solid #515151;
 background: transparent;
}

.input:focus {
 outline: none;
}

.group label {
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
 width: 200px;
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

/*LOGIN按钮样式 */

.login-btn {
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

.login-btn:active {
    margin-top: 57px;
    margin-right: 10px;
    transition: all 0.1s;
}

.login-btn:hover {
  box-shadow: inset 0 0 0 4px #ef476f, 
              inset 0 0 0 8px #ffd166, 
              inset 0 0 0 12px #06d6a0,
              inset 0 0 0 16px #118ab2;
  background: #073b4c;
  color: #fff;
  height: 45px;
  width: 140px;

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

/*记住密码样式*/
.cyberpunk-checkbox {
  appearance: none;
  width: 15px;
  height:15px;
  border: 2px solid #30cfd0;
  border-radius: 1px;
  background-color: transparent;
  display: inline-block;
  position: relative;
  margin-right: 10px;
  cursor: pointer;
}

.cyberpunk-checkbox:before {
  content: "";
  background-color: #30cfd0;
  display: block;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0);
  width: 15px;
  height: 15px;
  transition: all 0.3s ease-in-out;
}

.cyberpunk-checkbox:checked:before {
  transform: translate(-50%, -50%) scale(1);
}

.cyberpunk-checkbox-label {
  font-weight: normal;
  font-family: 'squareChinese';
  font-size: 15px;
  color: #000000;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  margin-left: 10px;
}
/*记住密码样式(ED*/
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

/* 去除浏览器自动填充的默认背景色 */
input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus,
input:-webkit-autofill:active {
    -webkit-box-shadow: 0 0 0 30px rgba(37, 99, 235, 0.5) inset !important; 
    box-shadow: 0 0 0 30px rgba(37, 99, 235, 0.5) inset !important;
    -webkit-text-fill-color: #141414 !important; 
    caret-color: #343434;
}
</style>