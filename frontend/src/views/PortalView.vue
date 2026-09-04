<template>
  <main class="portal">
    <div class="glow a"/><div class="glow b"/>
    <header><b>AD</b><span>AQUATIC KNOWLEDGE LAB</span></header>
    <section class="intro"><small>WELCOME TO MY DIGITAL SPACE</small><h1>今天想去哪里？</h1><p>两个独立空间，一个入口。选择你想浏览的内容。</p></section>
    <section class="choices">
      <article class="choice personal" @click="$router.push('/personal')"><em>01</em><i>✦</i><div><small>TOOLS SPACE</small><h2>学习工具</h2><p>学习记录、休闲小游戏，以及更多实验性功能。</p><button>进入工具空间 →</button></div></article>
      <article class="choice fish" @click="$router.push('/fish')"><em>02</em><i>◈</i><div><small>AQUACULTURE INTELLIGENCE</small><h2>水产智诊平台</h2><p>鱼类病害诊断、知识图谱与水产知识服务。</p><button>进入鱼类平台 →</button></div></article>
    </section>
    <section class="visit-stats"><div><b>{{ stats.todayVisitors }}</b><span>今日访问人数</span></div><i/><div><b>{{ stats.monthVisitors }}</b><span>本月访问人数</span></div><i/><div><b>{{ stats.thisIpVisits }}</b><span>本 IP 今日访问</span></div></section>
    <section class="ranking"><div class="ranking-title"><span>MONTHLY RANKING</span><h3>本月访问次数排行榜</h3></div><div class="ranking-list"><div v-for="(item,index) in ranking" :key="item.ip" class="rank-row"><b>{{ index + 1 }}</b><code>{{ maskIp(item.ip) }}</code><span>{{ item.location }}</span><strong>{{ item.visits }} 次</strong></div><p v-if="!ranking.length">暂无访问数据</p></div></section>
    <footer><div class="footer-line"><span>© 2026</span><span>Aquatic Disease Knowledge Platform</span></div><p class="site-notice">本项目用于学习、科研训练和课程/竞赛原型搭建；公开版本不包含个人部署信息。</p></footer>
  </main>
</template>
<script setup lang="ts">
import { onMounted, reactive } from 'vue'
const stats = reactive<{ todayVisitors: number | string; monthVisitors: number | string; thisIpVisits: number | string }>({ todayVisitors: '--', monthVisitors: '--', thisIpVisits: '--' })
const ranking = reactive<Array<{ ip: string; location: string; visits: number }>>([])
async function readData<T>(url: string, options?: RequestInit): Promise<T | null> {
  try {
    const response = await fetch(url, { cache: 'no-store', credentials: 'same-origin', ...options })
    if (!response.ok) return null
    const envelope = await response.json()
    return envelope?.success ? envelope.data as T : null
  } catch { return null }
}
function maskIp(ip: string) {
  return ip.replace(/\d+$/, '*')
}
onMounted(async () => {
  const [summary, list] = await Promise.all([
    readData<typeof stats>('/api/visits/stats'),
    readData<Array<{ ip: string; location: string; visits: number }>>('/api/visits/monthly-ranking'),
  ])
  if (summary) Object.assign(stats, summary)
  if (list) ranking.push(...list)
  const tracked = await readData<typeof stats>('/api/visits/track', { method: 'POST' })
  if (tracked) Object.assign(stats, tracked)
})
</script>
<style scoped>
.portal{min-height:100vh;position:relative;overflow:hidden;background:#f4f7fc;color:#10233e;padding:0 clamp(22px,6vw,90px);font-family:'Segoe UI','Microsoft YaHei',sans-serif}.glow{position:absolute;width:500px;height:500px;border-radius:50%;filter:blur(120px);opacity:.09}.a{background:#00d4ff;top:-250px;right:-100px}.b{background:#7b6cff;bottom:-300px;left:-120px}header{height:74px;display:flex;align-items:center;gap:12px;font-size:12px;letter-spacing:3px;color:#60758e}header b{width:34px;height:34px;border:1px solid #00a4d4;color:#009bc9;display:grid;place-items:center;border-radius:9px;background:#fff}.intro{text-align:center;padding:clamp(25px,5vh,55px) 0 30px}.intro small{letter-spacing:4px;color:#009dcc}.intro h1{font-size:clamp(38px,6vw,64px);margin:14px 0 10px}.intro p,.choice p{color:#657b94}.choices{display:grid;grid-template-columns:repeat(2,1fr);gap:24px;max-width:1080px;margin:auto}.choice{height:330px;position:relative;padding:35px;border:1px solid #dbe5f1;border-radius:20px;background:#fff;box-shadow:0 10px 35px rgba(35,75,120,.07);cursor:pointer;transition:.3s}.choice:hover{transform:translateY(-6px);border-color:var(--accent);box-shadow:0 22px 55px color-mix(in srgb,var(--accent) 14%,transparent)}.personal{--accent:#7667ed}.fish{--accent:#00a4d4}.choice em{position:absolute;right:28px;top:22px;color:#dce7f3;font-size:50px;font-weight:800;font-style:normal}.choice>i{width:48px;height:48px;border:1px solid var(--accent);color:var(--accent);border-radius:12px;display:grid;place-items:center;font-size:23px;font-style:normal}.choice>div{position:absolute;left:35px;bottom:30px}.choice small{font-size:9px;letter-spacing:2px;color:var(--accent)}h2{font-size:30px;margin:8px 0}.choice button{padding:10px 0;border:0;background:transparent;color:var(--accent);font-weight:700;cursor:pointer}.visit-stats{display:flex;justify-content:center;align-items:center;gap:28px;margin:28px auto 4px}.visit-stats div{text-align:center;min-width:100px}.visit-stats b{display:block;font-size:25px;color:#087eb4}.visit-stats span{font-size:12px;color:#71869d}.visit-stats i{height:34px;width:1px;background:#d7e2ee}footer{text-align:center;padding:22px 0 25px;color:#52677f;font-size:14px}.footer-line{display:flex;justify-content:center;gap:8px;flex-wrap:wrap}.footer-line a{color:#58718e;text-decoration:none}footer p{max-width:920px;margin:8px auto 0;color:#60758e;font-size:13px;line-height:1.7}@media(max-width:720px){header{height:60px}.choices{grid-template-columns:1fr}.choice{height:280px;padding:25px}.choice>div{left:25px;bottom:22px}.visit-stats{gap:10px}.visit-stats div{min-width:82px}.visit-stats b{font-size:21px}}
</style>
