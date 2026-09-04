<template>
  <el-container style="min-height: 100vh; background: var(--c-bg)">
    <el-header class="header">
      <!-- 品牌 Logo -->
      <div class="brand" @click="$router.push('/fish')">
        <span class="brand-icon">◈</span>
        <span class="brand-text">水产智诊</span>
        <span class="brand-badge">V1.0</span>
      </div>

      <el-menu mode="horizontal" :default-active="active" router class="menu">
        <el-menu-item index="/fish">首页</el-menu-item>
        <el-menu-item index="/fish/diseases">病害知识库</el-menu-item>
        <el-menu-item index="/fish/diagnose">AI智能诊断</el-menu-item>
        <el-menu-item index="/fish/rag">RAG检索</el-menu-item>
        <el-menu-item index="/fish/knowledge-graph">知识图谱</el-menu-item>
        <el-menu-item index="/fish/products">蓝脉优选</el-menu-item>
        <el-menu-item index="/fish/articles">科普学习</el-menu-item>
        <el-menu-item index="/fish/posts">交流社区</el-menu-item>
      </el-menu>

      <div class="right">
        <div class="quick-links" aria-label="快捷入口">
          <el-button class="quick-link" size="small" @click="go('/personal/me')">个人</el-button>
          <el-button class="quick-link" size="small" @click="go('/personal/games')">游戏</el-button>
          <el-button class="quick-link active" size="small" @click="go('/fish')">水产平台</el-button>
        </div>

        <!-- 日/夜模式切换 -->
        <button class="theme-toggle" @click="theme.toggle" :title="theme.isDark ? '切换日间模式' : '切换夜间模式'">
          <span class="theme-track" :class="{ dark: theme.isDark }">
            <span class="theme-thumb">{{ theme.isDark ? '🌙' : '☀️' }}</span>
          </span>
        </button>

        <el-button class="switch-site" size="small" @click="go('/')">← 返回主入口</el-button>

        <template v-if="auth.token">
          <el-dropdown>
            <span class="user-btn">
              <span class="user-dot" />
              {{ auth.me?.nickname || auth.me?.username }}
              <span class="arrow">▾</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="go('/personal/me')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="go('/orders')">我的订单</el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" @click="go('/admin')">管理后台</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="go('/login')">登录</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="main">
      <div class="container">
        <RouterView />
      </div>
    </el-main>

    <footer class="footer">
      <div class="footer-line">
        <span>© 2026</span>
        <span>Aquatic Disease Knowledge Platform</span>
      </div>
      <div class="footer-line footer-meta">
        <span>学习科研与课程/竞赛原型项目</span>
      </div>
      <div class="footer-note">
        公开版本已移除个人联系方式、备案信息、服务器地址和私有部署脚本。请在自己的环境中配置数据库、密钥和部署信息。
      </div>
    </footer>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const auth = useAuthStore()
const theme = useThemeStore()
const route = useRoute()
const router = useRouter()

const active = computed(() => route.path)
const isAdmin = computed(() => auth.me?.roles?.includes('ADMIN') ?? false)

function go(path: string) { router.push(path) }
function logout() { auth.logout(); router.push('/fish') }
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 60px !important;
  padding: 0 24px;
  background: var(--header-bg, rgba(7, 12, 24, 0.95));
  border-bottom: 1px solid var(--c-border);
  backdrop-filter: blur(12px);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 20px rgba(0, 212, 255, 0.05);
  transition: background 0.3s;
}

/* 品牌 */
.brand {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}
.brand-icon {
  font-size: 20px;
  color: var(--c-cyan);
  filter: drop-shadow(0 0 6px var(--c-cyan));
  animation: pulse-icon 3s ease-in-out infinite;
}
@keyframes pulse-icon {
  0%, 100% { filter: drop-shadow(0 0 4px var(--c-cyan)); }
  50%       { filter: drop-shadow(0 0 10px var(--c-cyan)); }
}
.brand-text {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(135deg, var(--c-cyan), #a8edff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.brand-badge {
  font-size: 9px;
  padding: 1px 5px;
  border: 1px solid rgba(0,212,255,0.4);
  border-radius: 3px;
  color: var(--c-cyan);
  opacity: 0.7;
  letter-spacing: 1px;
}

/* 菜单 */
.menu { flex: 1; background: transparent !important; border-bottom: none !important; }
.menu-disease { color: inherit; }

/* 主题切换开关 */
.theme-toggle {
  background: none; border: none; cursor: pointer; padding: 0;
  display: flex; align-items: center; margin-right: 12px;
  outline: none;
}
.theme-track {
  width: 52px; height: 26px;
  border-radius: 13px;
  display: flex; align-items: center;
  padding: 2px 3px;
  transition: background 0.3s, box-shadow 0.3s;
  background: #c8d8ee;
  box-shadow: inset 0 1px 4px rgba(0,0,0,0.1);
  position: relative;
}
.theme-track.dark {
  background: #0d1e3c;
  box-shadow: inset 0 1px 4px rgba(0,0,0,0.4), 0 0 8px rgba(0,212,255,0.2);
}
.theme-thumb {
  width: 22px; height: 22px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.2);
  display: flex; align-items: center; justify-content: center;
  font-size: 12px;
  transition: transform 0.3s cubic-bezier(0.34,1.56,0.64,1);
  transform: translateX(0);
}
.theme-track.dark .theme-thumb {
  transform: translateX(26px);
  background: #0a1a30;
  box-shadow: 0 1px 6px rgba(0,212,255,0.3);
}

/* 右侧用户 */
.right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.quick-links {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-bg-card);
}
.quick-link {
  min-width: 48px;
  margin: 0 !important;
  border: 0 !important;
  border-radius: 6px !important;
  background: transparent !important;
  color: var(--c-text-2) !important;
  font-weight: 700;
}
.quick-link:hover,
.quick-link.active {
  background: var(--c-cyan-dim) !important;
  color: var(--c-cyan) !important;
}
.print-entry {
  margin-right: 10px;
  border-color: rgba(0,212,255,0.35);
  color: var(--c-cyan);
  background: var(--c-cyan-dim);
}
.print-entry:hover {
  border-color: var(--c-cyan);
  color: #fff;
  background: var(--c-cyan);
}
.user-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: var(--c-text-2);
  font-size: 14px;
  padding: 6px 12px;
  border: 1px solid var(--c-border);
  border-radius: 20px;
  transition: all 0.2s;
}
.user-btn:hover {
  border-color: var(--c-cyan);
  color: var(--c-cyan);
  background: var(--c-cyan-dim);
}
.user-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: var(--c-green);
  box-shadow: 0 0 6px var(--c-green);
  flex-shrink: 0;
}
.arrow { font-size: 10px; opacity: 0.6; }

/* 主内容 */
.main {
  padding: 20px 0 32px;
  background: var(--c-bg);
  min-height: calc(100vh - 60px - 112px);
}
.container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 页脚 */
.footer {
  min-height: 112px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  font-size: 12px;
  color: var(--c-text-3);
  background: rgba(7, 12, 24, 0.8);
  border-top: 1px solid var(--c-border);
}
.footer-line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
  text-align: center;
}
.footer a {
  color: inherit;
  text-decoration: none;
  transition: color 0.2s;
}
.footer a:hover {
  color: var(--c-cyan);
}
.sep { opacity: 0.3; }
.footer-note {
  max-width: 980px;
  line-height: 1.6;
  text-align: center;
  color: var(--c-text-3);
  opacity: 0.78;
}
@media (max-width: 1180px) {
  .quick-links {
    display: none;
  }
}
</style>
