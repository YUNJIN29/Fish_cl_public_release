import { afterEach, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import CommandPalette from './CommandPalette.vue'

describe('CommandPalette', () => {
  afterEach(() => { document.body.innerHTML = ''; localStorage.clear() })
  it('opens with Ctrl+K and filters destinations', async () => {
    const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/', component: { template: '<div />' } }] })
    await router.push('/'); await router.isReady()
    const wrapper = mount(CommandPalette, { attachTo: document.body, global: { plugins: [router] } })
    window.dispatchEvent(new KeyboardEvent('keydown', { key: 'k', ctrlKey: true }))
    await wrapper.vm.$nextTick()
    const input = document.body.querySelector<HTMLInputElement>('input[type="search"]')
    expect(input).not.toBeNull()
    input!.value = '病害'
    input!.dispatchEvent(new Event('input'))
    await wrapper.vm.$nextTick()
    expect(document.body.textContent).toContain('病害知识库')
    expect(document.body.textContent).not.toContain('荣誉与证书')
    wrapper.unmount()
  })
})
