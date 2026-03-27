<script setup>
import { ref, onMounted, computed,watch,nextTick} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user' 
import defaultCoverImage from '@/resources/pictures/test.png'
import testBackground from '@/resources/pictures/background/background.png'
import headImage from '@/resources/pictures/heads/mc_head.png'
import { BookOpenText , CalendarRange ,NotepadText , ThumbsUp ,MessageSquareText} from 'lucide-vue-next';


const route = useRoute()
const router = useRouter()



// 获取封面图 
const getCoverImage = (essay) => {
  if (essay && essay.coverUrl && essay.coverUrl.trim() !== '') {
    return `http://localhost:8080/uploads/${essay.coverUrl}`
  }
  return defaultCoverImage
}

// 返回上一页
const goBack = () => router.back()
const goBackToList = () => router.push('/essayList')

// 数据
const essayDetail = ref(null)
const authorInfo = ref(null)
const loading = ref(false)

// 获取文章详情 
const fetchEssayDetail = async (id) => {
  loading.value = true
  try {
    const response = await fetch(`http://localhost:8080/api/essay/${id}`)
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    const data = await response.json()
    essayDetail.value = data
    viewsCount.value = data.views || 0
    likesCount.value = data.likes || 0

    if (data.authorId) {
      await fetchAuthorInfo(data.authorId)
    }
  } catch (err) {
    console.error('加载文章失败', err)
  } finally {
    loading.value = false
  }
}

const fetchAuthorInfo = async (authorId) => {
  try {
    const response = await fetch(`http://localhost:8080/api/user/${authorId}`)
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    const data = await response.json()    
    authorInfo.value = data

  } catch (err) {
    console.error('获取作者信息失败', err)
  }
}

onMounted(() => {
  const id = route.params.id
  if (id) {
    fetchEssayDetail(id)
    increaseView(id)          // 异步增加浏览量，不阻塞页面
    fetchLikeStatus()         // 获取点赞状态（需要登录）
  }
})
//////////////////////作者信息/////////////////////////
// 通用图片 URL 处理函数
const baseUrl = 'http://localhost:8080'

const getFullImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/')) return `${baseUrl}${url}`
  const cleanUrl = url.replace(/^\/+/, '')
  return `${baseUrl}/${cleanUrl}`
}

const authBackground = computed(() => {
  return getFullImageUrl(authorInfo.value?.backgroundImage) || testBackground
})
const authAvatar = computed(() => {
  return getFullImageUrl(authorInfo.value?.avatar) || headImage
})

//////////////////////作者信息(ED)/////////////////////////
//////////////////////用户头像/////////////////////

const userAvatar = computed(() => {
  return getFullImageUrl(userStore.userInfo?.avatar) || headImage
})

//////////////////////用户头像(ED)/////////////////////

///////////toCenterButton样式效果方法///////////
  
const toCenterButtonisHovered = ref(false)

///////////toCenterButton样式效果方法(ED)///////////
////////////////////点赞和浏览状态/////////////////////

const userStore = useUserStore()

// 点赞状态
const isLiked = ref(false)           // 当前用户是否已点赞
const likesCount = ref(0)            // 点赞总数
const viewsCount = ref(0)            // 浏览量（从接口获取）

