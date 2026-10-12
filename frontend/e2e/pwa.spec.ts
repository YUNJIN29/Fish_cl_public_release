import { expect, test } from '@playwright/test'

test('site exposes an installable manifest, service worker and responsive install prompt', async ({ page, request }) => {
  const manifestResponse = await request.get('/manifest.webmanifest')
  expect(manifestResponse.ok()).toBeTruthy()
  const manifest = await manifestResponse.json()
  expect(manifest.name).toContain('Aquatic Lab')
  expect(manifest.display).toBe('standalone')
  expect(manifest.shortcuts.map((item:any) => item.url)).toContain('/fish/tasks')

  const workerResponse = await request.get('/sw.js')
  expect(workerResponse.ok()).toBeTruthy()
  const worker = await workerResponse.text()
  expect(worker).toContain("url.pathname.startsWith('/api/')")
  expect(worker).toContain("url.pathname.startsWith('/diagnose-images/')")

  await page.goto('/')
  await page.evaluate(() => {
    const promptEvent = new Event('beforeinstallprompt', { cancelable:true })
    Object.defineProperty(promptEvent, 'prompt', { value:async () => undefined })
    Object.defineProperty(promptEvent, 'userChoice', { value:Promise.resolve({ outcome:'accepted' }) })
    window.dispatchEvent(promptEvent)
  })
  const status = page.getByRole('status')
  await expect(status).toContainText('安装 Aquatic Lab 到设备')
  await expect(status.getByRole('button', { name:'安装', exact:true })).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBeTruthy()

  await page.evaluate(() => window.dispatchEvent(new Event('offline')))
  await expect(status).toContainText('当前处于离线状态')
  await page.evaluate(() => window.dispatchEvent(new Event('online')))
  await expect(status).toContainText('安装 Aquatic Lab 到设备')

  await page.evaluate(() => navigator.serviceWorker.ready.then(() => true))
  await page.reload()
  expect(await page.evaluate(() => Boolean(navigator.serviceWorker.controller))).toBeTruthy()
  await page.context().setOffline(true)
  try {
    await page.goto('/offline-check', { waitUntil:'domcontentloaded' })
    await expect(page.getByRole('heading', { name:'当前网络不可用' })).toBeVisible()
    await expect(page.getByText('不会把诊断记录、病灶图片或个人数据保存到离线缓存')).toBeVisible()
  } finally {
    await page.context().setOffline(false)
  }
})
