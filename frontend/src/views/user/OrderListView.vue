<template>
  <el-card>
    <template #header>我的订单</template>

    <el-table v-if="orders.length > 0" :data="orders" border>
      <el-table-column prop="orderNo" label="订单号" min-width="220" />
      <el-table-column label="金额" min-width="110">
        <template #default="{ row }">{{ formatPrice(row.totalCents) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" min-width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="商品明细" min-width="300">
        <template #default="{ row }">
          <div v-for="it in row.items" :key="`${row.id}-${it.productId}`" class="item-line">
            {{ it.productName }} x {{ it.quantity }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row.id)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else description="你还没有订单，去商品页逛逛吧" />

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

  <el-dialog v-model="showDetail" title="订单详情" width="760px">
    <div v-if="detail">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.status)">{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatDate(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">{{ formatPrice(detail.totalCents) }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detail.items || []" border style="margin-top: 14px">
        <el-table-column prop="productName" label="商品" min-width="220" />
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column label="单价" width="120">
          <template #default="{ row }">{{ formatPrice(row.unitPriceCents) }}</template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">{{ formatPrice((row.unitPriceCents || 0) * (row.quantity || 0)) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { http } from '@/utils/http'

const orders = ref<any[]>([])
const page = ref(0)
const size = 10
const total = ref(0)
const showDetail = ref(false)
const detail = ref<any>(null)

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
  const res = await http.get<any>('/api/orders', { page: page.value, size })
  orders.value = res?.content || []
  total.value = res?.totalElements || 0
}

async function openDetail(id: number) {
  detail.value = await http.get(`/api/orders/${id}`)
  showDetail.value = true
}

onMounted(load)
</script>

<style scoped>
.item-line {
  line-height: 1.6;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
