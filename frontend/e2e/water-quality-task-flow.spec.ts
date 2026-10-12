import { expect, test } from '@playwright/test'

test('records water quality from a linked task and completes that task', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('token', 'e2e-token'))
  let savedPond = ''
  let completedTaskId = 0
  await page.route('**/api/**', async route => {
    const request = route.request()
    const url = new URL(request.url())
    if (url.pathname === '/api/auth/me') return route.fulfill({ json: { success: true, message: 'ok', data: { id: 7, username: 'tester', roles: ['USER'] } } })
    if (url.pathname === '/api/water-quality' && request.method() === 'POST') {
      savedPond = (await request.postDataJSON()).pondName
      return route.fulfill({ json: { success: true, message: 'saved', data: { id: 9 } } })
    }
    if (url.pathname === '/api/tasks/42/completion' && request.method() === 'PATCH') {
      completedTaskId = 42
      return route.fulfill({ json: { success: true, message: 'completed', data: { id: 42, completedAt: new Date().toISOString() } } })
    }
    const data = url.pathname === '/api/water-quality/ponds' ? ['一号池']
      : url.pathname === '/api/water-quality/trend' || url.pathname === '/api/water-quality/targets' || url.pathname === '/api/water-quality/overview' ? []
        : { content: [], totalElements: 0 }
    return route.fulfill({ json: { success: true, message: 'ok', data } })
  })

  await page.goto('/fish/water-quality?pond=%E4%B8%80%E5%8F%B7%E6%B1%A0&taskId=42&taskTitle=%E5%A4%8D%E6%B5%8B%E6%B0%B4%E8%B4%A8')
  await expect(page.getByText('正在记录待办结果')).toBeVisible()
  await expect(page.getByText(/复测水质 · 保存读数后将自动完成该待办/)).toBeVisible()
  await expect(page.getByLabel('水体名称 *')).toHaveValue('一号池')
  await page.getByLabel('pH').fill('7.4')
  await page.getByRole('button', { name: '保存水质记录' }).click()

  await expect.poll(() => savedPond).toBe('一号池')
  await expect.poll(() => completedTaskId).toBe(42)
  await expect(page.getByText('水质记录已保存，关联待办已完成')).toBeVisible()
  await expect(page).toHaveURL(url => url.pathname === '/fish/water-quality' && url.searchParams.get('pond') === '一号池' && !url.searchParams.has('taskId'))
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBeTruthy()
})
