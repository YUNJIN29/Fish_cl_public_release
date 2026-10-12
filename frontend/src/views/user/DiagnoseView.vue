<template>
  <div class="diagnose-wrap">
    <el-row :gutter="16">
      <!-- 聊天主区 -->
      <el-col :xs="24" :lg="17">
        <el-card shadow="never" class="chat-card">
          <template #header>
            <div class="chat-hdr">
              <span class="chat-title">AI 鱼病智能诊断</span>
              <span class="chat-sub">描述鱼的症状，AI 为您分析病因并给出治疗方案</span>
            </div>
          </template>

          <div class="safety-notice" role="note"><b>辅助判断，不替代现场诊断</b><span>如出现大量死亡、明显浮头、急性水质异常或强传染迹象，请立即增氧、隔离并联系水产技术人员或兽医。</span></div>

          <!-- 消息列表 -->
          <div class="chat-messages" ref="msgBox">
            <!-- 欢迎语 -->
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

            <!-- 消息气泡 -->
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
            <!-- 图片预览 -->
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

      <!-- 智能引导问诊区 (新功能) -->
      <el-card v-if="!sending && !diagnosisResult" shadow="never" class="guide-card" style="margin-bottom: 16px">
        <template #header>
          <div class="guide-header-wrap">
            <div class="guide-header">
              <el-icon><Guide /></el-icon>
              <span>智能问诊引导</span>
              <el-tag size="small" type="success" effect="plain" style="margin-left: 8px">模拟专家问诊</el-tag>
            </div>
            <el-button v-if="guideStep > 0" type="text" size="small" @click="resetGuide">重置问诊</el-button>
          </div>
        </template>
        
        <div class="guide-layout">
          <!-- 左侧问诊主区 -->
          <div class="guide-main">
            <div class="guide-history">
              <!-- 已完成的步骤列表 -->
              <div v-if="guideStep > 0" class="history-item">
                <div class="h-q">1. 养殖品种</div>
                <div class="h-a">
                  <el-tag size="small" closable @close="extraInfo.species='';guideStep=0">{{ extraInfo.species }}</el-tag>
                </div>
              </div>
              
              <div v-if="guideStep > 1" class="history-item">
                <div class="h-q">2. 核心症状</div>
                <div class="h-a">
                  <el-tag size="small" type="warning" closable @close="selectedInitialSymptom='';guideStep=1">{{ selectedInitialSymptom }}</el-tag>
                </div>
              </div>

              <div v-if="guideStep > 2" class="history-item">
                <div class="h-q">3. 专家追问</div>
                <div class="h-a-list">
                  <div v-for="(val, key) in consultAnswers" :key="key" class="h-a-sub">
                    <span class="h-a-q">{{ getQuestionLabel(key) }}:</span>
                    <el-tag size="mini" type="info" effect="plain">{{ val }}</el-tag>
                  </div>
                </div>
              </div>
            </div>

            <div class="guide-content">
              <div v-if="guideStep === 0" class="guide-step">
                <p class="guide-q">1. 请问您主要养殖的是什么品种？</p>
                <div class="guide-options">
                  <el-button v-for="opt in speciesOptions" :key="opt" size="small" :type="extraInfo.species === opt ? 'primary' : ''" @click="selectSpecies(opt)">{{ opt }}</el-button>
                  <el-input v-model="extraInfo.species" size="small" placeholder="其他品种..." style="width: 120px; margin-left: 8px" @change="guideStep = 1" />
                </div>
              </div>

              <div v-else-if="guideStep === 1" class="guide-step">
                <p class="guide-q">2. 鱼体目前最明显的异常症状是？</p>
                <div class="guide-options">
                  <el-button v-for="s in initialSymptoms" :key="s" size="small" plain @click="selectInitialSymptom(s)">{{ s }}</el-button>
                </div>
                <div class="guide-nav">
                  <el-button size="small" @click="guideStep--">上一步</el-button>
                  <el-button size="small" type="warning" plain @click="finishGuide">跳过引导直接诊断</el-button>
                </div>
              </div>

              <div v-else-if="guideStep === 2" class="guide-step">
                <p class="guide-q">3. 专家进一步追问：{{ currentQuestion.q }}</p>
                <div class="guide-options">
                  <el-button v-for="ans in currentQuestion.options" :key="ans" size="small" :type="consultAnswers[currentQuestion.key] === ans ? 'primary' : ''" @click="selectAnswer(currentQuestion.key, ans)">
                    {{ ans }}
                  </el-button>
                </div>
                <div class="guide-nav">
                  <el-button size="small" @click="prevQuestion">上一步</el-button>
                  <el-button size="small" type="warning" plain @click="finishGuide">停止问诊直接诊断</el-button>
                  <el-button size="small" type="primary" :disabled="!consultAnswers[currentQuestion.key]" @click="nextQuestion">下一题</el-button>
                </div>
              </div>

              <div v-else-if="guideStep === 3" class="guide-step">
                <p class="guide-q">4. 最后请确认环境信息：</p>
                <el-form label-width="80px" size="small">
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="当前水温">
                        <el-input v-model="extraInfo.temperature" placeholder="如: 25"><template #append>°C</template></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="发病时长">
                        <el-input v-model="extraInfo.duration" placeholder="如: 2天" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item label="水质描述">
                    <el-input v-model="extraInfo.waterQuality" placeholder="pH值、氨氮、近期是否换水等..." />
                  </el-form-item>
                </el-form>
                <div class="guide-nav">
                  <el-button size="small" @click="guideStep--">上一步</el-button>
                  <el-button size="small" type="success" @click="finishGuide">完成引导并诊断</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 诊断结果区 -->
      <el-col v-if="diagnosisResult" :span="24">
        <el-card shadow="never" class="result-actions-card">
          <div class="result-actions">
            <ProtectedImage
              v-if="diagnosisResult.imageUrl"
              :src="diagnosisResult.imageUrl"
              fit="cover"
              class="result-thumb"
              preview
              alt="本次诊断病灶图片"
            />
            <div>
              <div class="result-actions-title">诊断报告已生成</div>
              <div class="result-actions-sub">
                {{ diagnosisResult.saved ? '报告已保存到诊断历史。' : '登录后诊断报告会自动保存到个人历史。' }}
                {{ diagnosisResult.imageUrl ? '病灶图片已归档。' : '' }}可继续查看知识库证据或前往水产优选处理下一步。
              </div>
            </div>
            <div class="result-buttons">
              <el-button :icon="DocumentCopy" @click="copyCurrentReport">复制报告</el-button>
              <el-button :icon="Printer" @click="printCurrentReport">打印 / PDF</el-button>
              <el-button :icon="Clock" @click="goHistory">查看历史</el-button>
              <el-button type="success" :icon="Clock" @click="scheduleDiagnosisFollowUp">安排诊断复查</el-button>
              <el-button :icon="Collection" @click="$router.push('/fish/diseases')">病害知识库</el-button>
              <el-button type="primary" :icon="Goods" @click="$router.push('/fish/products')">推荐商品</el-button>
            </div>
          </div>

          <div v-if="diagnosisResult.relatedDiseases.length || diagnosisResult.relatedProducts.length || diagnosisResult.ragReferences.length" class="evidence-grid">
            <section v-if="diagnosisResult.relatedDiseases.length" class="evidence-section">
              <div class="evidence-title">关联病害知识</div>
              <div
                v-for="d in diagnosisResult.relatedDiseases"
                :key="d.id"
                class="evidence-item"
                @click="$router.push(`/fish/diseases/${d.id}`)"
              >
                <strong>{{ d.name }}</strong>
                <span>{{ d.diseaseType || '病害知识' }}</span>
                <p>{{ d.symptomsSummary || '查看病害详情与处理方案' }}</p>
              </div>
            </section>

            <section v-if="diagnosisResult.relatedProducts.length" class="evidence-section">
              <div class="evidence-title">推荐处理用品</div>
              <div
                v-for="p in diagnosisResult.relatedProducts"
                :key="p.id"
                class="evidence-item product"
                @click="$router.push(`/fish/products/${p.id}`)"
              >
                <strong>{{ p.name }}</strong>
                <span>{{ formatPrice(p.priceCents) }}</span>
                <p>库存 {{ p.stock }} 件，点击查看用法说明和下单。</p>
              </div>
            </section>

            <section v-if="diagnosisResult.ragReferences.length" class="evidence-section">
              <div class="evidence-title">RAG 引用资料</div>
              <div
                v-for="r in diagnosisResult.ragReferences"
                :key="r.id"
                class="evidence-item rag-ref"
                @click="openRagSource(r)"
              >
                <strong>{{ r.title }}</strong>
                <span>{{ r.sourceName || r.category || 'RAG 资料' }}</span>
                <p>{{ r.summary || r.snippet || '查看资料来源和检索片段' }}</p>
              </div>
            </section>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧提示面板 -->
      <el-col :xs="24" :lg="7">
        <el-card shadow="never" class="tip-card">
          <template #header><span class="tip-title">补充诊断信息 (推荐)</span></template>
          <div class="water-record-import">
            <label v-if="recentWaterRecords.length">
              <span>引用最近水质记录</span>
              <select v-model.number="selectedWaterRecordId" aria-label="引用最近水质记录" @change="applyWaterRecord">
                <option :value="0">请选择记录</option>
                <option v-for="record in recentWaterRecords" :key="record.id" :value="record.id">{{ record.pondName }} · {{ formatWaterRecordDate(record.observedAt) }}</option>
              </select>
              <small>选择后会填入水温和水质描述，提交前仍可修改。</small>
            </label>
            <div v-else-if="waterRecordsLoaded" class="water-record-empty"><span>还没有可引用的水质记录</span><el-button text size="small" @click="$router.push('/fish/water-quality')">前往记录</el-button></div>
          </div>
          <el-form label-position="top" size="small">
            <el-form-item label="鱼种类型">
              <el-input v-model="extraInfo.species" placeholder="如：锦鲤、对虾、加州鲈" />
            </el-form-item>
            <el-row :gutter="8">
              <el-col :span="12">
                <el-form-item label="水温 (°C)">
                  <el-input v-model="extraInfo.temperature" placeholder="25" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="发病时间">
                  <el-input v-model="extraInfo.duration" placeholder="2天" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="水质情况 (pH/氨氮/亚盐)">
              <el-input v-model="extraInfo.waterQuality" placeholder="pH 7.5, 氨氮正常" />
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="tip-card" style="margin-top:16px">
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
          <template #header><span class="tip-title">快速跳转</span></template>
          <div class="quick-nav">
            <el-button style="width:100%" @click="$router.push('/fish/diseases')">病害知识库</el-button>
            <el-button style="width:100%;margin-top:10px" @click="$router.push('/fish/knowledge-graph')">知识图谱</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, computed, onMounted } from 'vue'
