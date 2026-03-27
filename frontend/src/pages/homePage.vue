<script setup>
import { ref, onMounted ,computed } from 'vue'
import 'typeface-roboto';
import { useRouter, useRoute } from 'vue-router';
import { Search,Bell ,User, Sun ,UserRoundPlus ,KeyRound ,LogOut} from 'lucide-vue-next';
import { useUserStore } from '@/stores/user';
import testBackground from '@/resources/pictures/background/background.png';
import headImage from '@/resources/pictures/heads/mc_head.png';



/////////////////////////用户信息相关//////////////////////////////

const userStore = useUserStore()
const displayName = computed(() => userStore.userInfo?.username || '游客')
const displayUUID = computed(() => userStore.userInfo?.uuid || '------')
const displaySign = computed(() => userStore.userInfo?.signature || '这个人很懒，什么都没有留下。')
const baseUrl = 'http://localhost:8080'
const getFullImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/')) return `${baseUrl}${url}`
  const cleanUrl = url.replace(/^\/+/, '')
  return `${baseUrl}/${cleanUrl}`
}
const userBackground = computed(() => {
  return getFullImageUrl(userStore.userInfo?.backgroundImage) || testBackground
})
const userAvatar = computed(() => {
  return getFullImageUrl(userStore.userInfo?.avatar) || headImage
})
/////////////////////////用户信息相关//////////////////////////////
///////////////////////登出方法////////////////////////

const handleLogout = () => {
  userStore.logout()
  userCardShowed.value = false // 关闭用户卡片
  router.push('/') // 跳转到首页，也可以跳转到登录页
}

///////////////////////登出方法(ED)=////////////////////////

////////////////背景视频播放////////////

const videoPlayer = ref(null)
const blankBoxHeight = ref(600)
const topBoxOpacity = ref(0)
const topFrosted = ref(0)


onMounted(() => {

  const video = videoPlayer.value
  if (video) {
    video.play().catch(e => {
      console.log('自动播放被阻止:', e)
    })
  }
/////////////////////滚轮控制“空框”的高度/////////////////////

  const contentBox = document.getElementById('contentBox')

  if (contentBox) {
    contentBox.addEventListener('scroll', () => {
      const scrollTop = contentBox.scrollTop
      
      if(route.path == '/') {
      // 核心逻辑：滚动越多，高度越小
      blankBoxHeight.value = Math.max(100, 600 - scrollTop * 0.7)
      // 从完全透明(100)过渡到80%透明(20)
      topBoxOpacity.value = Math.min(100, scrollTop * 0.3)
      // 顶部毛玻璃的效果控制
      topFrosted.value = Math.min( 60 , scrollTop*0.1 )
     
      }else{
        topBoxOpacity.value = 100;
        topFrosted.value = 60;
      }
    }) 
  }
 
  const actualBoxOpacity = computed(() => {
  if (route.path !== '/') {
    return 100;
  }
  return topFrosted.value;
});
/////////////////////滚轮控制“空框”的高度(ED/////////////////////

})
////////////////背景视频播放(ED)////////////
/////////////////LOGE的颜色控制开关/////////////
 const shouldTurnBlack = computed(() => {
  if (route.path !== '/') {
    return true;
  }

  return topBoxOpacity.value >= 70;
})
/////////////////LOGE的颜色控制开关(ED)/////////////

/////////////////路由//////////////

const router = useRouter()
const route = useRoute();

const toHome = () => {
  router.push('/')
}

const toUserCenter = () =>{
  router.push('/userCenter')
}

const toEssayList = () => {
    if (route.path == '/essayList') {
      router.push('/')
    }else{
      router.push('/essayList')
    }
}

const toVideoList = () => {
  
}

const toCssList = () => {
  router.push('/main/cssList')
}
//前往编辑页面
const goToEssayManagePage =async()=>{
        if (route.path == '/manage/essayManage') {
        await router.push('/')
        }else{
          await router.push('/manage/essayManage')
        }
        
    }

//前往登录页面
const goToLoginPage =async()=>{
        await router.push('/login')
    }
/////////////////路由(ED//////////////
///////////ManagePageButton样式效果方法///////////
  
const managePageButtonisHovered = ref(false)

///////////ManagePageButton样式效果方法(ED///////////
//////////////////弹出用户小卡片方法//////////////////

const userCardShowed = ref(false)

