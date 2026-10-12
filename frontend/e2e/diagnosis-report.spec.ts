import { expect, test } from '@playwright/test'

const record = {
  id: 12,
  species: '锦鲤',
  temperature: '24',
  duration: '2 天',
  waterQuality: '氨氮略高',
  description: '体表出现白点，活动减少',
  result: '建议先隔离病鱼并复测水质。',
  imageUrl: '/diagnose-images/private.png',
  createdAt: '2026-10-12T08:30:00',
  confidence: 'MEDIUM',
  evidence: ['体表白点与活动下降同时出现'],
  sources: ['鱼类常见病知识库'],
  warnings: ['不要在未确认水体体积时直接下药'],
  nextSteps: ['复测氨氮和亚硝酸盐'],
}

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('token', 'e2e-user-token')
    Object.defineProperty(navigator, 'clipboard', {
      configurable: true,
      value: { writeText: async (value: string) => { (window as any).__copiedReport = value } },
    })
  })
  await page.route('**/api/auth/me', (route) => route.fulfill({
    contentType: 'application/json',
    body: JSON.stringify({ success: true, data: { id: 2, username: 'tester', nickname: '测试用户', roles: ['USER'] } }),
  }))
  await page.route('**/api/diagnose/history**', (route) => route.fulfill({
    contentType: 'application/json',
    body: JSON.stringify({ success: true, data: { content: [record], totalElements: 1, totalPages: 1, number: 0, size: 10 } }),
  }))
  let observations = [{ id: 13, conditionStatus: 'WORSENING', appetiteStatus: 'NONE', mortalityCount: 2, waterQuality: '氨氮升高', notes: '游动无力', observedAt: '2026-10-12T09:30:00Z', createdAt: '2026-10-12T09:31:00Z' }]
  await page.route('**/api/diagnose/12/follow-ups', (route) => {
    if (route.request().method() === 'POST') {
      const saved = { id: 14, ...route.request().postDataJSON(), createdAt: new Date().toISOString() }
      observations = [saved, ...observations]
      return route.fulfill({ contentType: 'application/json', body: JSON.stringify({ success: true, data: saved }) })
    }
    return route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({ success: true, data: observations }),
    })
  })
  await page.route('**/api/diagnose/12/follow-ups/*', (route) => {
    const id = Number(new URL(route.request().url()).pathname.split('/').pop())
    const index = observations.findIndex(item => item.id === id)
    if (route.request().method() === 'PUT' && index >= 0) {
      observations[index] = { ...observations[index], ...route.request().postDataJSON() }
      return route.fulfill({ contentType: 'application/json', body: JSON.stringify({ success: true, data: observations[index] }) })
    }
    if (route.request().method() === 'DELETE' && index >= 0) observations.splice(index, 1)
    return route.fulfill({ contentType: 'application/json', body: JSON.stringify({ success: true, data: null }) })
  })
  await page.route('**/api/tasks**', (route) => {
    const path = new URL(route.request().url()).pathname
    const data = path.endsWith('/summary') ? { openCount:0, overdueCount:0, dueSoonCount:0, recurringCount:0 } : path.endsWith('/ponds') ? [] : { content:[], totalElements:0 }
    return route.fulfill({ contentType:'application/json', body:JSON.stringify({ success:true, data }) })
  })
  await page.route('**/api/water-quality/ponds', route => route.fulfill({ json:{ success:true, data:[] } }))
  await page.route('**/api/personal/overview', (route) => route.fulfill({
    contentType: 'application/json', body: JSON.stringify({ success: true, data: {
      profileCompletion: 75,
      stats: { diagnoseCount: 1, orderCount: 0, postCount: 0, favoriteCount: 0 },
      recentDiagnoses: [record], recentOrders: [], recentPosts: [],
      attentionDiagnoses: [{ diagnosis: record, latestObservation: { id: 13, conditionStatus: 'WORSENING', appetiteStatus: 'NONE', mortalityCount: 2, waterQuality: '氨氮升高', notes: '游动无力', observedAt: '2026-10-12T09:30:00Z', createdAt: '2026-10-12T09:31:00Z' } }],
    } }),
  }))
  await page.route('**/diagnose-images/private.png', (route) => route.fulfill({
    contentType: 'image/svg+xml', body: '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16"><rect width="16" height="16" fill="#0ba7c4"/></svg>',
  }))
})

