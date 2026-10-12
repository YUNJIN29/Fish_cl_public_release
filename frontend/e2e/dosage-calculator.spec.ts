import { expect, test } from '@playwright/test'

test('calculates label dosage and keeps a local recent calculation', async ({ page }) => {
  await page.goto('/fish/calculator', { waitUntil: 'domcontentloaded' })
  await expect(page.getByRole('heading', { name: '水体用量计算器' })).toBeVisible()
  await page.getByLabel('长度（m）').fill('20')
  await page.getByLabel('宽度（m）').fill('10')
  await page.getByLabel('平均水深（m）').fill('1.5')
  await page.getByLabel('产品标签剂量（g/m³）').fill('0.5')
  await expect(page.locator('.mass-result strong')).toHaveText('150')
  await expect(page.locator('.mass-result')).toContainText('g')
  await page.getByRole('button', { name: '保存到本机' }).click()
  await expect(page.getByRole('heading', { name: '最近计算' })).toBeVisible()
  await expect(page.locator('.history-list')).toContainText('300 m³')
  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false)
})

test('converts an active ingredient target without recommending a dose', async ({ page }) => {
  await page.goto('/fish/calculator', { waitUntil: 'domcontentloaded' })
  await page.getByText('直接输入体积', { exact: true }).click()
  await page.getByLabel('水体体积（m³）').fill('100')
  await page.getByText('有效成分 mg/L', { exact: true }).click()
  await page.getByLabel('目标有效成分（mg/L）').fill('0.2')
  await page.getByLabel('产品有效成分含量（%）').fill('20')
  await expect(page.locator('.mass-result strong')).toHaveText('100')
  await expect(page.getByText('只做数学换算，不提供用药建议')).toBeVisible()
  await expect(page.getByText('目标浓度 × 体积 ÷ 有效成分比例')).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)).toBe(false)
})