const changeUserCardShowed = () => {
  userCardShowed.value = !userCardShowed.value
  console.log('userCardShowed值发生变化',userCardShowed )
}

//////////////////弹出用户小卡片方法(ED//////////////////
////////////////控制用户小卡片的展示逻辑////////////////

const isLogin = computed(() => !!userStore.userInfo)

////////////////控制用户小卡片的展示逻辑(ED)////////////////


</script>

<template>
  <!-- 总框 -->
  <div class="video-background-container">
   
    <!-- 内容层-->
    <div id="contentBox" class="content-overlay bg-slate-100 w-full h-full overflow-y-auto">

    
      <!--顶层-->
      <div :style="{
      position: 'sticky',
      top: '0',
      backdropFilter: `blur(${topFrosted}px)`,
      background: `rgba(241, 245, 249, ${topBoxOpacity / 100})`,
      transition: 'all 0.3s ease-out',
      boxShadow: shouldTurnBlack 
    ? '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)' 
    : '0 4px 6px -1px rgba(0, 0, 0, 0), 0 2px 4px -1px rgba(0, 0, 0, 0)'
      }"
      class="h-[60px] w-full flex items-center z-50">

<button
  :class="{ 'black-title-style': shouldTurnBlack }"
  :style="{
    fontFamily: 'connectionEnglish',
    fontWeight: 300,
    transform: 'scaleX(0.6)',
    // 动态边框颜色：黑色或白色
    borderColor: shouldTurnBlack ? '#000000' : '#ffffff',
    // 动态背景渐变
    background: shouldTurnBlack 
      ? 'linear-gradient(to right, #000000, #4a4a4a, #000000)' 
      : 'linear-gradient(to right, #FFFFFF, #979797, #FFFFFF)',
    // 以下裁剪属性保持不变
    WebkitBackgroundClip: 'text',
    backgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    color: 'transparent',
  }" 
  style="transition: color  0.5s ease;"
  @click="toHome"
  class="text-white inline-block text-2xl border-3 pl-[15px] pr-[15px] transition-all duration-500">
  Patchouli's Library