test('history report can be copied and offers a privacy-safe print action', async ({ page }) => {
  await page.goto('/personal/me?tab=history', { waitUntil: 'domcontentloaded' })
  const historyItem = page.locator('.history-list .history-item').first()
  await expect(historyItem).toContainText('体表出现白点，活动减少', { timeout: 15_000 })
  await historyItem.click()

  const dialog = page.getByRole('dialog', { name: '诊断报告回顾' })
  await expect(dialog).toBeVisible()
  await expect(dialog.getByText('建议先隔离病鱼并复测水质。')).toBeVisible()
  await page.evaluate(() => Object.defineProperty(navigator, 'clipboard', {
    configurable: true,
    value: { writeText: async (value: string) => { (window as any).__copiedReport = value } },
  }))
  await dialog.getByRole('button', { name: '复制完整病例' }).dispatchEvent('click')

  await expect.poll(() => page.evaluate(() => (window as any).__copiedReport as string)).toContain('诊断对象：锦鲤')
  const copied = await page.evaluate(() => (window as any).__copiedReport as string)
  expect(copied).toContain('诊断对象：锦鲤')
  expect(copied).toContain('参考置信度：中等')
  expect(copied).toContain('复测氨氮和亚硝酸盐')
  expect(copied).toContain('病灶图片已受保护存储')
  expect(copied).toContain('连续观察（1 条）')
  expect(copied).toContain('病情：恶化')
  expect(copied).toContain('升级处理提示')
  await expect(dialog.getByRole('button', { name: '打印 / PDF' })).toBeVisible()
  await expect(dialog.getByText('病情恶化')).toBeVisible()
  await expect(dialog.getByText('需要升级处理')).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false)
})

test('a diagnosis can receive a structured follow-up observation', async ({ page }) => {
  await page.goto('/personal/me?tab=history', { waitUntil: 'domcontentloaded' })
  await page.locator('.history-list .history-item').first().click()
  const dialog = page.getByRole('dialog', { name: '诊断报告回顾' })
  await dialog.getByRole('button', { name: '新增观察' }).click()
  await dialog.getByPlaceholder('如：换水后氨氮下降、溶氧正常').fill('换水后溶氧正常')
  await dialog.getByPlaceholder('记录病灶、活动、用药或换水后的变化').fill('游动状态稳定')

  const requestPromise = page.waitForRequest((request) => request.url().endsWith('/api/diagnose/12/follow-ups') && request.method() === 'POST')
  await dialog.getByRole('button', { name: '保存观察记录' }).click()
  const payload = (await requestPromise).postDataJSON()
  expect(payload.conditionStatus).toBe('STABLE')
  expect(payload.appetiteStatus).toBe('REDUCED')
  expect(payload.waterQuality).toBe('换水后溶氧正常')
  await expect(page.getByText('观察记录已保存')).toBeVisible()
})

test('overview surfaces a risky latest observation and opens its case', async ({ page }) => {
  await page.goto('/personal/me', { waitUntil: 'domcontentloaded' })
  const attention = page.getByRole('region', { name: '需要关注的病例' })
  await expect(attention).toBeVisible({ timeout: 15_000 })
  await expect(attention.getByText('新增死亡 2 尾')).toBeVisible()
  await attention.locator('.attention-item').click()
  await expect(page.getByRole('dialog', { name: '诊断报告回顾' })).toBeVisible()
  await expect(page.getByText('需要升级处理')).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false)
})

test('a recovered observation closes the active escalation', async ({ page }) => {
  await page.goto('/personal/me?tab=history', { waitUntil: 'domcontentloaded' })
  await page.locator('.history-list .history-item').first().click()
  const dialog = page.getByRole('dialog', { name: '诊断报告回顾' })
  await expect(dialog.getByText('需要升级处理')).toBeVisible()
  await dialog.getByRole('button', { name: '新增观察' }).click()
  await dialog.locator('.follow-up-form .el-select').first().click()
  await page.getByRole('option', { name: '已恢复（结束观察）' }).click()
  const requestPromise = page.waitForRequest((request) => request.url().endsWith('/api/diagnose/12/follow-ups') && request.method() === 'POST')
  await dialog.getByRole('button', { name: '保存观察记录' }).click()
  expect((await requestPromise).postDataJSON().conditionStatus).toBe('RECOVERED')
  await expect(dialog.getByText('病例已恢复')).toBeVisible()
  await expect(dialog.getByText('需要升级处理')).toBeHidden()
})

test('an existing observation can be corrected and reschedules the case', async ({ page }) => {
  await page.goto('/personal/me?tab=history', { waitUntil: 'domcontentloaded' })
  await page.locator('.history-list .history-item').first().click()
  const dialog = page.getByRole('dialog', { name: '诊断报告回顾' })
  await dialog.locator('.follow-up-actions').first().getByRole('button', { name: /编辑/ }).click()
  await expect(dialog.getByText('正在修改这条观察记录')).toBeVisible()
  await dialog.getByPlaceholder('记录病灶、活动、用药或换水后的变化').fill('复测后活动有所恢复')
  const requestPromise = page.waitForRequest(request => request.url().endsWith('/api/diagnose/12/follow-ups/13') && request.method() === 'PUT')
  await dialog.getByRole('button', { name: '保存修改' }).click()
  const payload = (await requestPromise).postDataJSON()
  expect(payload.notes).toBe('复测后活动有所恢复')
  await expect(page.getByText('观察记录已更新')).toBeVisible()
  await expect(dialog.getByText('复测后活动有所恢复')).toBeVisible()

  await dialog.getByRole('button', { name: '安排下次观察' }).click()
  await expect(page).toHaveURL(url => url.pathname === '/fish/tasks' && url.searchParams.get('source') === 'diagnosis' && url.searchParams.get('title') === '复查锦鲤诊断情况')
  await expect(page.getByLabel('备注')).toHaveValue(/最近观察：病情恶化/)
})
