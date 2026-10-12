<template>
  <div class="calculator-page">
    <header class="calculator-hero">
      <div><small>AQUACULTURE UTILITY</small><h1>水体用量计算器</h1><p>把池塘体积、产品标签剂量和有效成分含量换算成清晰的投放量。</p></div>
      <div class="formula-mark">m³<br><span>→ g / kg</span></div>
    </header>

    <el-alert type="warning" :closable="false" show-icon title="只做数学换算，不提供用药建议">
      <template #default>剂量必须来自合法产品标签或水产技术人员。使用前请复核水体体积、有效成分、适用品种、休药期与当地规定；不确定时不要投放。</template>
    </el-alert>

    <div class="calculator-grid">
      <section class="input-card">
        <div class="step-title"><b>01</b><div><h2>确认水体体积</h2><p>池塘不规则时建议分区估算，平均水深不要直接使用最深处数值。</p></div></div>
        <el-radio-group v-model="volumeMode" class="mode-switch">
          <el-radio-button value="dimensions">按长宽水深</el-radio-button>
          <el-radio-button value="direct">直接输入体积</el-radio-button>
        </el-radio-group>
        <div v-if="volumeMode === 'dimensions'" class="field-grid three">
          <label><span>长度（m）</span><input v-model.number="length" type="number" min="0" step="0.1" inputmode="decimal" /></label>
          <label><span>宽度（m）</span><input v-model.number="width" type="number" min="0" step="0.1" inputmode="decimal" /></label>
          <label><span>平均水深（m）</span><input v-model.number="depth" type="number" min="0" step="0.01" inputmode="decimal" /></label>
        </div>
        <div v-else class="field-grid"><label><span>水体体积（m³）</span><input v-model.number="directVolume" type="number" min="0" step="0.1" inputmode="decimal" /></label></div>
        <div class="volume-preview"><span>估算水体</span><strong>{{ volumeM3 ? formatVolume(volumeM3) : '等待有效输入' }}</strong></div>

        <div class="step-title second"><b>02</b><div><h2>输入标签数据</h2><p>请逐字核对包装单位，`g/m³` 与 `mg/L` 不能混用。</p></div></div>
        <el-radio-group v-model="doseMode" class="mode-switch">
          <el-radio-button value="product">标签剂量 g/m³</el-radio-button>
          <el-radio-button value="active">有效成分 mg/L</el-radio-button>
        </el-radio-group>
        <div v-if="doseMode === 'product'" class="field-grid"><label><span>产品标签剂量（g/m³）</span><input v-model.number="dosePerM3" type="number" min="0" step="0.001" inputmode="decimal" /></label></div>
        <div v-else class="field-grid two">
          <label><span>目标有效成分（mg/L）</span><input v-model.number="targetMgL" type="number" min="0" step="0.001" inputmode="decimal" /></label>
          <label><span>产品有效成分含量（%）</span><input v-model.number="concentrationPercent" type="number" min="0" max="100" step="0.1" inputmode="decimal" /></label>
        </div>
      </section>

      <aside class="result-card" aria-live="polite">
        <small>CALCULATED AMOUNT</small>
        <div v-if="mass" class="mass-result"><strong>{{ mass.value }}</strong><span>{{ mass.unit }}</span></div>
        <div v-else class="mass-empty">填写左侧有效数据后显示结果</div>
        <dl v-if="mass">
          <div><dt>水体体积</dt><dd>{{ formatVolume(volumeM3!) }}</dd></div>
          <div><dt>计算方式</dt><dd>{{ doseMode === 'product' ? '产品标签剂量' : '有效成分浓度换算' }}</dd></div>
          <div><dt>计算公式</dt><dd>{{ formulaText }}</dd></div>
        </dl>
        <div class="result-actions">
          <el-button type="primary" :disabled="!mass" @click="copyResult">复制计算结果</el-button>
          <el-button :disabled="!mass" @click="saveResult">保存到本机</el-button>
          <el-button text @click="reset">重置</el-button>
        </div>
        <p class="rounding-note">显示值已适度四舍五入；实际称量前请用原始参数再次复核。</p>
      </aside>
    </div>

    <section v-if="history.length" class="history-card">
      <div class="history-head"><div><h2>最近计算</h2><p>仅保存在当前浏览器，不会上传到服务器。</p></div><el-button text type="danger" @click="clearHistory">清空</el-button></div>
      <div class="history-list"><article v-for="item in history" :key="item.id"><strong>{{ item.amount }}</strong><span>{{ item.volume }} · {{ item.mode }}</span><small>{{ new Date(item.createdAt).toLocaleString() }}</small></article></div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { calculateProductAmount, calculateVolume, formatMass, formatVolume } from '@/utils/dosageCalculator'

