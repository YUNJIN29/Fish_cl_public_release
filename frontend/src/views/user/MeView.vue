<template>
  <div class="me-page">
    <template v-if="auth.me">
      <section class="profile-hero">
        <div class="identity">
          <el-avatar :size="84" :src="auth.me.avatarUrl || ''" class="hero-avatar">
            {{ initials }}
          </el-avatar>
          <div class="identity-copy">
            <div class="eyebrow">PERSONAL CENTER</div>
            <h1>{{ auth.me.nickname || auth.me.username }}</h1>
            <div class="identity-meta">
              <span>@{{ auth.me.username }}</span>
              <span>{{ roleText }}</span>
              <span>{{ joinedText }}</span>
            </div>
            <div class="identity-tags">
              <el-tag v-if="auth.me.mainSpecies" effect="dark">{{ auth.me.mainSpecies }}</el-tag>
              <el-tag v-if="auth.me.farmLocation" type="success" effect="plain">{{ auth.me.farmLocation }}</el-tag>
              <el-tag v-if="auth.me.farmName" type="warning" effect="plain">{{ auth.me.farmName }}</el-tag>
            </div>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" :icon="FirstAidKit" @click="router.push('/fish/diagnose')">开始诊断</el-button>
          <el-button :icon="Edit" @click="activeTab = 'profile'">编辑资料</el-button>
          <el-button type="danger" plain :icon="SwitchButton" @click="logout">退出登录</el-button>
        </div>
      </section>

      <el-tabs v-model="activeTab" class="center-tabs">
        <el-tab-pane label="数据总览" name="overview">
          <div v-loading="overviewLoading" class="overview-panel">
            <template v-if="overview">
              <div class="insight-row">
                <section class="completion-panel">
                  <el-progress type="dashboard" :percentage="overview.profileCompletion" :width="118" :stroke-width="12" />
                  <div class="completion-copy">
                    <h2>资料完整度</h2>
                    <p>{{ completionAdvice }}</p>
                    <div v-if="missingFields.length" class="missing-fields">
                      <span v-for="field in missingFields" :key="field">{{ field }}</span>
                    </div>
                    <el-button size="small" type="primary" plain @click="activeTab = 'profile'">继续完善</el-button>
                  </div>
                </section>

                <section class="health-panel">
                  <div class="panel-title">使用建议</div>
                  <p>{{ nextSuggestion }}</p>
                  <div class="suggestion-actions">
                    <el-button size="small" :icon="DocumentChecked" @click="activeTab = 'history'">复看历史</el-button>
                    <el-button size="small" :icon="ArrowRight" @click="router.push('/fish/rag')">RAG 检索</el-button>
                  </div>
                </section>
              </div>

              <div class="stats-grid">
                <button v-for="item in statCards" :key="item.label" class="stat-card" type="button" @click="item.action">
                  <span class="stat-icon" :class="item.className">
                    <component :is="item.icon" />
                  </span>
                  <span class="stat-value">{{ item.value }}</span>
                  <span class="stat-label">{{ item.label }}</span>
                </button>
              </div>

              <div class="quick-grid">
                <button type="button" @click="router.push('/fish/diagnose')"><FirstAidKit /><span>继续诊断</span></button>
                <button type="button" @click="router.push('/fish/orders')"><ShoppingCart /><span>查看订单</span></button>
                <button type="button" @click="router.push('/fish/posts')"><ChatDotRound /><span>交流发帖</span></button>
                <button type="button" @click="router.push('/fish/diseases')"><DocumentChecked /><span>病害知识库</span></button>
              </div>

              <div class="overview-sections">
                <section class="overview-section">
                  <div class="section-head">
                    <div><span>最近诊断</span><small>快速回看问诊结论</small></div>
                    <el-button text type="primary" @click="activeTab = 'history'">全部</el-button>
                  </div>
                  <div v-if="overview.recentDiagnoses.length" class="mini-list">
                    <button v-for="item in overview.recentDiagnoses" :key="item.id" class="mini-item" type="button" @click="showDetail(item)">
                      <span class="mini-title">{{ summarize(item.description) }}</span>
                      <span class="mini-meta">{{ item.species || '未填写鱼种' }} · {{ formatDate(item.createdAt) }}</span>
                    </button>
                  </div>
                  <div v-else class="empty-state">
                    <FirstAidKit />
                    <p>还没有诊断记录</p>
                    <el-button type="primary" size="small" @click="router.push('/fish/diagnose')">开始第一次诊断</el-button>
                  </div>
                </section>

                <section class="overview-section">
                  <div class="section-head">
                    <div><span>最近订单</span><small>查看蓝脉优选购买记录</small></div>
                    <el-button text type="primary" @click="router.push('/fish/orders')">全部</el-button>
                  </div>
                  <div v-if="overview.recentOrders.length" class="mini-list">
                    <button v-for="item in overview.recentOrders" :key="item.id" class="mini-item" type="button" @click="router.push('/fish/orders')">
                      <span class="mini-title">{{ item.orderNo }} · {{ formatMoney(item.totalCents) }}</span>
                      <span class="mini-meta">{{ statusText(item.status) }} · {{ formatDate(item.createdAt) }}</span>
                    </button>
                  </div>
                  <div v-else class="empty-state">
                    <ShoppingCart />
                    <p>暂无订单</p>
                    <el-button size="small" @click="router.push('/fish/products')">去蓝脉优选</el-button>
                  </div>
                </section>

                <section class="overview-section">
                  <div class="section-head">
                    <div><span>社区动态</span><small>你的交流与经验沉淀</small></div>
                    <el-button text type="primary" @click="router.push('/fish/posts')">全部</el-button>
                  </div>
                  <div v-if="overview.recentPosts.length" class="mini-list">
                    <button v-for="item in overview.recentPosts" :key="item.id" class="mini-item" type="button" @click="router.push('/fish/posts')">
                      <span class="mini-title">{{ item.title }}</span>
                      <span class="mini-meta">{{ formatDate(item.createdAt) }}</span>
                    </button>
                  </div>
                  <div v-else class="empty-state">
                    <ChatDotRound />
                    <p>还没发布内容</p>
                    <el-button size="small" @click="router.push('/fish/posts')">发布交流帖</el-button>
                  </div>
                </section>
              </div>
            </template>
            <el-empty v-else description="暂无个人数据">
              <el-button type="primary" :icon="Refresh" @click="loadOverview">重新加载</el-button>
            </el-empty>
          </div>
        </el-tab-pane>

        <el-tab-pane label="个人资料" name="profile">
          <div class="profile-panel">
            <aside class="profile-preview">
              <el-avatar :size="96" :src="profileForm.avatarUrl || ''">
                {{ profileForm.nickname?.slice(0, 1) || initials }}
              </el-avatar>
              <h2>{{ profileForm.nickname || auth.me.username }}</h2>
              <p>{{ profileForm.bio || '完善简介后，这里会展示你的养殖经验和关注方向。' }}</p>
              <div class="contact-list">
                <span><Location />{{ profileForm.farmLocation || '未填写地区' }}</span>
                <span><Picture />{{ profileForm.mainSpecies || '未填写养殖品种' }}</span>
                <span><Message />{{ profileForm.email || '未填写邮箱' }}</span>
                <span><Phone />{{ profileForm.phone || '未填写电话' }}</span>
              </div>
            </aside>

            <el-form :model="profileForm" label-position="top" class="profile-form">
              <div class="form-section-title">基础资料</div>
              <el-row :gutter="16">
                <el-col :xs="24" :sm="12"><el-form-item label="昵称"><el-input v-model="profileForm.nickname" maxlength="32" show-word-limit /></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="头像 URL"><el-input v-model="profileForm.avatarUrl" placeholder="https://..." /></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="邮箱"><el-input v-model="profileForm.email" placeholder="用于接收诊断资料" /></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="电话"><el-input v-model="profileForm.phone" placeholder="选填" /></el-form-item></el-col>
              </el-row>

              <div class="form-section-title">养殖信息</div>
              <el-row :gutter="16">
                <el-col :xs="24" :sm="12"><el-form-item label="养殖场/店铺"><el-input v-model="profileForm.farmName" placeholder="例如：云锦水产基地" /></el-form-item></el-col>
                <el-col :xs="24" :sm="12"><el-form-item label="所在地区"><el-input v-model="profileForm.farmLocation" placeholder="省/市/县" /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="主要养殖品种"><el-input v-model="profileForm.mainSpecies" placeholder="草鱼、锦鲤、对虾、加州鲈..." /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="个人简介"><el-input v-model="profileForm.bio" type="textarea" :rows="4" maxlength="512" show-word-limit /></el-form-item></el-col>
              </el-row>
              <div class="profile-actions">
                <el-button @click="syncProfileForm">恢复当前资料</el-button>
                <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
              </div>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="诊断历史" name="history">
          <div v-loading="loading" class="history-panel">
            <div class="history-toolbar">
              <div><h2>诊断历史</h2><p>按时间复盘症状、水质和 AI 建议，便于连续观察。</p></div>
              <el-button type="primary" :icon="FirstAidKit" @click="router.push('/fish/diagnose')">再诊断一次</el-button>
            </div>

            <div v-if="history.length > 0" class="history-list">
              <button v-for="item in history" :key="item.id" class="history-item" type="button" @click="showDetail(item)">
                <el-image v-if="item.imageUrl" :src="item.imageUrl" fit="cover" class="history-thumb" />
                <div v-else class="history-placeholder"><DocumentChecked /></div>
                <div class="history-main">
                  <div class="history-title-row">
                    <strong>{{ summarize(item.description) }}</strong>
                    <span>{{ formatDate(item.createdAt) }}</span>
                  </div>
                  <div class="history-tags">
                    <span v-if="item.species">{{ item.species }}</span>
                    <span v-if="item.temperature">水温 {{ item.temperature }}°C</span>
                    <span v-if="item.duration">发病 {{ item.duration }}</span>
                    <span v-if="item.waterQuality">水质 {{ item.waterQuality }}</span>
                  </div>
                </div>
              </button>
            </div>
            <div v-else class="large-empty">
              <FirstAidKit />
              <h3>暂无诊断记录</h3>
              <p>完成一次 AI 鱼病诊断后，记录会自动归档到这里。</p>
              <el-button type="primary" @click="router.push('/fish/diagnose')">去诊断</el-button>
            </div>

            <div class="pagination" v-if="total > size">
              <el-pagination small layout="prev, pager, next" :total="total" :page-size="size" v-model:current-page="page" @current-change="loadHistory" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <el-dialog v-model="detailVisible" title="诊断报告回顾" width="80%" class="diagnose-dialog">
        <div v-if="current" class="record-detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="诊断对象">{{ current.species || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="水温环境">{{ current.temperature ? current.temperature + '°C' : '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="发病时长">{{ current.duration || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ formatDate(current.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="水质情况" :span="2">{{ current.waterQuality || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="用户描述" :span="2">{{ current.description }}</el-descriptions-item>
          </el-descriptions>

          <div v-if="current.imageUrl" class="image-review">
            <div class="result-title">病灶图片</div>
            <el-image :src="current.imageUrl" fit="contain" class="detail-image" :preview-src-list="[current.imageUrl]" />
          </div>

          <div class="result-box">
            <div class="result-title">AI 诊断建议</div>
            <div class="result-content" v-html="formatContent(current.result)"></div>
          </div>

          <div class="detail-actions">
            <el-button @click="detailVisible = false">关闭</el-button>
            <el-button type="primary" @click="diagnoseAgain">再诊断一次</el-button>
          </div>
        </div>
      </el-dialog>
    </template>

    <div v-else class="large-empty">
      <UserFilled />
      <h3>请先登录</h3>
      <p>登录后可以管理资料、查看诊断历史和个人数据。</p>
      <el-button type="primary" @click="router.push('/login')">去登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { http } from '@/utils/http'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  ChatDotRound,
  DocumentChecked,
  Edit,
  FirstAidKit,
  Location,
  Message,
  Phone,
  Picture,
  Refresh,
  ShoppingCart,
  Star,
  SwitchButton,
  UserFilled,
} from '@element-plus/icons-vue'

