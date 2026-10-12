import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import ProtectedImage from './ProtectedImage.vue'

describe('ProtectedImage', () => {
  beforeEach(() => {
    localStorage.setItem('token', 'private-token')
    Object.defineProperty(URL, 'createObjectURL', { configurable: true, value: vi.fn(() => 'blob:protected-image') })
    Object.defineProperty(URL, 'revokeObjectURL', { configurable: true, value: vi.fn() })
  })
  afterEach(() => { localStorage.clear(); vi.unstubAllGlobals(); vi.restoreAllMocks() })

  it('loads a private image with the JWT and revokes its object URL', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, blob: async () => new Blob(['image'], { type: 'image/png' }) })
    vi.stubGlobal('fetch', fetchMock)
    const wrapper = mount(ProtectedImage, {
      props: { src: '/diagnose-images/private.png', alt: '病灶图片' },
      global: { stubs: { ElImage: { props: ['src', 'alt'], template: '<img :src="src" :alt="alt">' } } },
    })
    await flushPromises()
    expect(fetchMock).toHaveBeenCalledWith('/diagnose-images/private.png', expect.objectContaining({ headers: { Authorization: 'Bearer private-token' } }))
    expect(wrapper.get('img').attributes('src')).toBe('blob:protected-image')
    wrapper.unmount()
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:protected-image')
  })
})
