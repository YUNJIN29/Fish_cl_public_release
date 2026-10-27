import { expect, test } from '@playwright/test'

for (const identity of ['aquatic', 'admin', 'visitor', 'anonymous']) {
  test(`visitor ranking access for ${identity}`, async ({ page }) => {
    if (identity !== 'anonymous') {
      await page.addInitScript(() => localStorage.setItem('token', 'ranking-token'))
      await page.route('**/api/auth/me', route => route.fulfill({ json: { success: true, data: { id: 1, username: identity, nickname: identity, roles: [identity === 'admin' ? 'ADMIN' : 'USER'] } } }))
    }
    let requests = 0
    await page.route('**/api/visits/ranking', route => {
      requests++
      expect(route.request().headers().authorization).toBe('Bearer ranking-token')
      return route.fulfill({ json: { success: true, data: [{ ipAddress: '203.0.113.5', location: '测试地区', visitCount: 12 }] } })
    })
    await page.goto('/')
    const ranking = page.getByRole('region', { name: '本月访客排行榜' })
    if (identity === 'admin') {
      await expect(ranking.getByText('203.0.113.5')).toBeVisible()
      const belowGuestbook = await page.evaluate(() => document.querySelector('#guestbook')!.compareDocumentPosition(document.querySelector('.visitor-ranking')!) & Node.DOCUMENT_POSITION_FOLLOWING)
      expect(belowGuestbook).toBeTruthy()
      expect(requests).toBe(1)
    } else {
      await expect(page.getByLabel('昵称')).toHaveValue(identity === 'anonymous' ? '匿名用户' : identity)
      await expect(ranking).toHaveCount(0)
      expect(requests).toBe(0)
    }
  })
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/visits/summary', route => route.fulfill({ json: { success: true, data: { todayVisitors: 1, monthVisitors: 1, totalVisitors: 1 } } }))
  await page.route('**/api/visits/track', route => route.fulfill({ json: { success: true, data: { todayVisitors: 1, monthVisitors: 1, totalVisitors: 1 } } }))
  await page.route('**/api/visits/ranking', route => route.fulfill({ json: { success: true, data: [] } }))
  await page.route('**/api/guestbook**', route => route.fulfill({ json: { success: true, message: 'ok', data: {
    content: [{ id: 1, author: '测试访客', content: '首页留言板很清楚。', reply: '谢谢你的反馈！', createdAt: '2026-10-12T00:00:00Z' }],
    totalElements: 1,
  } } }))
})

test('homepage cards open personal space and profile from real click paths', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('link', { name: '进入学习工具' }).click({ noWaitAfter: true })
  await expect(page).toHaveURL(/\/personal$/, { timeout: 10_000 })
  await expect(page.getByRole('heading', { name: /欢迎回来/ })).toBeVisible()
  await page.goto('/')
  await page.getByRole('link', { name: '进入项目展示' }).click({ noWaitAfter: true })
  await expect(page).toHaveURL(/\/profile$/, { timeout: 10_000 })
  await expect(page.getByRole('heading', { name: '项目展示页' })).toBeVisible()
})

test('homepage guestbook is publicly readable and offers an anonymous form', async ({ page }) => {
  await page.goto('/#guestbook')
  await expect(page.getByRole('heading', { name: '留下一句话' })).toBeVisible()
  await expect(page.getByText('首页留言板很清楚。')).toBeVisible()
  await expect(page.getByText('谢谢你的反馈！')).toBeVisible()
  await expect(page.getByLabel('昵称')).toHaveValue('匿名用户')
  await expect(page.getByLabel('留言')).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false)
})

test('anonymous visitor can publish immediately with the default nickname', async ({ page }) => {
  let submitted = ''
  let submittedAuthor: string | undefined
  await page.route('**/api/guestbook', async route => {
    if (route.request().method() === 'POST') {
      const body = await route.request().postDataJSON()
      submitted = body.content
      submittedAuthor = body.author
      return route.fulfill({ json: { success: true, message: 'ok', data: { id: 2, status: 'APPROVED', author: '匿名用户' } } })
    }
    return route.fallback()
  })
  await page.goto('/#guestbook')
  await page.getByLabel('留言').fill('期待更多作品')
  await page.getByRole('button', { name: '提交留言 →' }).click()
  await expect(page.locator('.guestbook .notice')).toContainText('已发布')
  expect(submitted).toBe('期待更多作品')
  expect(submittedAuthor).toBe('匿名用户')
})

test('visitor can insert emoji and kaomoji at the text cursor', async ({ page }) => {
  await page.goto('/#guestbook')
  const textarea = page.getByLabel('留言')
  await textarea.fill('你好世界')
  await textarea.evaluate((element: HTMLTextAreaElement) => element.setSelectionRange(2, 2))
  await page.getByRole('button', { name: '😊 表情 / 颜文字' }).click()
  await page.getByRole('button', { name: '插入 😊' }).click()
  await expect(textarea).toHaveValue('你好😊世界')
  await page.getByRole('tab', { name: '颜文字' }).click()
  await page.getByRole('button', { name: '插入 (＾▽＾)', exact: true }).click()
  await expect(textarea).toHaveValue('你好😊(＾▽＾)世界')
  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false)
})

