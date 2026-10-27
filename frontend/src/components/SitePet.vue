<template>
  <div v-if="visible" ref="petRoot" class="site-pet" :class="{ dragging, reacting, quiet, ready: positionReady }" :style="petStyle" data-testid="site-pet" @contextmenu.prevent="openMenu">
    <p v-if="bubble && bubblesEnabled" class="pet-bubble" role="status">{{ bubble }}</p>
    <div class="pet-tools">
      <button type="button" title="桌宠设置" aria-label="打开桌宠设置" @click.stop="openSettings">⚙</button>
      <button type="button" title="暂时隐藏蓝色大肥鱼" aria-label="隐藏桌宠" @click.stop="hidePet">×</button>
    </div>
    <button class="pet-character" type="button" aria-label="和蓝色大肥鱼互动；按住可拖动" @pointerdown="startDrag">
      <span class="pet-ripple" aria-hidden="true" />
      <img v-show="!videoVisible" class="pet-poster" src="/assets/pet/shenshen-poster.png" alt="蓝色大肥鱼桌宠" draggable="false">
      <video ref="petVideo" :key="currentVideo" :class="{ visible: videoVisible }" :src="currentVideo" autoplay muted playsinline :loop="!interacting" aria-label="蓝色大肥鱼桌宠动画" @timeupdate="markVideoVisible" @error="videoVisible = false" @ended="returnToIdle" />
    </button>
    <span class="pet-shadow" aria-hidden="true" />
  </div>
  <button v-else class="pet-restore" type="button" aria-label="唤回蓝色大肥鱼" title="唤回蓝色大肥鱼" @click="restorePet">🐋</button>

  <div v-if="menuOpen" ref="menuRoot" class="pet-menu" :style="menuStyle" role="menu" aria-label="桌宠菜单">
    <strong>蓝色大肥鱼</strong>
    <button type="button" role="menuitem" @click="interactFromMenu">✨ 和我互动</button>
    <button type="button" role="menuitem" @click="randomAction">🎬 随机动作</button>
    <button type="button" role="menuitem" @click="openSettings">⚙ 桌宠设置</button>
    <button type="button" role="menuitem" @click="resetPosition">↘ 回到右下角</button>
    <button type="button" role="menuitem" @click="hidePet">💧 暂时隐藏</button>
  </div>

  <div v-if="settingsOpen" class="pet-dialog-backdrop" @click.self="closeSettings">
    <section class="pet-dialog" role="dialog" aria-modal="true" aria-labelledby="pet-settings-title">
      <header><div><small>DESKTOP PET</small><h2 id="pet-settings-title">蓝色大肥鱼桌宠设置</h2></div><button type="button" aria-label="关闭桌宠设置" @click="closeSettings">×</button></header>
      <label class="pet-size-control" for="pet-size">桌宠大小 <output>{{ size }}px</output></label>
      <input id="pet-size" v-model.number="size" type="range" min="60" max="600" step="10" @input="applySettings">
      <label class="pet-switch"><span><b>自动活动</b><small>偶尔移动并和你打招呼</small></span><input v-model="autoActivity" type="checkbox" @change="applySettings"></label>
      <label class="pet-switch"><span><b>显示气泡</b><small>显示互动与自言自语内容</small></span><input v-model="bubblesEnabled" type="checkbox" @change="applySettings"></label>
      <label class="pet-switch"><span><b>免打扰</b><small>暂停自动活动与自动发言</small></span><input v-model="quiet" type="checkbox" @change="applySettings"></label>
      <footer><button type="button" class="secondary" @click="resetPosition">重置位置</button><button type="button" class="primary" @click="closeSettings">完成</button></footer>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'

