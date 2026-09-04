<template>
  <el-card>
    <template #header>
      <div class="hdr">
        <span>交流社区（帖子列表）</span>
        <div class="filters">
          <el-input v-model="q" placeholder="搜索（标题关键词）" style="max-width: 260px" @keyup.enter="load" />
          <el-button @click="showCreate = true">发帖</el-button>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
      </div>
    </template>

    <el-table v-if="list.length > 0" :data="list" border style="width: 100%">
      <el-table-column prop="title" label="标题" min-width="320" />
      <el-table-column prop="nickname" label="作者" min-width="140" />
      <el-table-column prop="createdAt" label="发布时间" min-width="160">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="互动" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row.id)">详情/评论</el-button>
          <el-button size="small" @click="toggleLike(row.id)">点赞 {{ likeCount[row.id] || 0 }}</el-button>
          <el-button size="small" @click="toggleFav(row.id)">收藏 {{ favCount[row.id] || 0 }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else description="暂无帖子" />

    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="page + 1"
        :total="total"
        :page-size="size"
        @current-change="(p) => { page = p - 1; load() }"
      />
    </div>
  </el-card>

  <el-dialog v-model="showCreate" title="发布帖子" width="560px">
    <el-form :model="createForm" label-width="80px">
      <el-form-item label="标题">
        <el-input v-model="createForm.title" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="createForm.content" type="textarea" :rows="6" maxlength="10000" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showCreate = false">取消</el-button>
      <el-button type="primary" @click="submitPost">发布</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="showDetail" :title="detail?.title || '帖子详情'" size="45%">
    <div v-if="detail" class="detail-wrap">
      <div class="meta">作者：{{ detail.nickname }} ｜ 发布时间：{{ formatDate(detail.createdAt) }}</div>
      <div class="content">{{ detail.content }}</div>
      <div class="actions">
        <el-button size="small" @click="toggleLike(detail.id)">点赞 {{ likeCount[detail.id] || 0 }}</el-button>
        <el-button size="small" @click="toggleFav(detail.id)">收藏 {{ favCount[detail.id] || 0 }}</el-button>
      </div>
      <el-divider />
      <h4>评论区</h4>
      <div v-if="comments.length > 0">
        <div v-for="c in comments" :key="c.id" class="comment">
          <div class="comment-meta">{{ c.nickname }} · {{ formatDate(c.createdAt) }}</div>
          <div>{{ c.content }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无评论" />
      <div class="comment-form">
        <el-input v-model="commentText" type="textarea" :rows="3" placeholder="写下你的评论..." />
        <el-button type="primary" style="margin-top: 10px" @click="submitComment">发表评论</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { http } from '@/utils/http'

type Post = { id: number; title: string; nickname: string; createdAt?: string }

const q = ref('')
const list = ref<Post[]>([])
const page = ref(0)
const size = 10
const total = ref(0)
const showCreate = ref(false)
const showDetail = ref(false)
const detail = ref<any>(null)
const comments = ref<any[]>([])
const commentText = ref('')
const likeCount = reactive<Record<number, number>>({})
const favCount = reactive<Record<number, number>>({})
const createForm = reactive({ title: '', content: '' })

function formatDate(v?: string) {
  if (!v) return ''
  const d = new Date(v)
  return isNaN(d.getTime()) ? '' : d.toLocaleDateString()
}

async function load() {
  const res = await http.get<any>('/api/posts', { q: q.value || undefined, page: page.value, size })
  list.value = res?.content || []
  total.value = res?.totalElements || 0
}

async function submitPost() {
  await http.post('/api/posts', createForm)
  createForm.title = ''
  createForm.content = ''
  showCreate.value = false
  await load()
}

async function openDetail(id: number) {
  detail.value = await http.get(`/api/posts/${id}`)
  showDetail.value = true
  await loadComments(id)
}

async function loadComments(postId: number) {
  const res = await http.get<any>('/api/comments', { targetType: 'POST', targetId: postId, page: 0, size: 100 })
  comments.value = res?.content || []
}

async function submitComment() {
  if (!detail.value || !commentText.value.trim()) return
  await http.post(`/api/posts/${detail.value.id}/comments`, { content: commentText.value.trim() })
  commentText.value = ''
  await loadComments(detail.value.id)
}

async function toggleLike(postId: number) {
  const res = await http.post<any>(`/api/likes?targetType=POST&targetId=${postId}`)
  likeCount[postId] = res?.count || 0
}

async function toggleFav(postId: number) {
  const res = await http.post<any>(`/api/favorites?targetType=POST&targetId=${postId}`)
  favCount[postId] = res?.count || 0
}

onMounted(load)
</script>

<style scoped>
.hdr {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.filters {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.detail-wrap {
  line-height: 1.7;
}
.meta {
  color: #666;
  margin-bottom: 10px;
}
.content {
  white-space: pre-wrap;
}
.actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}
.comment {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-meta {
  color: #888;
  font-size: 12px;
  margin-bottom: 4px;
}
.comment-form {
  margin-top: 14px;
}
</style>