const fetchLikeStatus = async () => {
  if (!userStore.isLoggedIn) return  // 未登录不获取点赞状态
  try {
    const response = await fetch(`http://localhost:8080/api/essay/${route.params.id}/like-status`, {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    if (response.ok) {
      const data = await response.json()
      isLiked.value = data.liked
      likesCount.value = data.likes
    }
  } catch (err) {
    console.error('获取点赞状态失败', err)
  }
}

const increaseView = async (essayId) => {
  // 防刷：检查 sessionStorage 是否已记录
  const storageKey = `viewed_${essayId}`
  if (sessionStorage.getItem(storageKey)) {
    console.log('本次会话已记录过浏览量，跳过')
    return
  }
  try {
    const response = await fetch(`http://localhost:8080/api/essay/${essayId}/view`, {
      method: 'POST'
    })
    if (response.ok) {
      const data = await response.json()
      viewsCount.value = data.views
      sessionStorage.setItem(storageKey, 'true')
    }
  } catch (err) {
    console.error('增加浏览量失败', err)
  }
}

const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    // 未登录，跳转到登录页
    router.push('/login')
    return
  }
  try {
    const response = await fetch(`http://localhost:8080/api/essay/${route.params.id}/like`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    if (response.ok) {
      const data = await response.json()
      isLiked.value = data.liked
      likesCount.value = data.likes
    } else if (response.status === 401) {
      router.push('/login')
    } else {
      console.error('点赞操作失败')
    }
  } catch (err) {
    console.error('点赞请求失败', err)
  }
}
////////////////////点赞和浏览状态(ED)/////////////////////
////////////////评论逻辑/////////////////

// 评论数据
const commentCount = ref(0)
const comments = ref([])                 // 扁平评论列表
const loadingComments = ref(false)       // 加载中
const replyTo = ref(null)                // 当前回复的目标 { id, username, parentId? }
const commentContent = ref('')
const isFocused = ref(false)
const commentTextarea = ref(null)

// 获取评论列表
const fetchComments = async () => {
  if (!essayDetail.value) return
  loadingComments.value = true
  try {
    const response = await fetch(`http://localhost:8080/api/comments?targetType=essay&targetId=${essayDetail.value.id}`)
    if (!response.ok) throw new Error('加载评论失败')
    const data = await response.json()
    comments.value = data
    // 更新评论数显示
    commentCount.value = data.length
  } catch (err) {
    console.error('加载评论失败', err)
  } finally {
    loadingComments.value = false
  }
}

// 发表评论（顶级或回复）
const publishComment = async () => {
  console.log('publishComment 被调用')
  const content = commentContent.value.trim()
  if (!content) return

  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }

  const payload = {
    content: content,
    targetType: 'essay',
    targetId: essayDetail.value.id,
    parentId: replyTo.value ? replyTo.value.id : null
  }

  try {
    const response = await fetch('http://localhost:8080/api/comments', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userStore.token}`
      },
      body: JSON.stringify(payload)
    })
    if (!response.ok) throw new Error('发布失败')
    const newComment = await response.json()
    // 重新加载评论列表
    await fetchComments()
    // 清空输入框和回复目标
    commentContent.value = ''
    replyTo.value = null
    isFocused.value = false
    adjustTextareaHeight()
  } catch (err) {
    console.error('发布评论失败', err)
    alert('发布失败，请重试')
  }
}

// 删除评论
const deleteComment = async (commentId) => {
  if (!confirm('确定删除这条评论吗？')) return
  try {
    const response = await fetch(`http://localhost:8080/api/comments/${commentId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    if (!response.ok) throw new Error('删除失败')
    await fetchComments() // 重新加载
  } catch (err) {
    console.error('删除评论失败', err)
    alert('删除失败，请重试')
  }
}

// 设置回复目标
const setReplyTo = (comment) => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  replyTo.value = {
    id: comment.id,
    username: comment.username
  }
  // 聚焦输入框并添加 @用户名
  commentContent.value = ``
  isFocused.value = true
  // 聚焦后让光标移到末尾
  nextTick(() => {
    commentTextarea.value?.focus()
    commentTextarea.value?.setSelectionRange(commentContent.value.length, commentContent.value.length)
  })
}

// 取消回复
const cancelReply = () => {
  replyTo.value = null
  commentContent.value = ''
  isFocused.value = false
}

