import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'portal', component: () => import('@/views/PortalView.vue') },
  {
    path: '/fish',
    component: () => import('@/views/user/UserLayout.vue'),
    children: [
      { path: '', name: 'fishHome', component: () => import('@/views/user/HomeView.vue') },
      { path: 'products', name: 'products', component: () => import('@/views/user/ProductListView.vue') },
      { path: 'products/:id', name: 'productDetail', component: () => import('@/views/user/ProductDetailView.vue') },
      { path: 'articles', name: 'articles', component: () => import('@/views/user/ArticleListView.vue') },
      { path: 'articles/:id', name: 'articleDetail', component: () => import('@/views/user/ArticleDetailView.vue') },
      { path: 'posts', name: 'posts', component: () => import('@/views/user/PostListView.vue') },
      { path: 'orders', name: 'orders', component: () => import('@/views/user/OrderListView.vue'), meta: { requiresAuth: true } },
      { path: 'diseases', name: 'diseases', component: () => import('@/views/user/DiseaseListView.vue') },
      { path: 'diseases/:id', name: 'diseaseDetail', component: () => import('@/views/user/DiseaseDetailView.vue') },
      { path: 'diagnose', name: 'diagnose', component: () => import('@/views/user/DiagnoseView.vue') },
      { path: 'rag', name: 'rag', component: () => import('@/views/user/RagView.vue') },
      { path: 'knowledge-graph', name: 'knowledgeGraph', component: () => import('@/views/user/KnowledgeGraphView.vue') },
    ],
  },
  {
    path: '/personal',
    component: () => import('@/views/personal/PersonalLayout.vue'),
    children: [
      { path: '', name: 'personalHome', component: () => import('@/views/personal/PersonalHomeView.vue') },
      { path: 'games', name: 'games', component: () => import('@/views/user/GamesView.vue') },
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
    path: '/:section(products|articles|posts|orders|diseases|diagnose|knowledge-graph|rag)/:rest(.*)*',
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
    ],
  },
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

