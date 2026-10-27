import { expect, test } from '@playwright/test'

test.beforeEach(async ({ page }) => {
  await page.route('**/api/guestbook**', route => route.fulfill({ json: { success: true, message: 'ok', data: { content: [], totalElements: 0 } } }))
  await page.goto('/', { waitUntil: 'domcontentloaded' })
  await page.evaluate(() => localStorage.removeItem('aquatic:site-pet'))
  await page.reload({ waitUntil: 'domcontentloaded' })
})

test('site pet can talk, hide, and return', async ({ page }) => {
  const pet = page.getByTestId('site-pet')
  await expect(pet).toBeVisible()
  await expect(pet).toHaveCSS('width', '250px')
  await expect(pet.getByRole('img', { name: '蓝色大肥鱼桌宠' })).toHaveCount(1)
  await expect(pet.locator('video[aria-label="蓝色大肥鱼桌宠动画"]')).toBeVisible()
  await pet.getByRole('button', { name: /和蓝色大肥鱼互动/ }).click()
  await expect(pet.getByRole('status')).toBeVisible()
  await pet.hover()
  await pet.getByRole('button', { name: '隐藏桌宠' }).click()
  await expect(pet).toBeHidden()
  await page.getByRole('button', { name: '唤回蓝色大肥鱼' }).click()
  await expect(page.getByTestId('site-pet')).toBeVisible()
})

test('site pet is draggable and remains inside the viewport', async ({ page }) => {
  const pet = page.getByTestId('site-pet')
  const before = await pet.boundingBox()
  await page.mouse.move(before!.x + before!.width / 2, before!.y + before!.height / 2)
  await page.mouse.down()
  await page.mouse.move(30, 80, { steps: 8 })
  await page.mouse.up()
  const after = await pet.boundingBox()
  const viewport = page.viewportSize()!
  expect(after!.x).toBeGreaterThanOrEqual(7)
  expect(after!.y).toBeGreaterThanOrEqual(7)
  expect(after!.x + after!.width).toBeLessThanOrEqual(viewport.width - 7)
  expect(after!.y + after!.height).toBeLessThanOrEqual(viewport.height - 7)
  expect(Math.abs(after!.x - before!.x)).toBeGreaterThan(20)
})

test('site pet persists its hidden state after navigation reload', async ({ page }) => {
  const pet = page.getByTestId('site-pet')
  await pet.hover()
  await pet.getByRole('button', { name: '隐藏桌宠' }).click()
  await page.reload()
  await expect(page.getByTestId('site-pet')).toHaveCount(0)
  await expect(page.getByRole('button', { name: '唤回蓝色大肥鱼' })).toBeVisible()
})

test('site pet provides a desktop-style context menu', async ({ page }) => {
  const pet = page.getByTestId('site-pet')
  await pet.click({ button: 'right' })
  const menu = page.getByRole('menu', { name: '桌宠菜单' })
  await expect(menu).toBeVisible()
  await expect(menu.getByRole('menuitem', { name: /随机动作/ })).toBeVisible()
  await menu.getByRole('menuitem', { name: /回到右下角/ }).click()
  const box = await pet.boundingBox()
  const viewport = page.viewportSize()!
  expect(box!.x + box!.width).toBeGreaterThan(viewport.width - 40)
})

test('site pet settings persist size and quiet mode', async ({ page }) => {
  const pet = page.getByTestId('site-pet')
  await pet.hover()
  await pet.getByRole('button', { name: '打开桌宠设置' }).click()
  const dialog = page.getByRole('dialog', { name: '蓝色大肥鱼桌宠设置' })
  await expect(dialog).toBeVisible()
  await dialog.locator('#pet-size').fill('200')
  await dialog.getByText('免打扰', { exact: true }).click()
  await dialog.getByRole('button', { name: '完成' }).click()
  await expect(pet).toHaveCSS('width', '200px')
  await page.reload({ waitUntil: 'domcontentloaded' })
  await expect(page.getByTestId('site-pet')).toHaveCSS('width', '200px')
  await page.getByTestId('site-pet').hover()
  await page.getByRole('button', { name: '打开桌宠设置' }).click()
  await expect(page.getByRole('dialog').getByRole('checkbox').nth(2)).toBeChecked()
})

test('site pet keeps a visible poster when animation cannot play', async ({ page }) => {
  await page.route('**/assets/pet/*.webm', route => route.abort())
  await page.reload({ waitUntil: 'domcontentloaded' })
  const pet = page.getByTestId('site-pet')
  await expect(pet.getByRole('img', { name: '蓝色大肥鱼桌宠' })).toBeVisible()
})
