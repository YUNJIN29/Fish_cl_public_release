export type PrintableDiagnosis = {
  report: string
  species?: string | null
  temperature?: string | null
  duration?: string | null
  waterQuality?: string | null
  createdAt?: string | null
  confidence?: string | null
  evidence?: string[]
  sources?: string[]
  warnings?: string[]
  nextSteps?: string[]
  hasPrivateImage?: boolean
  observations?: PrintableObservation[]
}

export type PrintableObservation = {
  conditionStatus: 'IMPROVING' | 'STABLE' | 'WORSENING' | 'RECOVERED' | string
  appetiteStatus: 'GOOD' | 'REDUCED' | 'NONE' | string
  mortalityCount: number
  waterQuality?: string | null
  notes?: string | null
  observedAt: string
}

const disclaimer = '本报告仅用于学习和辅助判断，不能替代水产技术人员或兽医的现场诊断。出现大量死亡、浮头或急性水质异常时，请立即寻求专业帮助。'
const confidenceText: Record<string, string> = { LOW: '较低', MEDIUM: '中等', HIGH: '较高' }
const conditionText: Record<string, string> = { IMPROVING: '好转', STABLE: '稳定', WORSENING: '恶化', RECOVERED: '已恢复（结束观察）' }
const appetiteText: Record<string, string> = { GOOD: '正常', REDUCED: '减少', NONE: '停食' }

export function buildDiagnosisReportText(data: PrintableDiagnosis) {
  const lines = ['Aquatic Lab · 水产智诊平台', 'AI 辅助诊断报告', '']
  const metadata = [
    ['诊断对象', data.species], ['水温', data.temperature ? `${data.temperature}°C` : ''],
    ['发病时长', data.duration], ['水质情况', data.waterQuality],
    ['报告时间', data.createdAt ? new Date(data.createdAt).toLocaleString('zh-CN') : new Date().toLocaleString('zh-CN')],
    ['参考置信度', data.confidence ? confidenceText[data.confidence] || data.confidence : ''],
  ]
  metadata.filter(([, value]) => value).forEach(([label, value]) => lines.push(`${label}：${value}`))
  lines.push('', '诊断建议', data.report.trim())
  appendList(lines, '判断依据', data.evidence)
  appendList(lines, '资料来源', data.sources)
  appendList(lines, '风险提示', data.warnings)
  appendList(lines, '建议下一步', data.nextSteps)
  appendObservations(lines, data.observations)
  if (data.hasPrivateImage) lines.push('', '隐私说明：病灶图片已受保护存储，未包含在复制或打印内容中。')
  lines.push('', `免责声明：${disclaimer}`)
  return lines.join('\n')
}

export function buildDiagnosisPrintHtml(data: PrintableDiagnosis) {
  const text = buildDiagnosisReportText(data).replace(`\n免责声明：${disclaimer}`, '')
  return `<!doctype html><html lang="zh-CN"><head><meta charset="utf-8"><title>水产智诊平台诊断报告</title><style>
  @page{size:A4;margin:18mm}*{box-sizing:border-box}body{margin:0;color:#17212b;font-family:"Microsoft YaHei",sans-serif;font-size:12pt;line-height:1.75}header{padding-bottom:18px;border-bottom:3px solid #0ba7c4}header small{color:#087f98;letter-spacing:.15em}h1{margin:5px 0 0;font-size:25pt}.report{margin:22px 0;white-space:pre-wrap;overflow-wrap:anywhere}.notice{padding:12px 14px;border:1px solid #e4b660;background:#fff9e9;color:#6e5015;font-size:9.5pt}footer{margin-top:22px;padding-top:10px;border-top:1px solid #dce3e8;color:#77828d;font-size:9pt}@media print{button{display:none}}
  </style></head><body><header><small>Aquatic Lab · BLUE PULSE CARE</small><h1>AI 辅助诊断报告</h1></header><main class="report">${escapeHtml(text.replace(/^Aquatic Lab · 水产智诊平台\nAI 辅助诊断报告\n*/, ''))}</main><div class="notice">${escapeHtml(disclaimer)}</div><footer>生成于 example.com · 私有病灶图片不会被写入打印报告</footer></body></html>`
}

export async function copyDiagnosisReport(data: PrintableDiagnosis) {
  const text = buildDiagnosisReportText(data)
  if (navigator.clipboard?.writeText) { await navigator.clipboard.writeText(text); return }
  const textarea = document.createElement('textarea'); textarea.value = text; textarea.style.position = 'fixed'; textarea.style.opacity = '0'; document.body.appendChild(textarea); textarea.select(); document.execCommand('copy'); textarea.remove()
}

export function printDiagnosisReport(data: PrintableDiagnosis) {
  const popup = window.open('', '_blank', 'width=900,height=720')
  if (!popup) return false
  popup.opener = null
  popup.document.open(); popup.document.write(buildDiagnosisPrintHtml(data)); popup.document.close()
  window.setTimeout(() => { popup.focus(); popup.print() }, 250)
  return true
}

function appendList(lines: string[], title: string, values?: string[]) {
  if (!values?.length) return
  lines.push('', title, ...values.map((value, index) => `${index + 1}. ${value}`))
}
function appendObservations(lines: string[], observations?: PrintableObservation[]) {
  if (!observations?.length) return
  lines.push('', `连续观察（${observations.length} 条）`)
  observations.forEach((item, index) => {
    lines.push('', `观察 ${index + 1}｜${new Date(item.observedAt).toLocaleString('zh-CN')}`)
    lines.push(`病情：${conditionText[item.conditionStatus] || item.conditionStatus}`)
    lines.push(`摄食：${appetiteText[item.appetiteStatus] || item.appetiteStatus}`)
    lines.push(`新增死亡：${item.mortalityCount || 0} 尾`)
    if (item.waterQuality) lines.push(`水质：${item.waterQuality}`)
    if (item.notes) lines.push(`备注：${item.notes}`)
  })
  const latest = observations[0]
  if (latest.conditionStatus !== 'RECOVERED' && (latest.conditionStatus === 'WORSENING' || latest.mortalityCount > 0)) {
    lines.push('', '升级处理提示：观察中出现病情恶化或新增死亡，请尽快复测关键水质指标，并联系水产技术人员或兽医。')
  }
}
function escapeHtml(value: string) { return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;') }
