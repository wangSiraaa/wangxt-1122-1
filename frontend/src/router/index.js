import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/manager', component: () => import('../views/Manager.vue'), meta: { role: 'MANAGER' } },
  { path: '/risk', component: () => import('../views/Risk.vue'), meta: { role: 'RISK' } },
  { path: '/fund', component: () => import('../views/Fund.vue'), meta: { role: 'FUND' } },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('factoring_user') || 'null')
  if (to.path === '/login') {
    if (user) {
      const roleMap = { MANAGER: '/manager', RISK: '/risk', FUND: '/fund' }
      next(roleMap[user.role] || '/login')
    } else {
      next()
    }
  } else {
    if (!user) {
      next('/login')
    } else if (to.meta.role && to.meta.role !== user.role) {
      const roleMap = { MANAGER: '/manager', RISK: '/risk', FUND: '/fund' }
      next(roleMap[user.role])
    } else {
      next()
    }
  }
})

export default router
