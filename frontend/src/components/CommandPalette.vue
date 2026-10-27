<template>
  <button class="command-trigger" type="button" aria-label="打开全站快捷导航" @click="openPalette">
    <span>快速前往</span><kbd>⌘ K</kbd>
  </button>

  <Teleport to="body">
    <div v-if="open" class="command-overlay" role="presentation" @mousedown.self="closePalette">
      <section class="command-panel" role="dialog" aria-modal="true" aria-labelledby="command-title">
        <header>
          <div><small>Aquatic Lab NAVIGATOR</small><h2 id="command-title">想去哪里？</h2></div>
          <button type="button" aria-label="关闭快捷导航" @click="closePalette">Esc</button>
        </header>
        <label class="command-search">
          <span aria-hidden="true">⌕</span>
          <input ref="inputEl" v-model="query" type="search" placeholder="搜索项目、游戏、诊断、知识库…" autocomplete="off" />
        </label>

        <div v-if="!query && recentCommands.length" class="command-group">
          <p>最近访问</p>
          <button v-for="item in recentCommands" :key="`recent-${item.path}`" type="button" @click="navigate(item.path)">
            <i>{{ item.icon }}</i><span><b>{{ item.title }}</b><small>{{ item.description }}</small></span><em>最近</em>
          </button>
        </div>
        <div class="command-group">
          <p>{{ query ? `找到 ${filteredCommands.length} 个入口` : '全部空间' }}</p>
          <button v-for="item in filteredCommands" :key="item.path" type="button" @click="navigate(item.path)">
            <i>{{ item.icon }}</i><span><b>{{ item.title }}</b><small>{{ item.description }}</small></span><em>↗</em>
          </button>
          <div v-if="filteredCommands.length === 0" class="command-empty">没有匹配入口，试试“病害”“项目”或“游戏”。</div>
        </div>
        <footer><span>输入关键词快速筛选</span><span><kbd>Enter</kbd> 唯一结果时打开</span><span><kbd>Esc</kbd> 关闭</span></footer>
      </section>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { findDestination, siteDestinations, type SiteDestination } from '@/data/siteDestinations'

type RecentPage = { path: string; title: string; visitedAt: number }

const router = useRouter()
const open = ref(false)
const query = ref('')
const inputEl = ref<HTMLInputElement | null>(null)
const recent = ref<RecentPage[]>([])

const filteredCommands = computed(() => {
  const keyword = query.value.trim().toLocaleLowerCase()
  if (!keyword) return siteDestinations
  return siteDestinations.filter((item) => `${item.title} ${item.description} ${item.keywords}`.toLocaleLowerCase().includes(keyword))
})
const recentCommands = computed(() => recent.value
  .map((entry) => findDestination(entry.path))
  .filter((item): item is SiteDestination => Boolean(item))
  .filter((item, index, list) => list.findIndex((other) => other.path === item.path) === index)
  .slice(0, 3))

function readRecent() {
  try { recent.value = JSON.parse(localStorage.getItem('aquatic:recent-pages') || '[]') }
  catch { recent.value = [] }
}
async function openPalette() { readRecent(); open.value = true; query.value = ''; await nextTick(); inputEl.value?.focus() }
function closePalette() { open.value = false }
async function navigate(path: string) { closePalette(); await router.push(path) }
function onKeydown(event: KeyboardEvent) {
  if ((event.ctrlKey || event.metaKey) && event.key.toLocaleLowerCase() === 'k') { event.preventDefault(); open.value ? closePalette() : openPalette(); return }
  if (event.key === 'Escape' && open.value) closePalette()
  if (event.key === 'Enter' && open.value && filteredCommands.value.length === 1) navigate(filteredCommands.value[0].path)
}
onMounted(() => { readRecent(); window.addEventListener('keydown', onKeydown); window.addEventListener('aquatic:recent-pages', readRecent) })
onBeforeUnmount(() => { window.removeEventListener('keydown', onKeydown); window.removeEventListener('aquatic:recent-pages', readRecent) })
</script>

<style scoped>
.command-trigger{position:fixed;right:20px;bottom:20px;z-index:900;min-height:44px;display:flex;align-items:center;gap:12px;padding:0 14px;border:1px solid rgba(85,219,250,.34);border-radius:999px;background:rgba(6,15,29,.9);color:#eaf7ff;box-shadow:0 12px 34px rgba(0,0,0,.24);backdrop-filter:blur(16px);cursor:pointer}.command-trigger span{font-size:12px;font-weight:750}.command-trigger kbd,.command-panel kbd{padding:3px 6px;border:1px solid rgba(255,255,255,.18);border-radius:5px;background:rgba(255,255,255,.08);font:10px ui-monospace}.command-overlay{position:fixed;inset:0;z-index:2000;display:grid;place-items:start center;padding:11vh 16px 24px;background:rgba(2,7,15,.62);backdrop-filter:blur(10px)}.command-panel{width:min(680px,100%);max-height:78vh;overflow:auto;border:1px solid rgba(107,211,242,.28);border-radius:18px;background:#091321;color:#e9f5ff;box-shadow:0 35px 100px rgba(0,0,0,.45)}.command-panel header{display:flex;justify-content:space-between;align-items:start;padding:24px 24px 16px}.command-panel header small{color:#55dbfa;font:700 10px ui-monospace;letter-spacing:.18em}.command-panel h2{margin:5px 0 0;font-size:28px}.command-panel header button{min-width:44px;min-height:36px;border:1px solid #263b51;border-radius:8px;background:#0d1c2e;color:#9fb2c6;cursor:pointer}.command-search{display:flex;align-items:center;gap:12px;margin:0 18px;padding:0 15px;border:1px solid #2b4964;border-radius:12px;background:#07101d}.command-search>span{color:#55dbfa;font-size:24px}.command-search input{width:100%;min-height:54px;border:0;outline:0;background:transparent;color:#f2f8ff;font:15px inherit}.command-search input::placeholder{color:#657b91}.command-group{padding:14px 12px 2px}.command-group>p{margin:0 10px 8px;color:#6f8499;font:700 10px ui-monospace;letter-spacing:.15em;text-transform:uppercase}.command-group>button{width:100%;min-height:60px;display:grid;grid-template-columns:38px 1fr auto;align-items:center;gap:12px;padding:8px 10px;border:0;border-radius:10px;background:transparent;color:inherit;text-align:left;cursor:pointer}.command-group>button:hover,.command-group>button:focus-visible{outline:0;background:rgba(85,219,250,.1)}.command-group i{width:36px;height:36px;display:grid;place-items:center;border:1px solid #29455f;border-radius:9px;color:#55dbfa;font-style:normal;font-weight:800}.command-group span{display:grid;gap:4px}.command-group b{font-size:14px}.command-group small{color:#8296aa;font-size:12px}.command-group em{color:#668198;font:11px ui-monospace;font-style:normal}.command-empty{padding:26px;text-align:center;color:#7f93a8}.command-panel footer{display:flex;gap:18px;padding:14px 22px 18px;border-top:1px solid #1b2d40;color:#71869a;font-size:11px}.command-panel footer span{display:flex;align-items:center;gap:5px}@media(max-width:640px){.command-trigger{right:12px;bottom:12px}.command-trigger span{display:none}.command-overlay{padding:4vh 10px}.command-panel{max-height:90vh}.command-panel footer{display:none}}
</style>