// 构建评论树（扁平 -> 树形）
const buildCommentTree = (flatComments) => {
  const map = new Map()
  const roots = []
  flatComments.forEach(comment => {
    map.set(comment.id, { ...comment, children: [], username: comment.username || '匿名' })
  })
  flatComments.forEach(comment => {
    const node = map.get(comment.id)
    if (comment.parentId === null) {
      roots.push(node)
    } else {
      const parent = map.get(comment.parentId)
      if (parent) {
        parent.children.push(node)
      } else {
        roots.push(node)
      }
    }
  })

  roots.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
  roots.forEach(root => {
    root.children.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
  })

  roots.forEach(root => {
    const descendants = []
    const collect = (node) => {
      node.children.forEach(child => {
        descendants.push(child)
        collect(child)
      })
    }
    collect(root)
    root.allDescendants = descendants
  })
  return roots
}

// 顶级评论树
const commentTree = computed(() => buildCommentTree(comments.value))

// 每个顶级评论的展开状态（用于显示全部子评论）
const expandState = ref(new Map()) // key: commentId, value: boolean

// 切换子评论展开/折叠
const toggleExpand = (commentId) => {
  const current = expandState.value.get(commentId) || false
  expandState.value.set(commentId, !current)
}

// 是否显示全部子评论（用于判断）
const isExpanded = (commentId) => {
  return expandState.value.get(commentId) || false
}

// 格式化时间
const formatCommentTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}
const adjustTextareaHeight = () => {
  nextTick(() => {
    const textarea = commentTextarea.value
    if (textarea) {
      const style = getComputedStyle(textarea)
      const minHeight = parseFloat(style.minHeight)
      const maxHeight = parseFloat(style.maxHeight)
      if (!commentContent.value.trim()) {
        textarea.style.height = minHeight + 'px'
        return
      }
      textarea.style.height = 'auto'
      const scrollHeight = textarea.scrollHeight
      if (scrollHeight > minHeight) {
        textarea.style.height = maxHeight + 'px'
      } else {
        textarea.style.height = minHeight + 'px'
      }
    }
  })
}
// 监听文章详情加载完成后获取评论
watch(essayDetail, (newVal) => {
  if (newVal) {
    fetchComments()
  }
}, { immediate: true })
// 获取父级用户名
const getParentUsername = (parentId) => {
  const parent = comments.value.find(c => c.id === parentId)
  return parent ? parent.username : ''
}
////////////////评论逻辑(ED)/////////////////

</script>

