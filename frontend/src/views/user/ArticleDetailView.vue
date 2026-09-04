<template>
  <div class="article-detail">
    <div class="back-bar">
      <el-button link @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
    </div>

    <el-skeleton v-if="loading" :rows="10" animated />

    <template v-else-if="article">
      <div class="article-header">
        <h1 class="title">{{ article.title }}</h1>
        <div class="meta">
          <span class="date">📅 发布于 {{ formatDate(article.createdAt) }}</span>
          <span class="sep">|</span>
          <span class="tag">💡 科普资讯</span>
        </div>
      </div>

      <div v-if="article.coverImageUrl" class="cover-container">
        <img :src="article.coverImageUrl" class="cover-img" />
      </div>

      <div class="content-wrapper">
        <div class="article-content" v-html="article.content"></div>
      </div>

      <div class="article-footer">
        <div class="actions">
          <el-button plain @click="toggleLike">
            👍 点赞 ({{ likeCount }})
          </el-button>
          <el-button plain @click="toggleFavorite">
            ⭐ 收藏
          </el-button>
        </div>
      </div>
    </template>

    <el-empty v-else description="未找到文章内容" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { http } from '@/utils/http'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

type Article = {
  id: number
  title: string
  coverImageUrl?: string
  content: string
  createdAt?: string
}

const route = useRoute()
const article = ref<Article | null>(null)
const loading = ref(true)
const likeCount = ref(0)

function formatDate(v?: string) {
  if (!v) return ''
  const d = new Date(v)
  return d.toLocaleString()
}

async function load() {
  const id = route.params.id
  loading.value = true
  try {
    const res = await http.get<Article>(`/api/articles/${id}`)
    article.value = res
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

async function toggleLike() {
  try {
    const res = await http.post<any>(`/api/likes`, null, {
      params: { targetType: 'ARTICLE', targetId: article.value?.id }
    })
    likeCount.value = res.count
    ElMessage.success(res.liked ? '点赞成功' : '已取消点赞')
  } catch (err) {
    ElMessage.error('请先登录')
  }
}

async function toggleFavorite() {
  try {
    const res = await http.post<any>(`/api/favorites`, null, {
      params: { targetType: 'ARTICLE', targetId: article.value?.id }
    })
    ElMessage.success(res.favored ? '收藏成功' : '已取消收藏')
  } catch (err) {
    ElMessage.error('请先登录')
  }
}

onMounted(load)
</script>

<style scoped>
.article-detail {
  max-width: 900px; margin: 0 auto; padding-bottom: 60px;
}
.back-bar { margin-bottom: 24px; }

.article-header { margin-bottom: 32px; text-align: center; }
.title {
  font-size: 32px; font-weight: 800; color: var(--c-text);
  margin-bottom: 16px; line-height: 1.3;
}
.meta {
  display: flex; align-items: center; justify-content: center;
  gap: 12px; color: var(--c-text-3); font-size: 14px;
}
.sep { opacity: 0.3; }

.cover-container {
  width: 100%; border-radius: 12px; overflow: hidden;
  margin-bottom: 40px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}
.cover-img { width: 100%; display: block; }

.content-wrapper {
  background: var(--c-bg-card, #fff);
  padding: 40px; border-radius: 16px;
  border: 1px solid var(--c-border);
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
}
.article-content {
  font-size: 17px; line-height: 1.8; color: var(--c-text-2);
}
.article-content :deep(img) { max-width: 100%; height: auto; border-radius: 8px; margin: 20px 0; }
.article-content :deep(p) { margin-bottom: 20px; }
.article-content :deep(h2), .article-content :deep(h3) {
  color: var(--c-text); margin: 40px 0 20px 0;
}

.article-footer {
  margin-top: 40px; padding-top: 30px;
  border-top: 1px solid var(--c-border);
  display: flex; justify-content: center;
}
.actions { display: flex; gap: 16px; }

@media (max-width: 600px) {
  .title { font-size: 24px; }
  .content-wrapper { padding: 20px; }
}
</style>
