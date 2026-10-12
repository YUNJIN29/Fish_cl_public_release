import { afterEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import GameLeaderboard from './GameLeaderboard.vue'

vi.mock('vue-router', () => ({ useRouter: () => ({ push: vi.fn(), currentRoute: { value: { fullPath: '/game' } } }) }))

describe('GameLeaderboard', () => {
  afterEach(() => { vi.unstubAllGlobals(); vi.restoreAllMocks(); localStorage.clear() })

  it('renders anonymous player aliases without network identity fields', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ success: true, data: [{ player: '玩家-AB12', score: 120, mine: true }] }),
    }))
    const wrapper=mount(GameLeaderboard,{props:{game:'snake',score:120}})
    await flushPromises()

    expect(wrapper.text()).toContain('玩家-AB12')
    expect(wrapper.text()).toContain('我的成绩')
    expect(wrapper.get('.row').classes()).toContain('mine')
    expect(wrapper.text()).toContain('不公开账号、IP 或地区')
    expect(wrapper.text()).not.toContain('203.0.113')
  })
})
