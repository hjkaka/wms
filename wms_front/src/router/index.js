import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
  { path: '/', redirect: '/dashboard' },
  // 登录页
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  // 主页（带侧边栏布局）
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'warehouse', name: 'Warehouse', component: () => import('../views/Warehouse.vue'), meta: { title: '仓库管理' } },
      { path: 'product', name: 'Product', component: () => import('../views/Product.vue'), meta: { title: '商品管理' } },
      { path: 'category', name: 'Category', component: () => import('../views/Category.vue'), meta: { title: '分类管理' } },
      { path: 'stockin', name: 'StockIn', component: () => import('../views/StockIn.vue'), meta: { title: '入库管理' } },
      { path: 'stockout', name: 'StockOut', component: () => import('../views/StockOut.vue'), meta: { title: '出库管理' } },
      { path: 'stock', name: 'Stock', component: () => import('../views/Stock.vue'), meta: { title: '库存查询' } },
      { path: 'stocktake', name: 'StockTake', component: () => import('../views/StockTake.vue'), meta: { title: '盘点管理' } },
      { path: 'report', name: 'Report', component: () => import('../views/Report.vue'), meta: { title: '决策报表' } },
      { path: 'user', name: 'User', component: () => import('../views/User.vue'), meta: { title: '用户管理', roles: ['ADMIN'] } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳登录页；有角色限制的页面校验角色
router.beforeEach(to => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/'
  }
  // 校验角色（meta.roles：允许访问的角色列表）
  if (to.meta.roles && to.meta.roles.length) {
    let userInfo = {}
    try {
      userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    } catch (e) {
      userInfo = {}
    }
    if (!to.meta.roles.includes(userInfo.role)) {
      return '/dashboard'
    }
  }
  // 更新页面标题
  document.title = to.meta.title ? `${to.meta.title} - WMS 仓储系统` : 'WMS 仓储系统'
})

export default router