type Overview = {
  profileCompletion: number
  stats: {
    diagnoseCount: number
    orderCount: number
    postCount: number
    favoriteCount: number
  }
  recentDiagnoses: any[]
  recentOrders: any[]
  recentPosts: any[]
}

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const activeTab = ref('overview')
const overview = ref<Overview | null>(null)
const overviewLoading = ref(false)
const history = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = 10

const detailVisible = ref(false)
const current = ref<any>(null)
const savingProfile = ref(false)
const profileForm = ref({
  nickname: '',
  avatarUrl: '',
  email: '',
  phone: '',
  farmName: '',
  farmLocation: '',
  mainSpecies: '',
  bio: '',
})

const initials = computed(() => (auth.me?.nickname || auth.me?.username || 'U').slice(0, 1).toUpperCase())
const roleText = computed(() => auth.me?.roles?.includes('ADMIN') ? '管理员' : '普通用户')
const joinedText = computed(() => auth.me?.createdAt ? `加入于 ${new Date(auth.me.createdAt).toLocaleDateString()}` : '已登录用户')

const missingFields = computed(() => {
  const fields = [
    ['头像', profileForm.value.avatarUrl],
    ['邮箱', profileForm.value.email],
    ['电话', profileForm.value.phone],
    ['养殖场', profileForm.value.farmName],
    ['地区', profileForm.value.farmLocation],
    ['品种', profileForm.value.mainSpecies],
    ['简介', profileForm.value.bio],
  ]
  return fields.filter(([, value]) => !value).map(([label]) => label)
})

