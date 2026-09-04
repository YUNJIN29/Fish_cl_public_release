<template>
  <el-card>
    <template #header>
      <div class="hdr">
        <span>商品列表</span>
        <div class="filters">
          <el-select v-model="categoryId" placeholder="分类" clearable style="width: 140px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-select v-model="tag" placeholder="症状/功能" clearable style="width: 140px">
            <el-option v-for="t in commonTags" :key="t" :label="t" :value="t" />
          </el-select>
          <el-input v-model="q" placeholder="搜索关键词" style="max-width: 180px" @keyup.enter="load" />
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
      </div>
    </template>
    <div class="tips">支持按分类和关键词筛选，点击“查看”进入详情页选图浏览。</div>

    <el-row v-if="list.length > 0" :gutter="12">
      <el-col v-for="row in list" :key="row.id" :xs="24" :sm="12" :md="8" :lg="6" class="card-col">
        <el-card shadow="hover" class="goods-card">
          <el-image
            v-if="row.coverImageUrl"
            :src="toImg(row.coverImageUrl)"
            fit="cover"
            class="cover"
            @click="goDetail(row.id)"
          >
            <template #error>
              <div class="img-fallback">商品图片</div>
            </template>
          </el-image>

          <div class="title" @click="goDetail(row.id)">{{ row.name }}</div>
          <div class="price">{{ formatPrice(row.priceCents) }}</div>

          <div class="tags">
            <el-tag size="small" effect="plain">{{ row.categoryName }}</el-tag>
            <el-tag size="small" :type="row.stock > 0 ? 'success' : 'danger'" effect="light">
              {{ row.stock > 0 ? `库存 ${row.stock}` : '缺货' }}
            </el-tag>
          </div>

          <div class="actions">
            <el-button size="small" @click="goDetail(row.id)">查看详情</el-button>
            <el-button size="small" type="primary" :disabled="row.stock <= 0" @click="quickBuy(row.id)">
              立即下单
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-else description="暂无商品，请尝试更换筛选条件" />

    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="page + 1"
        :total="total"
        :page-size="size"
        @current-change="(p) => { page = p - 1; load() }"
      />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { http } from '@/utils/http'
import { ElMessage } from 'element-plus'

type Category = { id: number; name: string }
type Product = {
  id: number
  categoryId: number
  categoryName: string
  name: string
  priceCents: number
  stock: number
  coverImageUrl?: string | null
  status: string
}

const router = useRouter()
const categories = ref<Category[]>([])
const list = ref<Product[]>([])

const q = ref('')
const tag = ref('')
const categoryId = ref<number | null>(null)
const page = ref(0)
const size = 10
const total = ref(0)

const commonTags = [
  '烂鳍', '烂尾', '炸鳞', '细菌感染', '白毛', '水霉', '棉絮状物',
  '水质混浊', '氨氮偏高', '食欲减退', '体色暗淡', '浮头', '缺氧'
]

function formatPrice(cents: number) {
  const v = Number(cents || 0) / 100
  return '￥' + v.toFixed(2)
}

function toImg(url?: string | null) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url
  return url
}

function goDetail(id: number) {
  router.push(`/fish/products/${id}`)
}

function quickBuy(id: number) {
  router.push(`/fish/products/${id}`)
}

async function load() {
  const res = await http.get<any>('/api/products', {
    categoryId: categoryId.value || undefined,
    q: q.value || undefined,
    tag: tag.value || undefined,
    page: page.value,
    size,
  })
  list.value = res?.content || []
  total.value = res?.totalElements || 0
}

onMounted(async () => {
  const c = await http.get<Category[]>('/api/categories')
  categories.value = c || []
  await load()
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
.tips {
  margin-bottom: 10px;
  font-size: 12px;
  color: #888;
}
.card-col {
  margin-bottom: 12px;
}
.goods-card {
  border-radius: 10px;
}
.cover {
  width: 100%;
  height: 170px;
  border-radius: 8px;
  background: #f5f7fa;
  cursor: pointer;
}
.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #999;
}
.title {
  margin-top: 10px;
  font-weight: 700;
  line-height: 1.4;
  min-height: 40px;
  cursor: pointer;
}
.price {
  margin-top: 8px;
  color: #1677ff;
  font-size: 20px;
  font-weight: 800;
}
.tags {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.actions {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
}
</style>
