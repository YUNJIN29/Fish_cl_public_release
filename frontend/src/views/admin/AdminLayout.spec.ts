import { afterEach, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import AdminLayout from './AdminLayout.vue'
import { useAuthStore } from '@/stores/auth'

describe('AdminLayout', () => {
  afterEach(() => localStorage.clear())
  it('opens a touch-friendly mobile drawer with every admin destination', async () => {
    const pinia = createPinia()
    const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/admin', component: { template: '<div />' } }] })
    await router.push('/admin'); await router.isReady()
    const auth = useAuthStore(pinia)
    auth.$patch({ token: 'admin-token', meLoaded: true, me: { id: 1, username: 'admin', nickname: '管理员', roles: ['ADMIN'] } })
    const pass = { template: '<div><slot /></div>' }
    const wrapper = mount(AdminLayout, { global: { plugins: [pinia, router], stubs: {
      ElContainer: pass, ElAside: pass, ElHeader: pass, ElMain: pass, ElMenu: pass,
      ElMenuItem: { template: '<div><slot /></div>' }, ElButton: { template: '<button><slot /></button>' },
      ElDrawer: { props: ['modelValue'], template: '<aside v-if="modelValue" class="mobile-drawer"><slot /></aside>' }, RouterView: true,
    } } })
    await wrapper.get('[aria-label="打开管理导航"]').trigger('click')
    const drawer = wrapper.get('.mobile-drawer')
    expect(drawer.text()).toContain('仪表盘')
    expect(drawer.text()).toContain('资源管理')
    expect(drawer.get('[aria-label="关闭管理导航"]')).toBeTruthy()
  })
})
