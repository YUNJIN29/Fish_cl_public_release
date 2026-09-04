<template>
  <el-card>
    <template #header>用户管理</template>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="160" />
      <el-table-column prop="nickname" label="昵称" min-width="140" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '正常' : '已封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
            @click="toggle(row)"
          >
            {{ row.status === 'ACTIVE' ? '封禁' : '解封' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '@/utils/http'

const list = ref<any[]>([])

async function load() {
  const res = await http.get<any>('/api/admin/users', { page: 0, size: 200 })
  list.value = res?.content || []
}

async function toggle(row: any) {
  const status = row.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE'
  await http.patch(`/api/admin/users/${row.id}/status?status=${status}`)
  await load()
}

onMounted(load)
</script>

