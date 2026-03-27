<script setup>
import { ref, onMounted, computed } from 'vue'
import testBackground from '@/resources/pictures/background/background.png'
import headImage from '@/resources/pictures/heads/mc_head.png'
import { useUserStore } from '@/stores/user'
import Sycamore from '@/resources/pictures/background/Sycamore.jpg'
import { Menu, Settings } from 'lucide-vue-next'
import { useRouter, useRoute } from 'vue-router'

const userStore = useUserStore()
const displayName = computed(() => userStore.userInfo?.username || '游客')

// 图片地址辅助函数
const baseUrl = 'http://localhost:8080'
const getFullImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/')) return `${baseUrl}${url}`
  const cleanUrl = url.replace(/^\/+/, '')
  return `${baseUrl}/${cleanUrl}`
}

// 用户背景图片（如果用户有则用，否则用默认）
const userBackground = computed(() => {
  return getFullImageUrl(userStore.userInfo?.backgroundImage) || testBackground
})

// 用户头像图片（如果用户有则用，否则用默认）
const userAvatar = computed(() => {
  return getFullImageUrl(userStore.userInfo?.avatar) || headImage
})

/////////////// router //////////////////
const router = useRouter()
const route = useRoute()

const switchToWorks = () => {
  router.push({ query: { ...route.query, tab: 'userWorks' } })
}

const switchToSettings = () => {
  router.push({ query: { ...route.query, tab: 'setting' } })
}

onMounted(() => {
  if (!route.query.tab) {
    router.replace({ query: { tab: 'userWorks' } })
  }
})
/////////////// router(ED) //////////////////
</script>

<template>
  <div class="w-full min-h-[600px] flex justify-center">
    <!-- 总框 -->
    <div class="w-[80%] min-w-[1200px]">
      <!-- 顶部背景，头像等信息 -->
      <div
        class="w-full h-[500px] classic_shadow bg-center bg-cover relative"
        :style="{ backgroundImage: `url(${userBackground})` }"
      >
        <!-- 下方容纳框 -->
        <div
          style="bottom: -50px; left: -30px; background: rgba(255, 255, 255, 0.2); backdrop-filter: blur(2px);"
          class="classic_shadow infor_content w-[calc(100%+60px)] h-[150px] absolute flex items-end"
        >
          <div style="border: 1.5px #000 solid;" class="w-full h-[100px] bg-white flex relative">
            <!-- 头像与名称 -->
            <div
              style="position: absolute; top: -120px; left: 70px; border: 1.5px #000 solid;"
              class="w-[150px] h-[200px] little_shadow bg-slate-100"
            >
              <div
                :style="{ backgroundImage: `url(${userAvatar})` }"
                class="w-[120px] h-[120px] m-[15px] bg-green-300 inset_shadow bg-cover bg-center"
              ></div>
              <div class="w-full h-[50px] border-t-2 border-gray-700">
                <div class="w-full h-[35px] flex items-center justify-center">
                  <div class="inset_shadow name w-[100px] h-[25px] flex items-center justify-center">
                    {{ displayName }}
                  </div>
                </div>
                <div
                  style="background: linear-gradient(180deg, rgb(62, 107, 254) 0%, rgb(24, 42, 171) 50%, rgb(25, 38, 180) 100%);"
                  class="w-full h-[10px]"
                ></div>
              </div>
            </div>
            <!-- 头像与名称(ED) -->

            <div style="border-right: 1.5px #000 solid;" class="w-[60px] h-full"></div>
            <div class="w-[170px] h-full flex items-end">
              <div class="w-full h-[12px] border-t-[1.5px]"></div>
            </div>
            <div style="border-left: 1.5px #000 solid;" class="w-[calc(100%-230px)] h-full"></div>
          </div>
        </div>
        <!-- 下方容纳框(ED) -->
      </div>
      <!-- 顶部背景，头像等信息(ED) -->

      <div class="w-full mt-[70px] mb-[30px] content_box1 flex">
        <div class="w-[300px] h-[800px] mr-[30px]">
          <!-- 功能列表 -->
          <div class="w-full h-[600px] bg-white classic_shadow mb-[30px]">
            <!-- 标题 -->
            <div style="font-family:'squareChinese'; font-size: 20px;" class="w-full h-[50px] bottom_shadow">
              <div class="w-full flex items-center justify-center h-[45px] pt-[15px]">——— 个人主页 ———</div>
              <div class="w-full h-[1px] bg-black"></div>
            </div>
            <!-- 标题(ED) -->
            <!-- 内容 -->
            <div class="w-full h-[calc(100%-80px)] flex">
              <div
                :style="{ backgroundImage: `url(${Sycamore})` }"
                style="background-position: -30px 0px;"
                class="w-[120px] h-full bg-center bg-cover"
              ></div>
              <div class="w-[calc(100%-120px)] h-full flex">
                <div class="w-[10px] h-full border-r-[1.5px] border-slate-700/10"></div>
                <!-- 功能框 -->
                <div class="w-[calc(100%-10px)] function_button h-full pt-[20px]">
                  <div :class="{ active: route.query.tab === 'userWorks' }"
                  @click="switchToWorks" class="button">
                    <Menu class="w-[20px] mr-[20px]"/> 我的作品
                    <span class="bar"></span>
                  </div>
                  <div :class="{ active: route.query.tab === 'setting' }" 
                  @click="switchToSettings" class="button">
                    <Settings class="w-[20px] mr-[20px]"/> 设置
                    <span class="bar"></span>
                  </div>
                </div>
                <!-- 功能框(ED) -->
              </div>
            </div>
            <!-- 内容(ED) -->
            <!-- 下方填充层 -->
            <div class="w-full h-[30px] top_shadow"></div>
            <!-- 下方填充层(ED) -->
          </div>
          <!-- 功能列表(ED) -->
          <div class="w-full h-[160px] bg-white classic_shadow"></div>
        </div>
        <div class="w-[calc(100%-320px)] h-[790px] flex">
          <!-- 多种内容展示 -->
          <div class="w-[600px] mr-[30px]">
            <router-view></router-view>
          </div>
          <!-- 多种内容展示(ED) -->
          <div class="w-[calc(100%-630px)] h-full bg-white classic_shadow"></div>
        </div>
      </div>
    </div>
    <!-- 总框(ED) -->
  </div>
