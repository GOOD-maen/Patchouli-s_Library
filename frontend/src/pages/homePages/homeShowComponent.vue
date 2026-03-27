<script setup>
import { ref, onMounted ,computed ,watch ,onUnmounted} from 'vue'
import 'typeface-roboto';
import { useRouter } from 'vue-router';
import { Search,Bell ,User, Sun} from 'lucide-vue-next';
import { useEssayStore } from '@/stores/essayStore';
import defaultCoverImage from '@/resources/pictures/test.png';
import weatherModule from './modules/weatherComponent.vue';
import homeClock from './modules/homeClock.vue';
import axios from 'axios';

////////////////背景视频播放////////////

const videoSrc = ref('/src/resources/pictures/background/catWithSnow .mp4') 
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
      
      // 核心逻辑：滚动越多，高度越小
      // 从600px开始，每滚动1px减少0.2px高度，最小100px
      blankBoxHeight.value = Math.max(100, 600 - scrollTop * 0.7)
      // 从完全透明(100)过渡到80%透明(20)
      topBoxOpacity.value = Math.min(100, scrollTop * 0.3)
      // 顶部毛玻璃的效果控制
      topFrosted.value = Math.min( 60 , scrollTop*0.1 )
    }) 
  }

 
/////////////////////滚轮控制“空框”的高度(ED/////////////////////
  essayStore.ensureDataLoaded().then(() => {
    refreshEssayShowed() 
  })
})
////////////////背景视频播放(ED)////////////
///////////////////////////获取essay数据///////////////////////////

const essayStore = useEssayStore();

const essays = computed(() =>essayStore.essays || [] )

const loading = computed (()=> essayStore.loading)

onMounted(() => {
  essayStore.ensureDataLoaded()
})

watch(essays, (newVal) => {
  console.log('📋 获取到 essays 数据:', newVal)
}, { immediate: true })

//用于展示的essay
const essayShowed = ref(null)

watch(essays, (newVal) => {
  if (newVal && newVal.length > 0) {
    const randomIndex = Math.floor(Math.random() * newVal.length)
    essayShowed.value = newVal[randomIndex]
  } else {
    essayShowed.value = null
  }
}, { immediate: true })

//获取封面
const getCoverImage = (essay) => {
  if (essay && essay.coverUrl && essay.coverUrl.trim() !== '') {
    return `http://localhost:8080/uploads/${essay.coverUrl}`;
  }
  return defaultCoverImage;
};

const refreshEssayShowed = () => {
  if (essays.value && essays.value.length > 0) {
    const randomIndex = Math.floor(Math.random() * essays.value.length)
    essayShowed.value = essays.value[randomIndex]
  } else {
    essayShowed.value = null
  }
}

///////////////////////////获取essay数据(ED)///////////////////////////
/////////////////////////未来三天天气数据获取///////////////////////
const threeDayForecast = ref([])

const fetchForecast = async () => {
  try {
    const response = await axios.get('/api/weather/forecast?city=101190101')
    threeDayForecast.value = response.data
  } catch (error) {
    console.error('获取预报失败', error)
  }
}

onMounted(() => {
  fetchForecast()
})

const getWeekDay = (dateStr) => {
  const date = new Date(dateStr + 'T00:00:00')
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[date.getDay()]
}

/////////////////////////未来三天天气数据获取(ED)///////////////////////

///////////////////////轮播流动框逻辑///////////////////////////

// 轮播当前索引（指向 carouselItems 中的哪一项应显示在中间）
const currentCarouselIndex = ref(0)

// 基础五篇文章（假设 essays 至少有5篇，取前5篇作为轮播固定集合）
const carouselItems = computed(() => essays.value.slice(0, 5))

// 上一张（让 currentCarouselIndex 减1，循环）
const prevCarousel = () => {
  if (carouselItems.value.length === 0) return
  currentCarouselIndex.value = (currentCarouselIndex.value - 1 + carouselItems.value.length) % carouselItems.value.length
}

// 下一张
const nextCarousel = () => {
  if (carouselItems.value.length === 0) return
  currentCarouselIndex.value = (currentCarouselIndex.value + 1) % carouselItems.value.length
}

const cardCount = 5


