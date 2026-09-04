<template>
  <el-card>
    <template #header>
      <div class="hdr">
        <span>分类管理</span>
        <el-button type="primary" size="small" @click="openCreate">新建分类</el-button>
      </div>
    </template>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名" />
      <el-table-column prop="sortOrder" label="排序" width="120" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="showDialog" :title="form.id ? '编辑分类' : '新建分类'" width="420px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showDialog = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { http } from '@/utils/http'
import { ElMessageBox } from 'element-plus'

const list = ref<any[]>([])
const showDialog = ref(false)
const form = reactive<any>({ id: null, name: '', sortOrder: 0 })

async function load() {
  list.value = await http.get<any[]>('/api/admin/categories')
}

function openCreate() {
  form.id = null
  form.name = ''
  form.sortOrder = 0
  showDialog.value = true
}

function openEdit(row: any) {
  form.id = row.id
  form.name = row.name
  form.sortOrder = row.sortOrder
  showDialog.value = true
}

async function submit() {
  const payload = { name: form.name, sortOrder: form.sortOrder }
  if (form.id) await http.put(`/api/admin/categories/${form.id}`, payload)
  else await http.post('/api/admin/categories', payload)
  showDialog.value = false
  await load()
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该分类？', '提示', { type: 'warning' })
  await http.delete(`/api/admin/categories/${id}`)
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

