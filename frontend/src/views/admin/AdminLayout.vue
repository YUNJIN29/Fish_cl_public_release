<template>
  <el-container class="admin-shell">
    <el-aside width="232px" class="desktop-aside">
      <button class="logo" type="button" @click="router.push('/admin')"><b>BM</b><span>水产智诊平台<small>管理控制台</small></span></button>
      <el-menu router :default-active="active" class="menu">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path"><span class="menu-index">{{ item.index }}</span>{{ item.label }}</el-menu-item>
      </el-menu>
      <div class="aside-foot"><span>Aquatic Lab OPERATIONS</span><small>权限与关键操作均受保护</small></div>
    </el-aside>

    <el-container class="admin-content">
      <el-header class="header">
        <div class="left">
          <button class="mobile-menu" type="button" aria-label="打开管理导航" @click="drawerOpen = true"><span></span><span></span><span></span></button>
          <el-button @click="router.push('/')">返回用户端</el-button>
          <span class="page-name">{{ currentLabel }}</span>
        </div>
        <div class="right"><span class="user">{{ auth.me?.nickname || auth.me?.username }}</span><el-button link type="danger" @click="logout">退出</el-button></div>
      </el-header>
      <el-main class="main"><RouterView /></el-main>
    </el-container>

    <el-drawer v-model="drawerOpen" direction="ltr" size="min(82vw, 300px)" :with-header="false" class="mobile-drawer">
      <div class="drawer-brand"><b>BM</b><div>水产智诊平台<small>管理控制台</small></div><button type="button" aria-label="关闭管理导航" @click="drawerOpen = false">×</button></div>
      <el-menu router :default-active="active" class="drawer-menu" @select="drawerOpen = false">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path"><span class="menu-index">{{ item.index }}</span>{{ item.label }}</el-menu-item>
      </el-menu>
      <div class="drawer-actions"><button type="button" @click="router.push('/'); drawerOpen = false">← 返回用户端</button><button type="button" class="danger" @click="logout">退出登录</button></div>
    </el-drawer>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore(); const route = useRoute(); const router = useRouter(); const drawerOpen = ref(false)
const menuItems = [
  { index: '01', path: '/admin', label: '仪表盘' }, { index: '02', path: '/admin/products', label: '商品管理' },
  { index: '03', path: '/admin/categories', label: '分类管理' }, { index: '04', path: '/admin/orders', label: '订单管理' },
  { index: '05', path: '/admin/review', label: '内容审核' }, { index: '06', path: '/admin/users', label: '用户管理' },
  { index: '07', path: '/admin/uploads', label: '资源管理' },
]
const active = computed(() => route.path)
const currentLabel = computed(() => menuItems.find((item) => item.path === route.path)?.label || '管理控制台')
watch(() => route.path, () => { drawerOpen.value = false })
function logout() { drawerOpen.value = false; auth.logout(); router.push('/') }
</script>

<style scoped>
.admin-shell{min-height:100vh;min-width:0;background:#f3f6fa}.desktop-aside{position:sticky;top:0;height:100vh;display:flex;flex-direction:column;background:#071827;color:white}.logo{width:100%;min-height:72px;display:flex;align-items:center;gap:11px;padding:0 17px;border:0;border-bottom:1px solid rgba(255,255,255,.08);background:transparent;color:white;text-align:left;cursor:pointer}.logo>b,.drawer-brand>b{width:36px;height:36px;display:grid;place-items:center;border-radius:9px;background:linear-gradient(135deg,#7357d8,#00b4d8);font:900 13px ui-monospace}.logo>span{display:grid;gap:3px;font-weight:800}.logo small,.drawer-brand small{display:block;color:#7e9caf;font:9px ui-monospace;letter-spacing:.12em}.menu{flex:1;border-right:0;background:transparent;padding:12px 8px}.menu :deep(.el-menu-item){height:48px;margin:3px 0;border-radius:8px;color:#9eb3c2}.menu :deep(.el-menu-item:hover){background:rgba(0,180,216,.09);color:#dff9ff}.menu :deep(.el-menu-item.is-active){background:rgba(0,180,216,.15);color:#57ddf5}.menu-index{width:29px;color:#4f7185;font:700 9px ui-monospace}.aside-foot{display:grid;gap:4px;padding:18px;border-top:1px solid rgba(255,255,255,.08);color:#557487}.aside-foot span{font:700 9px ui-monospace;letter-spacing:.13em}.aside-foot small{font-size:10px}.admin-content{min-width:0}.header{position:sticky;top:0;z-index:20;display:flex;align-items:center;justify-content:space-between;gap:12px;border-bottom:1px solid #e4eaf0;background:rgba(255,255,255,.94);backdrop-filter:blur(14px)}.left,.right{display:flex;align-items:center;gap:10px;min-width:0}.page-name{color:#657483;font-size:13px;font-weight:700}.user{max-width:180px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-weight:700}.mobile-menu{width:44px;height:44px;display:none;place-content:center;gap:4px;border:1px solid #dce5ec;border-radius:9px;background:white;cursor:pointer}.mobile-menu span{width:18px;height:2px;border-radius:2px;background:#173345}.main{min-width:0;overflow-x:hidden;background:#f3f6fa}.drawer-brand{display:grid;grid-template-columns:36px 1fr 38px;align-items:center;gap:11px;padding:14px 8px 20px;color:#173345;font-weight:850}.drawer-brand>button{width:38px;height:38px;border:1px solid #dde6ec;border-radius:8px;background:white;color:#6f7f8c;font-size:22px;cursor:pointer}.drawer-menu{border:0}.drawer-menu :deep(.el-menu-item){min-height:48px;border-radius:8px}.drawer-actions{display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-top:20px;padding:16px 8px;border-top:1px solid #e7edf1}.drawer-actions button{min-height:44px;border:1px solid #dce5eb;border-radius:8px;background:white;color:#405463;cursor:pointer}.drawer-actions .danger{color:#d33;border-color:#f0caca}@media(max-width:900px){.desktop-aside{display:none}.mobile-menu{display:grid}.header{height:62px;padding:0 12px}.main{padding:14px}.page-name{display:none}.right .user{max-width:90px;font-size:12px}}@media(max-width:480px){.header .left>.el-button{display:none}.main{padding:10px}.right{gap:3px}}
</style>
