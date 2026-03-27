<template>
    <button
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
  class="text-white inline-block text-2xl border-3 pl-[15px] pr-[15px] transition-all duration-500">
  Patchouli's Library
</button>
</template>
<script setup>
import { ref } from 'vue';

const shouldTurnBlack = ref(true)
</script>
<style scoped>
/*字体规定 */
@font-face {
  font-family: 'connectionEnglish'; /* 给字体起一个你喜欢的名字，后面就用它来调用 */
  src: url('../resources/font/Bitcount_Single/BitcountSingle-VariableFont.ttf') format('truetype');/*像素英语 */
  font-weight: normal;
  font-style: normal;
}
</style>