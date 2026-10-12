<template>
  <aside v-if="offline || installEvent" class="app-status" :class="{ offline }" role="status" aria-live="polite">
    <div><b>{{ offline ? '当前处于离线状态' : '安装 Aquatic Lab 到设备' }}</b><span>{{ offline ? '只读离线页可用，业务数据将在恢复网络后加载。' : '从桌面快速进入水质日志、养殖待办和诊断工具。' }}</span></div>
    <button v-if="installEvent && !offline" type="button" @click="install">安装</button>
    <button v-if="installEvent && !offline" type="button" class="dismiss" aria-label="暂不安装" @click="dismiss">×</button>
  </aside>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

type InstallEvent = Event & { prompt: () => Promise<void>; userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }> }
const installEvent = ref<InstallEvent | null>(null)
const offline = ref(!navigator.onLine)

function onInstallAvailable(event: Event) {
  event.preventDefault()
  if (localStorage.getItem('yunjin:install-dismissed') !== '1') installEvent.value = event as InstallEvent
}
function onOnline() { offline.value = false }
function onOffline() { offline.value = true }
function onInstalled() { installEvent.value = null; localStorage.removeItem('yunjin:install-dismissed') }
async function install() {
  const event = installEvent.value
  if (!event) return
  await event.prompt()
  await event.userChoice
  installEvent.value = null
}
function dismiss() { installEvent.value = null; localStorage.setItem('yunjin:install-dismissed', '1') }

onMounted(() => {
  window.addEventListener('beforeinstallprompt', onInstallAvailable)
  window.addEventListener('online', onOnline)
  window.addEventListener('offline', onOffline)
  window.addEventListener('appinstalled', onInstalled)
})
onBeforeUnmount(() => {
  window.removeEventListener('beforeinstallprompt', onInstallAvailable)
  window.removeEventListener('online', onOnline)
  window.removeEventListener('offline', onOffline)
  window.removeEventListener('appinstalled', onInstalled)
})
</script>

<style scoped>
.app-status{position:fixed;z-index:2200;right:18px;bottom:18px;display:flex;align-items:center;gap:12px;width:min(440px,calc(100vw - 36px));padding:13px 14px;border:1px solid var(--c-cyan);border-radius:12px;background:color-mix(in srgb,var(--c-bg-card) 95%,transparent);box-shadow:0 16px 45px rgba(0,0,0,.3);backdrop-filter:blur(14px)}.app-status>div{display:grid;gap:3px;min-width:0;flex:1}.app-status b{color:var(--c-text);font-size:13px}.app-status span{color:var(--c-text-3);font-size:11px;line-height:1.45}.app-status button{min-width:58px;min-height:44px;padding:0 12px;border:0;border-radius:8px;background:linear-gradient(135deg,var(--c-purple),var(--c-cyan));color:white;font:800 12px inherit;cursor:pointer}.app-status .dismiss{min-width:44px;padding:0;background:transparent;color:var(--c-text-3);font-size:20px}.app-status.offline{border-color:var(--c-amber)}@media(max-width:560px){.app-status{right:10px;bottom:10px;width:calc(100vw - 20px);align-items:flex-start}.app-status:not(.offline)>div{max-width:calc(100% - 122px)}}
</style>
