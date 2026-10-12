import { expect, test } from '@playwright/test'

test.beforeEach(async ({ page }) => {
  await page.route('**/api/guestbook**', route => route.fulfill({ json: { success: true, message: 'ok', data: {
    content: [{ id: 1, author: '测试访客', content: '首页留言板很清楚。', reply: '谢谢你的反馈！', createdAt: '2026-10-12T00:00:00Z' }],
    totalElements: 1,
  } } }))
})

test('homepage cards open personal space and profile from real click paths', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('link', { name: '进入个人与游戏' }).click()
  await expect(page).toHaveURL(/\/personal$/)
  await expect(page.getByRole('heading', { name: /欢迎回来/ })).toBeVisible()
  await page.goto('/')
  await page.getByRole('link', { name: '进入个人简介' }).click()
  await expect(page).toHaveURL(/\/profile$/)
  await expect(page.getByText('PERSONAL PROFILE · 2026')).toBeVisible()
})

test('homepage guestbook is publicly readable and offers an anonymous form', async ({ page }) => {
  await page.goto('/#guestbook')
  await expect(page.getByRole('heading', { name: '留下一句话' })).toBeVisible()
  await expect(page.getByText('首页留言板很清楚。')).toBeVisible()
  await expect(page.getByText('谢谢你的反馈！')).toBeVisible()
  await expect(page.getByLabel('昵称')).toBeVisible()
  await expect(page.getByLabel('留言')).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false)
})

test('anonymous visitor can submit a moderated message', async ({ page }) => {
  let submitted = ''
  await page.route('**/api/guestbook', async route => {
    if (route.request().method() === 'POST') {
      submitted = (await route.request().postDataJSON()).content
      return route.fulfill({ json: { success: true, message: 'ok', data: { id: 2, status: 'PENDING' } } })
    }
    return route.fallback()
  })
  await page.goto('/#guestbook')
  await page.getByLabel('昵称').fill('路过的人')
  await page.getByLabel('留言').fill('期待更多作品')
  await page.getByRole('button', { name: '提交留言 →' }).click()
  await expect(page.getByRole('status')).toContainText('审核通过后')
  expect(submitted).toBe('期待更多作品')
})
