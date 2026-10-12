import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import NotFoundView from './NotFoundView.vue'
describe('404 page', () => { it('offers a route back to the portal', () => { const wrapper = mount(NotFoundView, { global: { stubs: { RouterLink: { template: '<a><slot /></a>' } } } }); expect(wrapper.text()).toContain('404'); expect(wrapper.text()).toContain('返回 Aquatic Lab 首页') }) })
