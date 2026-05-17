import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../views/login.vue'
import Register from '../views/register.vue'
import Dashboard from '../views/dashboard.vue'
import User from '../views/user.vue'
import Script from '../views/script.vue'
import Session from '../views/session.vue'
import Dm from '../views/dm.vue'
import Order from '../views/order.vue'
import Reservation from '../views/reservation.vue'
import Evaluation from '../views/evaluation.vue'
import Statistics from '../views/statistics.vue'
import Store from '../views/store.vue'
import SystemConfig from '../views/system-config.vue'
import DmSchedule from '../views/dm-schedule.vue'
import OperationLog from '../views/operation-log.vue'
import Profile from '../views/profile.vue'
import Notification from '../views/notification.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true, roles: ['ADMIN', 'DM', 'USER'] }
  },
  {
    path: '/user',
    name: 'User',
    component: User,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/script',
    name: 'Script',
    component: Script,
    meta: { requiresAuth: true, roles: ['ADMIN', 'DM'] }
  },
  {
    path: '/session',
    name: 'Session',
    component: Session,
    meta: { requiresAuth: true, roles: ['ADMIN', 'DM'] }
  },
  {
    path: '/dm',
    name: 'Dm',
    component: Dm,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/dm-schedule',
    name: 'DmSchedule',
    component: DmSchedule,
    meta: { requiresAuth: true, roles: ['ADMIN', 'DM'] }
  },
  {
    path: '/reservation',
    name: 'Reservation',
    component: Reservation,
    meta: { requiresAuth: true, roles: ['ADMIN', 'USER'] }
  },
  {
    path: '/order',
    name: 'Order',
    component: Order,
    meta: { requiresAuth: true, roles: ['ADMIN', 'USER'] }
  },
  {
    path: '/evaluation',
    name: 'Evaluation',
    component: Evaluation,
    meta: { requiresAuth: true, roles: ['ADMIN', 'USER'] }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: Statistics,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true, roles: ['ADMIN', 'DM', 'USER'] }
  },
  {
    path: '/store',
    name: 'Store',
    component: Store,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/system-config',
    name: 'SystemConfig',
    component: SystemConfig,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/operation-log',
    name: 'OperationLog',
    component: OperationLog,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/notification',
    name: 'Notification',
    component: Notification,
    meta: { requiresAuth: true, roles: ['ADMIN', 'DM'] }
  }
]

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes
})

function getCurrentRole() {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    return String(user.role || '').toUpperCase()
  } catch (e) {
    return ''
  }
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!to.meta.requiresAuth && token && (to.path === '/login' || to.path === '/register')) {
    next('/dashboard')
    return
  }

  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
      return
    }

    const allowedRoles = Array.isArray(to.meta.roles) ? to.meta.roles : []
    if (allowedRoles.length > 0) {
      const role = getCurrentRole()
      if (!role || !allowedRoles.includes(role)) {
        next('/dashboard')
        return
      }
    }
  }
  next()
})

export default router
