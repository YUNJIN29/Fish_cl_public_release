import { expect, test } from '@playwright/test'
test('global navigator opens and filters destinations', async ({ page }) => { await page.goto('/'); await page.keyboard.press('Control+K'); await expect(page.getByRole('dialog', { name: '想去哪里？' })).toBeVisible(); await page.getByPlaceholder('搜索项目、游戏、诊断、知识库…').fill('RAG'); await expect(page.getByRole('button', { name: /RAG 资料检索/ })).toBeVisible() })
test('portal and 404 remain usable without horizontal overflow', async ({ page }) => { await page.goto('/'); await expect(page.locator('body')).toContainText('Aquatic Lab'); expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false); await page.goto('/this-page-does-not-exist'); await expect(page.getByText('返回 Aquatic Lab 首页')).toBeVisible() })
test('protected diagnosis redirects anonymous visitors to login', async ({ page }) => { await page.goto('/fish/diagnose'); await expect(page).toHaveURL(/\/login\?redirect=/, { timeout: 10_000 }) })
test('admin upload management is not exposed to anonymous visitors', async ({ page }) => { await page.goto('/admin/uploads'); await expect(page).toHaveURL((url) => url.pathname === '/login' && url.searchParams.get('redirect') === '/admin/uploads') })
test('anonymous community write action preserves a return path to login', async ({ page }) => { await page.goto('/fish/posts'); await page.getByRole('button', { name: '发帖' }).click(); await expect(page).toHaveURL((url) => url.pathname === '/login' && url.searchParams.get('redirect') === '/fish/posts') })

test('personal workspace resumes browsing and customizes shortcuts without overflow', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('yunjin:recent-pages', JSON.stringify([{ path: '/fish/diseases', title: '知识库', visitedAt: Date.now() }])))
  await page.goto('/personal')
  await expect(page.getByRole('button', { name: /继续 病害知识库/ })).toBeVisible()
  await page.getByRole('button', { name: '自定义' }).click()
  await page.getByRole('button', { name: /RAG 资料检索/ }).click()
  await expect(page.getByRole('button', { name: /RAG 资料检索/ }).first()).toContainText('移除')
  await page.reload()
  await expect(page.getByRole('button', { name: /RAG 资料检索/ }).first()).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false)
})

test('knowledge graph expands to the available canvas width', async ({ page }) => {
  await page.route('**/api/knowledge-graph', route => route.fulfill({ json: {
    categories: [{ name: '病害' }, { name: '症状' }],
    nodes: [
      { id: 'disease:1', name: '草鱼出血病', category: 0, value: '病害', symbolSize: 34 },
      { id: 'symptom:1', name: '体表出血', category: 1, value: '症状', symbolSize: 18 },
    ],
    links: [{ source: 'disease:1', target: 'symptom:1', name: '表现为', value: '表现为', relationType: '属性关系' }],
    stats: { diseaseCount: 27, nodeCount: 601, linkCount: 802, categoryCount: 9, relatedDiseaseLinkCount: 107 },
  } }))
  await page.goto('/fish/knowledge-graph')
  const stats = page.locator('.stat-strip')
  await expect(stats).toContainText('27')
  await expect(stats).toContainText('107')
  await page.locator('.category-select').click()
  await expect(page.getByRole('option', { name: '症状' })).toBeVisible()
  await page.keyboard.press('Escape')
  const chart = page.locator('#kg-full')
  await expect(chart).toBeVisible({ timeout: 20_000 })
  await expect.poll(async () => chart.evaluate((el) => el.querySelector('canvas')?.clientWidth || 0), { timeout: 20_000 }).toBeGreaterThan(300)
  const widths = await chart.evaluate((el) => ({ client: el.clientWidth, rendered: el.querySelector('canvas')?.clientWidth || 0 }))
  expect(widths.client).toBeGreaterThan(300)
  expect(widths.rendered).toBeGreaterThan(widths.client * 0.9)
})
