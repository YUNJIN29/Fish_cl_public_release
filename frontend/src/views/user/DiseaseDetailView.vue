<template>
  <div v-if="disease">
    <el-page-header @back="$router.back()" style="margin-bottom:16px">
      <template #content>
        <span style="font-weight:700">{{ disease.name }}</span>
      </template>
    </el-page-header>

    <el-row :gutter="16">
      <!-- 左侧主内容 -->
      <el-col :xs="24" :lg="16">
        <!-- 基本信息 -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-hdr">
              <el-tag :type="typeColor(disease.diseaseType)" effect="light" size="large">
                {{ disease.diseaseType || '其他' }}
              </el-tag>
              <span class="disease-title">{{ disease.name }}</span>
            </div>
          </template>
          <p class="description">{{ disease.description }}</p>

          <el-descriptions :column="2" border size="default" class="desc-table">
            <el-descriptions-item label="危害对象">{{ disease.affectedSpecies || '—' }}</el-descriptions-item>
            <el-descriptions-item label="流行地区">{{ disease.prevalentRegions || '—' }}</el-descriptions-item>
            <el-descriptions-item label="流行温度">{{ disease.prevalentTemp || '—' }}</el-descriptions-item>
            <el-descriptions-item label="病害类型">{{ disease.diseaseType || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 症状 -->
        <el-card shadow="never" class="info-card">
          <template #header><span class="section-title">典型症状</span></template>
          <div class="section-content symptom-content">{{ disease.symptomsSummary || '暂无记录' }}</div>
        </el-card>

        <!-- 引发原因 -->
        <el-card shadow="never" class="info-card">
          <template #header><span class="section-title">引发原因</span></template>
          <div class="section-content">{{ disease.causes || '暂无记录' }}</div>
        </el-card>
      </el-col>

      <!-- 右侧治疗与预防 -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="info-card treatment-card">
          <template #header>
            <span class="section-title treat-title">治疗方案</span>
          </template>
          <div class="section-content">{{ disease.treatment || '暂无记录' }}</div>
        </el-card>

        <el-card shadow="never" class="info-card prevention-card">
          <template #header>
            <span class="section-title prev-title">预防措施</span>
          </template>
          <div class="section-content">{{ disease.prevention || '暂无记录' }}</div>
        </el-card>

        <el-card shadow="never" class="info-card">
          <template #header><span class="section-title">快速操作</span></template>
          <div class="quick-actions">
            <el-button type="primary" style="width:100%" @click="$router.push('/fish/diagnose')">
              AI 智能诊断
            </el-button>
            <el-button style="width:100%" @click="$router.push('/fish/knowledge-graph')">
              查看知识图谱
            </el-button>
            <el-button style="width:100%" @click="$router.push('/fish/diseases')">
              返回病害列表
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <el-skeleton v-else-if="loading" :rows="10" animated />
  <el-result v-else icon="error" title="病害不存在">
    <template #extra>
      <el-button @click="$router.push('/fish/diseases')">返回列表</el-button>
    </template>
  </el-result>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { http } from '@/utils/http'

type DiseaseDetail = {
  id: number
  name: string
  diseaseType: string
  description: string
  prevalentRegions: string
  affectedSpecies: string
  causes: string
  symptomsSummary: string
  treatment: string
  prevention: string
  prevalentTemp: string
  imageUrl: string | null
}

const route = useRoute()
const disease = ref<DiseaseDetail | null>(null)
const loading = ref(true)

function typeColor(type: string) {
  const map: Record<string, string> = {
    '细菌性': 'danger',
    '病毒性': 'warning',
    '寄生虫性': 'success',
    '真菌性': 'info',
  }
  return map[type] ?? 'info'
}

onMounted(async () => {
  try {
    disease.value = await http.get<DiseaseDetail>(`/api/diseases/${route.params.id}`)
  } catch {
    disease.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.info-card { margin-bottom: 16px; }
.card-hdr { display: flex; align-items: center; gap: 10px; }
.disease-title { font-size: 20px; font-weight: 800; color: var(--c-text); }
.description { color: var(--c-text-2); line-height: 1.9; margin-bottom: 16px; }
.desc-table { margin-top: 8px; }
.section-title { font-weight: 700; font-size: 15px; color: var(--c-text); }
.treat-title { color: var(--c-amber) !important; }
.prev-title  { color: var(--c-green) !important; }
.section-content { color: var(--c-text-2); line-height: 1.9; white-space: pre-line; }
.symptom-content {
  background: rgba(255,171,64,0.06);
  border-left: 3px solid var(--c-amber);
  padding: 12px 16px;
  border-radius: 0 6px 6px 0;
  color: var(--c-text);
}
.treatment-card { border-top: 2px solid var(--c-amber) !important; }
.prevention-card { border-top: 2px solid var(--c-green) !important; margin-top: 16px; }
.quick-actions { display: flex; flex-direction: column; gap: 10px; }
</style>