type HistoryItem = { id: number; amount: string; volume: string; mode: string; createdAt: string }
const historyKey = 'yunjin:dosage-calculations'
const volumeMode = ref<'dimensions' | 'direct'>('dimensions')
const doseMode = ref<'product' | 'active'>('product')
const length = ref<number | undefined>()
const width = ref<number | undefined>()
const depth = ref<number | undefined>()
const directVolume = ref<number | undefined>()
const dosePerM3 = ref<number | undefined>()
const targetMgL = ref<number | undefined>()
const concentrationPercent = ref<number | undefined>()
const history = ref<HistoryItem[]>(readHistory())

const volumeM3 = computed(() => calculateVolume({ mode: volumeMode.value, length: length.value, width: width.value, depth: depth.value, directVolume: directVolume.value }))
const grams = computed(() => volumeM3.value ? calculateProductAmount({ mode: doseMode.value, volumeM3: volumeM3.value, dosePerM3: dosePerM3.value, targetMgL: targetMgL.value, concentrationPercent: concentrationPercent.value }) : null)
const mass = computed(() => grams.value ? formatMass(grams.value) : null)
const formulaText = computed(() => doseMode.value === 'product' ? '体积 × 标签剂量' : '目标浓度 × 体积 ÷ 有效成分比例')

function resultText() {
  if (!mass.value || !volumeM3.value) return ''
  return `水产智诊平台水体用量换算\n水体：${formatVolume(volumeM3.value)}\n方式：${doseMode.value === 'product' ? '标签剂量 g/m³' : '有效成分 mg/L + 产品含量'}\n计算结果：${mass.value.value} ${mass.value.unit}\n提示：结果仅为数学换算，实际使用前必须核对产品标签和专业意见。`
}
async function copyResult() { await navigator.clipboard.writeText(resultText()); ElMessage.success('计算结果已复制') }
function saveResult() {
  if (!mass.value || !volumeM3.value) return
  history.value = [{ id: Date.now(), amount: `${mass.value.value} ${mass.value.unit}`, volume: formatVolume(volumeM3.value), mode: doseMode.value === 'product' ? '标签剂量' : '有效成分换算', createdAt: new Date().toISOString() }, ...history.value].slice(0, 5)
  localStorage.setItem(historyKey, JSON.stringify(history.value)); ElMessage.success('已保存到当前浏览器')
}
function readHistory(): HistoryItem[] { try { const value = JSON.parse(localStorage.getItem(historyKey) || '[]'); return Array.isArray(value) ? value.slice(0, 5) : [] } catch { return [] } }
function clearHistory() { history.value = []; localStorage.removeItem(historyKey) }
function reset() { length.value = undefined; width.value = undefined; depth.value = undefined; directVolume.value = undefined; dosePerM3.value = undefined; targetMgL.value = undefined; concentrationPercent.value = undefined }
</script>