</button>

      <div
       :style="
      {
        color: shouldTurnBlack ? '#000000' : '#ffffff'
      }" 
      @click="toEssayList" style="
      font-family: 'squareChinese';
      font-weight: 300;" 
      class="topButton   pl-[10px] pr-[10px] h-full mr-[20px] transition-all duration-500">
      ESSAY
      </div>

      <div :style="
      {
        color: shouldTurnBlack ? '#000000' : '#ffffff'
      }" 
      style="
      font-family: 'squareChinese';
      font-weight: 300;" 
      class="topButton  pl-[10px] pr-[10px] h-full mr-[20px] transition-all duration-500">
      VIDEOS
      </div>

      <div 
      :style="
      {
        color: shouldTurnBlack ? '#000000' : '#ffffff'
      }" 
      @click="toCssList" style="
      font-family: 'squareChinese';
      font-weight: 300;" 
      class="topButton   pl-[10px] pr-[10px] h-full mr-[20px] transition-all duration-500">
      CSS
      </div>

      <!--顶层右边部分-->
      <div style="
      display: flex;
      position: relative;
      margin-left: auto;" class=" h-full w-[380px] pr-[6%] ">
      <!--个人空间按钮与搜索按钮-->
      <div
       :style="
      {
        color: shouldTurnBlack ? 'rgba(41, 55, 75, 0.7)' : 'rgba(255, 251, 255, 0.7)'
      }" 
      style="
      display: flex;
      justify-content: center;
      align-items: center;
      " class="w-[80px] h-full ">

      <div class="w-full h-[28px] flex relative">
        <Search 
        class="flex  w-[18px] mr-[10px] hover:cursor-pointer"></Search>
        <div>|</div>
        <User 
        @mouseenter="userCardShowed = true"
        @mouseleave="userCardShowed = false" 
        class="flex w-[18px] ml-[10px] hover:cursor-pointer "></User>
        
     
           <!--用户小卡片(外框)-->
           <!--登录时-->
       <Transition name="slide-fade-login">
      <div
        v-if="userCardShowed && isLogin"
        @mouseenter="userCardShowed = true"
        @mouseleave="userCardShowed = false" 
        class="absolute flex p-[25px] bottom-[-230px] left-[-220px] z-50 "
      >
        <div style="box-shadow: 10px 10px 60px 0 rgba(0,0,0,0.3), 0 15px 30px -5px rgba(0,0,0,0.2);" 
        class=" login_card w-[270px] h-[200px] p-[4px] bg-slate-100/70 rounded-[2px]">
          <div style=" border: 2px dashed rgba(255, 255, 255, 0.8);" class=" w-full h-full">
            <!--背景-->
            <div class="w-full h-[75px] pl-[3px] pr-[3px] pt-[2px]">

              <div style="box-shadow: inset 0 -4px 10px -1px rgba(0, 0, 0, 0.5), inset 0 -2px 6px -5px rgba(0, 0, 0, 0.16);" class="w-full h-full bg-cover bg-center"  :style="{ backgroundImage: `url(${userBackground})` }">
              </div>

            </div>
            <!--背景(ED)-->
            <!--头像，名称，签名，按钮-->
            <div class="w-full h-[113px] pl-[3px] pr-[3px] pb-[3px]"> 
              <div  class="w-full h-full ">
                  <div 
                  style="background: linear-gradient(0deg, rgb(62, 107, 254) 0%, rgb(24, 42, 171) 50%, rgb(25, 38, 180) 100%);"
                  class="  w-full h-[15px] "></div>

                <!--头像，名称和id-->
                <div class=" w-full h-[50px] mt-[-35px] flex">
                  <!--头像-->
                  <div  class="classic_shadow w-[60px] h-[60px] mr-[5px] rounded-full shadow-2xl bg-slate-50/90 mt-[-10px] ml-[10px] p-[3px]" >
                    <div class="w-full h-full rounded-full  bg-cover bg-center" :style="{ backgroundImage: `url(${userAvatar})` }"></div>
                  </div>
                  <!--头像(ED)-->
                  <!--名称和id-->
                  <div class=" h-[60px] mt-[-10px]">
                    <div style="font-family: 'squareChinese';font-size: 20px;" class="text-[#000]"> {{ displayName }}</div>
                    <div class=" id_card flex items-center ml-[10px] p-[3px] pr-[15px] mt-[-2px]">
                      UUID: {{ displayUUID }}
                    </div>
                  </div>
                  <!--名称和id(ED)-->

                </div>
                <!--头像，名称和id(ED)-->
                <!--按钮和签名-->
                <div class="buttons_and_sign w-full h-[calc(100%-15px)] mt-[-15px]">
                  <div class="w-full h-[18px]"></div>
                  <!--签名-->
                  <div class=" w-full h-[20px] flex items-center pl-[15px] ">
                    <div class="sign w-[calc(100%-20px)]">
                      签名: {{ displaySign }}
                    </div>
                  </div>
                  <!--签名(ED)-->
                  <!--按钮-->
                  <div class="w-full h-[55px] flex items-center justify-center">
                    <!--用户中心-->
                    <button @click="toUserCenter" class="button user_center flex items-center justify-center">
                      <User class="w-[15px]"></User><div class="w-[4px]"></div> 用户中心
                    </button>
                    <!--用户中心(ED)-->

                    <!--退出登录-->
                    <button @click="handleLogout" class="button logout  flex items-center justify-center">
                      <LogOut class="w-[15px]"></LogOut> <div class="w-[4px]"></div>退出登录
                    </button>
                    <!--退出登录(ED)-->

                    <div></div>
                  </div>
                  <!--按钮(ED)-->

                </div>
                <!--按钮和签名(ED)-->

              </div>
            </div>
            <!--头像，名称，签名，按钮(ED)-->

          </div>
        </div>
      </div>
    </Transition>
    <!--登录时(ED)-->
    <!--未登录-->
     <Transition name="slide-fade-nologin">
      <div
        v-if="userCardShowed && !isLogin"
        @mouseenter="userCardShowed = true"
        @mouseleave="userCardShowed = false" 
        class="absolute flex p-[25px] bottom-[-130px] left-[-170px] z-50 "
      >
      <div style="box-shadow: 10px 10px 60px 0 rgba(0,0,0,0.3), 0 15px 30px -5px rgba(0,0,0,0.2);" 
     class="noLoginCard w-[250px] h-[100px]  p-[4px] bg-slate-500/70 rounded-[5px] ">
      <div style=" border: 2px dashed rgba(255, 255, 255, 0.6);" class=" w-full h-full flex items-center justify-center">
      <!--登录按钮-->
      <button @click="goToLoginPage" class="loginButton classic_shadow mt-[10px]">
        <div class="h-[20px] w-full flex items-center justify-center">
        <User style="font-size: 10px; margin: 8px; width: 20px;"></User>
        </div>
        <div style="font-size: 12px;font-family: 'squarechinese';transform: scaleX(1.2);">登 录</div>
      </button>
      <!--登录按钮(ED)-->
      <!--注册按钮-->
      <button  class="registerButton classic_shadow  mt-[10px]">
         <div class="h-[20px] w-full flex items-center justify-center">
        <UserRoundPlus style="font-size: 10px; margin: 8px; width: 20px;"></UserRoundPlus>
        </div>
        <div style="font-size: 12px;font-family: 'squarechinese';transform: scaleX(1.2);">注 册</div>
      </button>
      <!--注册按钮(ED)-->
      <!--找寻密码按钮-->
      <button class="findPasswordButton classic_shadow mt-[10px]">

        <div class="h-[20px] w-full flex items-center justify-center">
        <KeyRound style="font-size: 10px; margin: 8px; width: 20px;"></KeyRound>
        </div>
        <div style="font-size: 12px;font-family: 'squarechinese';transform: scaleX(1);">找回密码</div>

      </button>
      <!--找寻密码按钮-->
      </div>
    </div>
      </div>
    </Transition>
    <!--未登录(ED)-->
