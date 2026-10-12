import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useThemeStore } from './theme'
describe('theme state', () => {
  beforeEach(() => { localStorage.clear(); document.documentElement.className = ''; setActivePinia(createPinia()) })
  it('persists and applies dark mode', async () => { const store = useThemeStore(); store.toggle(); await Promise.resolve(); expect(document.documentElement.classList.contains('dark')).toBe(true); expect(localStorage.getItem('theme')).toBe('dark') })
})
