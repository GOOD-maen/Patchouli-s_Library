<script setup>
import {ref,computed} from 'vue'
import defaultCoverImage from '@/resources/pictures/test.png';
import {useEssayStore } from '@/stores/essayStore';
import { useRouter, useRoute } from 'vue-router'
import { MessageSquareMore,ThumbsUp,Share} from 'lucide-vue-next';
//////////////router///////////////

const router = useRouter()
const route = useRoute()

const openEssayDetail = (essayId) => {
  router.push(`/essay/${essayId}`)
}

const goWelcome = () => {
  router.push('/')
}
//////////////router(ED///////////////

////////////essay///////////
const essayStore =useEssayStore()
const essays = computed(() =>essayStore.essays || [] )
////////////essay(ED///////////
//////////////coverImage///////////////
const getCoverImage = (essay) => {
  if (essay.coverUrl && essay.coverUrl.trim() !== '') {
    return `http://localhost:8080/uploads/${essay.coverUrl}`
  }
  return defaultCoverImage
}
//////////////coverImage(ED///////////////

</script>
<template>
    <!--总框-->
<div class="w-[100%] h-[100%]">
    <div class="bg-white/80 border-b p-4 flex items-center h-[45px]">
      <h1 class="text-xl font-semibold">ESSAY 列表</h1>
    </div>
          <!--内容区-->
<div class="h-[calc(100%-45px)]  ml-[10%] mr-[10%]  border-l border-r border-slate-300 grid gap-2 grid-cols-[15%_55%_30%]">
  <!-- 左侧待开发区 -->
  <div class="h-full border-l-[1px] border-r-[1px]">
  
  
  
  
  </div>

  <!-- 列表区 -->
  <div id="essayList" class="h-full pt-[40px] pl-[4px] pb-[40px] bg-slate-200/80 grid grid-cols-1 auto-rows-min gap-1 overflow-y-auto">
   <!--帖子卡-->
    <div 
    style="aspect-ratio: 16 / 9;"
      v-for="essay in essays"
      :key="essay.id" 
      class="bg-slate-50 m-1 rounded-[10px] border-slate-300 border-2 grid grid-rows-[50%_1fr_30px]"
    >
     <!--帖子卡上部-->
    <div class="row-span-1 p-[10px] grid grid-cols-[50%_50%]" @click="openEssayDetail(essay.id)">
      <!--封面-->
      <div style=" aspect-ratio: 16 / 9;"
        :style="{
          backgroundImage: `url(${getCoverImage(essay)})`,
          backgroundSize: 'cover',
          backgroundRepeat: 'no-repeat',
          backgroundPosition: 'center',
        }" 
        class="w-[95%] "
      ></div>
      <!--封面(ED-->
      <!--上部右侧内容区-->
      <div class="col-span-1">
        <!--title-->
        <div class="mb-2
        text-lg">{{essay.title}}</div>
        <!--createTime-->
        <div class="text-slate-500 font-light
        text-xs">创建时间:{{essay.createdAt}}</div>
        <!--changeTime-->
        <div class="text-slate-500 font-light
        text-xs">更新时间:{{essay.updatedAt}}</div>
      </div>
      <!--上部右侧内容区(ED-->

      </div>
       <!--帖子卡上部(ED-->
       <!--帖子卡中部 -->
      <div class="row-span-1 border-b-2 border-t-2 border-slate-300 pr-2 pl-2">
        <!--中部展示内容区-->
      <div class="max-h-full overflow-y-auto line-clamp-6 break-words cursor-pointer bg-slate-200 pt-1 h-[100%]
      text-sm font-normal leading-snug text-gray-800 hide-scrollbar"
        @click="openEssayDetail(essay.id)"
          >
          {{ essay.content }}
        </div>
        <!--中部展示内容区(ED-->
        </div>
       <!--帖子卡中部(ED-->
        <!--帖子卡下部 -->
      <div class="row-span-1 grid grid-cols-[1fr_30px_30px_40px]">
        <div class="col-span-1"></div>
        <button class="px-1 py-1 text-slate-400 rounded text-sm hover:text-slate-600 transition-colors">
      <ThumbsUp class="w-[20px] h-[20px]"></ThumbsUp>
    </button>
    <button class="px-1 py-1 text-slate-400 rounded text-sm hover:text-slate-600 transition-colors">
      <MessageSquareMore class="w-[20px] h-[20px]"></MessageSquareMore>
    </button>
     <button class="px-1 py-1 text-slate-400 rounded text-sm hover:text-slate-600 transition-colors mr-[20px]">
      <Share class="w-[20px] h-[20px]"></Share>
    </button>
      </div>
       <!--帖子卡下部(ED-->
    </div>
      <!--帖子卡(ED-->
  </div>
  <!--列表区ED-->

  <!-- 右侧待开发区 -->
  <div class="bg-white h-full  border-l-[1px] border-r-[1px]"></div>
</div>
<!--内容区ED-->
</div>
    <!--总框ED-->

</template>
<style scoped>
@import 'tailwindcss';

/* css列表 */
#essayList::-webkit-scrollbar {
    width: 8px;/*滚动条宽度 */
}
#essayList::-webkit-scrollbar-thumb {
  background: #858585; /* 滑块颜色 - Tailwind blue-400 */
  /* 其他常用属性 */
  border: 2px solid transparent; /* 透明边框，用于创建间隔效果 */
  background-clip: padding-box; /* 背景裁剪到内边距框 */
  min-height: 40px; /* 滑块最小高度 */
}

/* css列表ED */

.hide-scrollbar {
  scrollbar-width: none;
  -ms-overflow-style: none; 
}

.hide-scrollbar::-webkit-scrollbar {
  display: none;  
}
</style>