<template>
  <el-card>
    <template #header>
      <div class="hdr">
        <span>商品管理</span>
        <el-button type="primary" size="small" @click="openCreate">新建商品</el-button>
      </div>
    </template>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商品名" min-width="200" />
      <el-table-column label="价格" width="110">
        <template #default="{ row }">{{ formatPrice(row.priceCents) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" @click="toggleStatus(row)">
            {{ row.status === 'ON' ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="showDialog" :title="form.id ? '编辑商品' : '新建商品'" width="560px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="分类ID"><el-input-number v-model="form.categoryId" :min="1" /></el-form-item>
      <el-form-item label="价格(分)"><el-input-number v-model="form.priceCents" :min="1" /></el-form-item>
      <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
      <el-form-item label="封面URL"><el-input v-model="form.coverImageUrl" /></el-form-item>
      <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showDialog = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { http } from '@/utils/http'

const list = ref<any[]>([])
const showDialog = ref(false)
const form = reactive<any>({
  id: null,
  name: '',
  categoryId: 1,
  priceCents: 1000,
  stock: 0,
  coverImageUrl: '',
  description: '',
})

function formatPrice(cents: number) {
  return '￥' + (Number(cents || 0) / 100).toFixed(2)
}

async function load() {
  const res = await http.get<any>('/api/admin/products', { page: 0, size: 200 })
  list.value = res?.content || []
}

function openCreate() {
  Object.assign(form, {
    id: null,
    name: '',
    categoryId: 1,
    priceCents: 1000,
    stock: 0,
    coverImageUrl: '',
    description: '',
  })
  showDialog.value = true
}

function openEdit(row: any) {
  Object.assign(form, row)
  showDialog.value = true
}

async function submit() {
  const payload = {
    name: form.name,
    categoryId: form.categoryId,
    priceCents: form.priceCents,
    stock: form.stock,
    coverImageUrl: form.coverImageUrl,
    description: form.description,
  }
  if (form.id) await http.put(`/api/admin/products/${form.id}`, payload)
  else await http.post('/api/admin/products', payload)
  showDialog.value = false
  await load()
}

async function toggleStatus(row: any) {
  await http.patch(`/api/admin/products/${row.id}/status?status=${row.status === 'ON' ? 'OFF' : 'ON'}`)
  await load()
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该商品？', '提示', { type: 'warning' })
  await http.delete(`/api/admin/products/${id}`)
  await load()
}

onMounted(load)
</script>

<style scoped>
.hdr {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

