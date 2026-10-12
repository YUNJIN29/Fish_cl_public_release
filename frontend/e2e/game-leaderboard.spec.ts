import { expect, test } from '@playwright/test'

test('game leaderboard uses account-owned anonymous aliases without exposing network identity', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('token', 'game-test-token'))
  await page.route('**/api/auth/me', route => route.fulfill({ json: { success: true, message: 'ok', data: { id: 7, username: 'private-user', roles: ['USER'] } } }))
  let submitted: Record<string, unknown> | undefined
  await page.route('**/api/game-scores**', async route => {
    if (route.request().method() === 'POST') submitted = await route.request().postDataJSON()
    return route.fulfill({ json: { success: true, message: 'ok', data: [{ player: '玩家-AB12', score: 120, mine: true }] } })
  })

  await page.goto('/personal/games/snake')
  const leaderboard=page.locator('.leader')
  await expect(leaderboard).toContainText('玩家-AB12')
  await expect(leaderboard).toContainText('我的成绩')
  await expect(leaderboard.locator('.row.mine')).toBeVisible()
  await expect(leaderboard).toContainText('不公开账号、IP 或地区')
  await expect(leaderboard).not.toContainText('private-user')
  await leaderboard.getByRole('button',{name:/提交成绩/}).click()
  await expect.poll(()=>submitted?.game).toBe('snake')
  expect(submitted?.score).toBe(0)
  await expect(leaderboard).toContainText('每个账号仅保留该游戏最高分')
  expect(await page.evaluate(()=>document.documentElement.scrollWidth<=document.documentElement.clientWidth)).toBeTruthy()
})
