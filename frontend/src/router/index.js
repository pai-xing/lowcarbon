import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue')
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import('../views/ArticleDetail.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile/edit',
        name: 'EditProfile',
        component: () => import('../views/EditProfile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'publish',
        name: 'PublishArticle',
        component: () => import('../views/PublishArticle.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'admin/articles',
        name: 'ArticleManage',
        component: () => import('../views/ArticleManage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'category/:key',
        name: 'Category',
        component: () => import('../views/Category.vue')
      },
      {
        path: 'footprint',
        name: 'Footprint',
        component: () => import('../views/Footprint.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'checkin',
        name: 'Checkin',
        component: () => import('../views/Checkin.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  try {
    const userStore = useUserStore()
    
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
    } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
      next({ name: 'Home' })
    } else {
      next()
    }
  } catch (error) {
    console.error('路由守卫错误:', error)
    next()
  }
})

export default router