const getCardStyle = (cardIndex) => {

  const visualIndex = (cardIndex - currentCarouselIndex.value + cardCount) % cardCount
  

  if (visualIndex === 0) { 
    return { transform: `translateX(-240px) scale(0.65)`, zIndex: 8, opacity: 0.7 }
  } else if (visualIndex === 1) { 
    return { transform: `translateX(-160px) scale(0.8)`, zIndex: 9, opacity: 0.85 }
  } else if (visualIndex === 2) {
    return { transform: `translateX(0) scale(1)`, zIndex: 10, opacity: 0.9 }
  } else if (visualIndex === 3) { 
    return { transform: `translateX(160px) scale(0.8)`, zIndex: 9, opacity: 0.85 }
  } else { 
    return { transform: `translateX(240px) scale(0.65)`, zIndex: 8, opacity: 0.7 }
  }
}

///////////////////////轮播流动框逻辑(ED)//////////////////////////
/////////////////////////////进入文章详情页//////////////////////////////

const router = useRouter()
const openEssayContent = (essayId) => {
  router.push(`/essay/${essayId}`)
}

/////////////////////////////进入文章详情页(ED)//////////////////////////////
/////////////////////双击流动框方法///////////////////////
// 存储每个卡片的定时器（使用索引作为 key）
const clickTimerMap = ref({})

// 卡片点击处理函数
const handleCardClick = (essay, index) => {
  if (!essay) return // 防止文章不存在

  // 如果该卡片已有定时器，说明是双击（第二次点击）
  if (clickTimerMap.value[index]) {
    clearTimeout(clickTimerMap.value[index])
    clickTimerMap.value[index] = null
    // 执行双击逻辑：进入详情页
    openEssayContent(essay.id)
  } else {
    // 第一次点击，设置定时器延迟执行单击逻辑
    clickTimerMap.value[index] = setTimeout(() => {
      // 单击：将下方展示的文章换成当前点击的文章
      essayShowed.value = essay
      // 清除定时器引用
      clickTimerMap.value[index] = null
    }, 250) // 延迟250ms
  }
}
/////////////////////双击流动框方法(ED)///////////////////////
onUnmounted(() => {
  Object.values(clickTimerMap.value).forEach(timer => {
    if (timer) clearTimeout(timer)
  })
})
</script>

<template>
    
    <!-- 视频背景层 -->
    <video
      ref="videoPlayer"
      class="background-video"
      :src="videoSrc"
      autoplay
      muted
      loop
      playsinline
    ></video>
    <!--视频背景层(ED)-->
      <!--空层-->
      <div 
        class="w-full  transition-height duration-100"
        :style="{ height: blankBoxHeight + 'px' ,
          transition: 'all 0.15s ease-out'  
        }"
      ></div>
      <!--空层-->
      <!--内容区-->
      <div style="background-color: rgba(235, 240, 240, 1); " class=" w-full  "> 
        <div class="w-full h-[20px] bg-slate-600/90 border-b-[3.5px] border-slate-200/30"></div>
        <!--推送-->
        <div class="w-full pb-[20px]">

          <!--推送标题-->

          <div class=" w-full h-[70px] flex justify-center items-center">
            <div class=" w-[1190px] h-full flex items-center border-b-[1.5px] border-slate-800/60">

              <div class="w-[210px] h-[2px] bg-slate-700"></div>
              <div class=" push_title h-full pl-[20px] pr-[20px] flex items-center">
                JUST  BROWSING
              </div>
              <div class="w-[210px] h-[2px] bg-slate-700"></div>

            </div>
          </div>

          <!--推送标题(ED)-->

          <!--上层区域-->
          <div class="w-full p-[20px] pt-[15px] flex items-center justify-center">
            
            <!--流动框-->     
            <div style="overflow: hidden;" class="flow_box w-[800px] h-[210px] inside_shadow relative overflow-hidden">
              <div class="relative w-full h-full">
                <div
                  v-for="cardIndex in 5"
                  :key="cardIndex"
                  class=" absolute top-0 left-0 w-full h-full flex items-center justify-center transition-all duration-300"
                  :style="getCardStyle(cardIndex - 1)"
                >
                  <el-tooltip
                    content="双击进入详情页"
                    placement="bottom-end"  
                    effect="dark"           
                    :show-after="200"       
                    popper-class="custom-tooltip">
                  <!-- 卡片内容 -->
                  <div class="w-[240px] h-[180px] card  classic_shadow pr-2 pl-2 pb-2 flex  justify-end flex-col"
                  @click="handleCardClick(carouselItems[cardIndex - 1], cardIndex - 1)">
                    <div class="w-full h-[2px] border-b-[1px] mt-[10px] mb-[2px] flex items-center justify-center"> 
                      <div
                        v-for="index in 20"
                        :key="index"
                        class="little_box"
                      ></div>
                    </div>
                    <div class="w-full h-[2px] border-t-[1px] bg-white mb-[5px]"> </div>
                    <!--图片-->
                    <div class="w-full h-[110px] bg-slate-300 overflow-hidden ">
              
                      <img 
                      :src="getCoverImage(carouselItems[cardIndex - 1])" 
                      class="w-full h-full object-cover"
                      alt="cover"
                      >
                    </div>
                    <!--图片(ED)-->
                    <!--信息区-->
                    <div style="background: #1D1D1D;" class="w-full  mt-[5px]">                  
                    <div class=" text-gray-100 font-medium text-sm truncate">{{ carouselItems[cardIndex - 1]?.title || '无标题' }}</div>
              
                    <div class="mt-1  text-xs text-gray-500 truncate">{{ carouselItems[cardIndex - 1]?.summary || '暂无简介' }}</div>
                    </div>
                      <!--信息区(ED)-->
                    
                  </div>
                  </el-tooltip>
    </div>
  </div>

  <!-- 左右箭头（保持不变） -->
  <button 
    v-if="carouselItems.length > 0"
    @click="prevCarousel"
    class="absolute left-2 top-1/2 -translate-y-1/2 w-8 h-8 bg-white/70 rounded-full shadow-md flex items-center justify-center hover:bg-white transition z-20"
  ><</button>
  <button 
    v-if="carouselItems.length > 0"
    @click="nextCarousel"
    class="absolute right-2 top-1/2 -translate-y-1/2 w-8 h-8 bg-white/70 rounded-full shadow-md flex items-center justify-center hover:bg-white transition z-20"
  >></button>
