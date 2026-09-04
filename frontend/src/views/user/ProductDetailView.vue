<template>
  <el-card v-if="detail">
    <template #header>
      <div class="hdr">
        <div class="title">{{ detail.name }}</div>
        <div class="price">{{ formatPrice(detail.priceCents) }}</div>
      </div>
    </template>

    <div class="grid">
      <div class="left">
        <el-image v-if="currentImage" :src="toImg(currentImage)" fit="cover" class="cover" />
        <div v-else class="cover-placeholder">暂无图片</div>

        <div class="thumbs" v-if="detail.images && detail.images.length > 0">
          <div
            v-for="img in detail.images"
            :key="img"
            class="thumb"
            :class="{ active: currentImage === img }"
            @click="currentImage = img"
          >
            <el-image :src="toImg(img)" fit="cover" class="thumb-img" />
          </div>
        </div>
      </div>

      <div class="right">
        <div class="meta-row">
          <span class="label">分类：</span>
          <span>{{ detail.categoryName }}</span>
        </div>
        <div class="meta-row">
          <span class="label">库存：</span>
          <span>{{ detail.stock }}</span>
        </div>
        <div class="meta-row">
          <span class="label">状态：</span>
          <el-tag :type="detail.status === 'ON' ? 'success' : 'info'">
            {{ detail.status === 'ON' ? '上架' : '下架' }}
          </el-tag>
        </div>

        <div class="meta-row" v-if="detail.symptomTags">
          <span class="label">对症标签：</span>
          <div class="tags">
            <el-tag v-for="tag in detail.symptomTags.split(',')" :key="tag" size="small" effect="plain" class="tag">
              {{ tag }}
            </el-tag>
          </div>
        </div>

        <div class="desc" v-if="detail.description">
          <div class="section-title">产品描述</div>
          {{ detail.description }}
        </div>

        <div class="advice" v-if="detail.usageAdvice">
          <div class="section-title">养殖建议 / 用法说明</div>
          <div class="advice-content">{{ detail.usageAdvice }}</div>
        </div>

        <div class="buy-box">
          <div class="buy-info">
            <div>单价：<span class="val">{{ formatPrice(detail.priceCents) }}</span></div>
            <div>数量：<span class="val">{{ safeQty }}</span></div>
            <div>小计：<span class="subtotal">{{ formatPrice(subtotalCents) }}</span></div>
            <div class="stock-tip">最多可买 {{ detail.stock || 0 }} 件</div>
          </div>
          <div class="qty-row">
            <span class="label">数量：</span>
            <el-input-number v-model="qty" :min="1" :max="detail.stock || 1" />
          </div>
          <el-button
            type="primary"
            :loading="creating"
            :disabled="detail.stock <= 0"
            @click="createOrder"
          >
            {{ detail.stock > 0 ? '立即下单' : '暂时缺货' }}
          </el-button>
        </div>
      </div>
    </div>
  </el-card>

  <el-skeleton v-else :rows="10" animated />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { http } from '@/utils/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)

const detail = ref<any>(null)
const currentImage = ref('')
const qty = ref(1)
const creating = ref(false)
const safeQty = computed(() => {
  const stock = Math.max(1, Number(detail.value?.stock || 1))
  return Math.min(Math.max(1, Number(qty.value || 1)), stock)
})
const subtotalCents = computed(() => Number(detail.value?.priceCents || 0) * safeQty.value)

function formatPrice(cents: number) {
  const v = Number(cents || 0) / 100
  return '￥' + v.toFixed(2)
}

onMounted(async () => {
  detail.value = await http.get<any>(`/api/products/${id}`)
  const imgs = (detail.value?.images || []) as string[]
  currentImage.value = imgs[0] || detail.value?.coverImageUrl || ''
})

watch([qty, detail], () => {
  qty.value = safeQty.value
})

function toImg(url?: string | null) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url
  return url
}

async function createOrder() {
  if (!detail.value) return
  await ElMessageBox.confirm(
    `确认下单？\n商品：${detail.value.name}\n数量：${safeQty.value}\n小计：${formatPrice(subtotalCents.value)}`,
    '下单确认',
    { type: 'warning' },
  )
  creating.value = true
  try {
    await http.post('/api/orders', {
      items: [{ productId: detail.value.id, quantity: safeQty.value }],
    })
    ElMessage.success('下单成功，已跳转我的订单')
    router.push('/orders')
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.hdr {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}
.title {
  font-weight: 700;
}
.price {
  color: #1677ff;
  font-weight: 800;
}
.grid {
  display: flex;
  gap: 20px;
}
.left {
  width: 380px;
}
.cover {
  width: 100%;
  height: 280px;
  border-radius: 6px;
  background: #f5f7fa;
}
.cover-placeholder {
  width: 100%;
  height: 280px;
  border-radius: 6px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}
.thumbs {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.thumb-img {
  width: 100px;
  height: 70px;
  border-radius: 4px;
  background: #f5f7fa;
}
.thumb {
  cursor: pointer;
  border: 2px solid transparent;
  border-radius: 6px;
}
.thumb.active {
  border-color: #1677ff;
}
.right {
  flex: 1;
}
.meta-row {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.tag {
  border-radius: 4px;
}
.section-title {
  font-size: 15px;
  font-weight: 700;
  margin: 16px 0 8px;
  padding-left: 8px;
  border-left: 4px solid var(--c-cyan);
  color: var(--c-text);
}
.desc {
  margin-top: 16px;
  color: var(--c-text-2);
  line-height: 1.6;
  white-space: pre-wrap;
}
.advice {
  margin-top: 16px;
  background: var(--c-bg-soft, #f9fafb);
  padding: 12px;
  border-radius: 8px;
  border: 1px dashed var(--c-border);
}
.advice-content {
  color: var(--c-text-2);
  line-height: 1.6;
  font-size: 14px;
}
.buy-box {
  margin-top: 18px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}
.buy-info {
  min-width: 210px;
  line-height: 1.8;
}
.val {
  color: #333;
  font-weight: 600;
}
.subtotal {
  color: #e53935;
  font-weight: 700;
}
.stock-tip {
  color: #909399;
  font-size: 12px;
}
.qty-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