<template>
  <div class="essay-detail-container w-full  " id="essayDetailBox">
    <div class="w-full h-full">
    <div class=" top-0 z-50 w-full h-[60px] flex items-center px-6 mb-[30px]" 
         :style="{
           backdropFilter: 'blur(12px)',
           background: 'rgba(241, 245, 249, 0.8)',
           boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)'
         }">
      <!-- 返回按钮 -->
      <button @click="goBack" 
              class="backPageButton w-[80px] h-[30px] rounded-full border-2 border-blue-500 text-white flex items-center justify-center relative overflow-hidden mr-4">
        <div class="absolute transition-all duration-300"
             :class="{ 'opacity-0 translate-y-full': false }">
          ← 返回
        </div>
        <div class="absolute transition-all duration-300 opacity-0 translate-y-full">
          Back
        </div>
      </button>
      <h1 class="text-xl text-gray-800" style="font-family: 'squareChinese';">文章详情</h1>
    </div>
    <!-- 主要内容区域 -->
    <div class="pb-6 w-full h-full" style="background-color: rgba(235, 240, 240, 1); min-height: calc(100% - 60px);">
      <div class="w-full flex justify-center">
        <!-- 加载中或未找到文章的处理 -->
         <div v-if="loading" class="w-[1000px] text-center py-20 text-gray-500">
        加载中...
        </div>
        <div v-else-if="!essayDetail" class="w-[1000px] text-center py-20 text-gray-500">
          文章不存在或正在加载...
        </div>
        <div v-else class="bg-white w-[1000px] mb-[30px] classic_shadow rounded-[5px] p-6">
          <!-- 标题区域 -->
          <div class="mb-6 flex items-end">
            <div class="h-[55px] w-full max-w-[300px] pl-[10px] pr-[10px] bg-white classic_shadow">
              <div style="border-bottom: #434343 1px solid;" class="h-[5px] w-full"></div>
              <div style="font-family: 'squareChinese'; font-size: 28px; color: #434343;" 
                   class="h-[45px] w-full flex items-end">
                {{ essayDetail.title }}
              </div>
              <div style="border-top: #434343 1px solid;" class="h-[5px] w-full"></div>
            </div>
            <div style="font-size: 15px; margin: 5px 10px; opacity: 0.6;">ID: {{ essayDetail.id }}</div>
          </div>

          <!-- 封面和内嵌信息 -->
          <div class="flex flex-wrap lg:flex-nowrap gap-6 mb-8">
            <!-- 封面图 -->
            <div style="box-shadow: inset 0 8px 6px -6px rgba(0,0,0,0.3), inset 0 -8px 6px -6px rgba(0,0,0,0.3);" 
                 class="w-full lg:w-[400px] h-[220px] flex justify-center overflow-hidden rounded">
              <div style="aspect-ratio: 16/9;" class="h-full bg-slate-400/70 overflow-hidden">
                <img  :src="getCoverImage(essayDetail)" alt="封面" class="w-full h-full object-cover">
              </div>
            </div>

            <!-- 简介卡片 -->
            <div class="flex-1 bg-slate-400/60 p-[3px] rounded">
              <div style="border-color: aliceblue;" class="p-[2px] w-full h-full border-2 border-dashed rounded">
                <div class="p-3 h-full overflow-y-auto" style="font-family: 'squareChinese'; color: #3e3e3e; font-size: 15px;">
                  {{ essayDetail.summary || '暂无简介' }}
                </div>
              </div>
            </div>
          </div>

          <!-- 正文区域（模仿主页的下部区域） -->
          <div class="mt-6">
  <div style="border: 2px solid #99A0A9;" class="w-full rounded">
    <textarea 
      class="p-4 w-full resize-none" 
      style="font-family: 'squareChinese'; font-size: 16px; color: #434343; line-height: 1.8; background: transparent; border: none; outline: none; height: 750px; overflow-y: auto;"
      :value="essayDetail.content || '暂无内容'"
      readonly
    ></textarea>
  </div>