<!--用户小卡片(外框)(ED)-->

      </div>

      </div>
      <!--个人空间按钮与搜索按钮(ED)-->

      <!--消息页面按钮-->
      <div style="
      display: flex;
      justify-content: center;
      align-items: center;
      " class=" h-full w-[50px] ">
      <button 
      style="display: flex;
      justify-content: center;
      align-items: center;
      " class="ml-[20px] w-[30px] h-[30px] bg-slate-500/25  rounded-full">
      <Bell class="w-[17px] h-[17px] text-amber-50/70"></Bell>
      </button>
      </div>
      <!--消息页面按钮(ED)-->


      <!--夜晚与白天模式切换按钮-->
      <div style="
      display: flex;
      justify-content: center;
      align-items: center;
      " class=" h-full w-[50px] ">
      <button 
      style="display: flex;
      justify-content: center;
      align-items: center;
      " class=" w-[30px] h-[30px] bg-slate-500/25  rounded-full">
      <Sun class="w-[20px] h-[20px] text-amber-50/70"></Sun>
      </button>
      </div>
      <!--夜晚与白天模式切换按钮(ED)-->
      
      <!--编辑页面按钮-->
      
      <button @click="goToEssayManagePage" 
      @mouseenter="managePageButtonisHovered = true"
      @mouseleave="managePageButtonisHovered = false"
      style="
      display: flex;
      justify-content: center;
      align-items: center;
      " class="h-full w-[80px] ml-[20px] ">
        <div style="
        display: flex;
        position: relative;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        overflow: hidden;
        "
        class="managePageButton w-full h-[26px] rounded-full border-2 border-blue-500 text-amber-100">
        
        <div 
         :style="{
          transform: managePageButtonisHovered ? 'translateY(100%)' : 'translateY(0)',
          opacity: managePageButtonisHovered ? 0 : 1
          }
          "
          style="
          display: flex;
          font-family: 'squareChinese';
          font-weight: 100;
          cursor: pointer;
          transition: transform 0.3s ease, opacity 0.3s ease;
          position: absolute;
        ">发 布</div>

        <!-- "OMG"文字 -->
        <div 
        :style="{
        transform: managePageButtonisHovered ? 'translateY(0)' : 'translateY(-100%)',
        opacity: managePageButtonisHovered ? 1 : 0
        }"
        style="
          display: flex;
          justify-content: center;
          align-items: center;
          font-family: 'squareChinese';
          font-weight: 100;
          cursor: pointer;
          transition: transform 0.3s ease, opacity 0.3s ease;
          position: absolute;
        "
        class=" h-[26px] rounded-full"
        >
        OMG!
        </div>

      </div>
      </button>

      <!--编辑页面按钮(ED)-->

      </div>
      <!--顶层右边部分(ED)-->

      </div>
      <!--顶层(ED)-->
      <router-view>

      </router-view>
      <!--下侧介绍卡片-->
      <div style="box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.1), 0 -2px 4px -1px rgba(0, 0, 0, 0.06);" class="w-full h-[200px] bg-slate-100 pl-[30px] pr-[30px] z-50">

        <!--空白填充层-->

        <div class="w-[100%-100px] h-[25px] border-b-2 border-slate-800/60"></div>
        <!--空白填充层(ED)-->

        <!--LOGE-->
        <button
        :class="{ 'black-title-style': shouldTurnBlack }"
        :style="{
          fontFamily: 'connectionEnglish',
          fontWeight: 300,
          transform: 'scaleX(0.6)',
          // 动态边框颜色：黑色或白色
          borderColor: shouldTurnBlack ? '#000000' : '#ffffff',
          // 动态背景渐变
          background: shouldTurnBlack 
            ? 'linear-gradient(to right, #000000, #4a4a4a, #000000)' 
            : 'linear-gradient(to right, #FFFFFF, #979797, #FFFFFF)',
          // 以下裁剪属性保持不变
          WebkitBackgroundClip: 'text',
          backgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          color: 'transparent',
        }" 
        style="transition: color  0.5s ease;"
        class="text-white inline-block text-2xl border-3 pl-[15px] pr-[15px] mt-[20px] transition-all duration-500">
        Patchouli's Library
      </button>
      <!--LOGE(ED)-->
      </div>
      <!--下侧介绍卡片(ED)-->

    </div>
    <!-- 内容层(ED)-->

  </div>
  <!--总框(ED)-->
