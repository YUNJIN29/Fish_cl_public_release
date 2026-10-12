import { expect, test } from '@playwright/test'

test('personal center surfaces latest water quality attention and links to the log', async ({ page }) => {
  let completedTaskId = 0
  await page.addInitScript(() => localStorage.setItem('token', 'personal-water-test-token'))
  await page.route('**/api/auth/me', route => route.fulfill({ json: { success: true, message: 'ok', data: { id: 7, username: 'tester', nickname: '测试用户', roles: ['USER'], createdAt: '2026-01-01T00:00:00Z' } } }))
  await page.route('**/api/personal/overview', route => route.fulfill({ json: { success: true, message: 'ok', data: {
    profileCompletion: 25,
    stats: { diagnoseCount: 1, orderCount: 0, postCount: 0, favoriteCount: 0, openTaskCount: 2 },
    recentDiagnoses: [], recentOrders: [], recentPosts: [], attentionDiagnoses: [],
    attentionWaterQuality: [
      { record: { id: 12, pondName: '育苗池', ph: 6.3, dissolvedOxygen: 4.2, observedAt: '2026-10-12T02:00:00Z' }, deviations: ['pH', '溶氧'], ageDays: 0, stale: false },
      { record: { id: 13, pondName: '屋顶池', ph: 7.2, observedAt: '2026-10-03T02:00:00Z' }, deviations: [], ageDays: 9, stale: true },
    ],
    upcomingTasks: [
      { id: 21, title: '复测育苗池水质', category: 'WATER_TEST', pondName: '育苗池', dueAt: '2026-01-01T02:00:00Z' },
      { id: 22, title: '检查今日投喂', category: 'FEEDING', pondName: '育苗池', dueAt: '2026-01-01T03:00:00Z' },
    ],
  } } }))
  await page.route('**/api/water-quality**', route => {
    const path = new URL(route.request().url()).pathname
    const data = path.endsWith('/ponds') ? ['育苗池', '屋顶池'] : path.endsWith('/targets') || path.endsWith('/trend') || path.endsWith('/overview') ? [] : { content: [], totalElements: 0 }
    return route.fulfill({ json: { success: true, message: 'ok', data } })
  })
  await page.route('**/api/tasks/*/completion', async route => {
    completedTaskId = Number(new URL(route.request().url()).pathname.split('/').at(-2))
    expect(await route.request().postDataJSON()).toEqual({ completed: true })
    return route.fulfill({ json: { success: true, message: '待办已完成', data: { id: completedTaskId, completedAt: new Date().toISOString() } } })
  })

  await page.goto('/personal/me')
  await expect(page.getByRole('region', { name: '需要关注的水质记录' })).toContainText('育苗池')
  await expect(page.getByRole('region', { name: '需要关注的水质记录' })).toContainText('pH、溶氧偏离个人范围')
  await expect(page.getByRole('region', { name: '需要关注的水质记录' })).toContainText('屋顶池')
  await expect(page.getByRole('region', { name: '需要关注的水质记录' })).toContainText('已有 9 天未更新水质')
  await expect(page.getByRole('region', { name: '需要关注的水质记录' })).toContainText('数据已过期')
  await expect(page.getByRole('region', { name: '近期养殖待办' })).toContainText('复测育苗池水质')
  await expect(page.getByRole('region', { name: '近期养殖待办' })).toContainText('已逾期')
  await page.getByRole('button', { name: '记录复测育苗池水质的检测结果' }).click()
  await expect(page).toHaveURL(url => url.pathname === '/fish/water-quality' && url.searchParams.get('pond') === '育苗池' && url.searchParams.get('taskId') === '21')
  await expect(page.getByText('正在记录待办结果')).toBeVisible()
  await page.goBack()
  await expect(page.getByRole('region', { name: '近期养殖待办' })).toBeVisible()
  await page.getByRole('button', { name: '完成检查今日投喂' }).click()
  await expect.poll(() => completedTaskId).toBe(22)
  await page.getByRole('region', { name: '需要关注的水质记录' }).getByRole('button').first().click()
  await expect(page).toHaveURL(/\/fish\/water-quality\?pond=%E8%82%B2%E8%8B%97%E6%B1%A0$/)
  await expect(page.locator('.trend-filters select').first()).toHaveValue('育苗池')
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBeTruthy()
})
