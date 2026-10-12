import { expect, test } from '@playwright/test'

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('token', 'e2e-admin-token'))
  await page.route('**/api/auth/me', (route) => route.fulfill({ contentType: 'application/json', body: JSON.stringify({ success: true, code: 'OK', message: 'OK', data: { id: 1, username: 'admin', nickname: '管理员', roles: ['ADMIN'] } }) }))
  await page.route('**/api/admin/operations/overview', (route) => route.fulfill({ contentType: 'application/json', body: JSON.stringify({ success: true, data: { status: 'UP', database: 'UP', disk: { status: 'UP', usableBytes: 10737418240 }, aiConfigured: true, pendingContent: 0, metrics: {} } }) }))
  await page.route('**/api/admin/operations/traffic', (route) => route.fulfill({ contentType: 'application/json', body: JSON.stringify({ success: true, data: [] }) }))
})

test('admin navigation adapts without horizontal page overflow', async ({ page }, testInfo) => {
  await page.goto('/admin')
  await expect(page.getByText('运行状态')).toBeVisible({ timeout: 15_000 })
  if (testInfo.project.name === 'desktop') {
    await expect(page.locator('.desktop-aside')).toBeVisible()
    await expect(page.getByRole('button', { name: '打开管理导航' })).toBeHidden()
  } else {
    await expect(page.locator('.desktop-aside')).toBeHidden()
    await page.getByRole('button', { name: '打开管理导航' }).click()
    await expect(page.locator('.mobile-drawer')).toBeVisible()
    await expect(page.locator('.mobile-drawer')).toContainText('资源管理')
    await expect(page.getByRole('button', { name: '关闭管理导航' })).toBeVisible()
  }
  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false)
})
