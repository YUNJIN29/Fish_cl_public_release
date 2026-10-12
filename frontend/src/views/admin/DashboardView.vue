<template>
  <section class="ops-dashboard" v-loading="loading">
    <header><div><p>OPERATIONS CENTER</p><h1>运行状态</h1></div><el-tag :type="overview.status === 'UP' ? 'success' : 'danger'" size="large">{{ overview.status }}</el-tag></header>
    <div class="status-grid">
      <el-card v-for="item in cards" :key="item.label" shadow="never"><small>{{ item.label }}</small><strong>{{ item.value }}</strong><span>{{ item.note }}</span></el-card>
    </div>
    <el-row :gutter="18" class="panels">
      <el-col :xs="24" :lg="15"><el-card shadow="never"><template #header>近七日访问趋势</template><el-table :data="traffic"><el-table-column prop="date" label="日期"/><el-table-column prop="views" label="浏览量"/><el-table-column prop="visitors" label="访客"/></el-table></el-card></el-col>
      <el-col :xs="24" :lg="9"><el-card shadow="never"><template #header>异常与调用</template><dl><template v-for="item in metricRows" :key="item.label"><dt>{{ item.label }}</dt><dd>{{ item.value }}</dd></template></dl></el-card></el-col>
    </el-row>
    <el-alert v-if="error" type="error" :title="error" show-icon :closable="false" />
  </section>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { http } from '@/utils/http'
const loading = ref(true); const error = ref(''); const traffic = ref<any[]>([])
const overview = reactive<any>({ status: 'UNKNOWN', database: 'UNKNOWN', disk: { status: 'UNKNOWN' }, aiConfigured: false, pendingContent: 0, metrics: {} })
const cards = computed(() => [
  { label: '数据库', value: overview.database, note: 'MySQL 连接' },
  { label: '磁盘空间', value: overview.disk?.status || 'UNKNOWN', note: `${Math.round((overview.disk?.usableBytes || 0) / 1073741824)} GB 可用` },
  { label: 'AI 服务', value: overview.aiConfigured ? 'READY' : 'NOT SET', note: '配置状态' },
  { label: '待审核', value: overview.pendingContent || 0, note: '帖子与评论' },
])
const metricRows = computed(() => [
  { label: 'AI 成功 / 失败', value: `${overview.metrics?.diagnoseSuccess || 0} / ${overview.metrics?.diagnoseFailure || 0}` },
  { label: 'AI 平均耗时', value: `${overview.metrics?.diagnoseAverageMs || 0} ms` },
  { label: '登录失败', value: overview.metrics?.loginFailure || 0 },
  { label: '上传失败', value: overview.metrics?.uploadFailure || 0 },
  { label: '限流次数', value: overview.metrics?.rateLimited || 0 },
])
onMounted(async () => { try { Object.assign(overview, await http.get('/api/admin/operations/overview')); traffic.value = await http.get('/api/admin/operations/traffic') as any[] } catch { error.value = '运行数据暂时不可用，请检查后端健康状态。' } finally { loading.value = false } })
</script>
<style scoped>
.ops-dashboard{display:grid;gap:18px}.ops-dashboard header{display:flex;justify-content:space-between;align-items:end}.ops-dashboard header p{margin:0;color:#1689a7;font:700 11px ui-monospace;letter-spacing:2px}.ops-dashboard h1{margin:7px 0 0;font-size:32px}.status-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:14px}.status-grid :deep(.el-card__body){display:grid;gap:7px}.status-grid small,.status-grid span{color:var(--el-text-color-secondary)}.status-grid strong{font-size:25px}.panels{row-gap:18px}dl{display:grid;grid-template-columns:1fr auto;gap:16px;margin:0}dt{color:var(--el-text-color-secondary)}dd{margin:0;font-weight:700}@media(max-width:900px){.status-grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:480px){.status-grid{grid-template-columns:1fr}.ops-dashboard h1{font-size:27px}}
</style>
