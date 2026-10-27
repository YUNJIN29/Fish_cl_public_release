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
      <div class="guestbook-title"><small>GUESTBOOK</small><h2 id="guestbook-title">留下一句话</h2><p>无需登录，提交后立即公开；默认昵称为“匿名用户”。</p></div>
      <form @submit.prevent="submitMessage">
        <label>昵称<input v-model="author" maxlength="32" autocomplete="nickname" placeholder="匿名用户" :readonly="Boolean(auth.me)" :aria-readonly="Boolean(auth.me)"></label>
        <label>留言<textarea ref="messageTextarea" v-model="draftMessage" maxlength="500" rows="4" placeholder="写下建议、问候或想法…" required /></label>
        <div class="expression-tools">
          <button type="button" class="expression-toggle" :aria-expanded="showExpressions" aria-controls="guestbook-expressions" @click="showExpressions = !showExpressions">😊 表情 / 颜文字</button>
          <div v-if="showExpressions" id="guestbook-expressions" class="expression-panel" aria-label="选择表情或颜文字">
            <div class="expression-tabs" role="tablist" aria-label="表情分类"><button v-for="group in expressionGroups" :key="group.name" type="button" role="tab" :aria-selected="activeExpressionGroup === group.name" :class="{ active: activeExpressionGroup === group.name }" @click="activeExpressionGroup = group.name">{{ group.name }}</button></div>
            <div class="expression-grid"><button v-for="expression in activeExpressions" :key="expression" type="button" :title="`插入 ${expression}`" :aria-label="`插入 ${expression}`" @click="insertExpression(expression)">{{ expression }}</button></div>
          </div>
        </div>
        <div><span>{{ draftMessage.length }} / 500</span><button type="submit" :disabled="submitting || !draftMessage.trim()">{{ submitting ? '提交中…' : '提交留言 →' }}</button></div>
        <p v-if="notice" class="notice" role="status">{{ notice }}</p>
      </form>
      <div class="messages" aria-live="polite">
        <article v-for="message in messages" :key="message.id"><header><b>{{ message.author }}</b><time :datetime="message.createdAt">{{ formatDate(message.createdAt) }}</time></header><p>{{ message.content }}</p><blockquote v-if="message.reply"><b>Aquatic Lab 回复</b>{{ message.reply }}</blockquote><div v-if="canManageGuestbook" class="manage-actions"><button type="button" @click="openEditor(message, 'content')">编辑</button><button type="button" @click="openEditor(message, 'reply')">{{ message.reply ? '修改回复' : '回复' }}</button><button type="button" class="danger" @click="removeMessage(message)">删除</button></div><form v-if="editingId === message.id" class="inline-editor" @submit.prevent="saveMessage(message)"><label>{{ editingMode === 'content' ? '编辑留言' : 'Aquatic Lab 回复' }}<textarea v-model="editingText" maxlength="500" rows="3" required /></label><div><button type="button" class="cancel" @click="editingId = null">取消</button><button type="submit">保存</button></div></form></article>
        <p v-if="!loadingMessages && !messages.length" class="empty">暂时还没有公开留言，欢迎写下第一句。</p>
        <p v-if="loadingMessages" class="empty">正在读取留言…</p>
      </div>
    </section>
    <section v-if="canManageGuestbook" class="visitor-ranking" aria-labelledby="visitor-ranking-title">
      <div class="ranking-heading"><div><small>VISITORS</small><h2 id="visitor-ranking-title">本月访客排行榜</h2></div><button type="button" :disabled="loadingVisitors" @click="loadVisitors">刷新列表</button></div>
      <p>按 IP 汇总本月访问次数，仅管理员可见。</p>
      <p v-if="loadingVisitors" role="status">正在读取访客…</p>
      <p v-else-if="visitorError" role="alert">{{ visitorError }}</p>
      <div v-else-if="visitors.length" class="ranking-table"><table><thead><tr><th scope="col">排名</th><th scope="col">访客 IP</th><th scope="col">地区</th><th scope="col">访问次数</th></tr></thead><tbody><tr v-for="(visitor, index) in visitors" :key="visitor.ipAddress"><td>{{ index + 1 }}</td><td>{{ visitor.ipAddress }}</td><td>{{ visitor.location }}</td><td>{{ visitor.visitCount }}</td></tr></tbody></table></div>
      <p v-else>本月暂无访客记录。</p>
    </section>
    <div v-if="pendingDelete" class="confirm-overlay" role="presentation" @click.self="closeDeleteConfirm">
      <section class="confirm-dialog" role="alertdialog" aria-modal="true" aria-labelledby="delete-title" aria-describedby="delete-description" @keydown.esc="closeDeleteConfirm">
        <span class="confirm-icon" aria-hidden="true">!</span>
        <h2 id="delete-title">删除留言</h2>
        <p id="delete-description">确定删除“{{ pendingDelete.author }}”的这条留言吗？删除后无法恢复。</p>
        <div><button type="button" class="cancel" @click="closeDeleteConfirm">取消</button><button ref="deleteConfirmButton" type="button" class="danger-confirm" @click="confirmDelete">确认删除</button></div>
      </section>
    </div>
    <footer><div class="footer-line"><span>© 2026 Aquatic Disease Knowledge Platform</span></div><p>公开版本已移除个人联系方式、备案信息、服务器地址和私有部署资料。AI 诊断仅供辅助参考，不能替代现场专业判断。</p></footer>
  </main>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { http } from '@/utils/http'
