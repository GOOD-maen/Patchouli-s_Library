<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import 'qweather-icons/font/qweather-icons.css'

const weatherData = ref(null)
const loading = ref(true)
const error = ref(null)

const isRainy = computed(() => {
  if (!weatherData.value) return false
  const text = weatherData.value.text
  const icon = weatherData.value.icon

  if (text.includes('雨')) return true

  return false
})

const textColor = computed(() => {
  return isRainy.value ? '#FFFFFF' : '#1a1a1a'   // 雨天白色，晴天深灰色
})

onMounted(async () => {
  try {
    const response = await axios.get('/api/weather/now?city=101190101')
    weatherData.value = response.data
  } catch (err) {
    console.error('天气获取失败', err)
    error.value = '天气加载失败'
  } finally {
    loading.value = false
  }
})
</script>
<template>

<div class="w-full h-full rounded-t-[10px] relative">
      <div v-if="loading" class="absolute inset-0 flex items-center justify-center text-white">
      加载天气中...
      </div>
      <div v-else-if="error" class="absolute inset-0 flex items-center justify-center text-white">
      {{ error }}
      </div>
      <template v-else>
        <!--最外层-->
      <div class=" w-full h-[100px] mt-[10px] absolute flex z-5 ">

        <!--天气图标-->
        <div class="w-[90px] h-full">
          <div class="w-full h-[calc(100%-30px)] flex items-center justify-center">
            <i :style="{ color: textColor, fontSize: '40px' }"  :class="'qi-' + weatherData?.icon" class="test"></i>
          </div>
          <div :style="{ color: textColor}" style="font-family: 'squareChinese';font-size: 20px; transform: scaleX(1.2);" class="test w-full h-[30px] flex items-center justify-center">
            {{ weatherData?.text }}
          </div>
        </div>
        <!--天气图标(ED)-->

        <!--温度与空气质量-->
        <div class="w-[calc(100%-90px)] h-full flex justify-end">
          <div  class="w-[140px] h-full">
            <div  class="w-full  h-[70px] flex ">
              <div :style="{ fontFamily: 'squareChinese', fontSize: '50px', color: textColor }"  class=" w-[70px] h-full ml-[50px]">
                {{ weatherData?.temp }}°
              </div>
            </div>
            <div  class="w-full  h-[60px] ">
              <div :style="{ fontFamily: 'squareChinese', fontSize: '20px', color: textColor }"  class=" w-full  h-[30px]  flex justify-center">
                {{ weatherData?.tempMin }}° / {{ weatherData?.tempMax }}°
              </div>
              <div :style="{ fontFamily: 'squareChinese', fontSize: '16px', transform: 'scaleX(1.3)', color: textColor }"  class="w-full  h-[30px]  flex justify-center">
                空气质量: {{ weatherData?.airCategory || '--' }} {{ weatherData?.aqi ? `(${weatherData.aqi})` : '' }}
              </div>
            </div>

          </div>
        </div>
        <!--温度与空气质量(ED)-->

      </div>
      <!--最外层(ED)-->
    <!--雨天-->
    <div v-if="isRainy" class="rainy_day w-full h-full rounded-t-[10px]  overflow-hidden">
      <!--最里层-->
      <div class="inside_content absolute w-[400px] h-[400px] ">
        <!--顶层 -->
        <div class="top_storey w-full h-[50px]  flex relative">
          <!--云1-->
          <svg id="cloud1"  viewBox="0 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
            
            d="M19.5 69.7s-21.3.5-25-12.2c0 0-4.3-21.3 16-21.8 0 0-2.1-12.2 12.2-14.9 0 0 15-3.2 21.3 6.9 0 0 3.6-20.7 17.8-22.3 0 0 24-3 26.6 13.1 0 0 .1 9.5-2.8 13.5 0 0 9.5-15 26.5-4.8 0 0 12.1 7.9 7 20.2 0 0 16 4.8 10.1 18.1 0 0-10.2 8.5-17.1-1.1 0 0-5.5 16-32.5 16 0 0-19.1 2.1-27-13.3 0 0 .5 10.1-13.3 10.6-.1 0-20.3 3.2-19.8-8z">

          </path>
        </svg>
          <!--云1(ED)-->

          <!--云2-->
        <svg id="cloud2" viewBox="0 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
            
            d="M19.5 69.7s-21.3.5-25-12.2c0 0-4.3-21.3 16-21.8 0 0-2.1-12.2 12.2-14.9 0 0 15-3.2 21.3 6.9 0 0 3.6-20.7 17.8-22.3 0 0 24-3 26.6 13.1 0 0 .1 9.5-2.8 13.5 0 0 9.5-15 26.5-4.8 0 0 12.1 7.9 7 20.2 0 0 16 4.8 10.1 18.1 0 0-10.2 8.5-17.1-1.1 0 0-5.5 16-32.5 16 0 0-19.1 2.1-27-13.3 0 0 .5 10.1-13.3 10.6-.1 0-20.3 3.2-19.8-8z">

          </path>
        </svg>
          <!--云2(ED)-->
          <!--云3-->
        <svg id="cloud3" viewBox="0 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
            
            d="M19.5 69.7s-21.3.5-25-12.2c0 0-4.3-21.3 16-21.8 0 0-2.1-12.2 12.2-14.9 0 0 15-3.2 21.3 6.9 0 0 3.6-20.7 17.8-22.3 0 0 24-3 26.6 13.1 0 0 .1 9.5-2.8 13.5 0 0 9.5-15 26.5-4.8 0 0 12.1 7.9 7 20.2 0 0 16 4.8 10.1 18.1 0 0-10.2 8.5-17.1-1.1 0 0-5.5 16-32.5 16 0 0-19.1 2.1-27-13.3 0 0 .5 10.1-13.3 10.6-.1 0-20.3 3.2-19.8-8z">

          </path>
        </svg>
          <!--云3(ED)-->

        </div>
        <!--顶层(ED) -->
        
        <!--底层-->
        <div class="button_storey w-full h-[30px] mt-[60px] relative">
          <!--云1-->
        <svg id="cloud1"  viewBox="0 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
  
            d="M19.5 69.7s-21.3.5-25-12.2c0 0-4.3-21.3 16-21.8 0 0-2.1-12.2 12.2-14.9 0 0 15-3.2 21.3 6.9 0 0 3.6-20.7 17.8-22.3 0 0 24-3 26.6 13.1 0 0 .1 9.5-2.8 13.5 0 0 9.5-15 26.5-4.8 0 0 12.1 7.9 7 20.2 0 0 16 4.8 10.1 18.1 0 0-10.2 8.5-17.1-1.1 0 0-5.5 16-32.5 16 0 0-19.1 2.1-27-13.3 0 0 .5 10.1-13.3 10.6-.1 0-20.3 3.2-19.8-8z">

          
          </path>
        </svg>
          <!--云1(ED)-->
          <!--云2-->
        <svg id="cloud2"  viewBox="500 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
  
            d="M511.7 12.4s-21.3-.3-25 7c0 0-4.3 12.2 16 12.5 0 0-2.1 7 12.2 8.6 0 0 15 1.8 21.3-4 0 0 3.6 11.9 17.8 12.8 0 0 19.5 1.6 27-4.4 0 0 5-4.4 2.1-6.7 0 0 4.1 4.4 21.2-1.5 0 0 12.1-4.6 7-11.6 0 0 16-2.8 10.1-10.4 0 0-10.2-4.9-17.1.6 0 0-5.5-9.2-32.5-9.2 0 0-19.1-1.2-27 7.6 0 0 .5-5.8-13.3-6.1-.1.2-20.3-1.6-19.8 4.8z">

          
          </path>
        </svg>
          <!--云2(ED)-->
          <!--云3-->
        <svg id="cloud3"  viewBox="500 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
  
            d="M511.7 12.4s-21.3-.3-25 7c0 0-4.3 12.2 16 12.5 0 0-2.1 7 12.2 8.6 0 0 15 1.8 21.3-4 0 0 3.6 11.9 17.8 12.8 0 0 19.5 1.6 27-4.4 0 0 5-4.4 2.1-6.7 0 0 4.1 4.4 21.2-1.5 0 0 12.1-4.6 7-11.6 0 0 16-2.8 10.1-10.4 0 0-10.2-4.9-17.1.6 0 0-5.5-9.2-32.5-9.2 0 0-19.1-1.2-27 7.6 0 0 .5-5.8-13.3-6.1-.1.2-20.3-1.6-19.8 4.8z">

          
          </path>
        </svg>
          <!--云3(ED)-->
        <!--雨1-->
          <div class="rain1">
            <div class="drop"></div>
            <div class="drop"></div>
            <div class="drop"></div>
            <div class="drop"></div>
            <div class="drop"></div>
          </div>
        <!--雨1(ED)-->
        <!--雨2-->
          <div class="rain2">
            <div class="drop"></div>
            <div class="drop"></div>
            <div class="drop"></div>
            <div class="drop"></div>
            <div class="drop"></div>
          </div>
        <!--雨2(ED)-->
        <!--雨3-->
          <div style="left: 200px;" class="rain2">
            <div class="drop"></div>
            <div class="drop"></div>
            <div class="drop"></div>
            <div class="drop"></div>
            <div class="drop"></div>
          </div>
        <!--雨3(ED)-->

        </div>
        <!--底层-->
        


      </div>
      <!--最里层(ED)-->


    </div>
    <!--雨天(ED)-->
    <!--晴天-->
    <div  v-else  class="sunny_day w-full h-full rounded-t-[10px]  overflow-hidden">
      
      <!--最里层-->
      <div class="inside_content absolute w-[400px] h-[400px] ">
      
        <!--整个太阳-->
        <div class="the_whole_sun ">
        <!--内光圈-->
        <svg id="light_aperture1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="341.54505920410156" height="337.5205383300781" viewBox="0 0 341.54505920410156 337.5205383300781" fill="none"><g filter="url(#filter_3_13)"><path d="M323.459 180.196C326.545 155.065 323.526 129.56 314.659 105.844C305.792 82.1275 291.338 60.8985 272.521 43.9563L261.983 55.6608C278.917 70.9089 291.926 90.015 299.907 111.36C307.887 132.704 310.604 155.659 307.827 178.277L323.459 180.196Z"   fill="#FFFFFF" ></path><path d="M113.265 13C89.4723 21.6599 68.118 35.9284 51.0121 54.5961C33.9063 73.2639 21.5533 95.7805 15 120.238L30.2133 124.314C36.1113 102.303 47.229 82.0376 62.6243 65.2367C78.0195 48.4357 97.2384 35.5941 118.652 27.8002L113.265 13Z"   fill="#FFFFFF" ></path><path d="M72.3475 286.787C92.5688 302.025 116.166 312.163 141.138 316.342C166.111 320.521 191.723 318.617 215.803 310.793L210.936 295.814C189.264 302.856 166.213 304.569 143.738 300.808C121.262 297.046 100.025 287.922 81.826 274.208L72.3475 286.787Z"   fill="#FFFCFC" ></path></g><defs><filter id="filter_3_13" x="0" y="0" width="341.54505920410156" height="337.5205383300781" filterUnits="userSpaceOnUse" color-interpolation-filters="sRGB"><feFlood flood-opacity="0" result="feFloodId_3_13"/><feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha_3_13"/><feOffset dx="0" dy="2"/><feGaussianBlur stdDeviation="7.5"/><feComposite in2="hardAlpha_3_13" operator="out"/><feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0"/><feBlend mode="normal" in2="feFloodId_3_13" result="dropShadow_1_3_13"/><feBlend mode="normal" in="SourceGraphic" in2="dropShadow_1_3_13" result="shape_3_13"/></filter></defs>
        </svg>
        <!--内光圈(ED)-->
        <!--太阳-->

        <div id="sun">

        </div>

        <!--太阳(ED)-->

        </div>
        <!--整个太阳(ED)-->

        <!--云1-->

        <svg id="cloud1_shadow" viewBox="0 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
            
            d="M19.5 69.7s-21.3.5-25-12.2c0 0-4.3-21.3 16-21.8 0 0-2.1-12.2 12.2-14.9 0 0 15-3.2 21.3 6.9 0 0 3.6-20.7 17.8-22.3 0 0 24-3 26.6 13.1 0 0 .1 9.5-2.8 13.5 0 0 9.5-15 26.5-4.8 0 0 12.1 7.9 7 20.2 0 0 16 4.8 10.1 18.1 0 0-10.2 8.5-17.1-1.1 0 0-5.5 16-32.5 16 0 0-19.1 2.1-27-13.3 0 0 .5 10.1-13.3 10.6-.1 0-20.3 3.2-19.8-8z">

          </path>
        </svg>
        <svg id="cloud1" viewBox="0 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
            
            d="M19.5 69.7s-21.3.5-25-12.2c0 0-4.3-21.3 16-21.8 0 0-2.1-12.2 12.2-14.9 0 0 15-3.2 21.3 6.9 0 0 3.6-20.7 17.8-22.3 0 0 24-3 26.6 13.1 0 0 .1 9.5-2.8 13.5 0 0 9.5-15 26.5-4.8 0 0 12.1 7.9 7 20.2 0 0 16 4.8 10.1 18.1 0 0-10.2 8.5-17.1-1.1 0 0-5.5 16-32.5 16 0 0-19.1 2.1-27-13.3 0 0 .5 10.1-13.3 10.6-.1 0-20.3 3.2-19.8-8z">

          </path>
        </svg>
        <!--云1(ED)-->
        <!--云2-->
        <svg id="cloud2_shadow" viewBox="500 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
            
            d="M511.7 12.4s-21.3-.3-25 7c0 0-4.3 12.2 16 12.5 0 0-2.1 7 12.2 8.6 0 0 15 1.8 21.3-4 0 0 3.6 11.9 17.8 12.8 0 0 19.5 1.6 27-4.4 0 0 5-4.4 2.1-6.7 0 0 4.1 4.4 21.2-1.5 0 0 12.1-4.6 7-11.6 0 0 16-2.8 10.1-10.4 0 0-10.2-4.9-17.1.6 0 0-5.5-9.2-32.5-9.2 0 0-19.1-1.2-27 7.6 0 0 .5-5.8-13.3-6.1-.1.2-20.3-1.6-19.8 4.8z">

          </path>
        </svg>
        <svg id="cloud2" viewBox="500 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
            
            d="M511.7 12.4s-21.3-.3-25 7c0 0-4.3 12.2 16 12.5 0 0-2.1 7 12.2 8.6 0 0 15 1.8 21.3-4 0 0 3.6 11.9 17.8 12.8 0 0 19.5 1.6 27-4.4 0 0 5-4.4 2.1-6.7 0 0 4.1 4.4 21.2-1.5 0 0 12.1-4.6 7-11.6 0 0 16-2.8 10.1-10.4 0 0-10.2-4.9-17.1.6 0 0-5.5-9.2-32.5-9.2 0 0-19.1-1.2-27 7.6 0 0 .5-5.8-13.3-6.1-.1.2-20.3-1.6-19.8 4.8z">

          </path>
        </svg>
        <!--云2(ED)-->
        <!--云3-->
        <svg id="cloud3"  viewBox="500 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
  
            d="M511.7 12.4s-21.3-.3-25 7c0 0-4.3 12.2 16 12.5 0 0-2.1 7 12.2 8.6 0 0 15 1.8 21.3-4 0 0 3.6 11.9 17.8 12.8 0 0 19.5 1.6 27-4.4 0 0 5-4.4 2.1-6.7 0 0 4.1 4.4 21.2-1.5 0 0 12.1-4.6 7-11.6 0 0 16-2.8 10.1-10.4 0 0-10.2-4.9-17.1.6 0 0-5.5-9.2-32.5-9.2 0 0-19.1-1.2-27 7.6 0 0 .5-5.8-13.3-6.1-.1.2-20.3-1.6-19.8 4.8z">

          
          </path>
        </svg>
        <!--云3(ED)-->
        <!--云4-->
        <svg id="cloud4_shadow" viewBox="0 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
            
            d="M19.5 69.7s-21.3.5-25-12.2c0 0-4.3-21.3 16-21.8 0 0-2.1-12.2 12.2-14.9 0 0 15-3.2 21.3 6.9 0 0 3.6-20.7 17.8-22.3 0 0 24-3 26.6 13.1 0 0 .1 9.5-2.8 13.5 0 0 9.5-15 26.5-4.8 0 0 12.1 7.9 7 20.2 0 0 16 4.8 10.1 18.1 0 0-10.2 8.5-17.1-1.1 0 0-5.5 16-32.5 16 0 0-19.1 2.1-27-13.3 0 0 .5 10.1-13.3 10.6-.1 0-20.3 3.2-19.8-8z">

          </path>
        </svg>
         <svg id="cloud4" viewBox="0 0 200 200" preserveAspectRatio="xMidYMid meet">
          <path
            
            d="M19.5 69.7s-21.3.5-25-12.2c0 0-4.3-21.3 16-21.8 0 0-2.1-12.2 12.2-14.9 0 0 15-3.2 21.3 6.9 0 0 3.6-20.7 17.8-22.3 0 0 24-3 26.6 13.1 0 0 .1 9.5-2.8 13.5 0 0 9.5-15 26.5-4.8 0 0 12.1 7.9 7 20.2 0 0 16 4.8 10.1 18.1 0 0-10.2 8.5-17.1-1.1 0 0-5.5 16-32.5 16 0 0-19.1 2.1-27-13.3 0 0 .5 10.1-13.3 10.6-.1 0-20.3 3.2-19.8-8z">

          </path>
        </svg>
        <!--云4-->

      </div>
      <!--最里层(ED)-->


    </div>
    <!--晴天(ED)-->
    </template>
  </div>
</template>
<style scoped lang="scss">
/*雨天*/
.rainy_day{
    background: linear-gradient(to bottom, #222033, #222033, #3a3a55);
    position: relative; 
  /*最里层 */
  .inside_content{
    .top_storey{
      #cloud1{
        fill: rgb(87, 87, 87);
        width: 200px;
        height: 150px;
        position: absolute;
        top: -20px;
        left: -30px;
        z-index: 1;
      }
      #cloud2{
        fill: rgb(39, 39, 39);
        width: 200px;
        height: 180px;
        position: absolute;
        top: -15px;
        left: 50px;
        transform: scaleX(2);
      }
      #cloud3{
        fill: rgb(54, 54, 54);
        width: 200px;
        height: 180px;
        position: absolute;
        top: -25px;
        right: -20px;
        transform: scaleX(1.7);
      }
    }
    .button_storey{

      /* 雨1 */
      .rain1 {
        width: 180px;
        height: 140px;
        position: absolute;
        opacity: 0.5;
        top: -100px;
              .drop {
        position: relative;
        float: left;
        width: 2px;
        margin: 10px;
        left: 20px;
        background: #789;
        box-shadow: 1px 0.5px 1px 0.5px rgba(0, 0, 0, 0.75);
        animation: rain_401 0.8s infinite ease-out;
      }

      .drop:nth-child(1) {
        height: 15px;
        top: 5px;
        animation-delay: -1.0s;
      }

      .drop:nth-child(2) {
        height: 20px;
        animation-delay: -1.4s;
      }

      .drop:nth-child(3) {
        height: 15px;
        top: 5px;
        animation-delay: -1.6s;
      }

      .drop:nth-child(4) {
        height: 10px;
        top: 10px;
        animation-delay: -1.2s;
      }

      .drop:nth-child(5) {
        height: 5px;
        top: 15px;
        animation-delay: -1.6s;
      }

      @keyframes rain_401 {
        0% {
          opacity: 1;
          transform: translate(0, 0);
        }

        100% {
          opacity: 0.2;
          transform: translate(0, 100px);
        }
      }

      }
      /* 雨1(ED) */
      /* 雨2 */
      .rain2 {
        width: 180px;
        height: 140px;
        position: absolute;
        opacity: 0.5;
        top: -100px;
        left: 100px;
      .drop {
        position: relative;
        float: left;
        width: 2px;
        margin: 10px;
        left: 20px;
        background: #789;
        box-shadow: 1px 0.5px 1px 0.5px rgba(0, 0, 0, 0.75);
        animation: rain_401 0.8s infinite ease-out;
      }

      .drop:nth-child(1) {
        height: 20px;
        animation-delay: -1.4s;
      }

      .drop:nth-child(2) {
        height: 15px;
        top: 5px;
        animation-delay: -1.0s;
       
      }

      .drop:nth-child(3) {
        height: 15px;
        top: 5px;
        animation-delay: -1.6s;
      }

      .drop:nth-child(4) {
        height: 10px;
        top: 10px;
        animation-delay: -1.2s;
      }

      .drop:nth-child(5) {
        height: 5px;
        top: 15px;
        animation-delay: -1.6s;
      }

      @keyframes rain_401 {
        0% {
          opacity: 1;
          transform: translate(0, 0);
        }

        100% {
          opacity: 0.2;
          transform: translate(0, 100px);
        }
      }

      }
      /* 雨2(ED) */


      #cloud1{
        fill: rgb(111, 115, 118);
        position: absolute;
        width: 200px;
        height: 100px;
        top: -10px;
        left: -40px;
        transform: scaleX(1.8);
        animation: cloud1Move 30s linear infinite;
      }
      #cloud2{
        fill: rgb(167, 171, 171);
        position: absolute;
        width: 100px;
        height: 50px;
        top: 0px;
        left: 140px;
        transform: scaleX(1.8);
        z-index: 1;
        animation: cloud2Move 30s linear infinite;
        
      }
      #cloud3{
        fill: rgb(99, 100, 107);
        position: absolute;
        width: 500px;
        height: 200px;
        top: 0px;
        left: 140px;
        transform: scaleX(1.8);
        z-index: 1;
        
      }
      @keyframes cloud1Move {
      0% { left: -150px; }
      100% { left: 500px; }
      }
      @keyframes cloud2Move {
      0% { left: -350px; }
      100% { left: 500px; }
      }
    }

  }
  /*最里层 (ED)*/

}

