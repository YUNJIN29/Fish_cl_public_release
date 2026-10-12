<template>
  <main class="portal">
    <div class="glow a" aria-hidden="true" /><div class="glow b" aria-hidden="true" />
    <header><RouterLink to="/" aria-label="Aquatic Lab 首页"><b>AD</b><span>AQUATIC KNOWLEDGE LAB</span></RouterLink></header>
    <section class="intro"><small>WELCOME TO MY DIGITAL SPACE</small><h1>今天想去哪里？</h1><p>三个独立空间，一个入口。选择你想浏览的内容。</p></section>
    <section class="choices" aria-label="站点空间">
      <RouterLink v-for="space in spaces" :key="space.title" :to="space.to" class="choice" :class="space.className" :aria-label="`进入${space.title}`">
        <em>{{ space.index }}</em><i>{{ space.icon }}</i><div><small>{{ space.label }}</small><h2>{{ space.title }}</h2><p>{{ space.description }}</p><strong>{{ space.action }} →</strong></div>
      </RouterLink>
    </section>
    <section class="visit-stats" aria-label="访问统计"><div><b>{{ stats.todayVisitors }}</b><span>今日访客</span></div><i/><div><b>{{ stats.monthVisitors }}</b><span>本月访客</span></div><i/><div><b>{{ stats.totalVisitors }}</b><span>累计访客</span></div></section>
    <section id="guestbook" class="guestbook" aria-labelledby="guestbook-title">
      <div class="guestbook-title"><small>GUESTBOOK</small><h2 id="guestbook-title">留下一句话</h2><p>无需登录，留言审核通过后公开显示。</p></div>
      <form @submit.prevent="submitMessage">
        <label>昵称<input v-model="author" maxlength="32" autocomplete="nickname" placeholder="怎么称呼你？" required></label>
        <label>留言<textarea v-model="draftMessage" maxlength="500" rows="4" placeholder="写下建议、问候或想法…" required /></label>
        <div><span>{{ draftMessage.length }} / 500</span><button type="submit" :disabled="submitting || !author.trim() || !draftMessage.trim()">{{ submitting ? '提交中…' : '提交留言 →' }}</button></div>
        <p v-if="notice" class="notice" role="status">{{ notice }}</p>
      </form>
      <div class="messages" aria-live="polite">
        <article v-for="message in messages" :key="message.id"><header><b>{{ message.author }}</b><time :datetime="message.createdAt">{{ formatDate(message.createdAt) }}</time></header><p>{{ message.content }}</p><blockquote v-if="message.reply"><b>Aquatic Lab 回复</b>{{ message.reply }}</blockquote></article>
        <p v-if="!loadingMessages && !messages.length" class="empty">暂时还没有公开留言，欢迎写下第一句。</p>
        <p v-if="loadingMessages" class="empty">正在读取留言…</p>
      </div>
    </section>
    <footer><div class="footer-line"><span>© 2026 Aquatic Disease Knowledge Platform</span></div><p>公开版本已移除个人联系方式、备案信息、服务器地址和私有部署资料。AI 诊断仅供辅助参考，不能替代现场专业判断。</p></footer>
  </main>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { http } from '@/utils/http'

