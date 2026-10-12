import { expect, test } from '@playwright/test'

test('exports the currently selected pond instead of the complete history', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('token', 'e2e-token'))
  await page.route('**/api/**', async route => {
    const url = new URL(route.request().url())
    if (url.pathname === '/api/auth/me') {
      await route.fulfill({ json: { success: true, message: 'ok', data: { id: 7, username: 'tester', nickname: '测试用户', roles: ['USER'] } } })
      return
    }
    if (url.pathname === '/api/water-quality/export') {
      await route.fulfill({ contentType: 'text/csv;charset=UTF-8', body: '\uFEFF水体名称,测量时间\n"一号池","2026-10-12"' })
      return
    }
    const data = url.pathname === '/api/water-quality/ponds' ? ['一号池', '二号池']
      : url.pathname === '/api/water-quality/trend' ? []
        : url.pathname === '/api/water-quality/targets' ? []
          : url.pathname === '/api/water-quality/overview' ? []
            : { content: [], totalElements: 2 }
    await route.fulfill({ json: { success: true, message: 'ok', data } })
  })

  await page.goto('/fish/water-quality')
  const exportButton = page.getByRole('button', { name: '导出一号池 CSV' })
  await expect(exportButton).toBeEnabled()
  const requestPromise = page.waitForRequest(request => request.url().includes('/api/water-quality/export'))
  await exportButton.click()
  const request = await requestPromise

  expect(new URL(request.url()).searchParams.get('pondName')).toBe('一号池')
  const root = page.locator('html')
  expect(await root.evaluate(element => element.scrollWidth)).toBe(await root.evaluate(element => element.clientWidth))
})