</div>
            <!--流动框(ED)-->
            <!--功能框-->
            <div style="box-shadow: 12px 12px 20px rgba(0, 0, 0, 0.2),
              -8px -8px 15px rgba(255, 255, 255, 0.7);" 
              class="w-[350px] ml-[40px] h-[210px] rounded-t-[10px] rounded-b-[5px]">
              <!--天气展示-->
              <div class="h-[140px] w-full ">
                <weatherModule></weatherModule>
              </div>
              <!--天气展示（ED）-->
              <!--时间天气信息框-->
              <div style="box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.1), 0 -2px 4px -1px rgba(0, 0, 0, 0.06);"
               class="information_function h-[70px] w-full rounded-br-[5px] flex">
               <!--时钟槽--> 
                <div class="clock_groove h-[calc(100%-3px)] mt-[3px] flex items-center justify-center overflow-hidden">

                  <homeClock style="
                  box-shadow: 2px 4px 10px 0px rgba(0, 0, 0, 0.3), 0 -2px 10px 12px rgba(0, 0, 0, 0.1);"></homeClock>

                </div>
               <!--时钟槽(ED)--> 
                <!--信息框-->

                <div class=" w-[calc(100%-130px)] h-full flex bg-slate-400/40">
                  <div class=" bg-[#414141] w-[5px] h-full "></div>
                    <div class="content_box w-[calc(100%-18px)] h-[calc(100%-6px)] mt-[3px] ml-[4px] mr-[4px] overflow-hidden">
                      <div class="forecast-container">
                      <div 
                        v-for="(day, index) in threeDayForecast" 
                        :key="index"
                        class="forecast-day">
                          <div class="weekday">{{ getWeekDay(day.fxDate) }}</div>
                          <i :class="'qi-' + day.iconDay"></i>
                          <div class="temp-range">{{ day.tempMin }}° / {{ day.tempMax }}°</div>
                          <div class="weather-desc">{{ day.textDay }}</div>
                        </div>
                      </div>
                    </div>
                  <div class="w-[5px] h-full bg-[#414141]"></div>

                </div>

                <!--信息框(ED)-->

              </div>
              <!--时间天气信息框-->
            </div>
            <!--功能框-->
            
          </div>
          <!--上层区域(ED)-->
          <!--下部区域-->
          <div class="w-full h-[640px] p-[20px] flex items-center justify-center">

            <!--展示内容-->
            <div class="show_box w-[900px] h-[600px] classic_shadow mr-[40px] bg-slate-100 rounded-[5px]">
              <div class="w-[100%-20px] h-[2px] m-[10px]  border-b-2 border-dashed border-slate-700"></div>
              <!--上部区域-->
              <div class="w-full  pl-[10px] flex mb-[15px]">
                <!--封面-->
                <div style="box-shadow: inset 0 8px 6px -6px rgba(0,0,0,0.3), inset 0 -8px 6px -6px rgba(0,0,0,0.3);" class=" w-[400px] h-[180px] flex flex-wrap  justify-center">
                   <div style="aspect-ratio: 16/9;" class="h-[180px] bg-slate-400/70 overflow-hidden">
                       <img 
                         v-if="essayShowed" 
                         :src="getCoverImage(essayShowed)" 
                         alt="封面" 
                         class="w-full h-full object-cover"
                       />
                       <div v-else class="w-full h-full flex items-center justify-center text-gray-500">
                         暂无封面
                       </div>
                     </div>
                </div>
                <!--封面(ED)-->
                <!--标题和简介-->
                <div class="ml-[40px] w-[calc(100%-450px)] h-[180px] ">
                  <!--标题-->
                  <div 
                  class="h-[55px] w-[300px] pl-[10px] pr-[10px] bg-white classic_shadow mb-[10px]">
                  <div style="border-bottom: #434343 1px solid;" class="h-[5px] w-full"> </div>
                  <div style="font-family: 'squareChinese';font-size: 25px;color: #434343;" 
                   class="h-[45px] w-full flex items-end">
                  {{ essayShowed ? essayShowed.title : '无标题' }}
                  </div>
                  <div style="border-top: #434343 1px solid;" class="h-[5px] w-full"> </div>
                  </div>
                  <!--标题(ED)-->
                  <!--简介-->
                  <div class="h-[calc(100%-65px)] w-full bg-slate-400/60 p-[3px]">

                    <div style="border-color: aliceblue;" class="p-[2px] w-full h-full border-2 border-dashed">

                      <textarea readonly class="show_introduction_textarea w-full h-full"
                      >{{ essayShowed ? essayShowed.summary : '暂无简介' }}
                      </textarea>

                    </div>

                  </div>
                  <!--简介(ED)-->

                </div>
                <!--标题和简介(ED)-->

              </div>
              <!--上部区域(ED)-->
            
              <!--下部区域-->
              <div class="w-full h-[calc(100%-230px)] pl-[10px] pr-[10px]">
                <div style="border: 2px solid #99A0A9;" class="w-full h-full">
                  <textarea  
                  placeholder="文章内容" class="show_content_textarea w-full h-full"
                  >{{ essayShowed ? essayShowed.content : '暂无内容' }}</textarea>
                </div>

              </div>
              <!--下部区域(ED)-->

            </div>

            <!--展示内容d(ED)-->
            <!--最新列表-->
            <div class="w-[250px] h-[600px] p-[10px] bg-white classic_shadow newestList">
              <div 
              style=" 
              font-size: 20px;
              font-weight: 500;
              font-family:Cambria, Cochin, Georgia, Times, 'Times New Roman', serif;
              " 
              class="w-full h-[50px] border-2 mb-[4px] flex items-center  justify-center">
              <div class="w-[20px] h-[2px] bg-slate-700"></div>  
              The Newest Content
              <div class="w-[20px] h-[2px] bg-slate-700"></div>  

              </div>
              <div style="scrollbar-width: none;" class="h-[calc(100%-54px)] w-full p-[7px] border-2 overflow-y-auto scrollbar-hide">
             
                  <div v-for="(essay, index) in essays" :key="essay.id"
                  @click="openEssayContent(essay.id)"
                  class="w-full h-[40px] border-b-2 pt-[10px] pl-[7px] mb-[10px]  border-slate-700 little_shadow ">
                  #{{ index+1 }}. {{ essay.title }}
                  <div class="w-full border-b-2 border-dashed border-slate-700"></div>
                  </div>
                  
              </div>
              
            </div>
            <!--最新列表(ED)-->

          </div>
          <!--下部区域(ED)-->

        </div>
        <!--推送(ED)-->
        <!--其他内容暂时填充框-->

        <div class="w-full h-[600px] border-t-2">



        </div>

        <!--其他内容暂时填充框(ED)-->

      </div>
      <!--内容区(ED)-->

