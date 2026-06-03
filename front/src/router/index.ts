import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/chats',
    name: 'ChatList',
    component: () => import('../views/ChatList.vue'),
    children: [
      {
        path: ':conversationId',
        name: 'ChatRoom',
        component: () => import('../views/ChatRoom.vue')
      },
      {
        path: 'profile/:userId?',
        name: 'Profile',
        component: () => import('../views/Profile.vue')
      },
      {
        path: 'agent',
        name: 'AgentChat',
        component: () => import('../views/AgentChat.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
