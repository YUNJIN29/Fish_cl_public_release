<template>
  <div class="article-page">
    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <div class="title-area">
          <span class="page-title">科普资讯</span>
          <span class="page-sub">探索水产知识，获取最新行业动态与养殖技巧</span>
        </div>
        <div class="filter-right">
          <el-input
            v-model="q"
            placeholder="搜索文章标题..."
            style="width:280px"
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

    <el-skeleton v-if="loading" :rows="6" animated style="margin-top:20px" />

    <template v-else>
      <el-empty v-if="list.length === 0" description="暂无相关科普文章" style="margin-top:60px" />

      <div v-else class="article-grid">
        <el-card
          v-for="a in list"
          :key="a.id"
          shadow="hover"
          class="article-card"
          :body-style="{ padding: '0px' }"
          @click="$router.push(`/articles/${a.id}`)"
        >
          <div class="card-cover">
            <img :src="a.coverImageUrl || '/uploads/banner_1.svg'" class="cover-img" />
            <div class="card-date">{{ formatDate(a.createdAt) }}</div>
          </div>
          <div class="card-content">
            <h3 class="card-title">{{ a.title }}</h3>
            <p class="card-desc">{{ getSummary(a.content) }}</p>
            <div class="card-footer">
              <span class="read-more">阅读全文 →</span>
            </div>
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
          @current-change="(p) => { page = p - 1; load() }"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '@/utils/http'
import { Search } from '@element-plus/icons-vue'

type Article = {
  id: number
  title: string
  coverImageUrl?: string
  content?: string
  createdAt?: string
}

const q = ref('')
const list = ref<Article[]>([])
const loading = ref(false)
const page = ref(0)
const size = 9
const total = ref(0)

function formatDate(v?: string) {
  if (!v) return ''
  const d = new Date(v)
  return isNaN(d.getTime()) ? '' : `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
}

function getSummary(content?: string) {
  if (!content) return '点击阅读详情...'
  const plainText = content.replace(/<[^>]+>/g, '')
  return plainText.length > 80 ? plainText.slice(0, 80) + '...' : plainText
}

function search() {
  page.value = 0
  load()
}

async function load() {
  loading.value = true
  try {
    const res = await http.get<any>('/api/articles', {
      q: q.value || undefined,
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
  flex-wrap: wrap; gap: 16px;
}
.title-area { display: flex; flex-direction: column; gap: 4px; }
.page-title {
  font-size: 24px; font-weight: 800;
  background: linear-gradient(135deg, var(--c-cyan), var(--c-purple));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
.page-sub { font-size: 14px; color: var(--c-text-2); }
.filter-right { display: flex; align-items: center; gap: 12px; }

.article-grid {
  display: grid; grid-template-columns: repeat(3, 1fr);
  gap: 24px; margin-top: 24px;
}
.article-card {
  cursor: pointer; transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid var(--c-border); overflow: hidden;
}
.article-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 35px rgba(0,212,255,0.15) !important;
  border-color: var(--c-cyan);
}
.card-cover {
  position: relative; height: 180px; overflow: hidden;
  background: var(--c-bg-soft);
}
.cover-img {
  width: 100%; height: 100%; object-fit: cover;
  transition: transform 0.5s;
}
.article-card:hover .cover-img { transform: scale(1.1); }
.card-date {
  position: absolute; bottom: 12px; right: 12px;
  background: rgba(0,0,0,0.6); color: #fff;
  padding: 4px 10px; border-radius: 4px; font-size: 11px;
  backdrop-filter: blur(4px);
}

.card-content { padding: 20px; }
.card-title {
  font-size: 18px; font-weight: 700; margin: 0 0 12px 0;
  color: var(--c-text);
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  line-height: 1.4;
}
.card-desc {
  font-size: 14px; color: var(--c-text-2); line-height: 1.6;
  margin: 0 0 20px 0;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;
}
.card-footer { display: flex; justify-content: flex-end; }
.read-more { font-size: 13px; color: var(--c-cyan); font-weight: 600; }

.pager { margin-top: 40px; display: flex; justify-content: center; }

@media (max-width: 1100px) { .article-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 650px) { .article-grid { grid-template-columns: 1fr; } }
</style>
