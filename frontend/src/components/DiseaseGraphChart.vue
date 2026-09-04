<template>
  <div class="graph-shell">
    <div v-if="showToolbar" class="graph-toolbar">
      <el-input
        v-model="searchVal"
        placeholder="搜索病害、症状、鱼种、药物..."
        class="search-input"
        clearable
        @keyup.enter="onSearch"
        @clear="resetGraph"
      >
        <template #append>
          <el-button :icon="Search" @click="onSearch" />
        </template>
      </el-input>

      <el-select v-model="activeCategory" placeholder="全部类别" class="category-select" clearable @change="applyFilters">
        <el-option v-for="cat in categoryOptions" :key="cat" :label="cat" :value="cat" />
      </el-select>

      <el-segmented v-model="viewMode" :options="viewOptions" size="small" @change="renderGraph" />
      <el-segmented v-model="layoutMode" :options="layoutOptions" size="small" @change="renderGraph" />
      <el-button :type="showRelationLabel ? 'primary' : 'default'" size="small" @click="toggleRelationLabel">关系标签</el-button>
      <el-button :icon="Download" size="small" @click="exportImage">导出图片</el-button>
      <el-button :icon="Refresh" size="small" @click="resetGraph">重置</el-button>
      <div class="view-hint">{{ activeViewHint }}</div>
    </div>

    <div v-if="showToolbar && rawData" class="stat-strip">
      <div><strong>{{ rawData.stats?.diseaseCount || diseaseNodes.length }}</strong><span>病害</span></div>
      <div><strong>{{ currentNodes.length }}</strong><span>当前节点</span></div>
      <div><strong>{{ currentLinks.length }}</strong><span>当前关系</span></div>
      <div><strong>{{ rawData.stats?.relatedDiseaseLinkCount || relatedDiseaseLinks.length }}</strong><span>病害关联</span></div>
    </div>

    <div v-if="tagList.length > 0" class="tag-panel">
      <el-tag v-for="tag in tagList" :key="tag" effect="plain" class="tag-item" @click="focusNode(tag)">
        {{ tag }}
      </el-tag>
    </div>

    <div class="graph-workspace" :class="{ compact: !showToolbar }">
      <div class="chart-wrap">
        <div v-if="showToolbar" class="graph-legend">
          <span v-for="item in relationLegend" :key="item.label">
            <i :style="{ background: item.color }"></i>{{ item.label }}
          </span>
        </div>
        <el-skeleton v-if="loading" :rows="6" animated :style="`height:${height}px`" />
        <div v-show="!loading" :id="chartId" :style="`width:100%;height:${height}px`" />
        <div v-if="error" class="error-tip">图谱数据加载失败，请检查后端服务是否启动。</div>
      </div>

      <aside v-if="showToolbar" class="detail-panel">
        <template v-if="selectedNode">
          <div class="node-kind">{{ selectedNode.value || categoryName(selectedNode.category) }}</div>
          <h3>{{ selectedNode.name }}</h3>
          <p v-if="selectedNode.description">{{ selectedNode.description }}</p>
          <div class="node-meta">
            <span v-if="selectedNode.diseaseType">类型：{{ selectedNode.diseaseType }}</span>
            <span v-if="selectedNode.prevalentTemp">温度：{{ selectedNode.prevalentTemp }}</span>
            <span>关联：{{ selectedDegree }} 条</span>
          </div>
          <div v-if="relatedNodes.length" class="info-block">
            <b>相关病害</b>
            <button v-for="item in relatedNodes" :key="item.id" type="button" class="related-disease" @click="focusNode(item.name)">
              {{ item.name }}
            </button>
          </div>
          <div v-if="selectedRelationRows.length" class="info-block">
            <b>关系脉络</b>
            <div v-for="row in selectedRelationRows" :key="`${row.relation}-${row.node.id}`" class="relation-row">
              <span>{{ row.relation }}</span>
              <button type="button" @click="focusNode(row.node.name)">{{ row.node.name }}</button>
            </div>
          </div>
          <div v-if="selectedNode.symptomsSummary" class="info-block">
            <b>典型症状</b>
            <p>{{ selectedNode.symptomsSummary }}</p>
          </div>
          <div v-if="selectedNode.treatment" class="info-block">
            <b>处理建议</b>
            <p>{{ selectedNode.treatment }}</p>
          </div>
          <div v-if="selectedNode.prevention" class="info-block">
            <b>预防措施</b>
            <p>{{ selectedNode.prevention }}</p>
          </div>
          <div class="detail-actions">
            <el-button size="small" :icon="Share" @click="focusNode(selectedNode.name)">查看邻域</el-button>
            <el-button v-if="selectedNode.detailUrl" size="small" type="primary" @click="openDetail(selectedNode.detailUrl)">知识详情</el-button>
          </div>
        </template>
        <template v-else>
          <div class="empty-detail">
            <DataAnalysis />
            <h3>点击节点查看详情</h3>
            <p>支持从病害扩展到症状、鱼种、病原诱因、治疗和预防关系。</p>
          </div>
        </template>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import axios from 'axios'