const completionAdvice = computed(() => {
  const percent = overview.value?.profileCompletion || 0
  if (percent >= 90) return '资料已经很完整，后续 AI 诊断可以更好结合你的养殖背景。'
  if (percent >= 60) return '再补充地区、品种或联系方式，个人中心会更像一份完整档案。'
  return '建议先补齐养殖品种、地区和联系方式，方便诊断历史长期复盘。'
})

const nextSuggestion = computed(() => {
  const stats = overview.value?.stats
  if (!stats || stats.diagnoseCount === 0) return '可以先完成一次 AI 鱼病诊断，系统会自动沉淀为历史记录。'
  if (stats.postCount === 0) return '你已经有诊断记录，可以把典型问题发布到社区，方便后续交流。'
  return '你的数据已经开始积累，建议定期复看诊断记录和水质变化。'
})

const statCards = computed(() => {
  const stats = overview.value?.stats || { diagnoseCount: 0, orderCount: 0, postCount: 0, favoriteCount: 0 }
  return [
    { label: '诊断次数', value: stats.diagnoseCount, icon: FirstAidKit, className: 'cyan', action: () => activeTab.value = 'history' },
    { label: '订单数量', value: stats.orderCount, icon: ShoppingCart, className: 'green', action: () => router.push('/fish/orders') },
    { label: '发帖数量', value: stats.postCount, icon: ChatDotRound, className: 'violet', action: () => router.push('/fish/posts') },
    { label: '收藏数量', value: stats.favoriteCount, icon: Star, className: 'amber', action: () => router.push('/fish/articles') },
  ]
})

