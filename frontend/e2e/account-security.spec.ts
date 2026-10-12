import { expect, test } from '@playwright/test'

test('authenticated user can change password with client-side confirmation', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('token', 'security-test-token'))
  let requestBody: Record<string, string> | null = null
  await page.route('**/api/auth/me', route => route.fulfill({ json: { success: true, message: 'ok', data: { id: 7, username: 'tester', nickname: '测试用户', roles: ['USER'], createdAt: '2026-01-01T00:00:00Z' } } }))
  await page.route('**/api/personal/overview', route => route.fulfill({ json: { success: true, message: 'ok', data: { profileCompletion: 25, stats: { diagnoseCount: 0, orderCount: 0, postCount: 0, favoriteCount: 0, openTaskCount: 0 }, recentDiagnoses: [], recentOrders: [], recentPosts: [], attentionDiagnoses: [], attentionWaterQuality: [], upcomingTasks: [] } } }))
  await page.route('**/api/auth/change-password', async route => {
    requestBody = await route.request().postDataJSON()
    return route.fulfill({ json: { success: true, message: '密码已更新', data: null } })
  })

  await page.goto('/personal/me?tab=profile')
  await expect(page.getByRole('heading', { name: '修改登录密码' })).toBeVisible()
  await page.getByLabel('当前密码').fill('OldPass123')
  await page.getByLabel('新密码', { exact: true }).fill('NewPass456')
  await page.getByLabel('确认新密码').fill('Different789')
  await page.getByRole('button', { name: '更新密码' }).click()
  await expect(page.getByText('两次输入的新密码不一致')).toBeVisible()
  expect(requestBody).toBeNull()

  await page.getByLabel('确认新密码').fill('NewPass456')
  await page.getByRole('button', { name: '更新密码' }).click()
  await expect.poll(() => requestBody).toEqual({ currentPassword: 'OldPass123', newPassword: 'NewPass456' })
  await expect(page.getByText('密码已更新，请妥善保管新密码')).toBeVisible()
  await expect(page.getByLabel('当前密码')).toHaveValue('')
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBeTruthy()
})