import { DataAnalysis, Download, Refresh, Search, Share } from '@element-plus/icons-vue'

const rawAxios = axios.create({ baseURL: '' })
const router = useRouter()

const props = withDefaults(defineProps<{
  height?: number
  showToolbar?: boolean
  chartId?: string
}>(), {
  height: 700,
  showToolbar: true,
  chartId: 'disease-graph-chart',
})

type Category = { name: string }
type GraphNode = {
  id: string
  name: string
  category: number
  value?: string
  symbolSize?: number
  diseaseType?: string
  description?: string
  symptomsSummary?: string
  treatment?: string
  prevention?: string
  prevalentTemp?: string
  detailUrl?: string
}
type GraphLink = { source: string; target: string; name?: string; value?: string; relationType?: string }
type GraphData = {
  categories: Category[]
  nodes: GraphNode[]
  links: GraphLink[]
  stats?: { diseaseCount: number; nodeCount: number; linkCount: number; categoryCount: number; relatedDiseaseLinkCount?: number }
}

const loading = ref(true)
const error = ref(false)
const searchVal = ref('')
const tagList = ref<string[]>([])
const activeCategory = ref('')
const viewMode = ref('all')
const layoutMode = ref('force')
const showRelationLabel = ref(false)
const selectedNode = ref<GraphNode | null>(null)
const currentNodes = ref<GraphNode[]>([])
const currentLinks = ref<GraphLink[]>([])

const layoutOptions = [
  { label: '力导向', value: 'force' },
  { label: '环形', value: 'circular' },
]
const viewOptions = [
  { label: '全域图谱', value: 'all' },
  { label: '病害关联', value: 'disease' },
]

let chart: echarts.ECharts | null = null
let rawData: GraphData | null = null

const categoryOptions = computed(() => rawData?.categories.map((c) => c.name) || [])
const diseaseNodes = computed(() => rawData?.nodes.filter((node) => categoryName(node.category) === '病害') || [])
const relatedDiseaseLinks = computed(() => rawData?.links.filter((link) => link.relationType === '病害关联') || [])
const selectedDegree = computed(() => {
  if (!selectedNode.value || !rawData) return 0
  return rawData.links.filter((link) => link.source === selectedNode.value?.id || link.target === selectedNode.value?.id).length
})
const relationLegend = [
  { label: '病害关联', color: '#f2b84b' },
  { label: '症状/鱼种', color: '#21b7df' },
  { label: '病原/诱因', color: '#ef6f6c' },
  { label: '治疗/预防', color: '#42b883' },
]
const activeViewHint = computed(() => {
  if (viewMode.value === 'disease') return '当前只显示病害之间的相似、同类或共同宿主关系。'
  if (activeCategory.value) return `当前聚焦：${activeCategory.value} 与病害的关系。`
  return '拖拽节点、滚轮缩放；点击病害可在右侧查看症状、处理和相关疾病。'
})
const relatedNodes = computed(() => {
  if (!selectedNode.value || !rawData) return []
  const ids = new Set<string>()
  rawData.links
      .filter((link) => link.relationType === '病害关联')
      .forEach((link) => {
        if (link.source === selectedNode.value?.id) ids.add(link.target)
        if (link.target === selectedNode.value?.id) ids.add(link.source)
      })
  return rawData.nodes.filter((node) => ids.has(node.id)).slice(0, 8)
})
const selectedRelationRows = computed(() => {
  if (!selectedNode.value || !rawData) return []
  const rows: Array<{ relation: string; node: GraphNode }> = []
  rawData.links
      .filter((link) => link.source === selectedNode.value?.id || link.target === selectedNode.value?.id)
      .slice(0, 14)
      .forEach((link) => {
        const otherId = link.source === selectedNode.value?.id ? link.target : link.source
        const node = rawData?.nodes.find((item) => item.id === otherId)
        if (node) rows.push({ relation: link.name || link.value || '关联', node })
      })
  return rows
})

async function fetchData(): Promise<GraphData> {
  const { data } = await rawAxios.get<GraphData>('/api/knowledge-graph')
  return data
}

