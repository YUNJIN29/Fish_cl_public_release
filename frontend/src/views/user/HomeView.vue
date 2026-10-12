<template>
  <div class="home-page">
    <!-- 头部 Slogan -->
    <div class="platform-hero">
      <p class="hero-sub">AI 智能诊断 · RAG 知识检索 · 知识图谱可视化 · 水产优选商城</p>
      <div class="hero-desc">
        面向水产养殖户、观赏鱼爱好者和水族店，解决“识病难、查药难、处理难、买药难”四大痛点，
        构建从 AI 识病到水产优选服务的完整闭环。
      </div>
    </div>

    <!-- 第一部分：AI 智能诊断 -->
    <section class="home-section" id="ai-diagnose">
      <el-row :gutter="16">
        <el-col :xs="24" :lg="17">
          <el-card shadow="never" class="chat-card">
            <template #header>
              <div class="chat-hdr">
                <span class="chat-title">🤖 AI 鱼病智能诊断 (首选入口)</span>
                <span class="chat-sub">描述鱼的症状，AI 为您分析病因并给出治疗方案</span>
              </div>
            </template>

            <!-- 消息列表 -->
            <div class="chat-messages" ref="msgBox">
              <div v-if="messages.length === 0" class="welcome-wrap">
                <div class="welcome-icon">🐟</div>
                <div class="welcome-title">您好，我是水产病害诊断助手</div>
                <div class="welcome-desc">请描述您观察到的鱼类症状，例如体色变化、行为异常、体表病灶等，我将为您提供专业的诊断建议。</div>
                <div class="quick-questions">
                  <el-button
                    v-for="q in quickQuestions"
                    :key="q"
                    size="small"
                    round
                    @click="sendQuick(q)"
                  >{{ q }}</el-button>
                </div>
              </div>

              <div v-for="msg in messages" :key="msg.id" :class="['msg-row', msg.role]">
                <div class="avatar-wrap">
                  <div :class="['avatar', msg.role]">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
                </div>
                <div :class="['bubble', msg.role]">
                  <div v-if="msg.role === 'assistant' && msg.loading" class="loading-dots">
                    <span /><span /><span />
                  </div>
                  <div v-else class="msg-content" v-html="formatContent(msg.content)" />
                </div>
              </div>
            </div>

            <!-- 输入区 -->
            <div class="input-area">
              <div v-if="imagePreview" class="img-preview-wrap">
                <el-image :src="imagePreview" fit="contain" class="img-preview" />
                <el-button circle size="small" class="remove-img" @click="clearImage">×</el-button>
              </div>

              <div class="input-row">
                <el-input
                  v-model="inputText"
                  type="textarea"
                  :rows="3"
                  placeholder="描述症状，例如：鱼体表出现白色小点，游动异常，食欲下降…"
                  resize="none"
                  @keydown.enter.ctrl.prevent="sendMessage"
                />
                <div class="input-actions">
                  <el-tooltip content="上传鱼类图片（可选）" placement="top">
                    <el-button :icon="Picture" circle @click="triggerUpload" />
                  </el-tooltip>
                  <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />
                  <el-button
                    type="primary"
                    :loading="sending"
                    :disabled="!inputText.trim() && !imagePreview"
                    @click="sendMessage"
                  >
                    发送 (Ctrl+↵)
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧辅助面板 -->
        <el-col :xs="24" :lg="7">
          <el-card shadow="never" class="tip-card">
            <template #header><span class="tip-title">如何描述症状</span></template>
            <ul class="tip-list">
              <li v-for="tip in tips" :key="tip">{{ tip }}</li>
            </ul>
          </el-card>

          <el-card shadow="never" class="tip-card" style="margin-top:16px">
            <template #header><span class="tip-title">常见症状速查</span></template>
            <div class="symptom-tags">
              <el-tag
                v-for="s in commonSymptoms"
                :key="s"
                size="small"
                type="info"
                effect="plain"
                class="symptom-tag"
                @click="appendSymptom(s)"
              >{{ s }}</el-tag>
            </div>
          </el-card>

          <el-card shadow="never" class="tip-card" style="margin-top:16px">
            <template #header><span class="tip-title">核心功能直达</span></template>
            <div class="quick-nav">
              <el-button type="primary" style="width:100%" @click="$router.push('/fish/diseases')">病害知识库</el-button>
              <el-button style="width:100%;margin-top:10px" @click="$router.push('/fish/rag')">RAG 知识检索</el-button>
              <el-button style="width:100%;margin-top:10px" @click="$router.push('/fish/knowledge-graph')">知识图谱全屏</el-button>
              <el-button style="width:100%;margin-top:10px" @click="$router.push('/fish/calculator')">水体用量计算器</el-button>
              <el-button style="width:100%;margin-top:10px" @click="$router.push('/fish/water-quality')">记录水质日志</el-button>
              <el-button style="width:100%;margin-top:10px" @click="$router.push('/fish/tasks')">安排养殖待办</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 第二部分：知识图谱（按需进入，避免首页加载大型图谱运行时） -->
    <section class="home-section" id="knowledge-graph">
      <div class="graph-cta"><div><small>KNOWLEDGE GRAPH</small><h2>把病害、症状与处理方案连成一张图</h2><p>图谱模块体积较大，仅在进入探索页后加载，以保持首页轻快。</p></div><el-button type="primary" size="large" @click="$router.push('/fish/knowledge-graph')">进入图谱探索 →</el-button></div>
    </section>

    <!-- 第三部分：科普与商城推荐 -->
    <section class="home-section" id="resources">
      <el-row :gutter="16">
        <el-col :xs="24" :lg="16">
          <el-card shadow="never">
            <template #header>
              <div class="section-hdr">
                <div class="section-title">科普资讯</div>
                <el-button link type="primary" @click="$router.push('/fish/articles')">更多</el-button>
              </div>
            </template>
            <div class="article-grid">
              <el-card v-for="a in articles" :key="a.id" shadow="hover" class="article-mini-card" @click="$router.push(`/fish/articles/${a.id}`)">
                <el-image v-if="a.coverImageUrl" :src="toImg(a.coverImageUrl)" fit="cover" class="article-cover" />
                <div class="article-info">
                  <div class="article-title">{{ a.title }}</div>
                  <div class="article-time">{{ formatDate(a.createdAt) }}</div>
                </div>
              </el-card>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="8">
          <el-card shadow="never">
            <template #header>
              <div class="section-hdr">
                <div class="section-title">推荐商品</div>
                <el-button link type="primary" @click="$router.push('/fish/products')">去选鱼</el-button>
              </div>
            </template>
            <div class="mini-prod-list">
              <div v-for="p in products.slice(0, 4)" :key="p.id" class="mini-prod-item" @click="$router.push(`/fish/products/${p.id}`)">
                <el-image :src="toImg(p.coverImageUrl)" class="mini-prod-img" />
                <div class="mini-prod-info">
                  <div class="mini-prod-name">{{ p.name }}</div>
                  <div class="mini-prod-price">{{ formatPrice(p.priceCents) }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { http } from '@/utils/http'
import { Picture } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

// --- 数据类型 ---
type Message = { id: number; role: 'user' | 'assistant'; content: string; loading?: boolean }
type Article = { id: number; title: string; createdAt?: string; coverImageUrl?: string | null }
type ProductItem = { id: number; name: string; priceCents: number; coverImageUrl?: string | null }
type DiagnosisResult = { report: string }

// --- AI 诊断状态 ---
const messages = ref<Message[]>([])
const inputText = ref('')
const sending = ref(false)
const msgBox = ref<HTMLElement>()
const fileInput = ref<HTMLInputElement>()
const imagePreview = ref<string | null>(null)
const imageBase64 = ref<string | null>(null)

const quickQuestions = [
  '鱼体表出现白色小点怎么办？',
  '鱼尾部发白腐烂是什么病？',
  '如何预防草鱼出血病？',
]
const tips = [
  '描述体色变化（如充血、发白）',
  '说明体表症状（如溃疡、白点）',
  '描述异常行为（如浮头、打转）',
  '提供品种与水温信息',
]
const commonSymptoms = [
  '体表白点', '烂鳍', '竖鳞', '腹部膨大', '眼球突出', '食欲减退', '充血出血', '浮头',
]

// --- 科普与商品数据 ---
const articles = ref<Article[]>([])
const products = ref<ProductItem[]>([])

// --- 方法 ---
const toImg = (url?: string | null) => url ? url : '/uploads/banner_1.svg'
const formatPrice = (cents: number) => `¥${(cents / 100).toFixed(2)}`
const formatDate = (v?: string) => v ? new Date(v).toLocaleDateString() : ''

function formatContent(text: string): string {
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/(\d+\.) /g, '<br>$1 ')
}

