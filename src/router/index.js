import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/AIChat.vue')
  },
  {
    path: '/ai-chat',
    name: 'AIChat',
    component: () => import('../views/AIChat.vue')
  },
  
  {
    path: '/chat-pdf',
    name: 'ChatPDF',
    component: () => import('../views/ChatPDF.vue')
  },
  {
    path: '/AIInterview',
    name: 'AIInterview',
    component: () => import('../views/AIInterview.vue')
  },
  {
    path: '/ResumeEvaluation',
    name: 'ResumeEvaluation',
    component: () => import('../views/ResumeEvaluation.vue')
  },
  
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 