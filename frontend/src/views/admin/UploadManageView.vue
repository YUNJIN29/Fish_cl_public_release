<template>
  <section class="upload-admin">
    <header>
      <div><p>RESOURCE GOVERNANCE</p><h1>上传资源管理</h1><span>查看私有上传归属、下载核验并安全清理无用文件。</span></div>
      <el-tag effect="plain" size="large">共 {{ total }} 个资源</el-tag>
    </header>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="query" clearable placeholder="按原始文件名搜索" @keyup.enter="resetAndLoad" @clear="resetAndLoad" />
        <el-select v-model="kind" placeholder="全部类型" clearable @change="resetAndLoad">
          <el-option label="图片" value="IMAGE" /><el-option label="PDF 文档" value="DOCUMENT" />
        </el-select>
        <el-button type="primary" @click="resetAndLoad">查询</el-button>
      </div>
      <el-table v-loading="loading" :data="items" empty-text="暂无受控上传资源">
        <el-table-column label="文件" min-width="230">
          <template #default="{ row }"><div class="file-cell"><span class="file-icon">{{ row.contentType.startsWith('image/') ? 'IMG' : 'PDF' }}</span><div><b>{{ row.originalName }}</b><small>{{ row.contentType }}</small></div></div></template>
        </el-table-column>
        <el-table-column label="所属用户" min-width="150"><template #default="{ row }"><b>@{{ row.ownerUsername }}</b><small class="owner-id">UID {{ row.ownerUserId }}</small></template></el-table-column>
        <el-table-column label="大小" width="110"><template #default="{ row }">{{ formatSize(row.sizeBytes) }}</template></el-table-column>
        <el-table-column label="上传时间" min-width="170"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }"><el-button size="small" @click="openResource(row)">{{ row.contentType.startsWith('image/') ? '预览' : '下载' }}</el-button><el-button size="small" type="danger" plain @click="remove(row)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <div class="pager"><el-pagination v-if="total > pageSize" v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="load" /></div>
    </el-card>
    <el-alert title="删除会同时移除数据库记录和物理文件，操作不可撤销。公开证书、商品及文章素材不在此列表中。" type="warning" show-icon :closable="false" />
    <el-dialog v-model="previewVisible" title="受控图片预览" width="min(760px, 92vw)" @closed="clearPreview"><img v-if="previewUrl" :src="previewUrl" class="preview" alt="上传资源预览" /></el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { axiosInstance, http } from '@/utils/http'

type UploadItem = { id: string; originalName: string; contentType: string; sizeBytes: number; createdAt: string; ownerUserId: number; ownerUsername: string; resourceUrl: string }
const loading = ref(false); const items = ref<UploadItem[]>([]); const total = ref(0); const page = ref(1); const pageSize = 20
const query = ref(''); const kind = ref(''); const previewVisible = ref(false); const previewUrl = ref('')

async function load() {
  loading.value = true
  try { const result = await http.get<any>('/api/admin/uploads', { q: query.value || undefined, kind: kind.value || undefined, page: page.value - 1, size: pageSize }); items.value = result.content || []; total.value = result.totalElements || 0 }
  finally { loading.value = false }
}
function resetAndLoad() { page.value = 1; load() }
async function fetchBlob(item: UploadItem) { return axiosInstance.get<any, Blob>(item.resourceUrl, { responseType: 'blob' }) }
async function openResource(item: UploadItem) {
  const blob = await fetchBlob(item)
  if (item.contentType.startsWith('image/')) { clearPreview(); previewUrl.value = URL.createObjectURL(blob); previewVisible.value = true; return }
  const url = URL.createObjectURL(blob); const anchor = document.createElement('a'); anchor.href = url; anchor.download = item.originalName; anchor.click(); setTimeout(() => URL.revokeObjectURL(url), 1000)
}
async function remove(item: UploadItem) {
  await ElMessageBox.confirm(`确定删除“${item.originalName}”吗？文件和记录都会被永久移除。`, '删除上传资源', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  await http.delete(`/api/admin/uploads/${item.id}`); ElMessage.success('资源已安全删除'); if (items.value.length === 1 && page.value > 1) page.value--; await load()
}
function clearPreview() { if (previewUrl.value) URL.revokeObjectURL(previewUrl.value); previewUrl.value = '' }
function formatDate(value: string) { return new Date(value).toLocaleString('zh-CN') }
function formatSize(bytes: number) { if (bytes < 1024) return `${bytes} B`; if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`; return `${(bytes / 1048576).toFixed(1)} MB` }
onMounted(load); onBeforeUnmount(clearPreview)
</script>

<style scoped>
.upload-admin{display:grid;gap:16px}.upload-admin>header{display:flex;align-items:end;justify-content:space-between;gap:20px}.upload-admin header p{margin:0;color:#1689a7;font:700 11px ui-monospace;letter-spacing:2px}.upload-admin h1{margin:7px 0 4px;font-size:30px}.upload-admin header span{color:#7a8592;font-size:13px}.toolbar{display:flex;gap:10px;margin-bottom:16px}.toolbar .el-input{max-width:330px}.toolbar .el-select{width:160px}.file-cell{display:flex;align-items:center;gap:11px;min-width:0}.file-icon{width:40px;height:40px;display:grid;place-items:center;flex:0 0 auto;border-radius:8px;background:#e7f6fa;color:#087d99;font:800 10px ui-monospace}.file-cell div{display:grid;gap:4px;min-width:0}.file-cell b{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.file-cell small,.owner-id{display:block;color:#8b96a3;font-size:11px}.pager{display:flex;justify-content:flex-end;margin-top:16px}.preview{display:block;max-width:100%;max-height:70vh;margin:auto;object-fit:contain}@media(max-width:700px){.upload-admin>header{align-items:flex-start;flex-direction:column}.toolbar{align-items:stretch;flex-direction:column}.toolbar .el-input,.toolbar .el-select{max-width:none;width:100%}}
</style>