function appendSymptom(s: string) {
  inputText.value = inputText.value ? inputText.value + '，' + s : s
}

function sendQuick(q: string) {
  inputText.value = q
  sendMessage()
}

function triggerUpload() { fileInput.value?.click() }

function onFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    const result = ev.target?.result as string
    imagePreview.value = result
    imageBase64.value = result.split(',')[1]
  }
  reader.readAsDataURL(file)
}

function clearImage() {
  imagePreview.value = null
  imageBase64.value = null
  if (fileInput.value) fileInput.value.value = ''
}

async function scrollToBottom() {
  await nextTick()
  if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
}

async function sendMessage() {
  if (!auth.token) {
    await router.push({ name: 'login', query: { redirect: '/fish/diagnose' } })
    return
  }
  const text = inputText.value.trim()
  if (!text && !imagePreview.value) return
  if (sending.value) return

  const userMsg: Message = { id: Date.now(), role: 'user', content: text || '[已上传图片]' }
  messages.value.push(userMsg)
  inputText.value = ''
  await scrollToBottom()

  const aiMsg: Message = { id: Date.now() + 1, role: 'assistant', content: '', loading: true }
  messages.value.push(aiMsg)
  await scrollToBottom()

  sending.value = true
  const img = imageBase64.value
  clearImage()

  try {
    const result = await http.post<DiagnosisResult>('/api/diagnose', {
      description: text,
      imageBase64: img,
    })
    aiMsg.content = result?.report || '暂时无法获取诊断结果，请稍后重试。'
  } catch {
    aiMsg.content = 'AI 服务暂时不可用，您可以查看病害知识库获取参考信息。'
  } finally {
    aiMsg.loading = false
    sending.value = false
    await scrollToBottom()
  }
}

