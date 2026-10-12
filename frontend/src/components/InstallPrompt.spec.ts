import { afterEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InstallPrompt from './InstallPrompt.vue'

describe('InstallPrompt', () => {
  afterEach(() => { localStorage.clear(); Object.defineProperty(navigator, 'onLine', { configurable:true, value:true }) })

  it('offers installation only after the browser provides an install event', async () => {
    const prompt = vi.fn().mockResolvedValue(undefined)
    const event = new Event('beforeinstallprompt', { cancelable:true }) as Event & { prompt: () => Promise<void>; userChoice: Promise<{ outcome:'accepted' }> }
    Object.defineProperty(event, 'prompt', { value:prompt })
    Object.defineProperty(event, 'userChoice', { value:Promise.resolve({ outcome:'accepted' }) })
    const wrapper = mount(InstallPrompt)

    window.dispatchEvent(event)
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('安装 Aquatic Lab 到设备')
    await wrapper.get('button').trigger('click')
    await Promise.resolve()
    expect(prompt).toHaveBeenCalledOnce()
    wrapper.unmount()
  })

  it('shows a privacy-safe offline status', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable:true, value:false })
    const wrapper = mount(InstallPrompt)
    expect(wrapper.text()).toContain('当前处于离线状态')
    expect(wrapper.text()).toContain('业务数据将在恢复网络后加载')
    window.dispatchEvent(new Event('online'))
    await wrapper.vm.$nextTick()
    expect(wrapper.find('aside').exists()).toBe(false)
    wrapper.unmount()
  })
})
