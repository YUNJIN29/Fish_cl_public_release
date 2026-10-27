import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { findDestination, siteDestinations } from '@/data/siteDestinations'

export type RecentPage = { path: string; title: string; visitedAt: number }
const RECENT_KEY = 'aquatic:recent-pages'
const PINNED_KEY = 'aquatic:pinned-paths'
const defaultPins = ['/profile', '/personal/games', '/fish/diagnose', '/fish/diseases']

function readList<T>(key: string, fallback: T[]): T[] {
  try { const value = JSON.parse(localStorage.getItem(key) || 'null'); return Array.isArray(value) ? value : fallback }
  catch { return fallback }
}

export const useWorkspaceStore = defineStore('workspace', () => {
  const recent = ref<RecentPage[]>([])
  const pinnedPaths = ref<string[]>([])
  const pinned = computed(() => pinnedPaths.value.map((path) => siteDestinations.find((item) => item.path === path)).filter((item): item is (typeof siteDestinations)[number] => Boolean(item)))
  const recentDestinations = computed(() => recent.value.filter((entry) => entry.path !== '/personal').map((entry) => ({ ...entry, destination: findDestination(entry.path) })).filter((entry) => Boolean(entry.destination)).slice(0, 5))
  const continuePage = computed(() => recentDestinations.value[0] || null)

  function load() {
    recent.value = readList<RecentPage>(RECENT_KEY, [])
    pinnedPaths.value = readList<string>(PINNED_KEY, defaultPins).filter((path) => siteDestinations.some((item) => item.path === path))
    if (!pinnedPaths.value.length) pinnedPaths.value = [...defaultPins]
  }
  function togglePin(path: string) {
    pinnedPaths.value = pinnedPaths.value.includes(path) ? pinnedPaths.value.filter((item) => item !== path) : [...pinnedPaths.value, path].slice(-6)
    localStorage.setItem(PINNED_KEY, JSON.stringify(pinnedPaths.value))
  }
  function clearRecent() {
    recent.value = []; localStorage.removeItem(RECENT_KEY); window.dispatchEvent(new Event('aquatic:recent-pages'))
  }
  return { recent, pinnedPaths, pinned, recentDestinations, continuePage, load, togglePin, clearRecent }
})
