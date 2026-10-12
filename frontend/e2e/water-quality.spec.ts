import { expect, test } from '@playwright/test'

const initialRecord = {
  id: 1,
  pondName: '育苗池',
  temperature: 24.6,
  ph: 7.2,
  dissolvedOxygen: 6.8,
  ammonia: null,
  nitrite: null,
  notes: '换水前',
  observedAt: '2026-10-12T01:30:00Z',
  createdAt: '2026-10-12T01:31:00Z',
}

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('token', 'water-quality-test-token'))
  await page.route('**/api/auth/me', route => route.fulfill({ json: { success: true, message: 'ok', data: { id: 7, username: 'tester', roles: ['USER'] } } }))
})

for (const width of [360, 768, 1280]) {
  test(`water quality workflow fits ${width}px`, async ({ page }) => {
    const records = [{ ...initialRecord }]
    let submitted: Record<string, unknown> | undefined
    let updated: Record<string, unknown> | undefined
    let targetSubmitted: Record<string, unknown> | undefined
    let exportRequested = false
    const targetItems = [{ id: 8, pondName: '育苗池', phMin: 7, phMax: 7.3, dissolvedOxygenMin: 5, updatedAt: new Date().toISOString() }]
    await page.route('**/api/tasks**', route => route.fulfill({ json: { success: true, message: 'ok', data: new URL(route.request().url()).pathname.endsWith('/summary') ? { openCount: 0, overdueCount: 0, dueSoonCount: 0, recurringCount: 0 } : { content: [], totalElements: 0 } } }))
    await page.route('**/api/water-quality**', async route => {
      const method = route.request().method()
      const url = new URL(route.request().url())
      if (url.pathname.endsWith('/export')) {
        exportRequested = true
        return route.fulfill({
          status: 200,
          contentType: 'text/csv;charset=UTF-8',
          headers: { 'content-disposition': 'attachment; filename="water-quality.csv"' },
          body: '\uFEFF水体名称,测量时间,pH\n"育苗池","2026-10-12T01:30:00Z","7.2"',
        })
      }
      if (url.pathname.endsWith('/targets')) {
        if (method === 'POST') {
          targetSubmitted = await route.request().postDataJSON()
          targetItems[0] = { ...targetItems[0], ...targetSubmitted }
          return route.fulfill({ json: { success: true, message: 'saved', data: targetItems[0] } })
        }
        return route.fulfill({ json: { success: true, message: 'ok', data: targetItems } })
      }
      if (method === 'PUT') {
        updated = await route.request().postDataJSON()
        const id = Number(url.pathname.split('/').pop())
        const record = records.find(item => item.id === id)!
        Object.assign(record, updated)
        return route.fulfill({ json: { success: true, message: 'updated', data: record } })
      }
      if (method === 'POST') {
        submitted = await route.request().postDataJSON()
        records.unshift({ ...initialRecord, ...submitted, id: 2, createdAt: new Date().toISOString() })
        return route.fulfill({ json: { success: true, message: 'saved', data: records[0] } })
      }
      if (url.pathname.endsWith('/overview')) {
        const latest = records[0]
        const target = targetItems.find(item => item.pondName === latest.pondName)
        const deviations = target && latest.ph != null && target.phMax != null && latest.ph > target.phMax ? ['pH'] : []
        return route.fulfill({ json: { success: true, message: 'ok', data: [{ pondName: latest.pondName, latest, hasTarget: Boolean(target), deviations, ageDays: latest.id === 1 ? 12 : 0, stale: latest.id === 1 }] } })
      }
      if (method === 'GET' && /\/api\/water-quality\/\d+$/.test(url.pathname)) {
        const id = Number(url.pathname.split('/').pop())
        const record = records.find(item => item.id === id)
        return route.fulfill({ status: record ? 200 : 404, json: { success: Boolean(record), message: record ? 'ok' : 'not found', data: record } })
      }
      if (url.pathname.endsWith('/ponds')) return route.fulfill({ json: { success: true, message: 'ok', data: [...new Set(records.map(record => record.pondName))] } })
      if (url.pathname.endsWith('/trend')) {
        const pond = url.searchParams.get('pondName')
        return route.fulfill({ json: { success: true, message: 'ok', data: records.filter(record => record.pondName === pond).reverse() } })
      }
      return route.fulfill({ json: { success: true, message: 'ok', data: { content: records, totalElements: records.length } } })
    })

    await page.setViewportSize({ width, height: 900 })
    await page.goto('/fish/water-quality')
    await expect(page.getByRole('heading', { name: '水质日志' })).toBeVisible()
    await expect(page.getByRole('heading', { name: '水体状态总览' })).toBeVisible()
    await expect(page.locator('.pond-overview')).toContainText('12 天未测')
    await page.getByRole('button', { name: '为育苗池安排水质检测' }).click()
    await expect(page).toHaveURL(/\/fish\/tasks\?source=water-quality/)
    await expect(page.getByLabel('事项名称 *')).toHaveValue('检测育苗池水质')
    await expect(page.getByLabel('关联水体')).toHaveValue('育苗池')
    await expect(page.getByLabel('备注')).toHaveValue(/已有 12 天未更新/)
    await page.goBack()
    const downloadPromise = page.waitForEvent('download')
    await page.getByRole('button', { name: '导出全部 CSV' }).click()
    await downloadPromise
    expect(exportRequested).toBe(true)
    await page.getByRole('button', { name: '设置参考范围' }).click()
    await page.getByLabel('水体名称 *').first().fill('育苗池')
    await page.getByLabel('最低 pH').fill('7')
    await page.getByLabel('最高 pH').fill('7.3')
    await page.getByRole('button', { name: '保存个人范围' }).click()
    await expect.poll(() => targetSubmitted?.pondName).toBe('育苗池')
    await page.getByRole('button', { name: '基于此记录复测' }).first().click()
    await expect(page.getByText('复测草稿')).toBeVisible()
    await expect(page.getByLabel('水体名称 *').last()).toHaveValue('育苗池')
    await expect(page.getByLabel('水温（°C）')).toHaveValue('24.6')
    await expect(page.getByLabel('pH', { exact: true })).toHaveValue('7.2')
    await expect(page.getByLabel('溶氧（mg/L）')).toHaveValue('6.8')
    await expect(page.getByLabel('备注')).toHaveValue('')
    expect(new Date(await page.getByLabel('测量时间 *').inputValue()).getTime()).toBeGreaterThan(new Date(initialRecord.observedAt).getTime())
    await page.getByLabel('pH', { exact: true }).fill('7.4')
    await page.getByLabel('溶氧（mg/L）').fill('6.2')
    await page.getByLabel('备注').fill('增氧后复测')
    await page.getByRole('button', { name: '保存水质记录' }).click()

    await expect.poll(() => submitted?.pondName).toBe('育苗池')
    expect(submitted?.ph).toBe(7.4)
    expect(submitted?.dissolvedOxygen).toBe(6.2)
    await expect(page.locator('.quality-record strong', { hasText: '育苗池' }).first()).toBeVisible()
    await expect(page.getByText('偏离个人范围').first()).toBeVisible()
    await expect(page.locator('.pond-overview')).toContainText('1 项偏离')
    await page.getByRole('button', { name: '查看育苗池水质趋势' }).click()
    await expect(page.getByRole('img', { name: /育苗池pH变化折线图/ })).toBeVisible()
    await page.getByRole('button', { name: '将育苗池异常水质带入诊断' }).click()
    await expect(page).toHaveURL(/\/fish\/diagnose\?waterRecord=2/)
    await expect(page.getByLabel('水温 (°C)')).toHaveValue('24.6')
    await expect(page.getByLabel('水质情况 (pH/氨氮/亚盐)')).toHaveValue(/pH 7.4/)
    await page.goBack()
    await page.getByRole('button', { name: '为异常水质生成复测待办' }).first().click()
    await expect(page).toHaveURL(/\/fish\/tasks\?/)
    await expect(page.getByText('已引用异常水质记录')).toBeVisible()
    await expect(page.getByLabel('事项名称 *')).toHaveValue('复测育苗池水质')
    await expect(page.getByLabel('关联水体')).toHaveValue('育苗池')
    await expect(page.getByLabel('分类 *')).toHaveValue('WATER_TEST')
    await expect(page.getByLabel('备注')).toHaveValue(/偏离个人范围：pH/)
    await page.goto('/fish/water-quality')
    await page.locator('.trend-filters select').first().selectOption('育苗池')
    await expect(page.getByRole('img', { name: /育苗池pH变化折线图/ })).toBeVisible()
    await page.getByRole('button', { name: '编辑水质记录' }).first().click()
    await expect(page.getByRole('button', { name: '保存修改' })).toBeVisible()
    await page.getByLabel('pH', { exact: true }).fill('7.1')
    await page.getByRole('button', { name: '保存修改' }).click()
    await expect.poll(() => updated?.ph).toBe(7.1)
    await expect(page.locator('.quality-record').first()).toContainText('7.1')
    const layout = await page.evaluate(() => ({
      fits: document.documentElement.scrollWidth <= document.documentElement.clientWidth,
      viewport: document.documentElement.clientWidth,
      width: document.documentElement.scrollWidth,
      offenders: [...document.querySelectorAll<HTMLElement>('body *')]
        .filter(element => element.getBoundingClientRect().right > document.documentElement.clientWidth + 1)
        .slice(0, 8)
        .map(element => `${element.tagName}.${element.className}:${Math.round(element.getBoundingClientRect().right)}`),
    }))
    expect(layout, JSON.stringify(layout)).toMatchObject({ fits: true })
  })
}
