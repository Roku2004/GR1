<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
// Props
defineProps({
  activeMenu: String, // Menu đang được chọn
  onMenuClick: Function // Hàm xử lý khi nhấp vào menu
});


const router = useRouter();
// Menu Items

const goToItem = (item) => {
    const menuItem = menuItems.value.find(menu => menu.id === item);
    if (menuItem && menuItem.route) {
      router.push(menuItem.route); // Chuyển hướng đến route tương ứng
    } else {
      console.error('Route không tồn tại:', item);
    }
};

const goToHome = () => {
  router.push('/');
};

const menuItems = ref([
  { id: 'dashboard', text: 'Tổng quan', icon: 'fas fa-home', route: '/managerHome' },
  { id: 'members', text: 'Thành viên', icon: 'fas fa-users', route: '/members' },
  { id: 'classes', text: 'Lớp tập', icon: 'fas fa-dumbbell', route: '/classes' },
  { id: 'extend', text: 'Gia hạn gói tập', icon: 'fas fa-user-tie', route: '/extend' },
  { id: 'room', text: 'Thiết bị phòng tập', icon: 'fas fa-cog', route: '/room' },
  { id: 'review', text: 'Đánh giá của khách hàng', icon: 'fas fa-cog', route: '/review' },
  { id: 'payment', text: 'Thanh toán', icon: 'fas fa-credit-card', route: '/payment' }
]);
</script>

<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <div class="logo">
        <div class="logo-icon">
          <i class="fas fa-dumbbell"></i>
        </div>
        <div class="logo-text">PIG GYM</div>
      </div>
    </div>
    <ul class="menu-list">
      <li 
        v-for="(item, index) in menuItems" :key="index" 
        class="menu-item" 
        :class="{ active: activeMenu === item.id }"
        @click="goToItem(item.id)"
      >
        <div class="menu-icon">
          <i :class="item.icon"></i>
        </div>
        <div class="menu-text">{{ item.text }}</div>
      </li>
    </ul>
    <div class="sidebar-footer">
      <div class="account-info">
        <div  @click="goToHome" class="avatar-small">Đăng xuất</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sidebar {
  width: 250px;
  background-color: #1f2937;
  color: white;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 20px;
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  border-bottom: 1px solid #374151;
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
}

.menu-item {
  padding: 15px 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s;
}

.menu-item:hover {
  background-color: #374151;
}

/* .menu-item.active {
  background-color:yellow ;
} */

.menu-icon {
  margin-right: 10px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #374151;
}

.sidebar-footer:hover{
  background-color: #374151;
  cursor: pointer;
}
</style>