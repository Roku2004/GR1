import Login from '@/views/login.vue'; // Đổi tên file nếu cần
import Home from '@/views/home.vue'; // Đổi tên file nếu cần
import Price from '@/views/price.vue'; // Đổi tên file nếu cần
import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/price', component: Price},
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