/*雨天(ED)*/


/* 晴天 */

.sunny_day{


  background: linear-gradient(to bottom, #00acd7, #63e4fe, #98e2ff);
  position: relative; 
  /*  最里层 */
  .inside_content{
    .the_whole_sun{
      
      width: 150px;
      height: 150px;
      position: absolute;
      left: 190px;
      top: 30px;
      #light_aperture1{
        position: absolute;
        width: 180px;
        height: 180px;
        fill: #222033;
        animation: spin 10s linear infinite;
        z-index: 2;
        left: 1px;
        top: -3px;
      }

      #sun{
        width: 120px;
        height: 120px;
        position: absolute;
        background: #ffc400;
        border-radius: 100%;
        top: 26px;
        left: 30px;
        z-index: 1;
        box-shadow: 
         0 0 0 10px rgba(255, 251, 0, 0.35), 
         0 0 0 20px rgba(255, 253, 108, 0.35), 
         0 0 0 30px rgba(251, 250, 182, 0.25), 
         0 0 0 45px rgba(255, 255, 255, 0.25), 
         0 0 0 65px rgba(255, 255, 255, 0.1), 
        ;
        animation: pulse 2s ease-in-out infinite;
      }

      @keyframes light_aperture1_spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
      }
      @keyframes pulse {
      0% {
        transform: scale(0.95);
      }
      50% {
        transform: scale(1.05);
      }
      100% {
        transform: scale(0.95);
      }
    }
    }


    #cloud1_shadow{
      position: absolute;
      fill: rgba(215, 215, 215, 0.45);
      width: 200px;
      height: 200px;
      top: 50px;
    }
    #cloud1{
      position: absolute;
      fill: rgba(255, 255, 255, 0.55);
      width: 190px;
      height: 200px;
      top: 50px;
      left: -5px;
    }
    #cloud2_shadow{
      position: absolute;
      fill: rgba(65, 65, 65, 0.55);
      width: 230px;
      height: 230px;
      top: -15px;
      left: 5px;
      z-index: 1;
      transform: scaleX(1.4)
    }
    #cloud2{
      position: absolute;
      fill: rgba(252, 251, 251, 0.85);
      width: 210px;
      height: 210px;
      top: -20px;
      left: 20px;
      z-index: 1;
      transform: scaleX(1.4)
    }
    #cloud3{
      position: absolute;
      z-index: 2;
      fill: rgb(247, 251, 252);
      width: 150px;
      height: 130px;
      top: 50px;
      transform: scaleX(1.4);
      animation: cloud3Move 20s linear infinite;
    }
    @keyframes cloud3Move {
      0% { left: -150px; }
      100% { left: 500px; }
      }
    #cloud4_shadow{
      position: absolute;
      z-index: 0;
      fill: rgba(71, 71, 71, 0.4);
      width: 360px;
      height: 300px;
      transform: scaleX(1.4);
      right: -140px;
      top: 43px;
    }
      #cloud4{
      position: absolute;
      z-index: 0;
      fill: rgb(207, 241, 250);
      width: 320px;
      height: 290px;
      transform: scaleX(1.4);
      right: -120px;
      top: 50px;
    }
  }

  /*  最里层(ED) */

}

/* 晴天(ED) */

@font-face {
  font-family: 'squareChinese';
  src: url('../resources/font/WDXL_Lubrifont_SC/WDXLLubrifontSC-Regular.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
  }


</style>