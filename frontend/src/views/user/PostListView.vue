<template>
  <el-card>
    <template #header>
      <div class="hdr">
        <span>交流社区（帖子列表）</span>
        <div class="filters">
          <el-input v-model="q" placeholder="搜索（标题关键词）" style="max-width: 260px" @keyup.enter="load" />
          <el-button @click="beginCreate">发帖</el-button>
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
          <el-button size="small" :type="liked[row.id] ? 'primary' : ''" plain @click="toggleLike(row.id)">{{ liked[row.id] ? '已点赞' : '点赞' }} {{ likeCount[row.id] || 0 }}</el-button>
          <el-button size="small" :type="favored[row.id] ? 'warning' : ''" plain @click="toggleFav(row.id)">{{ favored[row.id] ? '已收藏' : '收藏' }} {{ favCount[row.id] || 0 }}</el-button>
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
        @current-change="(p: number) => { page = p - 1; load() }"
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
        <el-button size="small" :type="liked[detail.id] ? 'primary' : ''" plain @click="toggleLike(detail.id)">{{ liked[detail.id] ? '已点赞' : '点赞' }} {{ likeCount[detail.id] || 0 }}</el-button>
        <el-button size="small" :type="favored[detail.id] ? 'warning' : ''" plain @click="toggleFav(detail.id)">{{ favored[detail.id] ? '已收藏' : '收藏' }} {{ favCount[detail.id] || 0 }}</el-button>
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
import { useRoute, useRouter } from 'vue-router'
import { http } from '@/utils/http'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

type Post = { id: number; title: string; nickname: string; createdAt?: string }

const q = ref('')
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
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
const liked = reactive<Record<number, boolean>>({})
const favored = reactive<Record<number, boolean>>({})
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
  await loadEngagement()
}

async function loadEngagement() {
  const ids = list.value.map((item) => item.id)
  if (!ids.length) return
  const states = await http.get<Record<string, { likeCount: number; favoriteCount: number; liked: boolean; favored: boolean }>>('/api/engagement', { targetType: 'POST', targetIds: ids.join(',') })
  ids.forEach((id) => { const state = states[String(id)]; if (state) { likeCount[id] = state.likeCount; favCount[id] = state.favoriteCount; liked[id] = state.liked; favored[id] = state.favored } })
}

function requireLogin() {
  if (auth.token) return true
  ElMessage.info('登录后即可发帖、评论和参与互动')
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}

function beginCreate() { if (requireLogin()) showCreate.value = true }

async function submitPost() {
  if (!requireLogin()) return
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
  if (!requireLogin()) return
  if (!detail.value || !commentText.value.trim()) return
  await http.post(`/api/posts/${detail.value.id}/comments`, { content: commentText.value.trim() })
  commentText.value = ''
  await loadComments(detail.value.id)
}

async function toggleLike(postId: number) {
  if (!requireLogin()) return
  const res = await http.post<any>(`/api/likes?targetType=POST&targetId=${postId}`)
  likeCount[postId] = res?.count || 0
  liked[postId] = Boolean(res?.liked)
}

async function toggleFav(postId: number) {
  if (!requireLogin()) return
  const res = await http.post<any>(`/api/favorites?targetType=POST&targetId=${postId}`)
  favCount[postId] = res?.count || 0
  favored[postId] = Boolean(res?.favored)
}

onMounted(async () => {
  await load()
  const requestedPost = Number(route.query.post)
  if (Number.isInteger(requestedPost) && requestedPost > 0) await openDetail(requestedPost)
})
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