</template>

<style scoped>
/* 样式保持不变 */
.top_shadow {
  box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.1), 0 -2px 4px -1px rgba(0, 0, 0, 0.06);
}
.bottom_shadow {
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
.infor_content .solid_line {
  background: linear-gradient(0deg, rgb(184, 201, 255) 0%, rgb(66, 88, 250) 50%, rgb(47, 63, 249) 100%);
}
.infor_content .name {
  font-family: 'squareChinese';
  font-size: 18px;
  transform: scaleX(1.1);
}
.infor_content .inset_shadow {
  box-shadow: inset 2px 4px 5px 0px rgba(0, 0, 0, 0.1), inset 0 -2px 10px 0px rgba(0, 0, 0, 0.06);
}
.content_box1 .classic_shadow {
  box-shadow: 2px 4px 6px 5px rgba(0, 0, 0, 0.1), 0 -2px 8px 8px rgba(0, 0, 0, 0.06);
}
.function_button .button {
  font-family: "squareChinese";
  font-size: 17px;
  margin-bottom: 5px;
  display: flex;
  padding: 4px 15px 2px 15px;
  justify-content: space-between;
  color: rgb(87, 87, 87);
  border-bottom: 1.5px solid rgba(0, 0, 0, 0.1);
  position: relative;
  cursor: pointer;
  transition: 0.2s ease all;
}
.function_button .button:hover {
  color: #5b73ff;
}
.function_button .bar {
  position: absolute;
  display: block;
  width: 100%;
  bottom: 0;
  left: 0;
}
.function_button .bar:before,
.function_button .bar:after {
  content: '';
  height: 2px;
  width: 0;
  bottom: 0;
  position: absolute;
  background: #5b73ff;
  transition: 0.5s ease all;
}
.function_button .bar:before {
  left: 0%;
}
.function_button .button:hover .bar:before {
  width: 100%;
}
.function_button .button:focus .bar:before {
  box-shadow: 0 0 8px 1px rgba(91, 115, 255, 0.6);
  width: 100%;
}
.function_button .button.active {
  color: #5b73ff;
}
.function_button .button.active .bar:before {
  width: 100%;
}
@font-face {
  font-family: 'squareChinese';
  src: url('../resources/font/WDXL_Lubrifont_SC/WDXLLubrifontSC-Regular.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}
</style>