</template>
<style scoped>
/* 总容器：占据全屏/全区域 */
.video-background-container {
  position: relative; /* 关键 */
  width: 100%;
  height: 100vh; /* 使用视口高度，或你需要的其他高度 */
  min-height: 100vh;
  overflow: hidden; /* 隐藏视频溢出部分 */
}

/* 视频样式：固定为背景 */
.background-video {
  position: absolute;
  top: 30%;
  left: 50%;
  transform: translate(-50%, -50%); /* 完美居中 */
  min-width: 100%;
  min-height: 100%;
  width: auto;
  height: auto;
  
  /* 确保视频在最底层 */
  z-index: -1;
  
  /* 视频滤镜和效果（可选） */
  filter: brightness(0.7); /* 降低亮度让文字更清晰 */
  object-fit: cover;

}

.content-overlay {
  position: relative; 
  z-index: 1; 
}
/*内容框的滚轮 */


#contentBox::-webkit-scrollbar {
    width: 7px; /* 稍微加宽一点以容纳效果 */
}

#contentBox::-webkit-scrollbar-track {
    /* 渐变色轨道背景 */
    background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
    
    /* 内阴影效果 */
    box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.05);
}

#contentBox::-webkit-scrollbar-thumb {
    /* 渐变色滑块 */
    background: linear-gradient(180deg, #858585 0%, #636363 100%);
    border: 1px solid #f1f1f1; /* 与轨道背景形成边界 */
    background-clip: padding-box;
    min-height: 40px;
    border-radius: 5px;
    /* 平滑过渡效果 */
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}


/*内容框的滚轮(ED) */

.transition-height {
  transition: height 0.1s ease-out;
}

/*字体规定 */
@font-face {
  font-family: 'connectionEnglish';
  src: url('../resources/font/Bitcount_Single/BitcountSingle-VariableFont.ttf') format('truetype');/*像素英语 */
  font-weight: normal;
  font-style: normal;
}

@font-face {
  font-family: 'squareChinese'; 
  src: url('../resources/font/WDXL_Lubrifont_SC/WDXLLubrifontSC-Regular.ttf') format('truetype'); 
  font-weight: normal;
  font-style: normal;
}


/*字体规定(ED) */
/*顶部按钮 效果 */
.topButton {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: color 0.5s;
}

.topButton:hover{
  color:#353DD3;
  cursor: pointer;
}

.topButton::before{
  content: '';
  position: absolute;
  height: 0px;
  width: 100%;
  background-color:#353DD3;
  top: -15px;
  transition: height 0.3s ease ,top 0.3s;
}

.topButton:hover::before{
  top: 0px;
  height: 10px;
}

.topButton::after{
  content: '';
  position: absolute;
  height: 0px;
  width: 100%;
  background-color:#353DD3;
  bottom: 0px;
  transition: height 0.3s ease,bottom 0.3s;
}

.topButton:hover::after{
  height: 5px;
}

/*顶部按钮 效果(ED)*/
/*编辑页面按钮样式 */
.managePageButton{
  background-color:#353DD3;
  transition: background-color 0.2s ease, border 0.2s ease;
}