</div>

          <!-- 底部操作按钮-->
          <div class="mt-8 flex gap-4 justify-center">
            <button @click="goBackToList" 
                    class="px-6 py-2 bg-blue-600 text-white rounded-full shadow-md hover:bg-blue-700 transition-colors"
                    style="font-family: 'squareChinese';">返回列表</button>
            <button @click="goBack" 
                    class="px-6 py-2 bg-gray-600 text-white rounded-full shadow-md hover:bg-gray-700 transition-colors"
                    style="font-family: 'squareChinese';">返回上一页</button>
          </div>
        </div>
        <!--右侧功能框-->
        <div class="w-[350px] ml-[20px] mb-[30px] ">
          <!--作者卡片-->
          <div class="w-full h-[280px] bg-white author_card classic_shadow">
            <div class="w-full h-[calc(100%-40px)]">
              <!--作者背景-->
              <div :style="{ backgroundImage: `url(${authBackground})` }" 
              class="w-full h-[calc(100%-100px)] bg-center bg-cover">
              </div>
              <!--作者背景(ED)-->
              <!--信息栏-->
              <div class="w-full h-[100px] relative">
                
                <div class=" w-full h-[50px] border-b-[1.5px] border-slate-500/20 p-[2.5px] pb-[3px] flex">
                  <div class="w-[116px] h-full border-[1.5px] border-slate-500/20 pl-[7px] pr-[7px]">
                    <!--头像-->
                  <div class="w-[100px] h-[100px] mt-[-60px] classic_shadow p-[5px] bg-slate-400/90 top-[-60px] left-[10px]">
                    <div  :style="{ backgroundImage: `url(${authAvatar})` }" class="w-full h-full bg-white inset_shadow bg-center bg-cover"></div>
                  </div>
                   <!--头像(ED)-->
                  </div>
                  <div  class=" ml-[2.5px] h-full w-full pl-[8px] border-[1.5px] border-slate-500/20 flex  items-end ">
                    <div class="mt-[2px] auth_name">{{ authorInfo?.username || '佚名' }}</div>
                  </div>
                </div>
                <div class="w-full h-[50px] p-[3px] pb-[4.5px] flex">
                  <div class="w-[120px] h-full pl-[2px] border-[1.5px] border-slate-500/20  flex items-center">
                    <BookOpenText class="Icon_picture"></BookOpenText>
                    <div class="Icon_text">作品数: {{ authorInfo?.totalEssays || "-----" }}</div>
                  </div>
                  <div class="ml-[3px] w-[120px] h-full border-[1.5px] border-slate-500/20 flex items-center">
                    <CalendarRange class="Icon_picture"></CalendarRange>
                    <div class="Icon_text">站龄: {{ authorInfo?.accountAge || "-----" }} d</div>
                  </div>
                  <div class="ml-[3px] w-[calc(100%-225px)] h-full  flex justify-center items-center">
                    <button 
                    @mouseenter="toCenterButtonisHovered = true"
                    @mouseleave="toCenterButtonisHovered = false"style="
      display: flex;
      justify-content: center;
      align-items: center;
      " class="h-[30px] w-[100px] ">
        <div style="
        display: flex;
        position: relative;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        overflow: hidden;
        "
        class="toCenterButton w-full h-[30px] rounded-[5px] border-2 border-blue-300/80 text-amber-100">
        
        <div 
         :style="{
          transform: toCenterButtonisHovered ? 'translateY(100%)' : 'translateY(0)',
          opacity: toCenterButtonisHovered ? 0 : 1
          }
          "
          style="
          display: flex;
          font-family: 'squareChinese';
          font-weight: 100;
          cursor: pointer;
          transition: transform 0.3s ease, opacity 0.3s ease;
          position: absolute;
        ">进入主页</div>

        <!-- "Welcome"文字 -->
        <div 
        :style="{
        transform: toCenterButtonisHovered ? 'translateY(0)' : 'translateY(-100%)',
        opacity: toCenterButtonisHovered ? 1 : 0
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
        Welcome~
        </div>

      </div>
      </button>
                  </div>
                </div>
              </div>
              <!--信息栏(ED)-->
            </div>
            <div  class="informationColumn w-full h-[40px] top_shadow flex justify-center items-center">
              <div class="column flex items-center">
                <NotepadText class="Icon"></NotepadText>
                <div class="text">浏览量: {{ viewsCount }}</div>
              </div>
              <div class="column flex items-center">
                <ThumbsUp  @click="toggleLike"
                class="Icon cursor-pointer" 
                :class="{ 'text-blue-500': isLiked }"></ThumbsUp>
                <div class="text">点赞数: {{ likesCount }}</div>
              </div>
              <div class="column flex items-center">
                <MessageSquareText class="Icon"></MessageSquareText>
                <div class="text">评论数: {{ commentCount }}</div>
              </div>
            </div>
          </div>
          <!--作者卡片(ED)-->
          <!--评论区-->
