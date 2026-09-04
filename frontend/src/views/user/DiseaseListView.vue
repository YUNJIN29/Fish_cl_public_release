<template>
  <div>
    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <div class="title-area">
          <span class="page-title">病害知识库</span>
          <span class="page-sub">覆盖细菌性、病毒性、寄生虫等主要水产病害</span>
        </div>
        <div class="filter-right">
          <el-select v-model="typeFilter" placeholder="病害类型" clearable style="width:130px" @change="search">
            <el-option v-for="t in diseaseTypes" :key="t" :label="t" :value="t" />
          </el-select>
          <el-input
            v-model="q"
            placeholder="搜索病名/症状/危害对象…"
            style="width:260px"
            clearable
            @keyup.enter="search"
            @clear="search"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" @click="search">搜索</el-button>
        </div>
      </div>
    </el-card>

    <el-skeleton v-if="loading" :rows="6" animated style="margin-top:16px" />

    <template v-else>
      <el-empty v-if="list.length === 0" description="暂无匹配的病害信息" style="margin-top:40px" />

      <div v-else class="disease-grid">
        <el-card
          v-for="d in list"
          :key="d.id"
          shadow="hover"
          class="disease-card"
          @click="$router.push(`/fish/diseases/${d.id}`)"
        >
          <div class="card-top">
            <el-tag :type="typeColor(d.diseaseType)" size="small" effect="light" class="type-tag">
              {{ d.diseaseType || '其他' }}
            </el-tag>
            <span class="disease-name">{{ d.name }}</span>
          </div>
          <div class="card-meta">
            <span v-if="d.affectedSpecies" class="meta-item">🐟 {{ d.affectedSpecies }}</span>
            <span v-if="d.prevalentTemp" class="meta-item">🌡️ {{ d.prevalentTemp }}</span>
          </div>
          <div v-if="d.symptomsSummary" class="card-symptoms">{{ d.symptomsSummary }}</div>
          <div v-if="d.prevalentRegions" class="card-region">📍 {{ d.prevalentRegions }}</div>
          <div class="card-action">
            <el-button link type="primary" size="small">查看详情 →</el-button>
          </div>
        </el-card>
      </div>

      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :current-page="page + 1"
          :total="total"
          :page-size="size"
          @current-change="(p: number) => { page = p - 1; load() }"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '@/utils/http'
import { Search } from '@element-plus/icons-vue'

type DiseaseItem = {
  id: number
  name: string
  diseaseType: string
  affectedSpecies: string
  prevalentRegions: string
  symptomsSummary: string
  prevalentTemp: string
  imageUrl: string | null
}

const diseaseTypes = ['细菌性', '病毒性', '寄生虫性', '真菌性', '营养性', '其他']

const q = ref('')
const typeFilter = ref('')
const list = ref<DiseaseItem[]>([])
const loading = ref(false)
const page = ref(0)
const size = 12
const total = ref(0)

function typeColor(type: string) {
  const map: Record<string, string> = {
    '细菌性': 'danger',
    '病毒性': 'warning',
    '寄生虫性': 'success',
    '真菌性': 'info',
    '营养性': '',
  }
  return map[type] ?? 'info'
}

function search() {
  page.value = 0
  load()
}

async function load() {
  loading.value = true
  try {
    const res = await http.get<any>('/api/diseases', {
      q: q.value || undefined,
      type: typeFilter.value || undefined,
      page: page.value,
      size,
    })
    list.value = res?.content || []
    total.value = res?.totalElements || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex; align-items: center; justify-content: space-between;
  flex-wrap: wrap; gap: 12px;
}
.title-area { display: flex; flex-direction: column; gap: 4px; }
.page-title {
  font-size: 22px; font-weight: 800;
  background: linear-gradient(135deg, var(--c-cyan), var(--c-purple));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
.page-sub { font-size: 13px; color: var(--c-text-2); }
.filter-right { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

.disease-grid {
  display: grid; grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px; margin-top: 16px;
}
.disease-card {
  cursor: pointer; transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
  border-left: 3px solid var(--c-border) !important;
}
.disease-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 28px rgba(0,212,255,0.12) !important;
  border-left-color: var(--c-cyan) !important;
}
.card-top { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.disease-name { font-size: 16px; font-weight: 700; color: var(--c-text); flex: 1; }
.type-tag { flex-shrink: 0; }
.card-meta { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.meta-item { display: inline-flex; align-items: center; gap: 3px; font-size: 12px; color: var(--c-text-2); }
.card-symptoms {
  font-size: 13px; color: var(--c-text-2); line-height: 1.6; margin-bottom: 8px;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;
}
.card-region { font-size: 12px; color: var(--c-text-3); margin-bottom: 10px; }
.card-action {
  border-top: 1px solid var(--c-border); padding-top: 10px; text-align: right;
}
.pager { margin-top: 20px; display: flex; justify-content: center; }

@media (max-width: 960px) { .disease-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .disease-grid { grid-template-columns: 1fr; } }
</style>