.managePageButton:hover{
  background-color: #292fa7;
}

/*编辑页面按钮样式(ED) */

/*用户小卡片动态样式 */

.slide-fade-login-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-login-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-login-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.slide-fade-login-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.slide-fade-nologin-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-nologin-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-nologin-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.slide-fade-nologin-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}


/*用户小卡片动态样式(ED) */
/* 未登录用户小卡片 */
.noLoginCard .loginButton{
  cursor: pointer;
  color: #d9d9d9;
  width: 45px;
  height: 45px;
  border: 1px #484848 solid;
  margin-left: 10px;
  margin-right: 10px;
  background: #292fa7;
  opacity: 0.8;
  position: relative;
  box-shadow: 2px 4px 20px 5px rgba(0, 0, 0, 0.15), 0 -2px 10px 8px rgba(0, 0, 0, 0.06);
}

.noLoginCard .registerButton{
  cursor: pointer;
  color: #d9d9d9;
  width: 45px;
  height: 45px;
  border: 1px rgba(49, 136, 124, 0.1) solid;
  margin-left: 10px;
  margin-right: 10px;
  background: #01bdbd;
  opacity: 1;
  position: relative;
  box-shadow: 2px 4px 20px 5px rgba(0, 0, 0, 0.15), 0 -2px 10px 8px rgba(0, 0, 0, 0.06);

}

.noLoginCard .findPasswordButton{
  cursor: pointer;
  color: #d9d9d9;
  width: 45px;
  height: 45px;
  border: 1px rgba(50, 168, 50, 0.9) solid;
  margin-left: 10px;
  margin-right: 10px;
  background: #45cc74;
  opacity: 1;
  position: relative;
  box-shadow: 2px 4px 20px 5px rgba(0, 0, 0, 0.15), 0 -2px 10px 8px rgba(0, 0, 0, 0.06);

}
.classic_shadow {
  box-shadow: 2px 4px 10px 0px rgba(0, 0, 0, 0.1), 0 -2px 10px 8px rgba(0, 0, 0, 0.06);
}
/* 未登录用户小卡片(ED) */
/* 登录用户小卡片 */
.login_card{
  .id_card{
    border: #272727 2px solid;
    font-family: 'squareChinese';
    transform: scaleX(1.2);
    background: linear-gradient(135deg, rgb(255, 255, 255) 0%, rgb(144, 186, 254) 50%, rgb(99, 148, 255) 100%);
    opacity: 0.9;
    color: #303030;
    font-size: 11px;
  }
  .buttons_and_sign{
    background: #f1f1f1;
    background-image: linear-gradient(
      90deg,
      transparent 50px,
      #ffb4b8 50px,
      #ffb4b8 52px,
      transparent 52px
    ),
    linear-gradient(#e1e1e1 0.1em, transparent 0.1em);
    background-position: -35px 0;
    background-size: 100% 21px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06),
    inset 0 4px 6px 8px rgba(0, 0, 0, 0.1),inset 0 2px 4px 4px rgba(0, 0, 0, 0.06);

    .sign{
      color: #363636;
      font-family:'Franklin Gothic Medium', 'Arial Narrow', Arial, sans-serif;
      font-size: 12px;
      border-bottom: #383946 1.5px solid;
      white-space: nowrap;  
      overflow: hidden;        
      text-overflow: ellipsis; 
    }

    .button{
        width: 90px;
        height: 30px;
        margin-left: 20px;
        margin-right: 20px;
        box-shadow: 2px 4px 10px 0px rgba(0, 0, 0, 0.2), 0 -2px 7px 4px rgba(0, 0, 0, 0.1);
        font-size: 13px;
        font-family: 'squareChinese';
        color: white;
        transition: all 0.2s ease;
        cursor: pointer; 
    }
    .button:hover{
      transform: scale(1.03);           
      box-shadow: 0 8px 15px rgba(0, 0, 0, 0.3);  
      opacity: 1; 
    }
    .button:active{
      transform: scale(0.98); 
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    }
    .user_center{
      background: #2029d3;
      opacity: 0.7;
    }
    .user_center:hover{
      background: #2a35e0; 
      opacity: 0.9;
    }
    .logout{
      background: #c01717;
      opacity: 0.7;
    }
    .logout:hover{
      background: #d32f2f;
      opacity: 0.9;
    }
  }
}
</style>