<div class="w-full h-[910px] mt-[20px] bg-white classic_shadow overflow-hidden">
  <div style="font-family:'squareChinese';" class="w-full h-[50px] bg-white flex items-center pl-[20px]">
    <div style="transform: scaleX(1.2);font-size: 20px;">评论</div>
  </div>
  <div class="w-full h-[860px] flex flex-col">
    <!--评论输入框-->
    <div style="padding: 20px 20px 10px 10px;" class="w-full inset_shadow">
      <div class="w-full flex">
        <!--头像-->
        <div :style="{ backgroundImage: `url(${userAvatar})` }"
          class="w-[45px] h-[45px] rounded-full bg-blue-300 bg-cover bg-center">
        </div>
        <div class="flex-1 ml-2">
          <textarea
            v-model="commentContent"
            ref="commentTextarea"
            class="commentInput"
            placeholder="写下你的评论..."
            @focus="isFocused = true"
            @blur="isFocused = false"
            @input="adjustTextareaHeight"
            @keydown.ctrl.enter="publishComment"
          ></textarea>
          <div v-if="replyTo" class="text-xs text-blue-500 mt-1">
            回复 @{{ replyTo.username }}
            <button @click="cancelReply" class="ml-2 text-gray-400 hover:text-gray-600">取消</button>
          </div>
        </div>
      </div>
      <transition  name="fade-slide">
        <div v-if="commentContent.trim()" class="flex justify-end mt-2">
          <button
            class="publish-btn"
            :disabled="!commentContent.trim()"
            @click="publishComment"
          >发布</button>
        </div>
      </transition>
    </div>
    <!--评论输入框(ED)-->

    <div class="w-full h-[1.5px] bg-slate-400/40 mt-[8px] mb-[8px]"></div>

    <!--评论展示区-->
    <div class="w-full flex-1 overflow-y-auto commentArea">
      <div v-if="loadingComments" style="font-size: 15px; color: #99A0A9;" class="text-center py-10 ">加载评论中...</div>
      <div v-else-if="commentTree.length === 0" style="font-size: 15px; color: #99A0A9;" class="text-center py-10">暂无评论，来抢沙发吧~</div>
      <div v-else class="w-full comment">
        <!-- 遍历顶级评论 -->
        <div v-for="topComment in commentTree" :key="topComment.id" class="mb-6">
          <!-- 顶级评论 -->
          <div class="w-full flex">
            <div class="w-[60px] flex justify-center">
              <div :style="{ backgroundImage: `url(${getFullImageUrl(topComment.userAvatar) || headImage})` }"
                class="w-[40px] h-[40px] rounded-full bg-blue-300 bg-cover bg-center">
              </div>
            </div>
            <div class="w-[calc(100%-60px)] pr-[10px]">
              <div class="name">{{ topComment.username }}</div>
              <div class="content">{{ topComment.content }}</div>
              <div class="flex justify-between pr-[10px] mt-1">
                <div class="time">{{ formatCommentTime(topComment.createdAt) }}</div>
                <div class="flex gap-3">
                  <button v-if="userStore.isLoggedIn && (userStore.userInfo?.id === topComment.userId || userStore.userInfo?.id === essayDetail.authorId)"
                    @click="deleteComment(topComment.id)" class="text-xs text-gray-400 hover:text-red-500">删除</button>
                  <button @click="setReplyTo(topComment)" class="reply_button">回复</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 子评论区域 -->
          <div class="ml-[60px] mt-2" v-if="topComment.children.length">
            <!-- 默认显示第一条子评论 -->
            <div v-if="!isExpanded(topComment.id)">
  <div v-if="topComment.allDescendants.length > 0">
    <div v-for="(child, idx) in topComment.allDescendants.slice(0,1)" :key="child.id">
      <!-- 子评论渲染 -->
      <div class="flex w-full mt-[10px] son_comment">
        <div class="w-[45px] flex justify-center">
          <div :style="{ backgroundImage: `url(${getFullImageUrl(child.userAvatar) || headImage})` }"
            class="w-[35px] h-[35px] rounded-full bg-blue-300 bg-cover bg-center">
          </div>
        </div>
        <div class="w-[calc(100%-45px)]">
          <div class="name">{{ child.username }}</div>
          <div class="content">
            <!-- 判断是否需要 @ 父级：如果父级不是顶级评论 -->
            <span v-if="child.parentId !== topComment.id" class="text-blue-500">@{{ getParentUsername(child.parentId) }} </span>
            {{ child.content }}
          </div>
          <div class="flex justify-between pr-[10px] mt-1">
            <div class="time">{{ formatCommentTime(child.createdAt) }}</div>
            <div class="flex gap-3">
              <button v-if="userStore.isLoggedIn && (userStore.userInfo?.id === child.userId || userStore.userInfo?.id === essayDetail.authorId)"
                @click="deleteComment(child.id)" class="text-xs text-gray-400 hover:text-red-500">删除</button>
              <button @click="setReplyTo(child)" class="reply_button">回复</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="topComment.allDescendants.length > 1" class="w-full flex show_all_comments mt-2">
      <div class="w-[45px]"></div>
      <div class="text">共{{ topComment.allDescendants.length }}条回复，</div>
      <div class="button" @click="toggleExpand(topComment.id)">全部展开</div>
    </div>
  </div>
