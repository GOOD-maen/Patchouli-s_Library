<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Moon, Sun } from 'lucide-vue-next'

const now = ref(new Date())
const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const timeString = ref('')
const ampm = ref('')
const dateString = ref('')

// 判断白天/黑夜
const isDay = computed(() => {
  const hour = now.value.getHours()
  return hour >= 6 && hour < 18
})

const updateTime = () => {
  const date = new Date()
  now.value = date
  
  let hours = date.getHours()
  const minutes = date.getMinutes().toString().padStart(2, '0')
  ampm.value = hours >= 12 ? 'PM' : 'AM'
  hours = hours % 12
  hours = hours ? hours : 12
  const hoursStr = hours.toString().padStart(2, '0')
  timeString.value = `${hoursStr}:${minutes}`
  
  const weekday = weekdays[date.getDay()]
  const month = date.getMonth() + 1
  const day = date.getDate()
  dateString.value = `${weekday}, ${month}月 ${day}号`
}

let timer = null
onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <!-- 添加动态边框类 day-border / night-border -->
  <div class="card" :class="{ 'day-theme': isDay, 'night-theme': !isDay, 'day-border': isDay, 'night-border': !isDay }">
    <p class="time-text">
      <span>{{ timeString }}</span>
      <span class="time-sub-text">{{ ampm }}</span>
    </p>
    <p class="day-text">{{ dateString }}</p>
    <div class="icon-wrapper">
      <Sun v-if="isDay" :size="15" color="white" />
      <Moon v-else :size="15" color="white" />
    </div>
  </div>
</template>

<style scoped>
.card {
  width: 120px;
  height: 58px;
  border-radius: 5px;
  display: flex;
  color: white;
  justify-content: center;
  position: relative;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.3s ease-in-out;
  overflow: hidden;
  /* 默认边框透明，由主题类控制 */
  border: 2px solid transparent;
}

/* 白天边框：亮色柔和发光 */
.day-border {
  border-color: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 10px rgba(255, 255, 200, 0.6), inset 0 0 5px rgba(255, 255, 200, 0.3);
}

/* 夜晚边框：深色带微弱蓝光 */
.night-border {
  border-color: rgba(200, 220, 255, 0.5);
  box-shadow: 0 0 8px rgba(100, 150, 255, 0.4), inset 0 0 4px rgba(100, 150, 255, 0.2);
}

.day-theme {
  background: linear-gradient(to right, #87CEEB, #B0E2FF);
}

.night-theme {
  background: linear-gradient(to right, rgb(20, 30, 48), rgb(36, 59, 85));
}

.time-text {
  font-size: 33px;
  margin-top: 0px;
  margin-left: 10px;
  font-weight: 300;
  font-family: 'Gill Sans', 'Gill Sans MT', Calibri, 'Trebuchet MS', sans-serif;
}

.time-sub-text {
  font-size: 7px;
  margin-left: 2px;
}

.day-text {
  font-size: 14px;
  margin-top: -6px;
  margin-left: 15px;
  margin-bottom: 5px;
  font-weight: 300;
  font-family: 'squareChinese';
}

.icon-wrapper {
  position: absolute;
  right: 7px;
  top: 7px;
  transition: all 0.3s ease-in-out;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card:hover .icon-wrapper {
  transform: scale(1.2);
}

@font-face {
  font-family: 'squareChinese';
  src: url('../resources/font/WDXL_Lubrifont_SC/WDXLLubrifontSC-Regular.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}
</style>