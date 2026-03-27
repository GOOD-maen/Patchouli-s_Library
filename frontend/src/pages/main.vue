<script setup>
    import {ref} from 'vue'
    import headImage from '../resources/pictures/heads/mc_head.png'; // 导入头像图片
    import {useEssayStore} from '../stores/essayStore'
    import background from '@/resources/pictures/background/background.png';
    import { useRouter } from 'vue-router';

    //菜单的显示类
    const isMenuVisible = ref(false)
    //菜单显示/隐藏函数
    const changeMenuVisible =()=>{
        isMenuVisible.value=!isMenuVisible.value
    }


    ////////////essay///////////
    const essayStore =useEssayStore()
    ////////////essay(ED///////////
    //////////////router////////////////
    const router =useRouter()
    const goToEssayManagePage =async()=>{
        await router.push('/main/manage/essayManage')
    }
    const goToHomePage =async()=>{
        await router.push('/')
    }

    //////////////router(ED////////////////

</script>

<template>
    <!--总框-->
    <div class="h-full w-full ">
        <!--菜单展开时的蒙板-->
        <button @click="changeMenuVisible" 
        class="h-[100%] w-[100%] absolute bg-slate-500/20 z-10"
        :class="{'opacity-100 visible translate-x-0 ':isMenuVisible,
        'opacity-0 invisible translate-x-[-100%]':!isMenuVisible}"></button>
        <!--菜单展开时的蒙板ED-->

        <!--菜单-->
        <div class="duration-300 ease-in-out absolute w-[12%] h-[100%] bg-slate-50 border-r-2  border-slate-500 z-10"
        :class="{'opacity-100 visible translate-x-0 ':isMenuVisible,
        'opacity-0 invisible translate-x-[-100%]':!isMenuVisible}">
        <!--菜单上部-->
        <div class="bg-white w-full aspect-square pt-[10px] border-b-slate-400 border-b-2">
            <!--顶部信息区域-->
            <div class=" bg-slate-300 w-[calc(100%-20px)] aspect-square ml-[10px] rounded-2xl border-slate-400 border-2">


                <!--顶部信息区域（上-->
            <div style="background-image: repeating-linear-gradient(to right, #BBBBBB 0%, #BBBBBB 10%, transparent 10%, transparent 20%);
                background-size: 40px 2px;
                background-position: bottom;
                background-repeat: repeat-x;
                " class="w-full h-[66%] flex  justify-center ">
            
                <!--菜单上部头像-->
                <button  :style="{
                    backgroundImage: `url(${headImage})`,
                    backgroundSize: 'cover',
                    backgroundRepeat: 'no-repeat',
                    backgroundPosition: 'center'
                    }" 
                    class="aspect-square h-[calc(100%-20px)] mt-[10px] rounded-[7px] "></button>
            <!--菜单上部头像ED-->

        </div>
            <!--顶部信息区域（上ED-->
            </div>
            <!--顶部信息区域ED-->
        </div>
        <!--菜单上部ED-->
        <!--菜单中部(跳转页面按钮)-->
        <div class=" flex-1 w-[100%] h-[50%] border-b-2 border-t-8 border-slate-400">
             <div class="w-[100%] h-[50px]  flex items-center justify-center">
                <button @click="goToHomePage" class="w-[90%] h-[80%] border-slate-500 border-2 bg-blue-200 shadow-lg">返回HOME</button>
            </div>
            <div class="w-[100%] h-[50px] flex items-center justify-center">
                <button @click="goToEssayManagePage" class="w-[90%] h-[80%] border-slate-500 border-2 bg-blue-200 shadow-lg">管理页面</button>
            </div>
        </div>
        <!--菜单中部ED-->

        </div>
        <!--菜单ED-->
        
        <!--总框上部-->
        <div class="bg-slate-400 w-full h-[8%] min-h-[60px] flex items-center border-b-2 border-slate-400">
            <!--头像-->
          <button 
    :style="{
       backgroundImage: `url(${headImage})`,
      backgroundSize: 'cover',
      backgroundRepeat: 'no-repeat',
      backgroundPosition: 'center'
    }" 
    @click="changeMenuVisible" 
    class="aspect-square h-[70%] rounded-[8px] ml-2 bg-amber-300">
  </button>
        <!--头像ED-->
        </div>
        <!--总框上部ED-->
        <!--总框下部-->
        <div :style="{
            backgroundImage: `url(${background})`,
            backgroundSize: 'cover',
            backgroundRepeat: 'no-repeat',
            backgroundPosition: 'center'
            }" class=" w-full h-[92%] max-h-[calc(100%-60px)] ">
           <router-view></router-view>
        </div>
        <!--总框下部ED-->

    </div>
    <!--总框ED-->
</template>

<style scoped>
@import 'tailwindcss';


</style>
