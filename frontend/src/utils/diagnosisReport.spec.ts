import { describe, expect, it } from 'vitest'
import { buildDiagnosisPrintHtml, buildDiagnosisReportText } from './diagnosisReport'

describe('diagnosis report export', () => {
  it('includes structured evidence and excludes private image content', () => {
    const text = buildDiagnosisReportText({ report: '疑似水霉病', species: '锦鲤', confidence: 'MEDIUM', evidence: ['体表棉絮状物'], sources: ['鱼病手册'], hasPrivateImage: true })
    expect(text).toContain('参考置信度：中等')
    expect(text).toContain('1. 体表棉絮状物')
    expect(text).toContain('病灶图片已受保护存储')
    expect(text).not.toContain('data:image')
  })

  it('escapes report content in printable HTML', () => {
    const html = buildDiagnosisPrintHtml({ report: '<script>alert(1)</script>' })
    expect(html).toContain('&lt;script&gt;alert(1)&lt;/script&gt;')
    expect(html).not.toContain('<script>alert(1)</script>')
  })

  it('exports follow-up observations and escalates worsening cases', () => {
    const text = buildDiagnosisReportText({
      report: '继续隔离观察',
      observations: [{ conditionStatus: 'WORSENING', appetiteStatus: 'NONE', mortalityCount: 2, waterQuality: '氨氮升高', notes: '游动无力', observedAt: '2026-10-12T08:00:00Z' }],
    })
    expect(text).toContain('连续观察（1 条）')
    expect(text).toContain('病情：恶化')
    expect(text).toContain('摄食：停食')
    expect(text).toContain('新增死亡：2 尾')
    expect(text).toContain('升级处理提示')
  })

  it('keeps history but clears the current escalation after recovery', () => {
    const text = buildDiagnosisReportText({ report: '继续观察', observations: [
      { conditionStatus: 'RECOVERED', appetiteStatus: 'GOOD', mortalityCount: 0, notes: '症状消失', observedAt: '2026-10-13T08:00:00Z' },
      { conditionStatus: 'WORSENING', appetiteStatus: 'NONE', mortalityCount: 2, observedAt: '2026-10-12T08:00:00Z' },
    ] })
    expect(text).toContain('病情：已恢复（结束观察）')
    expect(text).toContain('病情：恶化')
    expect(text).not.toContain('升级处理提示')
  })
})