import { useAuthStore } from '@/stores/auth'

type GuestbookMessage = { id: number; author: string; content: string; reply?: string | null; createdAt: string }
const spaces = [
  { index: '01', icon: '✦', label: 'TOOLS SPACE', title: '学习工具', description: '学习记录、休闲小游戏，以及更多实验性功能。', action: '进入工具空间', to: '/personal', className: 'personal' },
  { index: '02', icon: '◈', label: 'AQUACULTURE INTELLIGENCE', title: '水产智诊平台', description: '鱼类病害诊断、知识图谱与水产知识服务。', action: '进入鱼类平台', to: '/fish', className: 'fish' },
  { index: '03', icon: '◎', label: 'PROJECT OVERVIEW', title: '项目展示', description: '公开脱敏版本的项目能力、模块结构与技术说明。', action: '查看项目展示', to: '/profile', className: 'profile' },
]
const stats = reactive({ todayVisitors: '—' as number | string, monthVisitors: '—' as number | string, totalVisitors: '—' as number | string })
const author = ref('匿名用户')
const draftMessage = ref('')
const messageTextarea = ref<HTMLTextAreaElement | null>(null)
const showExpressions = ref(false)
const expressionGroups = [
  { name: '常用表情', items: ['😀', '😊', '😂', '🥰', '😍', '😎', '🤔', '😭', '👍', '👏', '🎉', '✨', '❤️', '🐟', '🌊', '💡'] },
  { name: '颜文字', items: ['(＾▽＾)', '(≧▽≦)', '(๑•̀ㅂ•́)و✧', 'ヾ(≧▽≦*)o', '(｡･ω･｡)', '(づ｡◕‿‿◕｡)づ', '╰(*°▽°*)╯', 'φ(゜▽゜*)♪', '(￣▽￣)ノ', '(ง •_•)ง'] },
]
const activeExpressionGroup = ref(expressionGroups[0].name)
const activeExpressions = computed(() => expressionGroups.find(group => group.name === activeExpressionGroup.value)?.items || [])
const messages = ref<GuestbookMessage[]>([])
const loadingMessages = ref(true)
const submitting = ref(false)
const notice = ref('')
const auth = useAuthStore()
const canManageGuestbook = computed(() => {
  const me = auth.me
  if (!me) return false
  return me.roles?.includes('ADMIN')
})
const editingId = ref<number | null>(null)
type VisitorRanking = { ipAddress: string; location: string; visitCount: number }
const visitors = ref<VisitorRanking[]>([])
const loadingVisitors = ref(false)
const visitorError = ref('')
let visitorRequest = 0
async function loadVisitors() {
  if (!canManageGuestbook.value) return
  const request = ++visitorRequest
  loadingVisitors.value = true
  visitorError.value = ''
  try {
    const data = await http.get<VisitorRanking[]>('/api/visits/ranking')
    if (request === visitorRequest && canManageGuestbook.value) visitors.value = data
  } catch {
    if (request === visitorRequest) visitorError.value = '访客列表读取失败，请点击刷新重试。'
  } finally {
    if (request === visitorRequest) loadingVisitors.value = false
  }
}
watch(() => auth.me, () => {
  visitorRequest++
  visitors.value = []
  visitorError.value = ''
  loadingVisitors.value = false
  if (canManageGuestbook.value) void loadVisitors()
}, { immediate: true })
const editingMode = ref<'content' | 'reply'>('content')
const editingText = ref('')
const pendingDelete = ref<GuestbookMessage | null>(null)
const deleteConfirmButton = ref<HTMLButtonElement | null>(null)
watch(() => auth.me, (me) => { author.value = me ? (me.nickname?.trim() || me.username?.trim() || '匿名用户') : '匿名用户' }, { immediate: true })
async function insertExpression(expression: string) { const textarea = messageTextarea.value; const start = textarea?.selectionStart ?? draftMessage.value.length; const end = textarea?.selectionEnd ?? start; const next = `${draftMessage.value.slice(0, start)}${expression}${draftMessage.value.slice(end)}`; if (next.length > 500) return; draftMessage.value = next; await nextTick(); textarea?.focus(); textarea?.setSelectionRange(start + expression.length, start + expression.length) }
async function readEnvelope<T>(url: string, options?: RequestInit): Promise<T | null> { try { const response = await fetch(url, { cache: 'no-store', ...options }); const payload = await response.json(); return response.ok && payload?.success ? payload.data as T : null } catch { return null } }
async function loadMessages() { const data = await readEnvelope<any>('/api/guestbook?page=0&size=8'); messages.value = data?.content || []; loadingMessages.value = false }
async function submitMessage() { if (!draftMessage.value.trim() || submitting.value) return; submitting.value = true; notice.value = ''; try { await http.post('/api/guestbook', { author: author.value.trim() || '匿名用户', content: draftMessage.value.trim() }); draftMessage.value = ''; notice.value = '留言已发布。'; await loadMessages() } finally { submitting.value = false } }
function openEditor(message: GuestbookMessage, mode: 'content' | 'reply') { editingId.value = message.id; editingMode.value = mode; editingText.value = mode === 'content' ? message.content : message.reply || '' }
async function saveMessage(message: GuestbookMessage) { if (!editingText.value.trim()) return; await http.patch(`/api/admin/guestbook/${message.id}`, editingMode.value === 'content' ? { content: editingText.value.trim() } : { reply: editingText.value.trim() }); editingId.value = null; notice.value = '留言已保存。'; await loadMessages() }
async function removeMessage(message: GuestbookMessage) { pendingDelete.value = message; await nextTick(); deleteConfirmButton.value?.focus() }
function closeDeleteConfirm() { pendingDelete.value = null }
async function confirmDelete() { const message = pendingDelete.value; if (!message) return; await http.delete(`/api/admin/guestbook/${message.id}`); pendingDelete.value = null; editingId.value = null; notice.value = '留言已删除。'; await loadMessages() }
function formatDate(value: string) { return new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' }).format(new Date(value)) }
onMounted(async () => { const summary = await readEnvelope<typeof stats>('/api/visits/summary'); if (summary) Object.assign(stats, summary); await loadMessages(); const key = `aquatic-visit-${new Date().toISOString().slice(0, 10)}`; if (!sessionStorage.getItem(key)) { const tracked = await readEnvelope<typeof stats>('/api/visits/track', { method: 'POST' }); if (tracked) Object.assign(stats, tracked); sessionStorage.setItem(key, '1') } })
</script>

<style scoped>
.portal{min-height:100vh;position:relative;overflow:hidden;background:#f4f7fc;color:#10233e;padding:0 clamp(22px,5vw,80px);font-family:system-ui,-apple-system,BlinkMacSystemFont,sans-serif}.glow{position:absolute;width:500px;height:500px;border-radius:50%;filter:blur(120px);opacity:.09;pointer-events:none}.a{background:#00d4ff;top:-250px;right:-100px}.b{background:#7b6cff;bottom:-300px;left:-120px}.portal>header{height:74px;display:flex;align-items:center;position:relative;z-index:1}.portal>header a{min-height:44px;display:flex;align-items:center;gap:12px;color:#60758e;text-decoration:none;font-size:12px;letter-spacing:3px}.portal>header b{width:34px;height:34px;border:1px solid #00a4d4;color:#009bc9;display:grid;place-items:center;border-radius:9px;background:#fff}.intro{text-align:center;padding:clamp(20px,4vh,46px) 0 28px}.intro small,.guestbook-title small{letter-spacing:4px;color:#009dcc}.intro h1{font-size:clamp(38px,5vw,62px);margin:12px 0 8px}.intro p,.choice p,.guestbook-title p{color:#657b94}.choices{display:grid;grid-template-columns:repeat(3,1fr);gap:20px;max-width:1380px;margin:auto}.choice{height:310px;position:relative;padding:32px;border:1px solid #dbe5f1;border-radius:20px;background:#fff;box-shadow:0 10px 35px rgba(35,75,120,.07);cursor:pointer;transition:.3s;color:#10233e;text-decoration:none}.choice:hover,.choice:focus-visible{transform:translateY(-6px);border-color:var(--accent);box-shadow:0 22px 55px color-mix(in srgb,var(--accent) 14%,transparent);outline:none}.personal{--accent:#7667ed}.fish{--accent:#00a4d4}.profile{--accent:#e08b2f}.choice em{position:absolute;right:26px;top:20px;color:#dce7f3;font-size:48px;font-weight:800;font-style:normal}.choice>i{width:48px;height:48px;border:1px solid var(--accent);color:var(--accent);border-radius:12px;display:grid;place-items:center;font-size:23px;font-style:normal}.choice>div{position:absolute;left:32px;right:28px;bottom:27px}.choice small{font-size:9px;letter-spacing:2px;color:var(--accent)}h2{font-size:28px;margin:7px 0}.choice p{min-height:48px;margin:0}.choice strong{display:inline-block;padding:10px 0;color:var(--accent);font-size:14px}.visit-stats{display:flex;justify-content:center;align-items:center;gap:28px;margin:27px auto 38px}.visit-stats div{text-align:center;min-width:100px}.visit-stats b{display:block;font-size:25px;color:#087eb4}.visit-stats span{font-size:12px;color:#71869d}.visit-stats i{height:34px;width:1px;background:#d7e2ee}.guestbook{max-width:1080px;margin:0 auto 36px;padding:30px;display:grid;grid-template-columns:.7fr 1fr;gap:26px 38px;border:1px solid #dbe5f1;border-radius:20px;background:#fff;box-shadow:0 10px 35px rgba(35,75,120,.07)}.guestbook-title h2{font-size:30px}.guestbook-title p{line-height:1.7}.guestbook form{display:grid;gap:11px;min-width:0}.guestbook label{display:grid;gap:6px;min-width:0;color:#526b86;font-size:12px;font-weight:700}.guestbook input,.guestbook textarea{display:block;width:100%;min-width:0;padding:11px 13px;border:1px solid #d5e1ee;border-radius:9px;background:#f8fafd;color:#10233e;font:inherit;resize:vertical}.guestbook input[readonly]{color:#49627d;background:#eef4fa;cursor:default}.guestbook input:focus,.guestbook textarea:focus{outline:2px solid #00a4d4;outline-offset:1px}.guestbook form>div{display:flex;align-items:center;justify-content:space-between}.guestbook form>div span{color:#8497ab;font-size:11px}.guestbook button{min-height:44px;padding:0 18px;border:0;border-radius:9px;background:#087eb4;color:#fff;font-weight:700;cursor:pointer}.guestbook button:disabled{opacity:.5;cursor:not-allowed}.guestbook .expression-tools{position:relative;display:block}.guestbook .expression-toggle{min-height:36px;padding:0 12px;background:#edf5fb;color:#087eb4;font-size:12px}.expression-panel{position:absolute;z-index:10;left:0;top:42px;width:min(430px,100%);padding:12px;border:1px solid #d7e3ef;border-radius:12px;background:#fff;box-shadow:0 14px 38px rgba(35,75,120,.18)}.expression-tabs{display:flex;gap:6px;margin-bottom:10px}.expression-tabs button{min-height:32px;padding:0 10px;background:#f1f5fa;color:#657b94;font-size:12px}.expression-tabs button.active{background:#087eb4;color:#fff}.expression-grid{display:flex;flex-wrap:wrap;justify-content:flex-start!important;gap:6px}.expression-grid button{min-width:42px;min-height:42px;padding:5px 8px;background:#f7f9fd;color:#10233e;font-size:18px;font-weight:400}.expression-grid button:hover,.expression-grid button:focus-visible{background:#e6f4fb;outline:2px solid #00a4d4;outline-offset:1px}.notice{margin:0;color:#087151;font-size:13px}.messages{grid-column:1/-1;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px;padding-top:22px;border-top:1px solid #e2eaf3}.messages article{min-width:0;padding:17px;border:1px solid #e1e9f2;border-radius:12px;background:#f8fafd}.messages header{height:auto;display:flex;align-items:flex-start;justify-content:space-between;gap:12px}.messages header b{min-width:0;color:#087eb4;overflow-wrap:anywhere}.messages time{flex:0 0 auto;color:#8799ac;font-size:11px}.messages article>p{margin:12px 0 0;color:#3e5873;line-height:1.7;white-space:pre-wrap;overflow-wrap:anywhere}.messages blockquote{display:grid;gap:4px;margin:12px 0 0;padding:10px 12px;border-left:3px solid #7667ed;background:#f0efff;color:#65708b;font-size:13px}.messages blockquote b{color:#6657d7}.empty{grid-column:1/-1;text-align:center;color:#8497ab}footer{text-align:center;padding:18px 0 30px;color:#52677f;font-size:13px}.footer-line{display:flex;justify-content:center;gap:14px;flex-wrap:wrap}.footer-line a{min-height:44px;display:inline-flex;align-items:center;color:#58718e;text-decoration:none}footer p{max-width:920px;margin:4px auto 0;color:#60758e;line-height:1.7}.confirm-overlay{position:fixed;inset:0;z-index:10000;display:grid;place-items:center;padding:20px;background:rgba(16,35,62,.42);backdrop-filter:blur(3px)}.confirm-dialog{width:min(420px,100%);padding:28px;border:1px solid #d7e3ef;border-radius:18px;background:#fff;text-align:center;box-shadow:0 24px 70px rgba(16,35,62,.24)}.confirm-icon{width:44px;height:44px;margin:0 auto 12px;display:grid;place-items:center;border-radius:50%;background:#fff0f2;color:#c23d52;font-size:24px;font-weight:800}.confirm-dialog h2{margin:0 0 10px;font-size:22px}.confirm-dialog p{margin:0;color:#61758d;line-height:1.7}.confirm-dialog>div{display:flex;justify-content:center;gap:10px;margin-top:22px}.confirm-dialog button{min-width:108px;min-height:44px;border:0;border-radius:9px;font-weight:700;cursor:pointer}.confirm-dialog .cancel{background:#edf2f7;color:#52677f}.confirm-dialog .danger-confirm{background:#c23d52;color:#fff}@media(max-width:1050px){.choices{grid-template-columns:repeat(2,1fr)}.profile{grid-column:1/-1}.profile.choice{height:260px}}@media(max-width:720px){.portal{padding:0 16px}.portal>header{height:60px}.choices,.guestbook{grid-template-columns:1fr}.profile{grid-column:auto}.choice,.profile.choice{height:270px;padding:25px}.choice>div{left:25px;bottom:22px}.visit-stats{gap:10px}.visit-stats div{min-width:82px}.visit-stats b{font-size:21px}.guestbook{padding:20px}.messages{grid-template-columns:1fr}.guestbook form>div{align-items:stretch;flex-direction:column;gap:9px}.guestbook .expression-tools{display:block}.guestbook .expression-toggle{width:auto}.expression-panel{position:relative;top:auto;margin-top:7px;width:100%}.expression-tabs{flex-direction:row!important}.expression-grid{flex-direction:row!important}.guestbook button{width:100%}.expression-tabs button,.expression-grid button{width:auto}.confirm-dialog>div{flex-direction:column-reverse}.confirm-dialog button{width:100%}}@media(prefers-reduced-motion:reduce){.choice{transition:none}}
</style>
<style scoped>
.portal.portal{display:flex;flex-direction:column}
.choices,.guestbook{width:100%}
footer{margin-top:auto}
.footer-line{align-items:center}
.footer-line>span{min-height:44px;display:inline-flex;align-items:center}
.manage-actions{display:flex;gap:7px;margin-top:14px}
.manage-actions button{min-height:34px;padding:0 11px;background:#edf5fb;color:#087eb4}
.manage-actions .danger{background:#fff0f2;color:#c23d52}
.inline-editor{margin-top:12px;padding-top:12px;border-top:1px solid #dbe5f1}
.inline-editor>div{justify-content:flex-end!important;gap:8px}
.inline-editor .cancel{background:#edf1f5;color:#52677f}
.visitor-ranking{width:100%;max-width:1080px;margin:0 auto 36px;padding:30px;border:1px solid #dbe5f1;border-radius:20px;background:#fff;box-shadow:0 10px 35px rgba(35,75,120,.07)}
.ranking-heading{display:flex;align-items:center;justify-content:space-between;gap:16px}.ranking-heading small{letter-spacing:4px;color:#009dcc}.ranking-heading h2{font-size:26px}.ranking-heading button{min-height:44px;padding:0 14px;border:0;border-radius:9px;background:#edf5fb;color:#087eb4;cursor:pointer}.ranking-heading button:disabled{opacity:.5;cursor:wait}.visitor-ranking>p{color:#657b94;line-height:1.7}.ranking-table{overflow-x:auto}.ranking-table table{width:100%;border-collapse:collapse;text-align:left;font-size:14px}.ranking-table th,.ranking-table td{padding:13px 10px;border-bottom:1px solid #e2eaf3;overflow-wrap:anywhere}.ranking-table th{color:#526b86;background:#f8fafd}.ranking-table td:nth-child(2){font-family:monospace}@media(max-width:720px){.visitor-ranking{padding:20px}.ranking-heading h2{font-size:21px}.ranking-table th,.ranking-table td{padding:10px 6px;font-size:12px}}
</style>