function renderGraph() {
  if (!rawData) return
  const filtered = filterGraph(rawData.nodes, rawData.links)
  currentNodes.value = filtered.nodes
  currentLinks.value = filtered.links
  draw(filtered.nodes, filtered.links)
}

function draw(nodes: GraphNode[], links: GraphLink[]) {
  const dom = document.getElementById(props.chartId)
  if (!dom || !rawData) return
  if (chart) chart.dispose()

  chart = echarts.init(dom, 'dark')
  const styledNodes = nodes.map((node) => ({
    ...node,
    symbol: categoryName(node.category) === '病害' ? 'circle' : 'roundRect',
    symbolSize: nodeSize(node),
    itemStyle: {
      color: nodeColor(node),
      borderColor: categoryName(node.category) === '病害' ? '#ffffff' : 'rgba(255,255,255,0.72)',
      borderWidth: categoryName(node.category) === '病害' ? 2 : 1,
      shadowBlur: categoryName(node.category) === '病害' ? 18 : 8,
      shadowColor: nodeColor(node),
    },
    label: {
      show: props.showToolbar ? categoryName(node.category) === '病害' || (node.symbolSize || 0) >= 18 : categoryName(node.category) === '病害',
      position: 'right',
      formatter: '{b}',
      fontSize: categoryName(node.category) === '病害' ? 13 : 11,
      color: '#e8f3ff',
      textBorderColor: '#07101f',
      textBorderWidth: 3,
    },
  }))
  const styledLinks = links.map((link) => ({
    ...link,
    value: link.value || link.name,
    lineStyle: {
      color: linkColor(link),
      width: link.relationType === '病害关联' ? 2.4 : 1.15,
      opacity: link.relationType === '病害关联' ? 0.74 : 0.36,
      curveness: link.relationType === '病害关联' ? 0.28 : 0.12,
    },
  }))

  chart.setOption({
    backgroundColor: {
      type: 'radial',
      x: 0.42,
      y: 0.36,
      r: 0.92,
      colorStops: [
        { offset: 0, color: '#10284a' },
        { offset: 0.42, color: '#0b172b' },
        { offset: 1, color: '#07101f' },
      ],
    },
    color: ['#21b7df', '#7c65f5', '#ef6f6c', '#42b883', '#f2b84b', '#9cc449', '#4ba3ff', '#8a9ab8', '#de7fb6'],
    tooltip: {
      trigger: 'item',
      formatter: (p: any) => {
        if (p.dataType === 'edge') return `${p.data.relationType || '关系'}：${p.data.name || '关联'}`
        const data = p.data as GraphNode
        return `<strong style="color:#5ee7ff">${data.name}</strong><br/>类型：${data.value || categoryName(data.category)}`
      },
      backgroundColor: 'rgba(7,16,31,0.94)',
      borderColor: '#24466d',
      textStyle: { color: '#dce8ff', fontSize: 13 },
    },
    legend: [{
      type: 'scroll',
      data: rawData.categories.map((c) => c.name),
      top: 12,
      right: 12,
      orient: 'vertical',
      textStyle: { fontSize: 11, color: '#a9bad4' },
      itemWidth: 12,
      itemHeight: 12,
    }],
    animationDuration: 1200,
    animationEasingUpdate: 'quinticInOut',
    series: [{
      name: '水产病害知识图谱',
      type: 'graph',
      layout: layoutMode.value,
      circular: { rotateLabel: true },
      force: {
        edgeLength: viewMode.value === 'disease' ? [115, 190] : [80, 165],
        repulsion: viewMode.value === 'disease' ? 520 : 390,
        gravity: 0.08,
        friction: 0.35,
      },
      zoom: props.showToolbar ? 0.76 : 0.7,
      roam: true,
      draggable: true,
      data: styledNodes,
      links: styledLinks,
      categories: rawData.categories,
      label: { show: true },
      edgeLabel: {
        show: showRelationLabel.value,
        formatter: '{c}',
        color: '#9bb5d6',
        fontSize: 10,
      },
      lineStyle: { color: 'source', curveness: 0.18, opacity: 0.48, width: 1.2 },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 4, opacity: 0.95 },
        label: { show: true },
      },
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [0, 8],
    }],
  })

  chart.on('click', (params: any) => {
    if (params.dataType === 'node') selectedNode.value = params.data
  })
}

