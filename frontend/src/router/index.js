import { createRouter, createWebHistory } from 'vue-router'
import essayShowPage from '../pages/essayPages/essayShowPage.vue'
import essayDetailPage from '@/pages/essayPages/essayDetailPage.vue'
import main from '@/pages/main.vue'
import homePage from '@/pages/homePage.vue'
import managePage from '@/pages/managePage.vue'
import cssShowPage from '@/pages/cssPages/cssShowPage.vue'
import cssManage from '@/pages/cssPages/manageSon/cssManage.vue'
import essayManage from '@/pages/essayPages/manageSon/essayManage.vue'
import videoManage from '@/pages/videoPages/videoManage.vue'
import { comment } from 'postcss'
import Register from '@/pages/LoginAndRegister/Register.vue'
import ForgetPassword from '@/pages/LoginAndRegister/ForgetPassword.vue'
import test from '@/pages/test.vue'
import Login from '@/pages/LoginAndRegister/Login.vue'
import userCenter from '@/pages/usersPages/userCenter.vue'
import homeShowComponent from '@/pages/homePages/homeShowComponent.vue'
import user_center_function_moudles from '@/pages/usersPages/modules/function_moudles.vue'

const routes = [
 {
  path:'/test',
  name:'test',
  component: test
 },
  {
    path: '/main',
    name: 'main',
    component: main,
    children: [
       {
      path: 'essayList',
      name: 'essay-list', 
      component: essayShowPage,
      beforeEnter: async (to, from, next) => {
        console.log('🛡️ 进入 css-list 路由，准备预加载数据')
        try {
          const { useEssayStore } = await import('@/stores/essayStore')
          const essayStore =useEssayStore();
          await essayStore.ensureDataLoaded()
          console.log('✅ 路由守卫数据预加载完成')
          next()
        } catch (error) {
          console.error('❌ 路由守卫数据加载失败:', error)
          next()
        }
      }
    },
    {
      path: 'manage',
      name: 'manage',
      component: managePage,
      children: [
          {
          path:'essayManage',
          name:'essayManage',
          component:essayManage,
           beforeEnter: async (to, from, next) => {
            console.log('🛡️ 进入 css-list 路由，准备预加载数据')
            try {
              const { useEssayStore } = await import('@/stores/essayStore')
              const essayStore =useEssayStore();
              await essayStore.ensureDataLoaded()
              console.log('✅ 路由守卫数据预加载完成')
              next()
            } catch (error) {
              console.error('❌ 路由守卫数据加载失败:', error)
              next()
      }
    }
        }, {
          path:'cssManage',
          name:'cssManage',
          component:cssManage,
        }, {
          path:'videoManage',
          name:'videoManage',
          component:videoManage,
        },
    ]
  },{
    path: 'cssList',
    name: 'css-list',
    component: cssShowPage,
  }
     ]
  },{
    path: '/',
    name: 'homePage',
    component: homePage,
        children: [
       {
      path: '',
      name: 'homeShowComponent', 
      component: homeShowComponent,
    },{
      path: 'essayList',
      name: 'essay-list', 
      component: essayShowPage,
      beforeEnter: async (to, from, next) => {
        console.log('🛡️ 进入 css-list 路由，准备预加载数据')
        try {
          const { useEssayStore } = await import('@/stores/essayStore')
          const essayStore =useEssayStore();
          await essayStore.ensureDataLoaded()
          console.log('✅ 路由守卫数据预加载完成')
          next()
        } catch (error) {
          console.error('❌ 路由守卫数据加载失败:', error)
          next()
        }
      }
    },
    {
      path: 'manage',
      name: 'manage',
      component: managePage,
      children: [
          {
          path:'essayManage',
          name:'essayManage',
          component:essayManage,
           beforeEnter: async (to, from, next) => {
            console.log('🛡️ 进入 css-list 路由，准备预加载数据')
            try {
              const { useEssayStore } = await import('@/stores/essayStore')
              const essayStore =useEssayStore();
              await essayStore.ensureDataLoaded()
              console.log('✅ 路由守卫数据预加载完成')
              next()
            } catch (error) {
              console.error('❌ 路由守卫数据加载失败:', error)
              next()
            }
        }
        },
    {
          path:'cssManage',
          name:'cssManage',
          component:cssManage,
        }, {
          path:'videoManage',
          name:'videoManage',
          component:videoManage,
        },
    ]
  },
    {
      path: 'essay/:id',
      name: 'essay-detail',
      component: essayDetailPage,
      props: true
    },{
      path: 'userCenter',
      name: 'user-center',
      component: userCenter,
      children:[
        {
          path:'',
          name:'userCenterFunctionMoudle',
          component: user_center_function_moudles
        }
      ]
    }

     ]
  },{
    path: '/login',
    name: 'login',
    component: Login,
  },{
    path: '/register',
    name: 'register',
    component: Register,
  },{
    path: '/forgetpassword',
    name: 'forgetPassword',
    component: ForgetPassword,
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  // 开发环境：直接放行所有页面，方便开发
  if (import.meta.env.DEV) {
    console.log('开发模式：放行所有页面')
    next()
    return
  }

  // 生产环境的守卫逻辑
  const { useUserStore } = await import('@/stores/user')
  const userStore = useUserStore()

  const publicPages = ['/', '/login', '/register', '/forgetpassword']
  const authRequired = !publicPages.includes(to.path)

  if (authRequired && !userStore.isLoggedIn) {
    next(`/login?redirect=${to.fullPath}`)
  } else if (publicPages.includes(to.path) && userStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})


export default router