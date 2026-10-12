<template>
  <div class="workspace">
    <section class="hero">
      <div>
        <span class="eyebrow">PERSONAL WORKSPACE · {{ greeting }}</span>
        <h1>欢迎回来，<br><b>从感兴趣的地方继续。</b></h1>
        <p>把学习工具、游戏与水产智诊平台放进同一张工作台。这里会记住常用入口和最近浏览，但数据只保存在当前浏览器。</p>
        <div class="hero-actions">
          <button v-if="workspace.continuePage" class="primary" type="button" @click="go(workspace.continuePage.destination!.path)">继续 {{ workspace.continuePage.destination!.title }} →</button>
          <button class="secondary" type="button" @click="go('/profile')">查看项目展示</button>
        </div>
      </div>
      <div class="status" aria-label="工具空间概览">
        <div class="orb">AD</div>
        <span><b>{{ workspace.pinned.length }}</b> 个常用入口</span>
        <span><b>{{ workspace.recentDestinations.length }}</b> 条最近浏览</span>
        <i>LOCAL · PRIVATE</i>
      </div>
    </section>

    <section class="panel shortcuts">
      <header><div><span>QUICK START</span><h2>我的常用入口</h2></div><button type="button" @click="editing = !editing">{{ editing ? '完成' : '自定义' }}</button></header>
      <div class="shortcut-grid">
        <button v-for="item in workspace.pinned" :key="item.path" class="shortcut" type="button" @click="editing ? workspace.togglePin(item.path) : go(item.path)">
          <i>{{ item.icon }}</i><span><b>{{ item.title }}</b><small>{{ item.description }}</small></span><em>{{ editing ? '移除 ×' : '打开 ↗' }}</em>
        </button>
      </div>
      <div v-if="editing" class="picker">
        <p>选择最多 6 个入口</p>
        <button v-for="item in availablePins" :key="item.path" type="button" @click="workspace.togglePin(item.path)"><span>{{ item.icon }}</span>{{ item.title }}<b>＋</b></button>
      </div>
    </section>

    <div class="lower-grid">
      <section class="panel recent">
        <header><div><span>RECENT</span><h2>最近浏览</h2></div><button v-if="workspace.recentDestinations.length" type="button" @click="workspace.clearRecent">清空</button></header>
        <div v-if="workspace.recentDestinations.length" class="recent-list">
          <button v-for="entry in workspace.recentDestinations" :key="entry.path" type="button" @click="go(entry.destination!.path)">
            <i>{{ entry.destination!.icon }}</i><span><b>{{ entry.destination!.title }}</b><small>{{ formatTime(entry.visitedAt) }}</small></span><em>继续 →</em>
          </button>
        </div>
        <div v-else class="empty">开始探索后，这里会出现你的最近访问记录。</div>
      </section>
      <section class="panel focus">
        <header><div><span>EXPLORE</span><h2>今天探索什么？</h2></div></header>
        <button type="button" @click="go('/personal/games')"><span>放松一下</span><b>8 款小游戏与排行挑战</b><em>进入游戏中心 →</em></button>
        <button type="button" @click="go('/fish/knowledge-graph')"><span>发现关联</span><b>从症状追踪病害与鱼种</b><em>探索知识图谱 →</em></button>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { siteDestinations } from '@/data/siteDestinations'
import { useWorkspaceStore } from '@/stores/workspace'

const router = useRouter()
const workspace = useWorkspaceStore()
const editing = ref(false)
const greeting = computed(() => { const hour = new Date().getHours(); return hour < 6 ? '夜深了' : hour < 11 ? '早上好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好' })
const availablePins = computed(() => siteDestinations.filter((item) => !workspace.pinnedPaths.includes(item.path) && item.path !== '/'))
function go(path: string) { router.push(path) }
function formatTime(time: number) {
  const diff = Date.now() - time
  if (diff < 60_000) return '刚刚访问'
  if (diff < 3_600_000) return `${Math.floor(diff / 60_000)} 分钟前`
  if (diff < 86_400_000) return `${Math.floor(diff / 3_600_000)} 小时前`
  return new Date(time).toLocaleDateString('zh-CN')
}
onMounted(workspace.load)
</script>

