<template>
  <div class="rag-page">
    <section class="rag-header">
      <div>
        <div class="eyebrow">RAG Knowledge Retrieval</div>
        <h1>水产病害 RAG 知识检索</h1>
        <p>检索本地病害知识、权威资料片段和可继续接入的数据集，为 AI 诊断报告提供可追溯依据。</p>
      </div>
      <el-button type="primary" @click="$router.push('/fish/diagnose')">进入 AI 诊断</el-button>
    </section>

    <section class="search-band">
      <el-input
        v-model="q"
        size="large"
        clearable
        placeholder="输入症状、鱼种、水质或病名，例如：草鱼 出血 高温"
        @keyup.enter="search"
        @clear="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button size="large" type="primary" :loading="loading" @click="search">检索</el-button>
    </section>

    <section class="rag-grid">
      <div class="main-list">
        <div class="section-title">召回资料</div>
        <el-skeleton v-if="loading" :rows="6" animated />
        <el-empty v-else-if="results.length === 0" description="暂无匹配资料" />
        <article v-for="item in results" v-else :key="item.id" class="source-item">
          <div class="source-top">
            <el-tag size="small" effect="plain">{{ item.category || item.sourceType || '资料' }}</el-tag>
            <span>{{ item.sourceName || '本地资料' }}</span>
          </div>
          <h2>{{ item.title }}</h2>
          <p>{{ item.summary || item.snippet }}</p>
          <div class="tags">{{ item.tags }}</div>
          <a v-if="isExternal(item.sourceUrl)" :href="item.sourceUrl" target="_blank" rel="noopener noreferrer">打开来源</a>
        </article>
      </div>

      <aside class="side-panel">
        <div class="section-title">推荐接入的数据集</div>
        <el-skeleton v-if="datasetLoading" :rows="6" animated />
        <div v-else class="dataset-list">
          <article v-for="item in datasets" :key="item.url" class="dataset-item">
            <div class="dataset-name">{{ item.name }}</div>
            <div class="dataset-meta">{{ item.organization }} · {{ item.type }} · {{ item.language }}</div>
            <p>{{ item.useCase }}</p>
            <div class="advice">{{ item.ingestionAdvice }}</div>
            <a :href="item.url" target="_blank" rel="noopener noreferrer">查看数据源</a>
          </article>
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { http } from '@/utils/http'

type RagDoc = {
  id: number
  title: string
  sourceName: string
  sourceUrl: string
  sourceType: string
  language: string
  category: string
  tags: string
  summary: string
  snippet: string
}

type RagDataset = {
  name: string
  organization: string
  url: string
  type: string
  language: string
  useCase: string
  ingestionAdvice: string
}

const q = ref('')
const loading = ref(false)
const datasetLoading = ref(false)
const results = ref<RagDoc[]>([])
const datasets = ref<RagDataset[]>([])

function isExternal(url: string) {
  return /^https?:\/\//.test(url || '')
}

async function search() {
  loading.value = true
  try {
    results.value = await http.get<RagDoc[]>('/api/rag/search', {
      q: q.value,
      limit: 10,
    })
  } finally {
    loading.value = false
  }
}

async function loadDatasets() {
  datasetLoading.value = true
  try {
    datasets.value = await http.get<RagDataset[]>('/api/rag/datasets')
  } finally {
    datasetLoading.value = false
  }
}

onMounted(() => {
  search()
  loadDatasets()
})
</script>

<style scoped>
.rag-page {
  padding-bottom: 24px;
}

.rag-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 0 18px;
}

.eyebrow {
  font-size: 12px;
  color: var(--c-cyan);
  font-weight: 800;
  letter-spacing: 0;
  margin-bottom: 8px;
}

h1 {
  margin: 0;
  font-size: 28px;
  color: var(--c-text);
}

.rag-header p {
  margin: 8px 0 0;
  max-width: 720px;
  color: var(--c-text-2);
  line-height: 1.8;
}

.search-band {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  padding: 14px;
  background: var(--c-bg-card);
  border: 1px solid var(--c-border);
  border-radius: 8px;
}

.rag-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 18px;
  margin-top: 18px;
}

.section-title {
  font-size: 16px;
  font-weight: 800;
  color: var(--c-text);
  margin-bottom: 12px;
}

.source-item,
.dataset-item {
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-bg-card);
  padding: 16px;
}

.source-item + .source-item,
.dataset-item + .dataset-item {
  margin-top: 12px;
}

.source-top {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--c-text-3);
  font-size: 12px;
}

.source-item h2 {
  margin: 10px 0 8px;
  font-size: 18px;
  color: var(--c-text);
}

.source-item p,
.dataset-item p {
  margin: 0;
  color: var(--c-text-2);
  line-height: 1.8;
}

.tags {
  margin-top: 10px;
  color: var(--c-text-3);
  font-size: 12px;
  line-height: 1.6;
}

a {
  display: inline-block;
  margin-top: 10px;
  color: var(--c-cyan);
  font-weight: 700;
  text-decoration: none;
}

.side-panel {
  min-width: 0;
}

.dataset-name {
  font-weight: 800;
  color: var(--c-text);
}

.dataset-meta {
  margin-top: 6px;
  color: var(--c-text-3);
  font-size: 12px;
}

.advice {
  margin-top: 10px;
  padding: 10px;
  border-left: 3px solid var(--c-cyan);
  background: var(--c-cyan-dim);
  color: var(--c-text-2);
  line-height: 1.7;
  border-radius: 0 6px 6px 0;
  font-size: 13px;
}

@media (max-width: 980px) {
  .rag-header,
  .search-band {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }

  .rag-grid {
    grid-template-columns: 1fr;
  }
}
</style>