function filterGraph(nodes: GraphNode[], links: GraphLink[]) {
  if (viewMode.value === 'disease') {
    const diseaseIds = new Set(nodes.filter((node) => categoryName(node.category) === '病害').map((node) => node.id))
    return {
      nodes: nodes.filter((node) => diseaseIds.has(node.id)),
      links: links.filter((link) => link.relationType === '病害关联' && diseaseIds.has(link.source) && diseaseIds.has(link.target)),
    }
  }
  if (!activeCategory.value) return { nodes, links }
  const categoryIndexValue = rawData?.categories.findIndex((cat) => cat.name === activeCategory.value)
  const ids = new Set<string>()
  nodes.forEach((node) => {
    if (node.category === categoryIndexValue || categoryName(node.category) === '病害') ids.add(node.id)
  })
  links.forEach((link) => {
    if (ids.has(link.source) || ids.has(link.target)) {
      ids.add(link.source)
      ids.add(link.target)
    }
  })
  return {
    nodes: nodes.filter((node) => ids.has(node.id)),
    links: links.filter((link) => ids.has(link.source) && ids.has(link.target)),
  }
}

function applyFilters() {
  tagList.value = []
  renderGraph()
}

function onSearch() {
  if (!rawData) return
  const val = searchVal.value.trim()
  if (!val) {
    resetGraph()
    return
  }
  const matched = rawData.nodes
      .filter((node) => node.name.includes(val) || node.value?.includes(val) || node.description?.includes(val))
      .map((node) => node.name)
  tagList.value = matched.slice(0, 24)
  if (matched.length > 0) focusNode(matched[0])
}

function focusNode(keyword: string) {
  if (!rawData) return
  const target = rawData.nodes.find((node) => node.name === keyword)
  if (!target) return
  selectedNode.value = target
  const relIds = new Set<string>([target.id])
  rawData.links.forEach((link) => {
    if (link.source === target.id) relIds.add(link.target)
    if (link.target === target.id) relIds.add(link.source)
  })
  currentNodes.value = rawData.nodes.filter((node) => relIds.has(node.id))
  currentLinks.value = rawData.links.filter((link) => relIds.has(link.source) && relIds.has(link.target))
  draw(currentNodes.value, currentLinks.value)
}

function resetGraph() {
  tagList.value = []
  searchVal.value = ''
  activeCategory.value = ''
  viewMode.value = 'all'
  selectedNode.value = null
  renderGraph()
}

function toggleRelationLabel() {
  showRelationLabel.value = !showRelationLabel.value
  chart?.setOption({ series: [{ edgeLabel: { show: showRelationLabel.value } }] })
}

function openDetail(url: string) {
  router.push(url)
}

function exportImage() {
  if (!chart) return
  const url = chart.getDataURL({ type: 'png', pixelRatio: 2, backgroundColor: '#07101f' })
  const link = document.createElement('a')
  link.href = url
  link.download = '水产病害知识图谱.png'
  link.click()
}

function categoryName(index?: number) {
  return rawData?.categories[index || 0]?.name || '节点'
}

function nodeSize(node: GraphNode) {
  if (categoryName(node.category) === '病害') return viewMode.value === 'disease' ? 34 : Math.max(node.symbolSize || 24, 26)
  return Math.max(node.symbolSize || 13, 13)
}

function nodeColor(node: GraphNode) {
  const name = categoryName(node.category)
  if (name === '病害') return '#21b7df'
  if (name.includes('症状') || name.includes('鱼种')) return '#7c65f5'
  if (name.includes('病原') || name.includes('诱因')) return '#ef6f6c'
  if (name.includes('治疗') || name.includes('预防')) return '#42b883'
  if (name.includes('温度') || name.includes('地区')) return '#f2b84b'
  return '#8a9ab8'
}

function linkColor(link: GraphLink) {
  if (link.relationType === '病害关联') return '#f2b84b'
  if ((link.name || '').includes('症状') || (link.name || '').includes('鱼种')) return '#21b7df'
  if ((link.name || '').includes('病原') || (link.name || '').includes('诱因')) return '#ef6f6c'
  if ((link.name || '').includes('治疗') || (link.name || '').includes('预防')) return '#42b883'
  return '#8a9ab8'
}

function onResize() {
  chart?.resize()
}

onMounted(async () => {
  try {
    rawData = await fetchData()
    loading.value = false
    await nextTick()
    renderGraph()
  } catch (e) {
    console.error('[KG] fetch error', e)
    loading.value = false
    error.value = true
  }
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
})
</script>

