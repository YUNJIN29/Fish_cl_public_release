<template>
  <el-card>
    <template #header>订单管理</template>
    <el-table :data="list" border>
      <el-table-column prop="orderNo" label="订单号" min-width="220" />
      <el-table-column label="金额" width="120">
        <template #default="{ row }">{{ formatPrice(row.totalCents) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" min-width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="setStatus(row.id, 'PAID')">标记已支付</el-button>
          <el-button size="small" @click="setStatus(row.id, 'COMPLETED')">标记完成</el-button>
          <el-button size="small" type="danger" @click="setStatus(row.id, 'CANCELLED')">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '@/utils/http'

const list = ref<any[]>([])

function formatPrice(cents: number) {
  return '￥' + (Number(cents || 0) / 100).toFixed(2)
}

function formatDate(v: string) {
  const d = new Date(v)
  return isNaN(d.getTime()) ? v : d.toLocaleString()
}

function statusType(s: string) {
  if (s === 'PENDING') return 'warning'
  if (s === 'PAID') return 'primary'
  if (s === 'COMPLETED') return 'success'
  if (s === 'CANCELLED') return 'info'
  return 'info'
}

function statusText(s: string) {
  if (s === 'PENDING') return '待付款'
  if (s === 'PAID') return '已支付'
  if (s === 'COMPLETED') return '已完成'
  if (s === 'CANCELLED') return '已取消'
  return s
}

async function load() {
  const res = await http.get<any>('/api/admin/orders', { page: 0, size: 200 })
  list.value = res?.content || []
}

async function setStatus(id: number, status: string) {
  await http.patch(`/api/admin/orders/${id}/status?status=${status}`)
  await load()
}

onMounted(load)
</script>