test('administrator can edit reply and delete messages', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('token', 'owner-token'))
  await page.route('**/api/auth/me', route => route.fulfill({ json: { success: true, message: 'ok', data: { id: 1, username: 'aquatic', nickname: 'Aquatic Lab', roles: ['ADMIN'] } } }))
  let patchBody: any = null
  let deleted = false
  await page.route('**/api/admin/guestbook/1', async route => {
    if (route.request().method() === 'PATCH') { patchBody = await route.request().postDataJSON(); return route.fulfill({ json: { success: true, message: 'ok', data: {} } }) }
    if (route.request().method() === 'DELETE') { deleted = true; return route.fulfill({ json: { success: true, message: 'ok', data: null } }) }
    return route.fallback()
  })
  await page.goto('/#guestbook')
  await expect(page.getByLabel('昵称')).toHaveValue('Aquatic Lab')
  await expect(page.getByLabel('昵称')).toHaveAttribute('readonly', '')
  await page.getByRole('button', { name: '编辑' }).click()
  await page.getByLabel('编辑留言').fill('Aquatic Lab 修改后的留言')
  await page.getByRole('button', { name: '保存' }).click()
  expect(patchBody).toEqual({ content: 'Aquatic Lab 修改后的留言' })
  await expect(page.locator('.guestbook .notice')).toContainText('已保存')
  await page.getByRole('button', { name: '删除' }).click()
  const dialog = page.getByRole('alertdialog', { name: '删除留言' })
  await expect(dialog).toBeVisible()
  const dialogBox = await dialog.boundingBox()
  const viewport = page.viewportSize()!
  expect(Math.abs((dialogBox!.x + dialogBox!.width / 2) - viewport.width / 2)).toBeLessThan(2)
  expect(Math.abs((dialogBox!.y + dialogBox!.height / 2) - viewport.height / 2)).toBeLessThan(2)
  await dialog.getByRole('button', { name: '确认删除' }).click()
  expect(deleted).toBe(true)
  await expect(page.locator('.guestbook .notice')).toContainText('已删除')
})

test('long guestbook author names stay inside their message card', async ({ page }) => {
  await page.route('**/api/guestbook**', route => route.fulfill({ json: { success: true, message: 'ok', data: {
    content: [{ id: 8, author: '这是一个很长很长不会超出卡片边界的留言昵称', content: '测试昵称布局。', createdAt: '2026-10-12T00:00:00Z' }],
    totalElements: 1,
  } } }))
  await page.goto('/#guestbook')
  const author = page.getByText('这是一个很长很长不会超出卡片边界的留言昵称')
  const article = author.locator('xpath=ancestor::article')
  const authorBox = await author.boundingBox()
  const articleBox = await article.boundingBox()
  expect(authorBox!.x).toBeGreaterThanOrEqual(articleBox!.x)
  expect(authorBox!.x + authorBox!.width).toBeLessThanOrEqual(articleBox!.x + articleBox!.width)
})

test('guestbook request errors use a compact message instead of an oversized icon', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('token', 'owner-token'))
  await page.route('**/api/auth/me', route => route.fulfill({ json: { success: true, message: 'ok', data: { id: 1, username: 'aquatic', nickname: 'Aquatic Lab', roles: ['ADMIN'] } } }))
  await page.route('**/api/admin/guestbook/1', route => route.fulfill({ status: 403, json: { success: false, message: '暂时无法保存', data: null } }))
  await page.goto('/#guestbook')
  await page.getByRole('button', { name: '编辑' }).click()
  await page.getByRole('button', { name: '保存' }).click()
  const message = page.locator('.el-message')
  await expect(message).toContainText('暂时无法保存')
  const box = await message.boundingBox()
  expect(box?.height).toBeLessThan(100)
  expect(box?.width).toBeLessThan(600)
  const iconBox = await message.locator('svg').first().boundingBox()
  expect(iconBox?.height).toBeLessThan(40)
  expect(iconBox?.width).toBeLessThan(40)
})

test('footer stays at the viewport bottom on a short screenful', async ({ page }) => {
  await page.setViewportSize({ width: 1280, height: 1800 })
  await page.goto('/')
  const bottom = await page.locator('footer').evaluate(element => element.getBoundingClientRect().bottom)
  expect(Math.abs(bottom - 1800)).toBeLessThan(2)
  const centerDifference = await page.locator('.footer-line').evaluate(element => {
    const copyright = element.querySelector('span')!.getBoundingClientRect()
    const line = element.getBoundingClientRect()
    return Math.abs((copyright.top + copyright.bottom) / 2 - (line.top + line.bottom) / 2)
  })
  expect(centerDifference).toBeLessThan(1)
})
