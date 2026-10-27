import { createApp } from 'vue'
import 'element-plus/theme-chalk/dark/css-vars.css'
import 'element-plus/theme-chalk/el-message.css'

import App from './App.vue'
import { router } from './router'
import { createPinia } from 'pinia'
import GameLeaderboard from './components/GameLeaderboard.vue'

// 正式域名统一使用 HTTPS，避免 POST 在 HTTP 跳转时被浏览器改成 GET 并丢失请求体。
if (window.location.hostname === 'example.com' && window.location.protocol === 'http:') {
  window.location.replace(`https://${window.location.host}${window.location.pathname}${window.location.search}${window.location.hash}`)
} else {
  createApp(App).component('GameLeaderboard', GameLeaderboard).use(createPinia()).use(router).mount('#app')
  if ('serviceWorker' in navigator && import.meta.env.PROD) {
    window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js').catch(() => undefined))
  }
}
