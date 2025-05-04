import Login from '@/views/login.vue'; // Đổi tên file nếu cần
import Home from '@/views/home.vue'; // Đổi tên file nếu cần

import ptHome from '@/views/pt/ptHome.vue';

import customerHome from '@/views/customer/customerHome.vue';

// router cho manager
import managerHome from '@/views/manager/managerHome.vue';
import review from '@/views/manager/review.vue';
import room from '@/views/manager/room.vue';
import extend from '@/views/manager/extend.vue';
import members from '@/views/manager/members.vue';
import classes from '@/views/manager/classes.vue';
import payment
 from '@/views/manager/payment.vue';
import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },

  { path: '/ptHome', component: ptHome },

  { path: '/customerHome', component: customerHome },


  { path: '/managerHome', component: managerHome },
  { path: '/review', component: review },
  { path: '/room', component: room },
  { path: '/extend', component: extend },
  { path: '/members', component: members },
  { path: '/classes', component: classes },
  { path: '/payment', component: payment },
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
