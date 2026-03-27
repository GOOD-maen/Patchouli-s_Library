<script setup>
import {ref} from 'vue'
import { useRouter, useRoute } from 'vue-router'

//////////////router///////////////

const router = useRouter()
const route = useRoute()

const goWelcome = () => {
  router.push('/')
}
//////////////router(ED///////////////

////////////css///////////
const csses =[
  {id:'01'},
  {id:'02'},
  {id:'03'},
  {id:'04'},
  {id:'05'},
  {id:'06'},
]
////////////css(ED///////////

</script>
<template>
    <!--总框-->
<div class="w-[100%] h-[100%]">
    <div class="bg-white border-b p-4 flex items-center h-[45px]">
      <h1 class="text-xl font-semibold">CSS 列表</h1>
      <button class="border w-[150px] h-[30px] ml-auto bg-slate-400 rounded-[5px]" @click="goWelcome">back to welcome</button>
    </div>
            <!--内容区-->
            <div class="w-auto  h-[calc(100%-45px)]  bg-slate-200/50 ml-[10%] mr-[10%] border-l border-r border-slate-300">
                
              <!--列表区-->
                <div id="cssList" class="bg-slate-200/50 h-full w-[90%] ml-[5%] max-pt-[5%] pt-[40px] pl-[4px] pb-[40px] grid grid-cols-3 auto-rows-min gap-1 overflow-y-auto">
                    <div id="cssCard" v-for="css in csses"
                    :key="css.id" 
                    class="bg-slate-50 m-1 w-[calc(100%-10px)] rounded-[10px] border-slate-300 border-2 ">
                    <div  :style="{
                        backgroundSize: 'cover',
                        backgroundRepeat: 'no-repeat',
                        backgroundPosition: 'center'
                    }"  class="w-[50%] h-[50%] m-[20px]">
                     {{ css.id }}
                </div>
                </div>
                </div>
                <!--列表区ED-->

            </div>
            <!--内容区ED-->
</div>
    <!--总框ED-->

</template>
<style scoped>
@import 'tailwindcss';

/* css列表 */
#cssList::-webkit-scrollbar {
    width: 8px;/*滚动条宽度 */
}
#cssList::-webkit-scrollbar-thumb {
  background: #858585; /* 滑块颜色 - Tailwind blue-400 */
  /* 其他常用属性 */
  border: 2px solid transparent; /* 透明边框，用于创建间隔效果 */
  background-clip: padding-box; /* 背景裁剪到内边距框 */
  min-height: 40px; /* 滑块最小高度 */
}
#cssCard{
    aspect-ratio: 16 / 9;
}

/* css列表ED */


</style>