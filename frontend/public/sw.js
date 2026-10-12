const SHELL_CACHE = 'yunjin-shell-v2'
const ASSET_CACHE = 'yunjin-assets-v2'
const SHELL_FILES = ['/offline.html', '/manifest.webmanifest', '/favicon.svg']

self.addEventListener('install', event => {
  event.waitUntil(caches.open(SHELL_CACHE).then(cache => cache.addAll(SHELL_FILES)))
  self.skipWaiting()
})

self.addEventListener('activate', event => {
  event.waitUntil(caches.keys().then(keys => Promise.all(keys
    .filter(key => ![SHELL_CACHE, ASSET_CACHE].includes(key))
    .map(key => caches.delete(key)))).then(() => self.clients.claim()))
})

self.addEventListener('fetch', event => {
  const request = event.request
  if (request.method !== 'GET') return
  const url = new URL(request.url)
  if (url.origin !== self.location.origin || url.pathname.startsWith('/api/') || url.pathname.startsWith('/diagnose-images/') || url.pathname.startsWith('/uploads/')) return

  if (request.mode === 'navigate') {
    event.respondWith(fetch(request).catch(() => caches.match('/offline.html')))
    return
  }

  if (url.pathname.startsWith('/assets/')) {
    event.respondWith(caches.open(ASSET_CACHE).then(async cache => {
      const cached = await cache.match(request)
      if (cached) return cached
      const response = await fetch(request)
      if (response.ok) {
        await cache.put(request, response.clone())
        const keys = await cache.keys()
        if (keys.length > 80) await Promise.all(keys.slice(0, keys.length - 80).map(key => cache.delete(key)))
      }
      return response
    }))
  }
})