</div>

            <div v-else>
  <div v-for="child in topComment.allDescendants" :key="child.id" class="mt-[10px]">
    <div class="flex w-full">
      <div class="w-[45px] flex justify-center">
        <div :style="{ backgroundImage: `url(${getFullImageUrl(child.userAvatar) || headImage})` }"
          class="w-[35px] h-[35px] rounded-full bg-blue-300 bg-cover bg-center">
        </div>
      </div>
      <div class="w-[calc(100%-45px)]">
        <div class="name">{{ child.username }}</div>
        <div class="content">
          <span v-if="child.parentId !== topComment.id" class="text-blue-500">@{{ getParentUsername(child.parentId) }} </span>
          {{ child.content }}
        </div>
        <div class="flex justify-between pr-[10px] mt-1">
          <div class="time">{{ formatCommentTime(child.createdAt) }}</div>
          <div class="flex gap-3">
            <button v-if="userStore.isLoggedIn && (userStore.userInfo?.id === child.userId || userStore.userInfo?.id === essayDetail.authorId)"
              @click="deleteComment(child.id)" class="text-xs text-gray-400 hover:text-red-500">删除</button>
            <button @click="setReplyTo(child)" class="reply_button">回复</button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="w-full flex show_all_comments mt-2">
    <div class="w-[45px]"></div>
    <div class="button" @click="toggleExpand(topComment.id)">收起</div>
  </div>
</div>
          </div>
          <div class="w-full h-[10px] flex items-center justify-center pl-[30px] pr-[30px] mt-2" >
            <div class="w-full line"></div>
          </div>
        </div>
        <!-- 终止提示 -->
        <div style="font-size: 15px; color: #99A0A9;" class="w-full h-[80px] flex justify-center items-center">
          再翻也没有啦~
        </div>
      </div>
    </div>
    <!--评论展示区(ED)-->
  </div>
</div>
<!--评论区(ED)-->

        </div>
        <!--右侧功能框(ED)-->

      </div>
    </div>
    </div>
  </div>
</template>