async function loadOverview() {
  if (!auth.me) return
  overviewLoading.value = true
  try {
    overview.value = await http.get<Overview>('/api/personal/overview')
  } finally {
    overviewLoading.value = false
  }
}

function syncProfileForm() {
  if (!auth.me) return
  profileForm.value = {
    nickname: auth.me.nickname || '',
    avatarUrl: auth.me.avatarUrl || '',
    email: auth.me.email || '',
    phone: auth.me.phone || '',
    farmName: auth.me.farmName || '',
    farmLocation: auth.me.farmLocation || '',
    mainSpecies: auth.me.mainSpecies || '',
    bio: auth.me.bio || '',
  }
}

async function saveProfile() {
  savingProfile.value = true
  try {
    await auth.updateProfile(profileForm.value)
    syncProfileForm()
    await loadOverview()
    ElMessage.success('个人资料已保存')
  } finally {
    savingProfile.value = false
  }
}

function logout() {
  auth.logout()
  router.push('/fish')
}

async function loadHistory() {
  if (!auth.me) return
  loading.value = true
  try {
    const res = await http.get<any>('/api/diagnose/history', { page: page.value - 1 })
    history.value = res.content
    total.value = res.totalElements
  } finally {
    loading.value = false
  }
}

function showDetail(item: any) {
  current.value = item
  detailVisible.value = true
}

function summarize(text?: string) {
  const raw = text || '图片诊断'
  const plain = raw.replace(/\s+/g, ' ').trim()
  return plain.length > 90 ? plain.slice(0, 90) + '...' : plain
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString() : '未知时间'
}

function formatMoney(cents?: number) {
  return `￥${((cents || 0) / 100).toFixed(2)}`
}

