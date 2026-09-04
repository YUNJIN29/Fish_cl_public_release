import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref<boolean>(
    localStorage.getItem('theme') === 'dark' ? true : false
  )

  function apply(dark: boolean) {
    if (dark) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
    localStorage.setItem('theme', dark ? 'dark' : 'light')
  }

  function toggle() {
    isDark.value = !isDark.value
  }

  watch(isDark, apply, { immediate: true })

  return { isDark, toggle }
})