<style scoped>
.inset_shadow {
  box-shadow: inset 2px 4px 5px 5px rgba(0, 0, 0, 0.05), inset 0 -2px 5px 8px rgba(0, 0, 0, 0.03);
}
.bottom_shadow {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

/* 引入主页的字体和公共样式 */
@font-face {
  font-family: 'squareChinese';
  src: url('../resources/font/WDXL_Lubrifont_SC/WDXLLubrifontSC-Regular.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

@font-face {
  font-family: 'connectionEnglish';
  src: url('../resources/font/Bitcount_Single/BitcountSingle-VariableFont.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

.essay-detail-container {
  background-color: rgba(235, 240, 240, 1);
}

/* 复用主页的阴影类 */
.classic_shadow {
  box-shadow: 2px 4px 10px 0px rgba(0, 0, 0, 0.1), 0 -2px 10px 8px rgba(0, 0, 0, 0.06);
}

/* 管理页面按钮样式 */
.backPageButton {
  background-color: #353DD3;
  transition: background-color 0.2s ease, border 0.2s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.backPageButton:hover {
  background-color: #292fa7;
}
.backPageButton div {
  transition: transform 0.3s ease, opacity 0.3s ease;
}


/* 正文滚动条样式 */
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

/* 简介区域滚动条隐藏 */
.overflow-y-auto {
  scrollbar-width: none;
}
.overflow-y-auto::-webkit-scrollbar {
  display: none;
}


.author_card{
  .top_shadow {
  box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.1), 0 -2px 4px -1px rgba(0, 0, 0, 0.06);
}
  .bottom_shadow {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}
  .classic_shadow {
  box-shadow: 2px 4px 10px 0px rgba(0, 0, 0, 0.2), 0 -2px 10px 8px rgba(0, 0, 0, 0.08);
}
  .auth_name{
    font-family: 'squareChinese';
    font-size: 22px;
    
  }
  .Icon_picture{
    width: 25px;
    font-weight: 100;
    stroke-width: 1.5;
    color: #363636;
  }
  .Icon_text{
    margin-top: 5px;
    font-size: 15px;
    margin-left: 5px;
    font-family: 'squareChinese';
    color: #6e6e6e;
    transform: scaleX(1.1);
  }
  .toCenterButton{
    background-color:rgba(28, 28, 249, 0.8);
  transition: background-color 0.2s ease, border 0.2s ease;
  }
  .toCenterButton:hover{
    background-color: rgba(12, 1, 171, 0.8);
    
  }
}

.informationColumn{
  .column{
      width: 27%;
      height: 100%;
    .Icon{
      stroke-width: 1.5;
      width: 23px;
      color: #202020;
    }
    .text{
      font-family: 'squareChinese';
      font-size: 15px;
      margin-top: 2px;
      font-weight: 50;
      color: #3d3d3d;
    }
    .text-blue-500 {
      color: #3b82f6;
    }
  }
}
.commentArea{
  overflow-y: auto;
}
.commentArea::-webkit-scrollbar{
  width: 0px;
}

.commentInput {
  background: #ececec;
  margin-left: 10px;
  border-radius: 10px;
  padding: 8px 12px;
  width: calc(100% - 20px);
  font-size: 14px;
  line-height: 1.5;
  border: 1px solid transparent;
  resize: none; 
  min-height: 2.5rem;  
  max-height: 4.5rem; 
  overflow-y: auto;
  outline: none;
  font-family: inherit;
  margin-top: 5px;
  transition: all 0.2s ease;
}
.commentInput:focus {
  border-color: #3b82f6;
  background-color: #fff;
}
.commentInput::-webkit-scrollbar {
  width: 0px;
}
.commentInput::-webkit-scrollbar-track {
  border-radius: 0px;
}
.commentInput::-webkit-scrollbar-thumb {
  border-radius: 0px;
}

.publish-btn {
  background: #3b82f6;
  color: white;
  padding: 4px 16px;
  border-radius: 5px;
  font-size: 14px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.publish-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
}
.publish-btn:active {
  transform: translateY(0);
}
.publish-btn:disabled {
  background: #9ca3af;
  transform: none;
}
.fade-slide-enter-active,
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}


.comment{
  .line{
    height: 1.5px;
    background: rgba(27, 102, 252, 0.5);
  }
  .name{
    margin-top: 5px;
    font-family: 'squareChinese';
  }
  .content{
    margin-top: 4px;
    font-family: 'Gill Sans', 'Gill Sans MT', Calibri, 'Trebuchet MS', sans-serif;
    color: #363636;
  }
  .time{
    margin-top: 10px;
    font-size: 12px;
    color: #767676;
  }
  .reply_button{
    font-size: 14px;
    cursor: pointer;
  }
  .reply_button:hover{
    color: #2563eb;
  }

  .show_all_comments{
    .text{
      margin-top: 5px;
      font-size: 14px;
      color: #848484;
    }
    .button{
      font-size: 14px;
      margin-top: 5px;
      color: #848484;
    }
    .button:hover{
      color: #2563eb;
      cursor: pointer;
    }
  }

  .son_comment{
    .name{
      margin-top: 3px;
      font-family: 'squareChinese';
      font-size: 14px;
    }
    .content{
      font-family: 'Gill Sans', 'Gill Sans MT', Calibri, 'Trebuchet MS', sans-serif;
      color: #363636;
    }
  }
}
</style>