<template><section class="leader"><div class="head"><div><small>ONLINE TOP 10</small><h3>游戏排行榜</h3></div><button :disabled="busy" @click="submit">{{ busy?'提交中…':`提交成绩 ${score}` }}</button></div><div class="privacy-note">排行榜使用匿名玩家代号，不公开账号、IP 或地区。</div><div class="rows"><div v-for="(x,i) in rows" :key="x.player" class="row" :class="{ mine:x.mine }"><b>{{ i+1 }}</b><code>{{ x.player }}<em v-if="x.mine">我的成绩</em></code><strong>{{ x.score }}</strong></div><p v-if="!rows.length">还没有成绩，来当第一名吧！</p></div><div v-if="tip" class="tip">{{ tip }}</div></section></template>
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
const props = defineProps<{ game: string; score: number }>()
const router = useRouter()
type Row = { player: string; score: number; mine: boolean }
const rows = ref<Row[]>([]), busy = ref(false), tip = ref('')
async function request(url: string, options?: RequestInit) {
  const token = localStorage.getItem('token')
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`
  const response = await fetch(url, { cache: 'no-store', headers, ...options })
  const payload = await response.json().catch(() => null)
  if (!response.ok || !payload?.success) throw Error(payload?.message || `HTTP ${response.status}`)
  return payload.data
}
async function load() { try { rows.value = await request(`/api/game-scores/${props.game}`) } catch { tip.value = '排行榜暂时无法加载' } }
async function submit() {
  if (!localStorage.getItem('token')) { tip.value = '登录后才能提交成绩'; await router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } }); return }
  busy.value = true; tip.value = ''
  try { rows.value = await request('/api/game-scores', { method: 'POST', body: JSON.stringify({ game: props.game, score: props.score }) }); tip.value = '成绩已提交，每个账号仅保留该游戏最高分' }
  catch (error) { tip.value = `提交失败：${error instanceof Error ? error.message : '请稍后再试'}` }
  finally { busy.value = false }
}
onMounted(load)
</script>
<style scoped>.leader{max-width:520px;margin:30px auto 0;padding:18px;background:var(--c-bg-card);border:1px solid var(--c-border);border-radius:14px}.head{display:flex;justify-content:space-between;align-items:center;gap:12px}.head small{color:var(--c-purple);letter-spacing:2px}.head h3{margin:4px 0;color:var(--c-text)}button{min-height:44px;border:0;border-radius:8px;padding:9px 12px;background:linear-gradient(135deg,var(--c-purple),var(--c-cyan));color:white;font-weight:700;cursor:pointer}.privacy-note{margin-top:10px;padding:8px 10px;border-radius:7px;background:var(--c-bg-soft);color:var(--c-text-3);font-size:11px;line-height:1.5}.rows{display:grid;gap:5px;margin-top:12px}.row{display:grid;grid-template-columns:25px minmax(0,1fr) 70px;gap:7px;padding:7px 8px;border:1px solid transparent;background:var(--c-bg-card2);border-radius:7px;font-size:11px}.row.mine{border-color:var(--c-purple);background:rgba(115,87,216,.1)}.row b{color:var(--c-purple)}.row code{color:var(--c-text-2);overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.row code em{margin-left:7px;padding:2px 5px;border-radius:999px;background:var(--c-purple);color:white;font:700 9px sans-serif;font-style:normal}.row strong{text-align:right;color:var(--c-cyan)}.rows p,.tip{text-align:center;color:var(--c-text-3);font-size:12px}.tip{color:var(--c-green);margin-top:8px}@media(max-width:480px){.head{align-items:stretch;flex-direction:column}.head button{width:100%}}</style>
