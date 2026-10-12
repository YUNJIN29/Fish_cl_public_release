import { expect, test } from '@playwright/test'

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('token', 'diagnose-water-test-token'))
  await page.route('**/api/auth/me', route => route.fulfill({
    json: { success: true, message: 'ok', data: { id: 7, username: 'tester', roles: ['USER'] } },
  }))
  await page.route('**/api/water-quality**', route => route.fulfill({
    json: {
      success: true,
      message: 'ok',
      data: {
        content: [{
          id: 18,
          pondName: '育苗池',
          temperature: 24.6,
          ph: 7.35,
          dissolvedOxygen: 5.8,
          ammonia: 0.12,
          nitrite: 0.03,
          notes: '雨后复测',
          observedAt: '2026-10-12T01:30:00Z',
        }],
        totalElements: 1,
      },
    },
  }))
})

test('recent water quality record can be imported into diagnosis context', async ({ page }) => {
  await page.goto('/fish/diagnose')
  await page.getByLabel('引用最近水质记录').selectOption('18')

  await expect(page.getByLabel('水温 (°C)')).toHaveValue('24.6')
  const waterQuality = page.getByLabel('水质情况 (pH/氨氮/亚盐)')
  await expect(waterQuality).toHaveValue(/水体：育苗池/)
  await expect(waterQuality).toHaveValue(/pH 7.35/)
  await expect(waterQuality).toHaveValue(/溶氧 5.8 mg\/L/)
  await expect(waterQuality).toHaveValue(/氨氮 0.12 mg\/L/)
  await expect(waterQuality).toHaveValue(/亚硝酸盐 0.03 mg\/L/)
  await expect(waterQuality).toHaveValue(/备注：雨后复测/)
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBeTruthy()
})

test('water quality record query loads owned record outside the recent page', async ({ page }) => {
  await page.route('**/api/water-quality/99', route => route.fulfill({
    json: { success: true, message: 'ok', data: { id: 99, pondName: '远端塘', temperature: 26.1, ph: 8.2, observedAt: '2026-10-10T03:00:00Z' } },
  }))
  await page.goto('/fish/diagnose?waterRecord=99')
  await expect(page.getByLabel('引用最近水质记录')).toHaveValue('99')
  await expect(page.getByLabel('水温 (°C)')).toHaveValue('26.1')
  await expect(page.getByLabel('水质情况 (pH/氨氮/亚盐)')).toHaveValue(/水体：远端塘.*pH 8.2/)
})

test('diagnosis result can prefill a follow-up task', async ({ page }) => {
  await page.route('**/api/diagnose', route => route.fulfill({
    json: {
      success: true,
      message: 'ok',
      data: {
        report: '可能与细菌性感染有关，建议隔离观察并复测水质。',
        saved: true,
        relatedDiseases: [],
        relatedProducts: [],
        ragReferences: [],
        warnings: ['若死亡数量增加请联系水产技术人员'],
        nextSteps: ['24 小时后复查症状', '记录摄食与死亡情况'],
      },
    },
  }))
  await page.route('**/api/tasks**', route => route.fulfill({
    json: { success: true, message: 'ok', data: new URL(route.request().url()).pathname.endsWith('/summary') ? { openCount: 0, overdueCount: 0, dueSoonCount: 0, recurringCount: 0 } : { content: [], totalElements: 0 } },
  }))

  await page.goto('/fish/diagnose')
  await page.getByLabel('鱼种类型').fill('锦鲤')
  await page.getByPlaceholder(/描述症状/).fill('体表充血，食欲下降')
  await page.getByRole('button', { name: /发送/ }).click()
  await expect(page.getByText('诊断报告已生成')).toBeVisible()
  await page.getByRole('button', { name: '安排诊断复查' }).click()

  await expect(page).toHaveURL(/\/fish\/tasks\?/)
  await expect(page.getByText('已引用诊断报告')).toBeVisible()
  await expect(page.getByLabel('事项名称 *')).toHaveValue('复查锦鲤诊断情况')
  await expect(page.getByLabel('分类 *')).toHaveValue('MEDICATION')
  await expect(page.getByLabel('备注')).toHaveValue(/24 小时后复查症状/)
  await expect(page.getByLabel('备注')).toHaveValue(/若死亡数量增加/)
})
