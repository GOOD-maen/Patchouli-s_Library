<script setup>
import { onMounted, ref } from 'vue'
import { useEssayStore } from '@/stores/essayStore'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user' 

import defaultCoverImage from '@/resources/pictures/test.png'
import EssayCreateModule from '@/pages/essayPages/manageSon/essayCreateModule.vue'
import essayEditModule from './essayEditModule.vue'
import { PencilLine, Trash } from 'lucide-vue-next';
import EssayEditModule from './essayEditModule.vue'

const userStore = useUserStore()
//////////////router///////////////
const router = useRouter()

const openEssayDetail = (essayId) => {
  router.push(`/essay/${essayId}`)
}
//////////////router(ED///////////////
////////////essay///////////
const essayStore = useEssayStore()
////////////essay(ED///////////

//////////////coverImage///////////////
const getCoverImage = (essay) => {
  if (essay.coverUrl && essay.coverUrl.trim() !== '') {
    return `http://localhost:8080/uploads/${essay.coverUrl}`
  }
  return defaultCoverImage
}
//////////////coverImage(ED///////////////
/////////////////创建方法////////////////
const showCreateModule = ref(false)

const openCreateModule = () => {
  showCreateModule.value = true
}

const closeCreateModule = () => {
  showCreateModule.value = false
}

const handleEssayCreated = (newEssay) => {
  console.log('新帖子创建成功:', newEssay)
  // 刷新帖子列表
  essayStore.fetchEssayList()
  closeCreateModule()
}


/////////////////创建方法(ED////////////////
///////////////删除方法/////////////////

const showDeleteModule = ref(false)
const essayToDelete = ref(null)

const openDeleteModule = (essayId) =>{
  essayToDelete.value = essayId
  showDeleteModule.value =true
}
const closeDeleteModule = () =>{

  showDeleteModule.value =false
  essayToDelete.value = null
}
const deleteEssay = async () => {
  if (!essayToDelete.value) return

  try {
    const response = await fetch(`http://localhost:8080/api/essay/${essayToDelete.value}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${userStore.token}` 
      }
    })

    console.log('响应状态:', response.status)
    const contentType = response.headers.get('content-type')
    let result = null

    if (contentType && contentType.includes('application/json')) {
      try {
        result = await response.json()
      } catch (jsonError) {
        console.error('JSON解析失败:', jsonError)
        alert('服务器响应格式错误')
        closeDeleteModule()
        return
      }
    }

    if (response.ok) {
      console.log('删除成功')
      // 刷新列表（建议使用 fetchUserEssays 获取当前用户文章）
      if (userStore.userInfo?.id) {
        essayStore.fetchUserEssays(userStore.userInfo.id)
      } else {
        essayStore.fetchEssayList() // 降级
      }
    } else {
      console.error('删除失败:', result)
      alert(result?.message || `删除失败: HTTP ${response.status}`)
    }
  } catch (error) {
    console.error('删除请求失败:', error)
    alert('网络错误，请重试')
  } finally {
    closeDeleteModule()
  }
}
///////////////删除方法(ED/////////////////
///////////////编辑方法///////////////

const showEditModule = ref(false)
const currentEditid = ref(null)

const openEditModule = (essayId) =>{
  currentEditid.value = essayId
  showEditModule.value =true  
}
const closeEditModule = () => {
  showEditModule.value = false
  currentEditid.value = null  
}