type GuestbookMessage = { id: number; author: string; content: string; reply?: string | null; createdAt: string }
const spaces = [
  { index: '01', icon: '✦', label: 'TOOLS SPACE', title: '学习工具', description: '学习记录、休闲小游戏，以及更多实验性功能。', action: '进入工具空间', to: '/personal', className: 'personal' },
  { index: '02', icon: '◈', label: 'AQUACULTURE INTELLIGENCE', title: '水产智诊平台', description: '鱼类病害诊断、知识图谱与水产知识服务。', action: '进入鱼类平台', to: '/fish', className: 'fish' },
  { index: '03', icon: '◎', label: 'PROJECT OVERVIEW', title: '项目展示', description: '公开脱敏版本的项目能力、模块结构与技术说明。', action: '查看项目展示', to: '/profile', className: 'profile' },
]
const stats = reactive({ todayVisitors: '—' as number | string, monthVisitors: '—' as number | string, totalVisitors: '—' as number | string })
const author = ref(localStorage.getItem('aquatic:guestbook-author') || '')
const draftMessage = ref('')
const messages = ref<GuestbookMessage[]>([])
const loadingMessages = ref(true)
const submitting = ref(false)
const notice = ref('')
async function readEnvelope<T>(url: string, options?: RequestInit): Promise<T | null> { try { const response = await fetch(url, { cache: 'no-store', ...options }); const payload = await response.json(); return response.ok && payload?.success ? payload.data as T : null } catch { return null } }
async function loadMessages() { const data = await readEnvelope<any>('/api/guestbook?page=0&size=8'); messages.value = data?.content || []; loadingMessages.value = false }
async function submitMessage() { if (!author.value.trim() || !draftMessage.value.trim() || submitting.value) return; submitting.value = true; notice.value = ''; try { await http.post('/api/guestbook', { author: author.value.trim(), content: draftMessage.value.trim() }); localStorage.setItem('aquatic:guestbook-author', author.value.trim()); draftMessage.value = ''; notice.value = '留言已提交，审核通过后会显示在这里。' } finally { submitting.value = false } }
function formatDate(value: string) { return new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' }).format(new Date(value)) }
onMounted(async () => { const summary = await readEnvelope<typeof stats>('/api/visits/summary'); if (summary) Object.assign(stats, summary); await loadMessages(); const key = `aquatic-visit-${new Date().toISOString().slice(0, 10)}`; if (!sessionStorage.getItem(key)) { const tracked = await readEnvelope<typeof stats>('/api/visits/track', { method: 'POST' }); if (tracked) Object.assign(stats, tracked); sessionStorage.setItem(key, '1') } })
</script>

<style scoped>
.portal{min-height:100vh;position:relative;overflow:hidden;background:#f4f7fc;color:#10233e;padding:0 clamp(22px,5vw,80px);font-family:'Segoe UI','Microsoft YaHei',sans-serif}.glow{position:absolute;width:500px;height:500px;border-radius:50%;filter:blur(120px);opacity:.09;pointer-events:none}.a{background:#00d4ff;top:-250px;right:-100px}.b{background:#7b6cff;bottom:-300px;left:-120px}header{height:74px;display:flex;align-items:center;position:relative;z-index:1}header a{min-height:44px;display:flex;align-items:center;gap:12px;color:#60758e;text-decoration:none;font-size:12px;letter-spacing:3px}header b{width:34px;height:34px;border:1px solid #00a4d4;color:#009bc9;display:grid;place-items:center;border-radius:9px;background:#fff}.intro{text-align:center;padding:clamp(20px,4vh,46px) 0 28px}.intro small,.guestbook-title small{letter-spacing:4px;color:#009dcc}.intro h1{font-size:clamp(38px,5vw,62px);margin:12px 0 8px}.intro p,.choice p,.guestbook-title p{color:#657b94}.choices{display:grid;grid-template-columns:repeat(3,1fr);gap:20px;max-width:1380px;margin:auto}.choice{height:310px;position:relative;padding:32px;border:1px solid #dbe5f1;border-radius:20px;background:#fff;box-shadow:0 10px 35px rgba(35,75,120,.07);cursor:pointer;transition:.3s;color:#10233e;text-decoration:none}.choice:hover,.choice:focus-visible{transform:translateY(-6px);border-color:var(--accent);box-shadow:0 22px 55px color-mix(in srgb,var(--accent) 14%,transparent);outline:none}.personal{--accent:#7667ed}.fish{--accent:#00a4d4}.profile{--accent:#e08b2f}.choice em{position:absolute;right:26px;top:20px;color:#dce7f3;font-size:48px;font-weight:800;font-style:normal}.choice>i{width:48px;height:48px;border:1px solid var(--accent);color:var(--accent);border-radius:12px;display:grid;place-items:center;font-size:23px;font-style:normal}.choice>div{position:absolute;left:32px;right:28px;bottom:27px}.choice small{font-size:9px;letter-spacing:2px;color:var(--accent)}h2{font-size:28px;margin:7px 0}.choice p{min-height:48px;margin:0}.choice strong{display:inline-block;padding:10px 0;color:var(--accent);font-size:14px}.visit-stats{display:flex;justify-content:center;align-items:center;gap:28px;margin:27px auto 38px}.visit-stats div{text-align:center;min-width:100px}.visit-stats b{display:block;font-size:25px;color:#087eb4}.visit-stats span{font-size:12px;color:#71869d}.visit-stats i{height:34px;width:1px;background:#d7e2ee}.guestbook{max-width:1080px;margin:0 auto 36px;padding:30px;display:grid;grid-template-columns:.7fr 1fr;gap:26px 38px;border:1px solid #dbe5f1;border-radius:20px;background:#fff;box-shadow:0 10px 35px rgba(35,75,120,.07)}.guestbook-title h2{font-size:30px}.guestbook-title p{line-height:1.7}.guestbook form{display:grid;gap:11px}.guestbook label{display:grid;gap:6px;color:#526b86;font-size:12px;font-weight:700}.guestbook input,.guestbook textarea{width:100%;padding:11px 13px;border:1px solid #d5e1ee;border-radius:9px;background:#f8fafd;color:#10233e;font:inherit;resize:vertical}.guestbook input:focus,.guestbook textarea:focus{outline:2px solid #00a4d4;outline-offset:1px}.guestbook form>div{display:flex;align-items:center;justify-content:space-between}.guestbook form>div span{color:#8497ab;font-size:11px}.guestbook button{min-height:44px;padding:0 18px;border:0;border-radius:9px;background:#087eb4;color:#fff;font-weight:700;cursor:pointer}.guestbook button:disabled{opacity:.5;cursor:not-allowed}.notice{margin:0;color:#087151;font-size:13px}.messages{grid-column:1/-1;display:grid;grid-template-columns:repeat(2,1fr);gap:12px;padding-top:22px;border-top:1px solid #e2eaf3}.messages article{padding:17px;border:1px solid #e1e9f2;border-radius:12px;background:#f8fafd}.messages header{height:auto;display:flex;justify-content:space-between}.messages time{color:#8799ac;font-size:11px}.messages article>p{margin:12px 0 0;color:#3e5873;line-height:1.7;white-space:pre-wrap;overflow-wrap:anywhere}.messages blockquote{display:grid;gap:4px;margin:12px 0 0;padding:10px 12px;border-left:3px solid #7667ed;background:#f0efff;color:#65708b;font-size:13px}.messages blockquote b{color:#6657d7}.empty{grid-column:1/-1;text-align:center;color:#8497ab}footer{text-align:center;padding:18px 0 30px;color:#52677f;font-size:13px}.footer-line{display:flex;justify-content:center;gap:14px;flex-wrap:wrap}.footer-line a{min-height:44px;display:inline-flex;align-items:center;color:#58718e;text-decoration:none}footer p{max-width:920px;margin:4px auto 0;color:#60758e;line-height:1.7}@media(max-width:1050px){.choices{grid-template-columns:repeat(2,1fr)}.profile{grid-column:1/-1}.profile.choice{height:260px}}@media(max-width:720px){.portal{padding:0 16px}header{height:60px}.choices,.guestbook{grid-template-columns:1fr}.profile{grid-column:auto}.choice,.profile.choice{height:270px;padding:25px}.choice>div{left:25px;bottom:22px}.visit-stats{gap:10px}.visit-stats div{min-width:82px}.visit-stats b{font-size:21px}.guestbook{padding:20px}.messages{grid-template-columns:1fr}.guestbook form>div{align-items:stretch;flex-direction:column;gap:9px}.guestbook button{width:100%}}@media(prefers-reduced-motion:reduce){.choice{transition:none}}
</style>
