<template>
  <div class="kg-page">
    <section class="kg-hero">
      <div>
        <span class="eyebrow">AQUATIC DISEASE GRAPH</span>
        <h1>水产病害全域知识图谱</h1>
        <p>把病害、病原诱因、症状、鱼种、地区、温度、治疗和预防串成一张更清楚的关系网络，可搜索、可聚焦、可筛选、可导出。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="$router.push('/fish/diseases')">进入知识库</el-button>
        <el-button @click="$router.push('/fish/diagnose')">AI 智能诊断</el-button>
      </div>
    </section>

    <section class="tips-row">
      <div><strong>搜索聚焦</strong><span>输入病害、症状、鱼种或药剂，自动收缩邻域图谱。</span></div>
      <div><strong>维度筛选</strong><span>按症状、病原、治疗、预防等维度查看关系。</span></div>
      <div><strong>病害关联</strong><span>切换病害关联视图，用更简洁的网络查看相似或容易混淆的疾病。</span></div>
    </section>

    <DiseaseGraphChart :height="720" :show-toolbar="true" chart-id="kg-full" />
  </div>
</template>

<script setup lang="ts">
import DiseaseGraphChart from '@/components/DiseaseGraphChart.vue'
</script>

<style scoped>
.kg-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.kg-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 24px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background:
    linear-gradient(120deg, rgba(33, 183, 223, 0.18), transparent 42%),
    linear-gradient(260deg, rgba(66, 184, 131, 0.12), transparent 34%),
    var(--c-bg-card);
  box-shadow: 0 16px 42px rgba(15, 30, 53, 0.08);
}
.eyebrow {
  color: var(--c-cyan);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 2px;
}
.kg-hero h1 {
  margin: 8px 0 10px;
  color: var(--c-text);
  font-size: 30px;
  line-height: 1.15;
}
.kg-hero p {
  max-width: 760px;
  margin: 0;
  color: var(--c-text-2);
  line-height: 1.8;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  flex-shrink: 0;
}
.tips-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.tips-row div {
  min-height: 78px;
  padding: 14px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-bg-card);
  position: relative;
  overflow: hidden;
}
.tips-row div::before {
  content: "";
  display: block;
  width: 34px;
  height: 4px;
  margin-bottom: 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--c-cyan), var(--c-green));
}
.tips-row strong,
.tips-row span {
  display: block;
}
.tips-row strong {
  color: var(--c-text);
  font-size: 15px;
}
.tips-row span {
  margin-top: 8px;
  color: var(--c-text-3);
  line-height: 1.6;
  font-size: 13px;
}
@media (max-width: 900px) {
  .kg-hero {
    align-items: flex-start;
    flex-direction: column;
  }
  .hero-actions {
    justify-content: flex-start;
  }
  .tips-row {
    grid-template-columns: 1fr;
  }
}
</style>