///////////////编辑方法(ED///////////////
</script>
<template>
<div class="w-full h-full">
<div class="w-full h-full grid grid-cols-[70%_30%] gap-2">
    <!--展示列表-->
    <div id="essayList" class="bg-white/50 pt-[40px] pl-[4px] pb-[40px] grid grid-cols-1 auto-rows-min gap-1 overflow-y-auto z-0">
      <!--简要信息卡-->
      <div 
    style="aspect-ratio: 16 / 9;"
      v-for="essay in essayStore.essays"
      :key="essay.id" 
      class="bg-slate-50 m-1 rounded-[10px] border-slate-300 border-2 grid grid-rows-[50%_1fr_30px]"
    >
     <!--信息卡上部-->
   <div class="row-span-1 p-[10px] grid grid-cols-[50%_50%]" @click="openEssayDetail(essay.id)">
      <!--封面-->
      <div style=" aspect-ratio: 16 / 9;"
        :style="{
          backgroundImage: `url(${getCoverImage(essay)})`,
          backgroundSize: 'cover',
          backgroundRepeat: 'no-repeat',
          backgroundPosition: 'center',
        }" 
        class="w-[95%]"
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
       <!--信息卡上部(ED-->
       <!--信息卡中部 -->
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
       <!--信息卡中部(ED-->
        <!--信息卡下部 -->
      <div class="row-span-1 grid grid-cols-[1fr_30px_40px]">
        <div class="col-span-1"></div>
       <button 
      
      class="px-[10px] py-1 text-slate-400 rounded text-sm hover:text-slate-600 transition-colors ml-auto"
    >
      <PencilLine @click="openEditModule(essay.id)" class="w-[20px]"></PencilLine>
    </button>
    <button 
      class="px-1 py-1 text-slate-400 rounded text-sm hover:text-slate-600 transition-colors mr-[20px]"
      >
      <Trash @click="openDeleteModule(essay.id)" class="w-[20px]"></Trash>
    </button>
      </div>
       <!--信息卡下部(ED-->
    </div>
    <!--简要信息卡(ED-->
    </div>
    <!--展示列表(ED)-->
    <!--操作区-->
    <div class="bg-white/50 ">
       <button  @click="openCreateModule" style="aspect-ratio: 5/1;" class="w-[70%] ml-[15%] mt-[40px] rounded-[5px] bg-blue-600">创建新帖子</button>
        
    </div>
    <!--操作区(ED)-->
    

    <!-- 创建帖子弹窗 -->
    <EssayCreateModule
      :isVisible="showCreateModule"
      @close="closeCreateModule"
      @essay-created="handleEssayCreated"
    />
    <!-- 创建帖子弹窗(ED) -->
    <!--编辑帖子弹窗-->
    <EssayEditModule
    :isVisible = "showEditModule"
    :edit-id ="currentEditid"
    @close="closeEditModule"
    />

     <!--编辑帖子弹窗(ED)-->

    <!--删除帖子询问弹窗-->
    <Transition name="fade">
  <div 
    v-if="showDeleteModule"
    class="fixed inset-0 bg-slate-400/20 bg-opacity-50 flex items-center justify-center z-50"
    @click.self="closeDeleteModule"
  >
    <div class="bg-white rounded-xl shadow-xl p-6 w-[90%] max-w-md">
      <!-- 弹窗头部 -->
      <div class="flex items-center mb-4">
        <div class="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center mr-3">
          <Trash class="w-5 h-5 text-red-600" />
        </div>
        <h3 class="text-base font-semibold text-gray-900">删除确认</h3>
      </div>
      
      <!-- 弹窗内容 -->
      <div class="mb-6">
        <p class="text-gray-700">
          确定要删除这篇帖子吗？
        </p>
        <p class="text-sm text-gray-500 mt-2">
          删除后无法恢复，请谨慎操作。
        </p>
      </div>
      
      <!-- 弹窗底部按钮 -->
      <div class="flex justify-end space-x-3">
        <button
          @click="closeDeleteModule"
          class="px-4 py-2 text-sm text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
        >
          取消
        </button>
        <button
          @click="deleteEssay"
          class="px-4 py-2 text-sm text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors flex items-center"
        >
          <Trash class="w-4 h-4 mr-2" />
          确认删除
        </button>
      </div>
    </div>
  </div>
</Transition>
    <!--删除帖子询问弹窗(ED)-->

</div>
 
</div>
</template>
<style scoped>
#essayCard{
  aspect-ratio: 16 / 9;
}
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.hide-scrollbar {
  scrollbar-width: none;
  -ms-overflow-style: none; 
}

.hide-scrollbar::-webkit-scrollbar {
  display: none;  
}
</style>