const STORAGE_KEY = 'aquatic:site-pet'
type PetConfig = { visible?: boolean; x?: number; y?: number; size?: number; autoActivity?: boolean; bubblesEnabled?: boolean; quiet?: boolean }
const petRoot = ref<HTMLElement | null>(null)
const petVideo = ref<HTMLVideoElement | null>(null)
const menuRoot = ref<HTMLElement | null>(null)
const visible = ref(true), dragging = ref(false), reacting = ref(false), positionReady = ref(false)
const bubble = ref('嗨，我是蓝色大肥鱼！'), settingsOpen = ref(false), menuOpen = ref(false)
const menuX = ref(0), menuY = ref(0), x = ref(0), y = ref(0), size = ref(250)
const autoActivity = ref(true), bubblesEnabled = ref(true), quiet = ref(false)
const IDLE_VIDEO = '/assets/pet/shenshen-idle.webm'
const INTERACTION_VIDEOS = [
  '/assets/pet/shenshen-wave.webm',
  '/assets/pet/shenshen-code.webm',
  '/assets/pet/shenshen-stretch.webm',
  '/assets/pet/shenshen-bubbles.webm'
]
const DRAG_VIDEO = '/assets/pet/shenshen-drag.webm'
const currentVideo = ref(IDLE_VIDEO), interacting = ref(false)
const videoVisible = ref(false)
const petStyle = computed(() => ({ transform: `translate3d(${x.value}px, ${y.value}px, 0)`, '--pet-size': `${size.value}px` }))
const menuStyle = computed(() => ({ left: `${menuX.value}px`, top: `${menuY.value}px` }))
const phrases = ['今天也要元气满满！', '要去看看水产智诊平台吗？', '摸摸头，再出发～', '水质稳定，心情也晴朗！', '发现一条有趣的留言 🐟', '我会陪你逛完整个网站。']
let startX = 0, startY = 0, pointerStartX = 0, pointerStartY = 0, moved = false, bubbleTimer = 0, idleTimer = 0

function bounds() { const rect = petRoot.value?.getBoundingClientRect(); return { width: rect?.width || size.value, height: rect?.height || size.value * .95 } }
function clampPosition(nextX: number, nextY: number) { const box = bounds(); x.value = Math.min(Math.max(8, nextX), Math.max(8, window.innerWidth - box.width - 8)); y.value = Math.min(Math.max(8, nextY), Math.max(8, window.innerHeight - box.height - 8)) }
function saveConfig() { localStorage.setItem(STORAGE_KEY, JSON.stringify({ visible: visible.value, x: x.value, y: y.value, size: size.value, autoActivity: autoActivity.value, bubblesEnabled: bubblesEnabled.value, quiet: quiet.value })) }
function playAction(source: string) { interacting.value = true; videoVisible.value = false; currentVideo.value = source; nextTick(() => petVideo.value?.play().catch(() => undefined)) }
function playInteraction() { playAction(INTERACTION_VIDEOS[Math.floor(Math.random() * INTERACTION_VIDEOS.length)]) }
function returnToIdle() { interacting.value = false; videoVisible.value = false; currentVideo.value = IDLE_VIDEO; nextTick(() => petVideo.value?.play().catch(() => undefined)) }
function markVideoVisible() { if ((petVideo.value?.currentTime || 0) > .05) videoVisible.value = true }
function speak(text = phrases[Math.floor(Math.random() * phrases.length)]) { bubble.value = bubblesEnabled.value ? text : ''; reacting.value = false; playInteraction(); requestAnimationFrame(() => { reacting.value = true }); window.clearTimeout(bubbleTimer); bubbleTimer = window.setTimeout(() => { bubble.value = ''; reacting.value = false }, 3600) }
function startDrag(event: PointerEvent) { if (event.button !== 0) return; closeMenu(); dragging.value = true; moved = false; playAction(DRAG_VIDEO); pointerStartX = event.clientX; pointerStartY = event.clientY; startX = x.value; startY = y.value; (event.currentTarget as HTMLElement).setPointerCapture(event.pointerId); window.addEventListener('pointermove', dragPet); window.addEventListener('pointerup', stopDrag, { once: true }) }
function dragPet(event: PointerEvent) { const dx = event.clientX - pointerStartX, dy = event.clientY - pointerStartY; moved ||= Math.hypot(dx, dy) > 5; clampPosition(startX + dx, startY + dy) }
function stopDrag() { window.removeEventListener('pointermove', dragPet); dragging.value = false; saveConfig(); if (!moved) speak(quiet.value ? '免打扰中，摸摸头也可以～' : undefined); else returnToIdle() }
function openMenu(event: MouseEvent) { menuX.value = Math.max(8, Math.min(event.clientX, window.innerWidth - 188)); menuY.value = Math.max(8, Math.min(event.clientY, window.innerHeight - 194)); menuOpen.value = true }
function closeMenu() { menuOpen.value = false }
function interactFromMenu() { closeMenu(); speak(quiet.value ? '免打扰中，摸摸头也可以～' : undefined) }
function randomAction() { closeMenu(); speak(['看我露一手～', '休息一下，再继续！', '一起摸会儿鱼吧 🐋'][Math.floor(Math.random() * 3)]) }
function openSettings() { closeMenu(); settingsOpen.value = true }
function closeSettings() { settingsOpen.value = false; saveConfig() }
function applySettings() { nextTick(() => { clampPosition(x.value, y.value); saveConfig() }) }
function hidePet() { closeMenu(); settingsOpen.value = false; visible.value = false; bubble.value = ''; saveConfig() }
async function restorePet() { positionReady.value = false; visible.value = true; await nextTick(); setDefaultPosition(); requestAnimationFrame(() => { positionReady.value = true }); speak('我回来啦！'); saveConfig() }
function setDefaultPosition() { clampPosition(window.innerWidth - bounds().width - 28, window.innerHeight - bounds().height - 18) }
function resetPosition() { closeMenu(); setDefaultPosition(); saveConfig(); speak('回到熟悉的位置啦！') }
function handleResize() { clampPosition(x.value, y.value); closeMenu() }
function handleDocumentPointer(event: PointerEvent) { if (menuOpen.value && !menuRoot.value?.contains(event.target as Node)) closeMenu() }
function handleKeydown(event: KeyboardEvent) { if (event.key === 'Escape') { closeMenu(); closeSettings() } }
function idleMove() { if (!visible.value || !autoActivity.value || quiet.value || dragging.value || settingsOpen.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return; const direction = Math.random() > .5 ? 1 : -1; clampPosition(x.value + direction * (24 + Math.random() * 54), y.value); if (Math.random() > .55) speak() }

onMounted(async () => {
  let saved: PetConfig | null = null
  try { saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null') } catch { saved = null }
  visible.value = saved?.visible !== false
  if (Number.isFinite(saved?.size)) size.value = Math.min(600, Math.max(60, saved!.size!))
  autoActivity.value = saved?.autoActivity !== false; bubblesEnabled.value = saved?.bubblesEnabled !== false; quiet.value = saved?.quiet === true
  await nextTick()
  if (Number.isFinite(saved?.x) && Number.isFinite(saved?.y)) clampPosition(saved!.x!, saved!.y!); else setDefaultPosition()
  requestAnimationFrame(() => { positionReady.value = true })
  window.addEventListener('resize', handleResize); document.addEventListener('pointerdown', handleDocumentPointer); document.addEventListener('keydown', handleKeydown)
  bubbleTimer = window.setTimeout(() => { bubble.value = '' }, 4200); idleTimer = window.setInterval(idleMove, 12000)
})
onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); window.removeEventListener('pointermove', dragPet); document.removeEventListener('pointerdown', handleDocumentPointer); document.removeEventListener('keydown', handleKeydown); window.clearTimeout(bubbleTimer); window.clearInterval(idleTimer) })
</script>