<style scoped>
.calculator-page{max-width:1120px;margin:0 auto}.calculator-hero{display:flex;align-items:flex-end;justify-content:space-between;gap:24px;margin-bottom:16px;padding:26px;border:1px solid var(--c-border);border-radius:12px;background:radial-gradient(circle at 85% 20%,rgba(0,212,255,.16),transparent 28%),linear-gradient(135deg,rgba(0,155,214,.12),transparent 48%),var(--c-bg-card)}.calculator-hero small,.result-card>small{color:var(--c-cyan);font:800 10px ui-monospace;letter-spacing:.18em}.calculator-hero h1{margin:7px 0 8px;color:var(--c-text);font-size:34px}.calculator-hero p{margin:0;color:var(--c-text-3);line-height:1.7}.formula-mark{min-width:120px;color:var(--c-cyan);font:900 40px ui-monospace;text-align:right}.formula-mark span{font-size:15px}.calculator-grid{display:grid;grid-template-columns:minmax(0,1.45fr) minmax(300px,.75fr);gap:16px;margin-top:16px}.input-card,.result-card,.history-card{border:1px solid var(--c-border);border-radius:10px;background:var(--c-bg-card)}.input-card{padding:22px}.step-title{display:flex;align-items:flex-start;gap:13px}.step-title.second{margin-top:28px;padding-top:24px;border-top:1px solid var(--c-border)}.step-title>b{width:34px;height:34px;display:grid;place-items:center;border-radius:8px;background:var(--c-cyan-dim);color:var(--c-cyan);font:800 12px ui-monospace;flex-shrink:0}.step-title h2,.history-head h2{margin:0;color:var(--c-text);font-size:19px}.step-title p,.history-head p{margin:5px 0 0;color:var(--c-text-3);font-size:13px;line-height:1.6}.mode-switch{margin:16px 0 14px}.field-grid{display:grid;grid-template-columns:1fr;gap:12px}.field-grid.two{grid-template-columns:repeat(2,minmax(0,1fr))}.field-grid.three{grid-template-columns:repeat(3,minmax(0,1fr))}.field-grid label{display:grid;gap:7px;color:var(--c-text-2);font-size:13px;font-weight:700}.field-grid input{width:100%;min-height:44px;padding:0 12px;border:1px solid var(--c-border);border-radius:7px;outline:0;background:var(--c-bg-soft);color:var(--c-text);font:inherit}.field-grid input:focus{border-color:var(--c-cyan);box-shadow:0 0 0 2px var(--c-cyan-dim)}.volume-preview{display:flex;align-items:center;justify-content:space-between;gap:10px;margin-top:13px;padding:11px 12px;border-radius:7px;background:var(--c-bg-soft)}.volume-preview span{color:var(--c-text-3);font-size:12px}.volume-preview strong{color:var(--c-text);font-size:13px}.result-card{position:sticky;top:80px;align-self:start;padding:24px;background:linear-gradient(155deg,rgba(0,212,255,.1),transparent 42%),var(--c-bg-card)}.mass-result{display:flex;align-items:baseline;gap:10px;margin:25px 0}.mass-result strong{color:var(--c-text);font-size:54px;line-height:1;font-weight:900;overflow-wrap:anywhere}.mass-result span{color:var(--c-cyan);font-size:20px;font-weight:800}.mass-empty{min-height:96px;display:grid;place-items:center;margin:18px 0;color:var(--c-text-3);text-align:center}.result-card dl{margin:0}.result-card dl div{display:flex;justify-content:space-between;gap:12px;padding:10px 0;border-top:1px solid var(--c-border)}.result-card dt{color:var(--c-text-3);font-size:12px}.result-card dd{margin:0;color:var(--c-text-2);font-size:12px;text-align:right}.result-actions{display:flex;flex-wrap:wrap;gap:8px;margin-top:18px}.result-actions .el-button{margin:0}.rounding-note{margin:14px 0 0;color:var(--c-text-3);font-size:11px;line-height:1.6}.history-card{margin-top:16px;padding:18px}.history-head{display:flex;justify-content:space-between;align-items:flex-start}.history-list{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:9px;margin-top:14px}.history-list article{min-width:0;padding:11px;border:1px solid var(--c-border);border-radius:7px;background:var(--c-bg-soft)}.history-list strong,.history-list span,.history-list small{display:block}.history-list strong{color:var(--c-cyan);font-size:17px}.history-list span{margin-top:6px;color:var(--c-text-2);font-size:11px;line-height:1.5}.history-list small{margin-top:7px;color:var(--c-text-3);font-size:10px}@media(max-width:900px){.calculator-grid{grid-template-columns:1fr}.result-card{position:static}.history-list{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:600px){.calculator-hero{align-items:flex-start;flex-direction:column;padding:19px}.calculator-hero h1{font-size:27px}.formula-mark{text-align:left}.input-card,.result-card{padding:16px}.field-grid.two,.field-grid.three{grid-template-columns:1fr}.mode-switch{display:flex}.mode-switch :deep(.el-radio-button){flex:1}.mode-switch :deep(.el-radio-button__inner){width:100%;min-height:44px;padding:12px 8px}.volume-preview{align-items:flex-start;flex-direction:column}.mass-result strong{font-size:44px}.history-list{grid-template-columns:1fr}}
</style>