<style scoped>
.graph-shell {
  width: 100%;
}
.graph-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
  padding: 12px;
  background: linear-gradient(135deg, rgba(33, 183, 223, 0.08), rgba(66, 184, 131, 0.04)), var(--c-bg-card);
  border: 1px solid var(--c-border);
  border-radius: 8px;
}
.search-input {
  width: min(360px, 100%);
}
.category-select {
  width: 150px;
}
.view-hint {
  flex: 1;
  min-width: 240px;
  color: var(--c-text-3);
  font-size: 12px;
  line-height: 1.6;
  text-align: right;
}
.stat-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}
.stat-strip div {
  min-height: 70px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background:
    linear-gradient(160deg, rgba(255, 255, 255, 0.04), transparent 48%),
    var(--c-bg-card);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 12px;
  position: relative;
  overflow: hidden;
}
.stat-strip div::after {
  content: "";
  position: absolute;
  right: 12px;
  top: 12px;
  width: 26px;
  height: 4px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--c-cyan), var(--c-green));
  opacity: 0.55;
}
.stat-strip strong {
  color: var(--c-cyan);
  font-size: 24px;
  line-height: 1;
}
.stat-strip span {
  margin-top: 8px;
  color: var(--c-text-3);
  font-size: 12px;
}
.tag-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 0 12px;
}
.tag-item {
  cursor: pointer;
}
.graph-workspace {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 14px;
}
.graph-workspace.compact {
  display: block;
}
.chart-wrap,
.detail-panel {
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-bg-card);
  overflow: hidden;
}
.chart-wrap {
  position: relative;
  min-height: 420px;
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.02);
}
.graph-legend {
  position: absolute;
  left: 14px;
  top: 14px;
  z-index: 2;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-width: calc(100% - 170px);
  padding: 8px 10px;
  border: 1px solid rgba(142, 168, 205, 0.24);
  border-radius: 8px;
  background: rgba(7, 16, 31, 0.72);
  backdrop-filter: blur(10px);
}
.graph-legend span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #d9e8ff;
  font-size: 12px;
  white-space: nowrap;
}
.graph-legend i {
  width: 18px;
  height: 3px;
  border-radius: 999px;
}
.detail-panel {
  padding: 16px;
  align-self: start;
  max-height: 720px;
  overflow: auto;
  background:
    linear-gradient(180deg, rgba(33, 183, 223, 0.08), transparent 180px),
    var(--c-bg-card);
}
.node-kind {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 4px;
  background: rgba(0, 180, 216, 0.12);
  color: var(--c-cyan);
  font-size: 12px;
  font-weight: 700;
}
.detail-panel h3 {
  margin: 12px 0 8px;
  color: var(--c-text);
  font-size: 20px;
}
.detail-panel p {
  color: var(--c-text-2);
  line-height: 1.7;
  font-size: 13px;
}
.node-meta {
  display: flex;
  flex-direction: column;
  gap: 7px;
  margin: 12px 0;
}
.node-meta span {
  padding: 6px 8px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  color: var(--c-text-3);
  background: var(--c-bg-soft);
  font-size: 12px;
}
.info-block {
  padding-top: 12px;
  border-top: 1px solid var(--c-border);
}
.info-block b {
  color: var(--c-text);
  font-size: 13px;
}
.related-disease {
  width: 100%;
  min-height: 34px;
  margin-top: 8px;
  padding: 7px 9px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  background: var(--c-bg-soft);
  color: var(--c-text-2);
  cursor: pointer;
  text-align: left;
  font-size: 13px;
  transition: border-color 0.2s, color 0.2s;
}
.related-disease:hover {
  border-color: var(--c-cyan);
  color: var(--c-cyan);
}
.relation-row {
  display: grid;
  grid-template-columns: 86px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  margin-top: 8px;
}
.relation-row span {
  min-height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  border-radius: 4px;
  background: rgba(242, 184, 75, 0.13);
  color: var(--c-amber);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.relation-row button {
  min-width: 0;
  min-height: 30px;
  padding: 5px 8px;
  border: 1px solid var(--c-border);
  border-radius: 6px;
  background: var(--c-bg-soft);
  color: var(--c-text-2);
  cursor: pointer;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.relation-row button:hover {
  border-color: var(--c-cyan);
  color: var(--c-cyan);
}
.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}
.empty-detail {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--c-text-3);
}
.empty-detail svg {
  width: 42px;
  height: 42px;
  color: var(--c-cyan);
}
.error-tip {
  text-align: center;
  color: var(--c-red);
  padding: 40px 0;
  font-size: 14px;
}
@media (max-width: 980px) {
  .graph-workspace,
  .stat-strip {
    grid-template-columns: 1fr;
  }
  .detail-panel {
    max-height: none;
  }
  .view-hint {
    text-align: left;
  }
}
@media (max-width: 640px) {
  .graph-toolbar {
    align-items: stretch;
    flex-direction: column;
  }
  .search-input,
  .category-select {
    width: 100%;
  }
  .graph-legend {
    position: static;
    max-width: none;
    margin: 10px;
  }
  .relation-row {
    grid-template-columns: 1fr;
  }
}
</style>