<style scoped>
.site-pet{position:fixed;z-index:9500;left:0;top:0;width:var(--pet-size);height:calc(var(--pet-size) * .95);display:flex;align-items:flex-end;justify-content:center;touch-action:none;user-select:none;transition:transform 1.8s cubic-bezier(.2,.8,.25,1);filter:drop-shadow(0 12px 14px rgba(15,46,87,.2))}.site-pet.dragging{transition:none;cursor:grabbing}.pet-character{position:relative;z-index:2;width:100%;height:100%;padding:0;border:0;background:transparent;cursor:grab;touch-action:none}.pet-character:focus-visible{outline:3px solid #00a4d4;outline-offset:3px;border-radius:40%}.pet-character video{position:absolute;inset:0;width:100%;height:100%;object-fit:contain;pointer-events:none}.reacting .pet-character{animation:pet-hop .55s ease}.pet-ripple{position:absolute;z-index:-1;left:50%;bottom:21%;width:54%;aspect-ratio:1;border-radius:50%;background:radial-gradient(circle,rgba(0,212,255,.22),transparent 68%);transform:translateX(-50%);animation:pet-glow 2.4s ease-in-out infinite}.pet-shadow{position:absolute;left:24%;right:24%;bottom:3px;height:12px;border-radius:50%;background:rgba(24,62,104,.18);filter:blur(5px)}.pet-bubble{position:absolute;z-index:4;right:68%;bottom:78%;width:max-content;max-width:min(230px,65vw);margin:0;padding:9px 12px;border:1px solid #b8e9f7;border-radius:14px 14px 3px 14px;background:rgba(255,255,255,.96);color:#23415e;font-size:13px;line-height:1.5;box-shadow:0 8px 24px rgba(17,68,108,.14);animation:bubble-in .22s ease-out}.pet-tools{position:absolute;z-index:5;right:-2px;top:20%;display:flex;gap:5px;opacity:0;transition:.2s}.pet-tools button{width:29px;height:29px;padding:0;border:1px solid rgba(0,144,200,.22);border-radius:50%;background:rgba(255,255,255,.92);color:#55758e;font-size:16px;line-height:1;cursor:pointer;box-shadow:0 4px 12px rgba(17,68,108,.12)}.site-pet:hover .pet-tools,.pet-tools:focus-within{opacity:1}.pet-restore{position:fixed;z-index:9500;right:14px;bottom:14px;width:46px;height:46px;border:1px solid #9edff2;border-radius:50%;background:rgba(255,255,255,.94);box-shadow:0 8px 24px rgba(17,68,108,.18);font-size:22px;cursor:pointer}.pet-restore:focus-visible,.pet-tools button:focus-visible{outline:3px solid #00a4d4;outline-offset:2px}.pet-menu{position:fixed;z-index:9700;width:180px;padding:8px;border:1px solid #cce5ed;border-radius:14px;background:rgba(255,255,255,.98);box-shadow:0 14px 42px rgba(17,46,75,.22);color:#173b56}.pet-menu strong{display:block;padding:7px 10px 9px;font-size:13px;color:#008ebc}.pet-menu button{display:block;width:100%;min-height:38px;padding:8px 10px;border:0;border-radius:9px;background:transparent;color:#294b62;text-align:left;cursor:pointer}.pet-menu button:hover,.pet-menu button:focus-visible{background:#eaf8fc;outline:none}.pet-dialog-backdrop{position:fixed;z-index:9800;inset:0;display:grid;place-items:center;padding:20px;background:rgba(12,34,55,.28);backdrop-filter:blur(3px)}.pet-dialog{width:min(390px,100%);padding:20px;border:1px solid #c6e8f1;border-radius:22px;background:#fff;color:#17364e;box-shadow:0 22px 70px rgba(12,42,65,.28)}.pet-dialog header{display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:18px}.pet-dialog header small{color:#00a0cf;letter-spacing:.18em}.pet-dialog h2{margin:3px 0 0;font-size:23px}.pet-dialog header button{width:36px;height:36px;border:0;border-radius:50%;background:#edf6f9;color:#48697f;font-size:22px;cursor:pointer}.pet-size-control{display:flex;justify-content:space-between;font-weight:700}.pet-size-control output{color:#008fbd}.pet-dialog input[type=range]{width:100%;margin:12px 0 17px;accent-color:#009fcf}.pet-switch{display:flex;align-items:center;justify-content:space-between;gap:18px;padding:13px 0;border-top:1px solid #e5f0f4}.pet-switch span{display:grid;gap:3px}.pet-switch small{color:#71899a}.pet-switch input{width:22px;height:22px;accent-color:#009fcf}.pet-dialog footer{display:flex;justify-content:flex-end;gap:10px;margin-top:18px}.pet-dialog footer button{min-height:42px;padding:0 18px;border-radius:11px;cursor:pointer}.pet-dialog .secondary{border:1px solid #c8dce4;background:#fff;color:#35576d}.pet-dialog .primary{border:0;background:#008fbd;color:#fff}@keyframes pet-hop{0%,100%{transform:translateY(0) scale(1)}45%{transform:translateY(-10px) scale(1.03,.97)}}@keyframes pet-glow{0%,100%{transform:translateX(-50%) scale(.88);opacity:.55}50%{transform:translateX(-50%) scale(1.15);opacity:1}}@keyframes bubble-in{from{opacity:0;transform:translateY(6px) scale(.96)}to{opacity:1;transform:none}}@media(max-width:600px){.pet-bubble{right:48%;bottom:82%;font-size:12px}.pet-tools{top:16%;opacity:.9}.pet-tools button{width:34px;height:34px}.pet-dialog{padding:17px}}@media(prefers-reduced-motion:reduce){.site-pet{transition:none}.pet-character,.pet-ripple{animation:none}}
.site-pet{width:min(var(--pet-size),calc(100vw - 16px));height:min(calc(var(--pet-size) * .95),calc((100vw - 16px) * .95));transition:none}.site-pet.ready{transition:transform 1.8s cubic-bezier(.2,.8,.25,1)}.site-pet.dragging{transition:none}.pet-character video{opacity:0}.pet-character video.visible{opacity:1}.pet-poster{position:absolute;inset:0;width:100%;height:100%;object-fit:contain;pointer-events:none}
</style>