function statusText(status?: string) {
  const map: Record<string, string> = {
    PENDING: '待处理',
    PAID: '已支付',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return status ? (map[status] || status) : '未知状态'
}

function formatContent(text: string): string {
  if (!text) return ''
  let html = escapeHtml(text)
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\n/g, '<br>')
  const products = [
    { name: '复合黄粉', id: 1 },
    { name: '水霉特效杀菌剂', id: 2 },
    { name: '高活性硝化细菌', id: 3 },
    { name: '硝化细菌', id: 3 },
    { name: '增红高蛋白饲料', id: 4 },
    { name: '增氧泵', id: 5 },
  ]
  products.forEach(p => {
    const reg = new RegExp(p.name, 'g')
    html = html.replace(reg, `<a href="/fish/products/${p.id}" style="color:var(--c-cyan);font-weight:700"><u>${p.name}</u></a>`)
  })
  return html
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function diagnoseAgain() {
  detailVisible.value = false
  router.push('/fish/diagnose')
}

watch(activeTab, (val) => {
  if (val === 'overview' && !overview.value) loadOverview()
  if (val === 'history' && history.value.length === 0) loadHistory()
})

watch(() => auth.me, () => {
  syncProfileForm()
  if (auth.me && !overview.value) loadOverview()
}, { immediate: true })

onMounted(() => {
  syncProfileForm()
  if (route.query.tab === 'history') activeTab.value = 'history'
  if (route.query.tab === 'profile') activeTab.value = 'profile'
  if (activeTab.value === 'overview') loadOverview()
  if (activeTab.value === 'history') loadHistory()
})
</script>

<style scoped>
.me-page {
  max-width: 1160px;
  margin: 0 auto;
}
.profile-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 22px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: linear-gradient(120deg, rgba(0, 180, 216, 0.14), transparent 42%), var(--c-bg-card);
}
.identity {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}
.hero-avatar {
  flex-shrink: 0;
  border: 2px solid rgba(0, 212, 255, 0.45);
}
.identity-copy {
  min-width: 0;
}
.eyebrow {
  color: var(--c-cyan);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 2px;
}
.identity h1 {
  margin: 6px 0 8px;
  color: var(--c-text);
  font-size: 30px;
  line-height: 1.15;
}
.identity-meta,
.identity-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  color: var(--c-text-3);
  font-size: 13px;
}
.identity-tags {
  margin-top: 10px;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  flex-shrink: 0;
}
.center-tabs {
  margin-top: 16px;
}
.overview-panel {
  min-height: 390px;
}
.insight-row {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(260px, 0.9fr);
  gap: 14px;
}
.completion-panel,
.health-panel,
.overview-section,
.profile-preview,
.profile-form {
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-bg-card);
}
.completion-panel {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 18px;
}
.completion-copy h2,
.history-toolbar h2,
.profile-preview h2 {
  margin: 0;
  color: var(--c-text);
}
.completion-copy p,
.health-panel p,
.history-toolbar p,
.profile-preview p {
  color: var(--c-text-3);
  line-height: 1.7;
}
.missing-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 10px 0 12px;
}
.missing-fields span,
.history-tags span {
  padding: 3px 8px;
  border-radius: 4px;
  border: 1px solid var(--c-border);
  background: var(--c-bg-soft);
  color: var(--c-text-3);
  font-size: 12px;
}
.health-panel {
  padding: 18px;
}
.panel-title {
  color: var(--c-text);
  font-weight: 800;
}
.suggestion-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}
.stat-card,
.quick-grid button,
.mini-item,
.history-item {
  border: 1px solid var(--c-border);
  background: var(--c-bg-card);
  color: inherit;
  cursor: pointer;
  transition: border-color 0.2s, transform 0.2s, background 0.2s;
}
.stat-card:hover,
.quick-grid button:hover,
.mini-item:hover,
.history-item:hover {
  border-color: var(--c-cyan);
  transform: translateY(-1px);
}
.stat-card {
  min-height: 112px;
  padding: 14px;
  border-radius: 8px;
  text-align: left;
}
.stat-icon {
  width: 34px;
  height: 34px;
  display: inline-grid;
  place-items: center;
  border-radius: 8px;
  margin-bottom: 12px;
}
.stat-icon svg,
.quick-grid svg,
.empty-state svg,
.large-empty svg,
.history-placeholder svg,
.contact-list svg {
  width: 18px;
  height: 18px;
}
.stat-icon.cyan { color: #009bd6; background: rgba(0, 155, 214, 0.12); }
.stat-icon.green { color: #1f9d55; background: rgba(31, 157, 85, 0.13); }
.stat-icon.violet { color: #7357d8; background: rgba(115, 87, 216, 0.13); }
.stat-icon.amber { color: #c87900; background: rgba(200, 121, 0, 0.14); }
.stat-value {
  display: block;
  color: var(--c-text);
  font-size: 28px;
  line-height: 1;
  font-weight: 900;
}
.stat-label {
  display: block;
  margin-top: 8px;
  color: var(--c-text-3);
  font-size: 13px;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin: 14px 0;
}
.quick-grid button {
  min-height: 52px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--c-text-2);
  font-weight: 700;
}
.overview-sections {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}
.overview-section {
  min-width: 0;
  padding: 14px;
}
.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}
.section-head span {
  display: block;
  color: var(--c-text);
  font-weight: 800;
}
.section-head small {
  display: block;
  margin-top: 4px;
  color: var(--c-text-3);
  font-size: 12px;
}
.mini-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.mini-item {
  width: 100%;
  min-height: 62px;
  padding: 10px;
  border-radius: 7px;
  text-align: left;
}
.mini-title,
.mini-meta {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mini-title {
  color: var(--c-text-2);
  font-size: 14px;
  font-weight: 700;
}
.mini-meta {
  margin-top: 7px;
  color: var(--c-text-3);
  font-size: 12px;
}
.empty-state,
.large-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 10px;
  color: var(--c-text-3);
}
.empty-state {
  min-height: 165px;
}
.large-empty {
  min-height: 360px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-bg-card);
  padding: 28px;
}
.large-empty svg {
  width: 42px;
  height: 42px;
  color: var(--c-cyan);
}
.large-empty h3 {
  margin: 0;
  color: var(--c-text);
}
.profile-panel {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
}
.profile-preview {
  align-self: start;
  padding: 22px;
  text-align: center;
}
.profile-preview p {
  margin: 10px 0 16px;
  text-align: left;
}
.contact-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: left;
}
.contact-list span {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  color: var(--c-text-2);
  font-size: 13px;
  overflow-wrap: anywhere;
}
.contact-list svg {
  color: var(--c-cyan);
  flex-shrink: 0;
}
.profile-form {
  padding: 20px;
}
.form-section-title {
  margin: 2px 0 14px;
  color: var(--c-text);
  font-weight: 800;
}
.form-section-title:not(:first-child) {
  margin-top: 10px;
}
.profile-actions,
.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.history-panel {
  min-height: 330px;
}
.history-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}
.history-toolbar p {
  margin: 6px 0 0;
}
.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.history-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border-radius: 8px;
  text-align: left;
}
.history-thumb,
.history-placeholder {
  width: 68px;
  height: 68px;
  border-radius: 7px;
  flex-shrink: 0;
}
.history-placeholder {
  display: grid;
  place-items: center;
  color: var(--c-cyan);
  background: var(--c-bg-soft);
  border: 1px solid var(--c-border);
}
.history-main {
  min-width: 0;
  flex: 1;
}
.history-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.history-title-row strong {
  color: var(--c-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.history-title-row span {
  color: var(--c-text-3);
  font-size: 12px;
  flex-shrink: 0;
}
.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 10px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.result-box {
  margin-top: 16px;
  padding: 14px;
  background: var(--c-bg-soft);
  border-radius: 8px;
}
.image-review {
  margin-top: 16px;
}
.detail-image {
  width: 100%;
  max-height: 360px;
  border-radius: 8px;
  border: 1px solid var(--c-border);
  background: var(--c-bg-soft);
}
.result-title {
  font-weight: 800;
  margin-bottom: 8px;
  color: var(--c-purple);
}
.result-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--c-text);
}
@media (max-width: 980px) {
  .profile-hero,
  .identity {
    align-items: flex-start;
  }
  .profile-hero {
    flex-direction: column;
  }
  .hero-actions {
    justify-content: flex-start;
  }
  .insight-row,
  .profile-panel {
    grid-template-columns: 1fr;
  }
  .stats-grid,
  .quick-grid,
  .overview-sections {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 640px) {
  .profile-hero {
    padding: 16px;
  }
  .identity {
    flex-direction: column;
  }
  .identity h1 {
    font-size: 24px;
  }
  .hero-actions,
  .completion-panel,
  .history-toolbar {
    align-items: stretch;
    flex-direction: column;
  }
  .stats-grid,
  .quick-grid,
  .overview-sections {
    grid-template-columns: 1fr;
  }
  .history-item {
    align-items: flex-start;
  }
  .history-title-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }
  .history-title-row strong {
    white-space: normal;
  }
}
</style>