<style scoped>
.workspace{max-width:1120px;margin:auto;padding:38px 10px 70px}.hero{min-height:430px;display:grid;grid-template-columns:1.2fr .8fr;align-items:center;gap:70px;padding:34px 38px;margin-bottom:26px;border:1px solid var(--c-border);border-radius:24px;background:radial-gradient(circle at 85% 25%,rgba(85,219,250,.12),transparent 34%),linear-gradient(145deg,var(--c-bg-card),var(--c-bg));overflow:hidden}.eyebrow,.panel header span{font:700 10px ui-monospace;letter-spacing:.2em;color:var(--c-purple)}h1{margin:18px 0;font-size:clamp(42px,6vw,67px);line-height:1.08;letter-spacing:-.045em}h1 b{background:linear-gradient(100deg,var(--c-purple),var(--c-cyan));background-clip:text;color:transparent}.hero p{max-width:640px;color:var(--c-text-2);line-height:1.85}.hero-actions{display:flex;flex-wrap:wrap;gap:10px;margin-top:25px}.hero-actions button{min-height:46px;padding:0 18px;border-radius:10px;font-weight:750;cursor:pointer}.primary{border:0;background:linear-gradient(135deg,var(--c-purple),var(--c-cyan));color:white}.secondary{border:1px solid var(--c-border);background:var(--c-bg-card);color:var(--c-text)}.status{position:relative;min-height:300px;display:grid;place-content:center;gap:10px}.orb{width:180px;height:180px;display:grid;place-items:center;margin:auto auto 12px;border-radius:50%;background:radial-gradient(circle at 35% 30%,#9e92ff,#4737ba 48%,#111a34 72%);box-shadow:0 0 70px rgba(123,108,255,.35);font-size:48px;font-weight:900;color:white}.status span{padding:10px 14px;border:1px solid var(--c-border);border-radius:10px;background:var(--c-bg-card);color:var(--c-text-2);font-size:12px}.status span b{color:var(--c-cyan);font-size:16px}.status i{text-align:center;color:var(--c-text-3);font:10px ui-monospace;letter-spacing:.18em}.panel{padding:25px;border:1px solid var(--c-border);border-radius:18px;background:var(--c-bg-card)}.panel header{display:flex;align-items:start;justify-content:space-between;margin-bottom:18px}.panel h2{margin:5px 0 0;font-size:23px}.panel header button{min-height:38px;padding:0 13px;border:1px solid var(--c-border);border-radius:8px;background:transparent;color:var(--c-text-2);cursor:pointer}.shortcut-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.shortcut{min-height:130px;display:grid;grid-template-columns:42px 1fr;gap:12px;padding:16px;border:1px solid var(--c-border);border-radius:13px;background:var(--c-bg);color:var(--c-text);text-align:left;cursor:pointer;transition:.2s}.shortcut:hover{border-color:var(--c-cyan);transform:translateY(-2px)}.shortcut i,.recent-list i{width:40px;height:40px;display:grid;place-items:center;border-radius:10px;background:var(--c-cyan-dim);color:var(--c-cyan);font-style:normal;font-weight:800}.shortcut span{display:grid;align-content:start;gap:6px}.shortcut small,.recent-list small{color:var(--c-text-3);line-height:1.4}.shortcut em{grid-column:2;color:var(--c-purple);font-size:11px;font-style:normal}.picker{display:flex;flex-wrap:wrap;gap:8px;margin-top:16px;padding-top:16px;border-top:1px solid var(--c-border)}.picker p{width:100%;margin:0 0 4px;color:var(--c-text-3);font-size:12px}.picker button{min-height:40px;display:flex;align-items:center;gap:7px;padding:0 11px;border:1px solid var(--c-border);border-radius:9px;background:var(--c-bg);color:var(--c-text-2);cursor:pointer}.picker button b{color:var(--c-cyan)}.lower-grid{display:grid;grid-template-columns:1.1fr .9fr;gap:18px;margin-top:18px}.recent-list{display:grid;gap:8px}.recent-list button{min-height:62px;display:grid;grid-template-columns:40px 1fr auto;align-items:center;gap:12px;padding:8px;border:0;border-radius:10px;background:transparent;color:var(--c-text);text-align:left;cursor:pointer}.recent-list button:hover{background:var(--c-cyan-dim)}.recent-list span{display:grid;gap:4px}.recent-list em{color:var(--c-cyan);font-size:11px;font-style:normal}.empty{padding:42px 12px;text-align:center;color:var(--c-text-3)}.focus{display:grid;align-content:start}.focus>button{display:grid;gap:6px;padding:17px;margin-bottom:10px;border:1px solid var(--c-border);border-radius:12px;background:var(--c-bg);color:var(--c-text);text-align:left;cursor:pointer}.focus>button span{color:var(--c-purple);font:700 10px ui-monospace;letter-spacing:.14em}.focus>button b{font-size:15px}.focus>button em{color:var(--c-text-3);font-size:11px;font-style:normal}@media(max-width:900px){.hero{grid-template-columns:1fr;gap:10px}.status{display:none}.shortcut-grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:650px){.workspace{padding:18px 0 55px}.hero{min-height:auto;padding:28px 20px;border-radius:16px}h1{font-size:40px}.lower-grid{grid-template-columns:1fr}.panel{padding:18px}.shortcut-grid{grid-template-columns:1fr}.shortcut{min-height:100px}.hero-actions button{width:100%}}
</style>