async function loadData() {
  const [aRes, pRes] = await Promise.all([
    http.get<any>('/api/articles', { size: 4 }),
    http.get<any>('/api/products', { size: 4 })
  ])
  articles.value = aRes?.content || []
  products.value = pRes?.content || []
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-page { padding-bottom: 40px; }

/* Hero */
.platform-hero {
  text-align: center; padding: 40px 0; margin-bottom: 20px;
}
.hero-title {
  font-size: 32px; font-weight: 900; margin-bottom: 12px;
  background: linear-gradient(135deg, var(--c-cyan), #a8edff, var(--c-purple));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
.hero-sub { font-size: 16px; color: var(--c-text-2); letter-spacing: 1px; }

/* Sections */
.home-section { margin-bottom: 32px; }
.graph-cta{min-height:260px;display:flex;align-items:center;justify-content:space-between;gap:30px;padding:clamp(28px,5vw,64px);border:1px solid var(--c-border);border-radius:14px;background:radial-gradient(circle at 80% 20%,var(--c-cyan-dim),transparent 42%),var(--c-bg-card)}.graph-cta small{color:var(--c-cyan);letter-spacing:3px}.graph-cta h2{max-width:680px;font-size:clamp(26px,4vw,48px);margin:14px 0;color:var(--c-text)}.graph-cta p{color:var(--c-text-2)}
.section-hdr { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.section-title { font-size: 18px; font-weight: 700; color: var(--c-text); }
.section-sub { font-size: 13px; color: var(--c-text-2); flex: 1; }

/* Chat Card (same as DiagnoseView but adapted) */
.chat-card { border: 1px solid var(--c-border); }
.chat-title { font-weight: 800; color: var(--c-cyan); font-size: 16px; }
.chat-sub { font-size: 12px; color: var(--c-text-3); margin-left: 8px; }
.chat-messages {
  min-height: 400px; max-height: 500px;
  overflow-y: auto; padding: 10px;
}
.welcome-wrap {
  display: flex; flex-direction: column; align-items: center;
  padding: 40px 20px; text-align: center;
}
.welcome-icon { font-size: 52px; margin-bottom: 14px; }
.welcome-title { font-size: 18px; font-weight: 700; margin-bottom: 8px; }
.welcome-desc { color: var(--c-text-2); max-width: 480px; margin-bottom: 20px; font-size: 14px; }
.quick-questions { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }

.msg-row { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 16px; }
.msg-row.user { flex-direction: row-reverse; }
.avatar {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700;
}
.avatar.user { background: var(--c-cyan); color: #000; }
.avatar.assistant { background: var(--c-purple); color: #fff; }
.bubble { max-width: 80%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.6; }
.bubble.user { background: var(--c-cyan-dim); color: var(--c-text); border-radius: 12px 2px 12px 12px; }
.bubble.assistant { background: var(--c-bg-card2); border: 1px solid var(--c-border); border-radius: 2px 12px 12px 12px; }

.loading-dots { display: flex; gap: 4px; }
.loading-dots span { width: 4px; height: 4px; border-radius: 50%; background: var(--c-cyan); animation: dot-bounce 1.2s infinite; }
.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes dot-bounce { 0%, 80%, 100% { transform: scale(0.8); opacity: 0.3; } 40% { transform: scale(1.4); opacity: 1; } }

.input-area { border-top: 1px solid var(--c-border); padding-top: 12px; }
.input-row { display: flex; gap: 10px; align-items: flex-end; }
.input-actions { display: flex; flex-direction: column; gap: 8px; align-items: flex-end; }

/* Tip Cards */
.tip-card { border: 1px solid var(--c-border); }
.tip-title { font-weight: 700; font-size: 14px; }
.tip-list { padding-left: 18px; margin: 0; color: var(--c-text-2); font-size: 13px; line-height: 2; }
.symptom-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.symptom-tag { cursor: pointer; }

/* KG container */
.kg-container { background: var(--c-bg-card2); border-radius: 8px; overflow: hidden; }

/* Article Mini */
.article-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.article-mini-card { cursor: pointer; transition: all 0.2s; }
.article-mini-card:hover { transform: translateY(-3px); }
.article-cover { width: 100%; height: 100px; object-fit: cover; }
.article-info { padding: 10px; }
.article-title { font-size: 14px; font-weight: 700; margin-bottom: 4px; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.article-time { font-size: 11px; color: var(--c-text-3); }

/* Mini Product */
.mini-prod-list { display: flex; flex-direction: column; gap: 10px; }
.mini-prod-item { display: flex; gap: 12px; align-items: center; cursor: pointer; padding: 8px; border-radius: 6px; border: 1px solid transparent; transition: all 0.2s; }
.mini-prod-item:hover { background: var(--c-bg-soft); border-color: var(--c-border); }
.mini-prod-img { width: 50px; height: 50px; border-radius: 4px; object-fit: cover; }
.mini-prod-name { font-size: 13px; font-weight: 600; }
.mini-prod-price { font-size: 12px; color: var(--c-cyan); font-weight: 700; }
</style>
