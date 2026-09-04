<template>
  <div class="shell">
    <header>
      <div class="brand" @click="$router.push('/personal')"><b>AD</b><span>工具空间</span></div>
      <nav><RouterLink to="/personal">首页</RouterLink><RouterLink to="/personal/games">游戏</RouterLink><RouterLink to="/personal/print">打印</RouterLink><RouterLink v-if="auth.token" to="/personal/me">个人中心</RouterLink></nav>
      <div class="actions">
        <div class="quick-links">
          <button :class="{ active: route.path === '/personal/me' }" @click="$router.push('/personal/me')">个人</button>
          <button :class="{ active: route.path.startsWith('/personal/games') }" @click="$router.push('/personal/games')">游戏</button>
          <button @click="$router.push('/fish')">水产平台</button>
        </div>
        <button class="back" @click="$router.push('/')">← 返回主入口</button>
        <button v-if="!auth.token" @click="$router.push('/login')">登录</button>
      </div>
    </header>
    <main><RouterView/></main><footer><div>© 2026 Aquatic Disease Knowledge Platform</div><span>Public sanitized edition</span></footer>
  </div>
</template>
<script setup lang="ts">import { useRoute } from 'vue-router';import { useAuthStore } from '@/stores/auth';const auth=useAuthStore();const route=useRoute()</script>
<style scoped>
.shell{min-height:100vh;background:var(--c-bg);color:var(--c-text);display:flex;flex-direction:column}header{min-height:66px;display:flex;align-items:center;padding:0 clamp(12px,4vw,55px);border-bottom:1px solid var(--c-border);position:sticky;top:0;z-index:20;background:var(--c-bg);opacity:.97}.brand{display:flex;align-items:center;gap:10px;cursor:pointer}.brand b{width:34px;height:34px;display:grid;place-items:center;border-radius:9px;background:linear-gradient(135deg,var(--c-purple),var(--c-cyan));color:white}.brand span{font-weight:700}nav{display:flex;gap:26px;margin-left:auto;margin-right:28px}nav a{color:var(--c-text-2);text-decoration:none;font-size:14px;white-space:nowrap}nav a.router-link-exact-active{color:var(--c-cyan)}.actions{display:flex;align-items:center;gap:8px}.actions button{border:1px solid var(--c-border);background:var(--c-bg-card);color:var(--c-text-2);border-radius:7px;padding:7px 12px;cursor:pointer;white-space:nowrap}.actions .back{border-color:var(--c-cyan);color:var(--c-cyan)}.quick-links{display:flex;align-items:center;gap:4px;padding:3px;border:1px solid var(--c-border);border-radius:8px;background:var(--c-bg-card)}.quick-links button{border:0;background:transparent;border-radius:6px;padding:6px 10px;font-weight:700}.quick-links button:hover,.quick-links .active{background:var(--c-cyan-dim);color:var(--c-cyan)}main{flex:1;width:100%;max-width:1240px;margin:auto;padding:20px 16px}footer{text-align:center;border-top:1px solid var(--c-border);padding:22px;color:var(--c-text-3);font-size:12px}@media(max-width:980px){.quick-links{display:none}}@media(max-width:700px){header{flex-wrap:wrap;padding:8px 12px;gap:7px}.brand span{display:none}nav{order:3;width:100%;justify-content:center;gap:18px;margin:0}.actions{margin-left:auto}.actions button{padding:6px 8px;font-size:11px}}
</style>
