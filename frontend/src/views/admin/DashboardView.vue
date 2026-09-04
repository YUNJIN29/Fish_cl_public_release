<template>
  <el-row :gutter="16">
    <el-col :span="6">
      <el-statistic title="用户数" :value="stats.users" />
    </el-col>
    <el-col :span="6">
      <el-statistic title="商品数" :value="stats.products" />
    </el-col>
    <el-col :span="6">
      <el-statistic title="订单数" :value="stats.orders" />
    </el-col>
    <el-col :span="6">
      <el-statistic title="待审核内容" :value="stats.pendingContent" />
    </el-col>
  </el-row>
  <el-card style="margin-top: 16px">
    <template #header>近7天趋势（演示）</template>
    <el-table :data="trend" border>
      <el-table-column prop="date" label="日期" width="120" />
      <el-table-column prop="orders" label="订单数" width="120" />
      <el-table-column prop="users" label="新增用户数" width="140" />
    </el-table>
  </el-card>
  <el-card style="margin-top: 16px">
    <template #header>演示提示</template>
    <div>发帖/评论默认进入待审核；在“内容审核”里通过后，用户端可见。</div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { http } from '@/utils/http'

const stats = reactive({
  users: 0,
  products: 0,
  orders: 0,
  pendingContent: 0,
})
const trend = reactive<any[]>([])

onMounted(async () => {
  const res = await http.get<any>('/api/admin/stats')
  stats.users = res.users || 0
  stats.products = res.products || 0
  stats.orders = res.orders || 0
  stats.pendingContent = res.pendingContent || 0

  const t = await http.get<any[]>('/api/admin/stats/trend')
  trend.splice(0, trend.length, ...(t || []))
})
</script>