</template>
<style scoped>
/* 总容器：占据全屏/全区域 */
.video-background-container {
  position: relative; /* 关键 */
  width: 100%;
  height: 100vh;
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

@font-face {
  font-family: 'uprightChinese';
  src: url('../resources/font/upright_chinese/WDXL_Lubrifont_SC/WDXLLubrifontSC-Regular.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

/*字体规定(ED) */

.inside_shadow{
  box-shadow: 
    inset 0 6px 8px rgba(0, 0, 0, 0.1),
    inset 0 -6px 8px rgba(0, 0, 0, 0.1);
}

.classic_shadow {
  box-shadow: 2px 4px 10px 0px rgba(0, 0, 0, 0.1), 0 -2px 10px 8px rgba(0, 0, 0, 0.06);
}

.little_shadow {
  cursor: pointer;
  font-family:Georgia, 'Times New Roman', Times, serif;
  background-color: rgba(255, 255, 255, 0.377);
  box-shadow: 2px 3px 0 1px rgba(0, 0, 0, 0.2),2px -5px 5px 0px rgba(0, 0, 0, 0.1), 0 -2px 5px 0px rgba(0, 0, 0, 0.06);
}

.push_title{

  font-family:'Times New Roman', Times, serif;
  font-size: 30px;
  font-weight: 500;
}

/* 最新作品的列表 */
.newestList{

  box-shadow: inset 0 0 0 8px #464646 ,2px 4px 15px 0px rgba(0, 0, 0, 0.3), 0 5px 15px 8px rgba(0, 0, 0, 0.1);

}

/* 最新作品的列表(ED) */

/* 展示简介文本区 */
.show_introduction_textarea{
  border: none;
  outline: none;
  appearance: none;
  font-family: 'squareChinese';
  color: #3e3e3e;
  font-size: 15px;
  scrollbar-width: none;
  resize: none;
  padding: 0 10px;
}
/* 展示简介文本区 */

/* 展示内容文本区 */
.show_content_textarea{
  padding: 10px;
  box-sizing: border-box;
  font-family: 'squareChinese';
  font-size: 16px;
  font-weight: 100;
  color: #434343;
  border-radius: 5px;
  outline: none;
  resize: none;
  appearance: none;
  transition: all 0.2s ease;
  line-height: 1.6;
  letter-spacing: 0.5px;
}
.show_content_textarea::placeholder {
  color: #999;
  font-size: 16px;
  opacity: 0.7;
}
.show_content_textarea::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.show_content_textarea::-webkit-scrollbar-track {
  background: #f8fafc;
  border-radius: 3px;
}
.show_content_textarea::-webkit-scrollbar-thumb {
  background: #4b4b4b;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.show_content_textarea::-webkit-scrollbar-thumb:hover {
  background: #353DD3;
}
/* 展示内容文本区(ED) */
/* 流动框内容 */
.flow_box{
  background: radial-gradient(circle at center, #979491 0%, #3b362d 50%, #000000 100%);
}
.flow_box .card{
  background: #100b03;
  
}
.flow_box .card .little_box{
  height: 2px; 
  width: 6px;
  background: #ffffff;
  margin-left: 3px;
  margin-right: 3px;
}
/* 流动框内容(ED) */
/* 信息框功能 */
.information_function{
  background: rgba(255, 255, 255);
  .clock_groove {
  width: 130px;
  position: relative;
  border: none;
  }
  .clock_groove::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: repeating-linear-gradient(
    to right,
    rgba(60, 62, 101, 0.4) 0,
    rgba(60, 62, 101, 0.4) 6px,
    transparent 6px,
    transparent 12px
  );
  background-size: 100% 2px;
  background-repeat: no-repeat;
  background-position: top left, bottom left;
  pointer-events: none;
  }
  .content_box{
    border: 2px #27272d dashed;
    background: #ffffff;
  }
}
/* 信息框功能(ED)*/
/* 天气预报 */
/* 天气预报容器 - 使用 grid 实现三列等宽 */
.forecast-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2px;
  background: rgba(255, 255, 255, 0.6);
  height: 100%;
  width: 100%;
  padding: 2px;
  box-sizing: border-box;
}

/* 每个预报卡片 */
.forecast-day {
  align-items: center;
  justify-content: center;
  text-align: center;
  border-right: 1px dashed #ccc;
  overflow: hidden;
  white-space: nowrap;
}

/* 最后一项去掉右边框 */
.forecast-day:last-child {
  border-right: none;
}

/* 星期几 - 强制单行省略 */
.forecast-day .weekday {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.75rem;
  font-weight: 500;
  color: #4a5568; /* text-gray-600 */
}

/* 天气图标 */
.forecast-day i {
  font-size: 1.125rem;
  color: #4a5568;
  
}

/* 温度范围 */
.forecast-day .temp-range {
  font-size: 0.75rem;
  color: #4a5568;
  white-space: nowrap;
  margin-top: -5px;
}

/* 天气描述（可选） */
.forecast-day .weather-desc {
  font-size: 0.625rem; /* 10px */
  color: #718096; /* text-gray-500 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}
/* 天气预报(ED) */

</style>