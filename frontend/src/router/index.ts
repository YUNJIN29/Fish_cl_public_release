import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'portal', component: () => import('@/views/PortalView.vue'), meta: { title: 'Aquatic Lab · 数字空间', description: '个人作品、游戏实验、项目经历与水产病害智能知识平台。' } },
  { path: '/profile', name: 'personalProfile', component: () => import('@/views/personal/PersonalProfileView.vue'), meta: { title: '个人简介 · Aquatic Lab', description: 'Aquatic Lab 的项目经历、竞赛成果与资质证明。' } },
  { path: '/personal/profile', redirect: '/profile' },
  {
    path: '/fish',
    component: () => import('@/views/user/UserLayout.vue'),
    children: [
      { path: '', name: 'fishHome', component: () => import('@/views/user/HomeView.vue'), meta: { title: '水产智诊平台 · Aquatic Lab', description: '水产病害诊断、RAG 知识检索与知识图谱平台。' } },
      { path: 'products', name: 'products', component: () => import('@/views/user/ProductListView.vue') },
      { path: 'products/:id', name: 'productDetail', component: () => import('@/views/user/ProductDetailView.vue') },
      { path: 'articles', name: 'articles', component: () => import('@/views/user/ArticleListView.vue') },
      { path: 'articles/:id', name: 'articleDetail', component: () => import('@/views/user/ArticleDetailView.vue') },
      { path: 'posts', name: 'posts', component: () => import('@/views/user/PostListView.vue') },
      { path: 'orders', name: 'orders', component: () => import('@/views/user/OrderListView.vue'), meta: { requiresAuth: true } },
      { path: 'diseases', name: 'diseases', component: () => import('@/views/user/DiseaseListView.vue'), meta: { title: '病害知识库 · 水产智诊平台' } },
      { path: 'diseases/:id', name: 'diseaseDetail', component: () => import('@/views/user/DiseaseDetailView.vue') },
      { path: 'diagnose', name: 'diagnose', component: () => import('@/views/user/DiagnoseView.vue'), meta: { requiresAuth: true, title: 'AI 智能诊断 · 水产智诊平台' } },
      { path: 'rag', name: 'rag', component: () => import('@/views/user/RagView.vue'), meta: { title: 'RAG 知识检索 · 水产智诊平台' } },
      { path: 'knowledge-graph', name: 'knowledgeGraph', component: () => import('@/views/user/KnowledgeGraphView.vue'), meta: { title: '病害知识图谱 · 水产智诊平台' } },
      { path: 'calculator', name: 'dosageCalculator', component: () => import('@/views/user/DosageCalculatorView.vue'), meta: { title: '水体用量计算器 · 水产智诊平台', description: '根据水体体积和产品标签数据进行用量数学换算。' } },
      { path: 'water-quality', name: 'waterQuality', component: () => import('@/views/user/WaterQualityView.vue'), meta: { requiresAuth: true, title: '水质日志 · 水产智诊平台', description: '连续记录个人水体的客观测量数据。' } },
      { path: 'tasks', name: 'aquacultureTasks', component: () => import('@/views/user/TaskPlannerView.vue'), meta: { requiresAuth: true, title: '养殖待办 · 水产智诊平台', description: '安排投喂、水质检测、换水和复查事项。' } },
    ],
  },
  {
    path: '/personal',
    component: () => import('@/views/personal/PersonalLayout.vue'),
    children: [
      { path: '', name: 'personalHome', component: () => import('@/views/personal/PersonalHomeView.vue'), meta: { title: '个人工作台 · Aquatic Lab', description: '继续浏览作品、游戏与水产智诊平台常用功能。' } },
      { path: 'games', name: 'games', component: () => import('@/views/user/GamesView.vue'), meta: { title: '游戏中心 · Aquatic Lab' } },
      { path: 'games/1024', name: 'game1024', component: () => import('@/views/user/Game1024View.vue') },
      { path: 'games/tetris', name: 'tetris', component: () => import('@/views/user/TetrisView.vue') },
      { path: 'games/snake', name: 'snake', component: () => import('@/views/user/SnakeView.vue') },
      { path: 'games/tic-tac-toe', name: 'ticTacToe', component: () => import('@/views/user/TicTacToeView.vue') },
      { path: 'games/minesweeper', name: 'minesweeper', component: () => import('@/views/user/MinesweeperView.vue') },
      { path: 'games/memory', name: 'memory', component: () => import('@/views/user/MemoryGameView.vue') },
      { path: 'games/whack-a-mole', name: 'whackAMole', component: () => import('@/views/user/WhackAMoleView.vue') },
      { path: 'games/rps', name: 'rockPaperScissors', component: () => import('@/views/user/RockPaperScissorsView.vue') },
      { path: 'me', name: 'me', component: () => import('@/views/user/MeView.vue'), meta: { requiresAuth: true } },
      { path: 'print', name: 'print', component: () => import('@/views/user/PrintView.vue') },
    ],
  },
  {
    path: '/:section(products|articles|posts|orders|diseases|diagnose|knowledge-graph|rag|calculator|tasks)/:rest(.*)*',
    redirect: (to) => `/fish/${to.params.section}${Array.isArray(to.params.rest) && to.params.rest.length ? `/${to.params.rest.join('/')}` : ''}`,
  },
  {
    path: '/games/:rest(.*)*',
    redirect: (to) => `/personal/games${Array.isArray(to.params.rest) && to.params.rest.length ? `/${to.params.rest.join('/')}` : ''}`,
  },
  { path: '/print', redirect: '/personal/print' },
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      { path: '', name: 'adminDashboard', component: () => import('@/views/admin/DashboardView.vue') },
      { path: 'products', name: 'adminProducts', component: () => import('@/views/admin/ProductManageView.vue') },
      { path: 'categories', name: 'adminCategories', component: () => import('@/views/admin/CategoryManageView.vue') },
      { path: 'orders', name: 'adminOrders', component: () => import('@/views/admin/OrderManageView.vue') },
      { path: 'review', name: 'adminReview', component: () => import('@/views/admin/ReviewManageView.vue') },
      { path: 'users', name: 'adminUsers', component: () => import('@/views/admin/UserManageView.vue') },
      { path: 'uploads', name: 'adminUploads', component: () => import('@/views/admin/UploadManageView.vue'), meta: { title: '上传资源管理 · Aquatic Lab' } },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'notFound', component: () => import('@/views/NotFoundView.vue'), meta: { title: '页面未找到 · Aquatic Lab' } },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (auth.token && !auth.meLoaded) {
    await auth.fetchMe().catch(() => {
      auth.logout()
    })
  }

  const requiresAuth = Boolean(to.meta.requiresAuth)
  if (requiresAuth && !auth.token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  const requiredRoles = (to.meta.roles as string[] | undefined) ?? []
  if (requiredRoles.length > 0) {
    const has = auth.me?.roles?.some((r) => requiredRoles.includes(r)) ?? false
    if (!has) return { name: 'portal' }
  }

  return true
})

router.afterEach((to) => {
  document.title = String(to.meta.title || 'Aquatic Lab · 数字空间')
  const description = document.querySelector<HTMLMetaElement>('meta[name="description"]')
  if (description && to.meta.description) description.content = String(to.meta.description)
  const canonical = document.querySelector<HTMLLinkElement>('link[rel="canonical"]')
  if (canonical) canonical.href = `https://example.com${to.path === '/' ? '/' : to.path}`
  if (!to.path.startsWith('/admin') && to.name !== 'login') {
    try {
      const key = 'yunjin:recent-pages'
      const previous = JSON.parse(localStorage.getItem(key) || '[]') as Array<{ path: string; title: string; visitedAt: number }>
      const current = { path: to.path, title: document.title, visitedAt: Date.now() }
      localStorage.setItem(key, JSON.stringify([current, ...previous.filter((item) => item.path !== to.path)].slice(0, 6)))
      window.dispatchEvent(new Event('yunjin:recent-pages'))
    } catch { /* 浏览器禁用本地存储时不影响导航 */ }
  }
})

