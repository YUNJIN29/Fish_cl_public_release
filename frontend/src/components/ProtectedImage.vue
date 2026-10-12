<template>
  <el-image v-if="objectUrl" :src="objectUrl" :fit="fit" :alt="alt" :preview-src-list="preview ? [objectUrl] : []" :hide-on-click-modal="true" />
  <div v-else-if="loading" class="protected-image-state" role="img" :aria-label="`${alt}加载中`"><span></span></div>
  <button v-else class="protected-image-state error" type="button" :aria-label="`${alt}加载失败，点击重试`" @click.stop="load"><b>!</b><small>重试</small></button>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'

const props = withDefaults(defineProps<{ src: string; fit?: 'fill' | 'contain' | 'cover' | 'none' | 'scale-down'; preview?: boolean; alt?: string }>(), {
  fit: 'cover', preview: false, alt: '受保护图片',
})
const objectUrl = ref(''); const loading = ref(false); let requestVersion = 0

function clearObjectUrl() { if (objectUrl.value) URL.revokeObjectURL(objectUrl.value); objectUrl.value = '' }
async function load() {
  const version = ++requestVersion
  clearObjectUrl()
  if (!props.src) return
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(props.src, { headers: token ? { Authorization: `Bearer ${token}` } : {}, cache: 'no-store' })
    if (!response.ok) throw new Error(`Image request failed: ${response.status}`)
    const blob = await response.blob()
    if (!blob.type.startsWith('image/')) throw new Error('Protected resource is not an image')
    if (version === requestVersion) objectUrl.value = URL.createObjectURL(blob)
  } catch { if (version === requestVersion) objectUrl.value = '' }
  finally { if (version === requestVersion) loading.value = false }
}
watch(() => props.src, load, { immediate: true })
onBeforeUnmount(() => { requestVersion++; clearObjectUrl() })
</script>

<style scoped>
.protected-image-state{width:100%;height:100%;min-height:54px;display:grid;place-items:center;border:0;background:var(--c-bg-soft);color:var(--c-text-3)}.protected-image-state span{width:22px;height:22px;border:2px solid var(--c-border);border-top-color:var(--c-cyan);border-radius:50%;animation:spin .8s linear infinite}.protected-image-state.error{align-content:center;gap:2px;cursor:pointer}.protected-image-state.error b{color:var(--c-purple);font-size:18px}.protected-image-state.error small{font-size:10px}@keyframes spin{to{transform:rotate(360deg)}}@media(prefers-reduced-motion:reduce){.protected-image-state span{animation:none}}
</style>
