<template>
  <el-card>
    <template #header>内容审核</template>

    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="帖子审核" name="post" />
      <el-tab-pane label="评论审核" name="comment" />
    </el-tabs>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="content" label="内容" min-width="280" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="review(row.id, 'APPROVED')">通过</el-button>
          <el-button size="small" type="danger" @click="review(row.id, 'REJECTED')">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '@/utils/http'

const tab = ref('post')
const list = ref<any[]>([])

async function load() {
  if (tab.value === 'post') {
    const res = await http.get<any>('/api/admin/review/posts', { page: 0, size: 200 })
    list.value = res?.content || []
  } else {
    const res = await http.get<any>('/api/admin/review/comments', { page: 0, size: 200 })
    list.value = res?.content || []
  }
}

async function review(id: number, status: string) {
  if (tab.value === 'post') await http.patch(`/api/admin/review/posts/${id}`, { status })
  else await http.patch(`/api/admin/review/comments/${id}`, { status })
  await load()
}

onMounted(load)
</script>

