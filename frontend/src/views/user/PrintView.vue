<template>
  <div class="print-page">
    <section class="page-head">
      <div>
        <h1>打印</h1>
        <p>上传需要打印的文件，并查看打印文件夹中的全部内容。</p>
      </div>
      <el-button :icon="Refresh" @click="loadFiles" :loading="loading">刷新</el-button>
    </section>

    <div class="print-layout">
      <el-card shadow="never" class="upload-panel">
        <template #header>
          <div class="panel-title">
            <el-icon><UploadFilled /></el-icon>
            <span>上传文件</span>
          </div>
        </template>

        <el-upload
          drag
          action="/api/print/upload"
          name="file"
          :show-file-list="false"
          :headers="uploadHeaders"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">将文件拖到这里，或点击选择文件</div>
          <template #tip>
            <div class="upload-tip">文件会保存到后端独立的 <code>print-files</code> 文件夹。</div>
          </template>
        </el-upload>
      </el-card>

      <el-card shadow="never" class="file-panel">
        <template #header>
          <div class="panel-title">
            <el-icon><FolderOpened /></el-icon>
            <span>文件夹文件</span>
            <el-tag size="small" type="info">{{ files.length }} 个文件</el-tag>
          </div>
        </template>

        <el-skeleton v-if="loading" :rows="6" animated />

        <el-empty v-else-if="files.length === 0" description="暂无文件" />

        <el-table v-else :data="files" border style="width: 100%">
          <el-table-column prop="name" label="文件名" min-width="280" show-overflow-tooltip />
          <el-table-column label="大小" width="120">
            <template #default="{ row }">{{ formatSize(row.size) }}</template>
          </el-table-column>
          <el-table-column label="上传时间" width="180">
            <template #default="{ row }">{{ formatTime(row.lastModified) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" :icon="View" @click="openFile(row.url)">查看</el-button>
              <el-button size="small" :icon="Download" @click="downloadFile(row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadProps } from 'element-plus'
import { Download, FolderOpened, Refresh, UploadFilled, View } from '@element-plus/icons-vue'
import { http } from '@/utils/http'

type PrintFile = {
  name: string
  url: string
  size: number
  lastModified: number
}

const files = ref<PrintFile[]>([])
const loading = ref(false)

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const maxSize = 20 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件不能超过 20MB')
    return false
  }
  return true
}

const handleUploadSuccess: UploadProps['onSuccess'] = async () => {
  ElMessage.success('上传成功')
  await loadFiles()
}

const handleUploadError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败')
}

async function loadFiles() {
  loading.value = true
  try {
    files.value = await http.get<PrintFile[]>('/api/print/files')
  } finally {
    loading.value = false
  }
}

function openFile(url: string) {
  window.open(url, '_blank', 'noopener,noreferrer')
}

function downloadFile(file: PrintFile) {
  const link = document.createElement('a')
  link.href = file.url
  link.download = file.name
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

function formatSize(size: number) {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function formatTime(value: number) {
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function pad(value: number) {
  return value.toString().padStart(2, '0')
}

onMounted(loadFiles)
</script>

<style scoped>
.print-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 0 4px;
}

.page-head h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: var(--c-text);
}

.page-head p {
  margin: 8px 0 0;
  color: var(--c-text-2);
}

.print-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.upload-panel,
.file-panel {
  border: 1px solid var(--c-border);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
}

.panel-title .el-tag {
  margin-left: auto;
}

.upload-icon {
  font-size: 48px;
  color: var(--c-cyan);
}

.upload-text {
  margin-top: 8px;
  color: var(--c-text);
}

.upload-tip {
  margin-top: 12px;
  color: var(--c-text-3);
  font-size: 12px;
  line-height: 1.6;
}

.upload-tip code {
  color: var(--c-cyan);
}

@media (max-width: 900px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .print-layout {
    grid-template-columns: 1fr;
  }
}
</style>
