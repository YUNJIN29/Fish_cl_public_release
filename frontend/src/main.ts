import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'

import App from './App.vue'
import { router } from './router'
import { createPinia } from 'pinia'
import GameLeaderboard from './components/GameLeaderboard.vue'

createApp(App).component('GameLeaderboard', GameLeaderboard).use(createPinia()).use(router).use(ElementPlus).mount('#app')
