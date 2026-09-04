<template>
  <el-container style="min-height: 100vh">
    <el-aside width="220px" class="aside">
      <div class="logo" @click="$router.push('/admin')">水产智诊 管理端</div>
      <el-menu router :default-active="active" class="menu">
        <el-menu-item index="/admin">仪表盘</el-menu-item>
        <el-menu-item index="/admin/products">商品管理</el-menu-item>
        <el-menu-item index="/admin/categories">分类管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/review">内容审核</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="left">
          <el-button @click="$router.push('/')">返回用户端</el-button>
        </div>
        <div class="right">
          <span class="user">{{ auth.me?.nickname || auth.me?.username }}</span>
          <el-button link type="danger" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const active = computed(() => route.path)

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.aside {
  background: #001529;
  color: white;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-weight: 700;
  cursor: pointer;
}
.menu {
  border-right: none;
}
.header {
  background: white;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.main {
  background: #f6f7fb;
}
.user {
  margin-right: 12px;
  font-weight: 600;
}
</style>