import { http } from '@/utils/http'
import { Picture, Guide, Clock, Collection, Goods, DocumentCopy, Printer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ProtectedImage from '@/components/ProtectedImage.vue'
import { copyDiagnosisReport, printDiagnosisReport } from '@/utils/diagnosisReport'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

type Message = {
  id: number
  role: 'user' | 'assistant'
  content: string
  loading?: boolean
}

type RelatedDisease = {
  id: number
  name: string
  diseaseType?: string
  symptomsSummary?: string
}

type RelatedProduct = {
  id: number
  name: string
  priceCents: number
  stock: number
}

type RagReference = {
  id: number
  title: string
  sourceName?: string
  sourceUrl?: string
  category?: string
  summary?: string
  snippet?: string
}

type DiagnosisResult = {
  report: string
  saved: boolean
  imageUrl?: string | null
  relatedDiseases: RelatedDisease[]
  relatedProducts: RelatedProduct[]
  ragReferences: RagReference[]
  confidence?: string
  evidence?: string[]
  sources?: string[]
  warnings?: string[]
  nextSteps?: string[]
}

type WaterRecord = {
  id: number
  pondName: string
  temperature?: number | null
  ph?: number | null
  dissolvedOxygen?: number | null
  ammonia?: number | null
  nitrite?: number | null
  notes?: string | null
  observedAt: string
}

const messages = ref<Message[]>([])
const inputText = ref('')
const sending = ref(false)
const msgBox = ref<HTMLElement>()
const fileInput = ref<HTMLInputElement>()
const imagePreview = ref<string | null>(null)
const imageBase64 = ref<string | null>(null)
const imageMimeType = ref<string | null>(null)
const diagnosisResult = ref<DiagnosisResult | null>(null)
const recentWaterRecords = ref<WaterRecord[]>([])
const selectedWaterRecordId = ref(0)
const waterRecordsLoaded = ref(false)

const extraInfo = ref({
  species: '',
  temperature: '',
  duration: '',
  waterQuality: ''
})

async function loadRecentWaterRecords() {
  try {
    const data = await http.get<any>('/api/water-quality', { page: 0 })
    recentWaterRecords.value = data.content || []
    const requestedId = Number(Array.isArray(route.query.waterRecord) ? route.query.waterRecord[0] : route.query.waterRecord)
    if (Number.isSafeInteger(requestedId) && requestedId > 0) {
      let requested = recentWaterRecords.value.find(item => item.id === requestedId)
      if (!requested) {
        requested = await http.get<WaterRecord>(`/api/water-quality/${requestedId}`)
        recentWaterRecords.value.unshift(requested)
      }
      selectedWaterRecordId.value = requested.id
      applyWaterRecord()
    }
  } catch {
    recentWaterRecords.value = []
  } finally {
    waterRecordsLoaded.value = true
  }
}

function applyWaterRecord() {
  const record = recentWaterRecords.value.find(item => item.id === selectedWaterRecordId.value)
  if (!record) return
  if (record.temperature != null) extraInfo.value.temperature = String(record.temperature)
  const values = [
    `水体：${record.pondName}`,
    `测量时间：${formatWaterRecordDate(record.observedAt)}`,
    record.ph != null ? `pH ${record.ph}` : '',
    record.dissolvedOxygen != null ? `溶氧 ${record.dissolvedOxygen} mg/L` : '',
    record.ammonia != null ? `氨氮 ${record.ammonia} mg/L` : '',
    record.nitrite != null ? `亚硝酸盐 ${record.nitrite} mg/L` : '',
    record.notes ? `备注：${record.notes}` : '',
  ].filter(Boolean)
  extraInfo.value.waterQuality = values.join('；')
  ElMessage.success('已引用水质记录，请确认后提交诊断')
}

function formatWaterRecordDate(value: string) {
  return new Date(value).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(loadRecentWaterRecords)

// 智能引导问诊相关数据
const guideStep = ref(0)
const speciesOptions = ['锦鲤', '金鱼', '草鱼', '对虾', '加州鲈']
const initialSymptoms = [
  '鱼体发红/出血', '身上长白毛/白点', '浮头/呼吸急促', '不吃食/游动缓慢', '体表腐烂/掉鳞'
]

const consultQuestions: Record<string, any[]> = {
  '鱼体发红/出血': [
    { key: 'death', q: '近期死亡情况如何？', options: ['无死亡', '极个别(1-2尾)', '小规模(3-10尾)', '大规模暴发', '持续缓慢死亡'] },
    { key: 'red_pos', q: '红斑或出血主要部位？', options: ['鱼鳍基部', '鱼体腹部', '全身散发', '嘴角/头部', '鳃盖附近'] },
    { key: 'gill', q: '观察鳃部状态？', options: ['正常鲜红', '苍白贫血', '暗紫淤血', '发黑腐烂', '黏液包裹'] },
    { key: 'water', q: '近期水质状态？', options: ['清澈透明', '发绿浑浊', '发黄有臭味', '泡沫增多', '氨氮超标'] },
    { key: 'operation', q: '近期是否进行过操作？', options: ['大面积换水', '新鱼入缸', '搬运捞取', '加温/降温', '无特殊操作'] }
  ],
  '身上长白毛/白点': [
    { key: 'white_type', q: '白点/白毛形态特征？', options: ['细小如粉末', '明显白点', '棉絮状白毛', '云雾状白蒙', '粘液状白膜'] },
    { key: 'spread', q: '扩散速度如何？', options: ['仅局部一处', '缓慢扩散', '1-2天全身', '传染给全缸', '保持不变'] },
    { key: 'rub', q: '鱼只行为反馈？', options: ['频繁蹭缸', '身体抖动', '缩鳍不动', '狂游跳跃', '食欲废绝'] },
    { key: 'temp_change', q: '近期水温变化？', options: ['突然大幅降温', '水温持续偏低', '季节交替期', '加温过快', '温度稳定'] },
    { key: 'slime', q: '体表粘液情况？', options: ['粘液极多', '粘液脱落', '皮肤干燥', '局部充血', '基本正常'] }
  ],
  '浮头/呼吸急促': [
    { key: 'time', q: '浮头发生的规律？', options: ['凌晨/清晨', '阴雨天午后', '喂食后', '全天候', '开灯后'] },
    { key: 'action', q: '呼吸的具体表现？', options: ['鳃盖开合极快', '浮于水面吞气', '聚集在出水口', '侧卧水底', '游动无力'] },
    { key: 'oxygen', q: '溶氧设备状态？', options: ['正常开启', '气量不足', '停电断氧', '过滤器堵塞', '无增氧设备'] },
    { key: 'feed_status', q: '浮头时的摄食意愿？', options: ['依然抢食', '边吃边吐', '完全不吃', '缓慢进食', '吞咽困难'] },
    { key: 'weather', q: '近期天气情况？', options: ['连雨闷热', '气压突然降低', '烈日暴晒', '大风降温', '晴朗稳定'] }
  ],
  '腹部肿大/竖鳞': [
    { key: 'scale', q: '鳞片的状态描述？', options: ['全身鳞片竖起', '局部鳞片松动', '鳞片脱落出血', '鳞底有积液', '基本平整'] },
    { key: 'feces', q: '排泄物(粪便)特征？', options: ['白色长条拖便', '透明粘液便', '脓血便', '干结难排', '基本正常'] },
    { key: 'press', q: '腹部按压手感？', options: ['柔软有波动感', '坚硬有肿块', '明显凹陷', '无明显感觉', '不忍触碰'] },
    { key: 'eye_state', q: '伴随的眼部症状？', options: ['眼球明显凸出', '眼眶发红', '眼球凹陷', '白蒙浑浊', '基本正常'] },
    { key: 'feed_type', q: '近期投喂情况？', options: ['活饵料', '劣质饲料', '投喂过量', '长期未喂', '配方更换'] }
  ],
  '鳃部异常/黏液增多': [
    { key: 'gill_filament', q: '鳃丝的宏观形态？', options: ['鳃丝末端缺损', '鳃丝肿胀翻出', '鳃丝挂满污垢', '鳃丝发白腐烂', '鳃丝红肿'] },
    { key: 'mucus_type', q: '黏液的性质？', options: ['大量透明粘液', '黄褐色脓液', '血样粘液', '灰白色薄膜', '无明显粘液'] },
    { key: 'breath_sound', q: '观察到的呼吸频率？', options: ['急促沉重', '频率缓慢', '呼吸暂停间歇', '张口难闭', '完全正常'] },
    { key: 'gill_cover', q: '鳃盖的外观？', options: ['鳃盖张开', '鳃盖充血', '鳃盖凹陷', '鳃盖有洞', '基本正常'] },
    { key: 'parasite', q: '是否能看到异物？', options: ['有肉眼可见虫体', '有白色小点', '有黑色斑点', '有细沙感', '无可见异物'] }
  ]
}

const selectedInitialSymptom = ref('')
const questionIndex = ref(0)
const consultAnswers = ref<Record<string, string>>({})

const currentQuestion = computed(() => {
  const qs = consultQuestions[selectedInitialSymptom.value] || [
    { key: 'other', q: '近期是否进行过大面积换水或使用过药物？', options: ['是', '否'] }
  ]
  return qs[questionIndex.value] || qs[qs.length - 1]
})

function selectSpecies(s: string) {
  extraInfo.value.species = s
  guideStep.value = 1
}

function selectInitialSymptom(s: string) {
  selectedInitialSymptom.value = s
  inputText.value = `主要症状：${s}`
  guideStep.value = 2
  questionIndex.value = 0
}

function selectAnswer(key: string, val: string) {
  consultAnswers.value[key] = val
}

function prevQuestion() {
  if (questionIndex.value > 0) {
    questionIndex.value--
  } else {
    guideStep.value = 1
  }
}

function nextQuestion() {
  const qs = consultQuestions[selectedInitialSymptom.value] || [{ key: 'other' }]
  if (questionIndex.value < qs.length - 1) {
    questionIndex.value++
  } else {
    guideStep.value = 3
  }
}

function resetGuide() {
  guideStep.value = 0
  selectedInitialSymptom.value = ''
  consultAnswers.value = {}
  extraInfo.value = { species: '', temperature: '', duration: '', waterQuality: '' }
  selectedWaterRecordId.value = 0
}

function getQuestionLabel(key: string): string {
  const labels: Record<string, string> = {
    death: '死亡情况',
    red_pos: '出血部位',
    gill: '鳃部状态',
    water: '近期水质',
    operation: '近期操作',
    white_type: '白点形态',
    spread: '扩散速度',
    rub: '行为反馈',
    temp_change: '水温变化',
    slime: '体表粘液',
    time: '浮头规律',
    action: '呼吸表现',
    oxygen: '增氧状态',
    feed_status: '摄食意愿',
    weather: '近期天气',
    scale: '鳞片状态',
    feces: '粪便特征',
    press: '按压手感',
    eye_state: '眼部症状',
    feed_type: '投喂情况',
    gill_filament: '鳃丝形态',
    mucus_type: '黏液性质',
    breath_sound: '呼吸频率',
    gill_cover: '鳃盖外观',
    parasite: '可见异物',
    other: '补充说明'
  }
  return labels[key] || key
}

function finishGuide() {
  // 汇总问诊信息到描述中
  let summary = `【智能问诊报告】\n`
  summary += `- 养殖品种：${extraInfo.value.species || '未指定'}\n`
  summary += `- 核心症状：${selectedInitialSymptom.value}\n`
  
  // 遍历所有已回答的问题，获取问题文本和答案
  const qs = consultQuestions[selectedInitialSymptom.value] || [{ key: 'other', q: '近期是否进行过大面积换水或使用过药物？' }]
  qs.forEach(q => {
    const answer = consultAnswers.value[q.key]
    if (answer) {
      summary += `- ${q.q}：${answer}\n`
    }
  })

  inputText.value = summary
  sendMessage()
  guideStep.value = 0 // 重置问诊组件
  consultAnswers.value = {}
}

const quickQuestions = [
  '鱼体表出现白色小点怎么办？',
  '鱼尾部发白腐烂是什么病？',
  '鱼腹部膨大、鳞片竖立是什么原因？',
  '如何预防草鱼出血病？',
]

const tips = [
  '描述鱼的体色变化（如发黑、发白、充血）',
  '说明体表是否有溃疡、白点、黏液等',
  '描述鱼的行为异常（漂浮、打转、跳跃）',
  '提供鱼的品种、养殖方式、水温信息',
  '可上传鱼类病灶图片辅助诊断',
]

const commonSymptoms = [
  '体表白点', '烂鳍', '竖鳞', '腹部膨大', '体色发黑',
  '眼球突出', '食欲减退', '打转运动', '鳃丝苍白', '充血出血',
  '棉絮状物', '溃疡', '黏液增多', '浮头', '离群独游',
]

function formatContent(text: string): string {
  let html = escapeHtml(text)
  
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/(\d+\.) /g, '<br>$1 ')
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
    html = html.replace(reg, `<a href="/fish/products/${p.id}" class="product-link" target="_self"><u>${p.name}</u></a>`)
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

function formatPrice(cents: number) {
  return `￥${(Number(cents || 0) / 100).toFixed(2)}`
}

function goHistory() {
  router.push(auth.token ? '/personal/me?tab=history' : '/login?redirect=/personal/me%3Ftab%3Dhistory')
}

function scheduleDiagnosisFollowUp() {
  if (!diagnosisResult.value) return
  const result = diagnosisResult.value
  const species = extraInfo.value.species.trim()
  const reportSummary = result.report.replace(/[#*_>\r\n]+/g, ' ').replace(/\s+/g, ' ').trim().slice(0, 220)
  const steps = (result.nextSteps || []).slice(0, 3).join('；')
  const warnings = (result.warnings || []).slice(0, 2).join('；')
  const notes = [
    species ? `诊断对象：${species}` : '',
    reportSummary ? `报告摘要：${reportSummary}` : '',
    steps ? `建议步骤：${steps}` : '',
    warnings ? `风险提示：${warnings}` : '',
  ].filter(Boolean).join('。').slice(0, 500)
  router.push({
    path: '/fish/tasks',
    query: {
      source: 'diagnosis',
      title: `复查${species || '本次'}诊断情况`,
      category: 'MEDICATION',
      notes,
    },
  })
}

function currentPrintableReport() {
  const result = diagnosisResult.value!
  return { report: result.report, ...extraInfo.value, confidence: result.confidence, evidence: result.evidence, sources: result.sources, warnings: result.warnings, nextSteps: result.nextSteps, hasPrivateImage: Boolean(result.imageUrl) }
}
async function copyCurrentReport() { if (!diagnosisResult.value) return; await copyDiagnosisReport(currentPrintableReport()); ElMessage.success('诊断报告已复制') }
function printCurrentReport() { if (diagnosisResult.value && !printDiagnosisReport(currentPrintableReport())) ElMessage.warning('浏览器阻止了打印窗口，请允许本站打开新窗口') }

function openRagSource(item: RagReference) {
  if (item.sourceUrl && /^https?:\/\//.test(item.sourceUrl)) {
    window.open(item.sourceUrl, '_blank', 'noopener,noreferrer')
  } else {
    router.push('/fish/rag')
  }
}

function appendSymptom(s: string) {
  inputText.value = inputText.value
    ? inputText.value + '，' + s
    : s
}

function sendQuick(q: string) {
  inputText.value = q
  sendMessage()
}

function triggerUpload() {
  fileInput.value?.click()
}

function onFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  imageMimeType.value = file.type || 'image/jpeg'
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
  imageMimeType.value = null
  if (fileInput.value) fileInput.value.value = ''
}

async function scrollToBottom() {
  await nextTick()
  if (msgBox.value) {
    msgBox.value.scrollTop = msgBox.value.scrollHeight
  }
}

async function sendMessage() {
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
  const mime = imageMimeType.value
  
  // 构造增强的描述信息
  let fullDescription = text
  const { species, temperature, duration, waterQuality } = extraInfo.value
  if (species || temperature || duration || waterQuality) {
    fullDescription += '\n\n【补充信息】'
    if (species) fullDescription += `\n- 鱼种：${species}`
    if (temperature) fullDescription += `\n- 水温：${temperature}°C`
    if (duration) fullDescription += `\n- 发病时间：${duration}`
    if (waterQuality) fullDescription += `\n- 水质：${waterQuality}`
  }

  clearImage()

  try {
    const diagnosis = await http.post<DiagnosisResult>('/api/diagnose', {
      description: fullDescription,
      imageBase64: img,
      imageMimeType: mime,
      species,
      temperature,
      duration,
      waterQuality,
    })
    diagnosis.ragReferences = diagnosis.ragReferences || []
    diagnosis.relatedDiseases = diagnosis.relatedDiseases || []
    diagnosis.relatedProducts = diagnosis.relatedProducts || []
    diagnosisResult.value = diagnosis
    aiMsg.content = diagnosis?.report || '暂时无法获取诊断结果，请稍后重试。'
  } catch {
    aiMsg.content = 'AI 服务暂时不可用，您可以查看病害知识库获取参考信息。'
    diagnosisResult.value = null
  } finally {
    aiMsg.loading = false
    sending.value = false
    await scrollToBottom()
  }
}
</script>

<style scoped>
.safety-notice{display:flex;gap:10px;align-items:flex-start;margin:0 10px 12px;padding:12px 14px;border:1px solid rgba(255,171,64,.35);border-radius:8px;background:rgba(255,171,64,.08);font-size:12px;line-height:1.6}.safety-notice b{white-space:nowrap;color:var(--c-amber)}.safety-notice span{color:var(--c-text-2)}
.diagnose-wrap { padding-bottom: 20px; }

.result-actions-card {
  margin-top: 16px;
  border: 1px solid rgba(0,212,255,0.35);
  background: linear-gradient(135deg, rgba(0,212,255,0.08), rgba(123,108,255,0.06));
}

.result-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.result-thumb {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  border: 1px solid var(--c-border);
  flex-shrink: 0;
  background: var(--c-bg-soft);
}

.result-actions-title {
  font-size: 16px;
  font-weight: 800;
  color: var(--c-text);
}

.result-actions-sub {
  margin-top: 6px;
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.6;
}

.result-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.evidence-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px dashed var(--c-border);
}

.evidence-title {
  font-weight: 800;
  color: var(--c-text);
  margin-bottom: 10px;
}

.evidence-section {
  min-width: 0;
}

.evidence-item {
  padding: 12px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  background: var(--c-bg-card);
  cursor: pointer;
  transition: all 0.2s;
}

.evidence-item + .evidence-item {
  margin-top: 10px;
}

.evidence-item:hover {
  border-color: var(--c-cyan);
  background: var(--c-cyan-dim);
}

.evidence-item strong {
  display: block;
  color: var(--c-text);
  margin-bottom: 6px;
}

.evidence-item span {
  color: var(--c-cyan);
  font-size: 12px;
  font-weight: 700;
}

.evidence-item p {
  margin: 8px 0 0;
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.6;
}

/* 引导卡片 */
.guide-card {
  border: 1px solid var(--c-cyan-light);
  background: linear-gradient(to bottom, #ffffff, var(--c-bg-soft));
}
.guide-header-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.guide-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: var(--c-cyan);
}
.guide-layout {
  display: flex;
  gap: 24px;
}
.guide-main {
  flex: 1;
}
.guide-history {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--c-border-light);
}
.history-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  animation: slideIn 0.3s ease;
}
.h-q {
  font-size: 13px;
  font-weight: 700;
  color: var(--c-text-2);
  width: 70px;
  flex-shrink: 0;
  padding-top: 4px;
}
.h-a {
  flex: 1;
}
.h-a-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.h-a-sub {
  display: flex;
  align-items: center;
  gap: 4px;
  background: var(--c-bg-soft);
  padding: 2px 8px;
  border-radius: 4px;
}
.h-a-q {
  font-size: 12px;
  color: var(--c-text-2);
}
.guide-content {
  padding: 10px 0;
}
.guide-step {
  animation: fadeIn 0.4s ease;
}
.guide-q {
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--c-text);
}
.guide-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}
.guide-nav {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px dashed var(--c-border);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

/* 聊天卡片 */
.chat-hdr { display: flex; flex-direction: column; gap: 3px; }
.chat-title {
  font-size: 18px; font-weight: 800;
  background: linear-gradient(135deg, var(--c-cyan), var(--c-purple));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
.chat-sub { font-size: 13px; color: var(--c-text-2); }

/* 消息列表 */
.chat-messages {
  min-height: 420px; max-height: 520px;
  overflow-y: auto; padding: 8px 4px;
}
.welcome-wrap {
  display: flex; flex-direction: column; align-items: center;
  padding: 40px 20px; text-align: center;
}
.welcome-icon {
  font-size: 52px; margin-bottom: 14px;
  filter: drop-shadow(0 0 12px rgba(0,212,255,0.5));
}
.welcome-title { font-size: 18px; font-weight: 700; margin-bottom: 8px; color: var(--c-text); }
.welcome-desc { color: var(--c-text-2); max-width: 480px; line-height: 1.7; margin-bottom: 20px; }
.quick-questions { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }

/* 消息气泡 */
.msg-row { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 16px; }
.msg-row.user { flex-direction: row-reverse; }
.avatar {
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; flex-shrink: 0;
}
.avatar.user {
  background: linear-gradient(135deg, #0090b8, var(--c-cyan));
  color: #030c14;
  box-shadow: 0 0 10px rgba(0,212,255,0.4);
}
.avatar.assistant {
  background: linear-gradient(135deg, var(--c-purple), #8a2be2);
  color: white;
}

:deep(.product-link) {
  color: var(--c-cyan);
  font-weight: 700;
  text-decoration: none;
  border-bottom: 1px dashed var(--c-cyan);
  transition: all 0.3s;
}
:deep(.product-link:hover) {
  color: var(--c-purple);
  border-bottom-color: var(--c-purple);
  opacity: 0.8;
}
.bubble {
  max-width: 75%; padding: 12px 16px; border-radius: 12px;
  line-height: 1.7; font-size: 14px;
}
.bubble.user {
  background: linear-gradient(135deg, #005577, #007a9a);
  color: #e0f8ff;
  border-radius: 12px 2px 12px 12px;
  box-shadow: 0 2px 12px rgba(0,212,255,0.15);
}
.bubble.assistant {
  background: var(--c-bg-card2);
  color: var(--c-text);
  border: 1px solid var(--c-border);
  border-radius: 2px 12px 12px 12px;
}
.msg-content :deep(strong) { font-weight: 700; color: var(--c-cyan); }

/* Loading dots */
.loading-dots { display: flex; gap: 5px; align-items: center; height: 20px; }
.loading-dots span {
  width: 6px; height: 6px; border-radius: 50%;
  background: var(--c-cyan);
  animation: dot-bounce 1.2s infinite;
  opacity: 0.7;
}
.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.3; }
  40% { transform: scale(1.2); opacity: 1; }
}

/* 输入区 */
.input-area { border-top: 1px solid var(--c-border); padding-top: 12px; }
.img-preview-wrap { position: relative; display: inline-block; margin-bottom: 8px; }
.img-preview { width: 80px; height: 80px; border-radius: 6px; border: 1px solid var(--c-border); }
.remove-img {
  position: absolute; top: -6px; right: -6px;
  width: 20px !important; height: 20px !important; min-height: 20px;
  font-size: 12px; background: var(--c-red); color: white; border: none;
}
.input-row { display: flex; gap: 10px; align-items: flex-end; }
.input-actions { display: flex; flex-direction: column; gap: 8px; align-items: flex-end; flex-shrink: 0; }

/* 右侧面板 */
.tip-title { font-weight: 700; font-size: 14px; color: var(--c-text); }
.tip-list { padding-left: 18px; margin: 0; color: var(--c-text-2); font-size: 13px; line-height: 2.2; }
.tip-list li::marker { color: var(--c-cyan); }
.symptom-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.symptom-tag { cursor: pointer; transition: all 0.2s !important; }
.symptom-tag:hover {
  background: var(--c-cyan-dim) !important;
  border-color: var(--c-cyan) !important;
  color: var(--c-cyan) !important;
}
.quick-nav { display: flex; flex-direction: column; gap: 10px; }

@media (max-width: 900px) {
  .result-actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .result-buttons {
    justify-content: flex-start;
  }

  .evidence-grid {
    grid-template-columns: 1fr;
  }

  .input-row {
    flex-direction: column;
    align-items: stretch;
  }

  .input-actions {
    flex-direction: row;
    justify-content: flex-end;
  }
}
.water-record-import{margin-bottom:14px;padding:11px;border:1px solid var(--c-border);border-radius:8px;background:var(--c-bg-soft)}.water-record-import label{display:grid;gap:6px}.water-record-import label>span{color:var(--c-text-2);font-size:12px;font-weight:700}.water-record-import select{width:100%;min-height:40px;padding:0 9px;border:1px solid var(--c-border);border-radius:7px;background:var(--c-bg-card);color:var(--c-text)}.water-record-import select:focus{outline:2px solid var(--c-cyan-dim);border-color:var(--c-cyan)}.water-record-import small{color:var(--c-text-3);line-height:1.5}.water-record-empty{display:flex;align-items:center;justify-content:space-between;gap:8px;color:var(--c-text-3);font-size:12px}
</style>
