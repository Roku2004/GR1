<script setup>
import axios from 'axios';
import { onMounted, ref } from 'vue';
import SlidebarCustomer from '@/components/SlidebarCustomer.vue';
import { useRouter } from 'vue-router';

const activeMenu = ref('dashboard');
const router = useRouter();
const isLoading = ref(true);
const customerInfo = ref(null);
const upcomingClasses = ref([]);
const membershipInfo = ref(null);
const message = ref('');
const messageType = ref('');
const currentDate = ref(new Date());

// Format date to DD/MM/YYYY
const formatDate = (date) => {
  if (!date) return 'N/A';
  const options = { year: 'numeric', month: '2-digit', day: '2-digit' };
  return new Date(date).toLocaleDateString('vi-VN', options);
};

// Calculate days remaining in membership
const calculateDaysLeft = (endDate) => {
  if (!endDate) return 0;
  const end = new Date(endDate);
  const today = new Date();
  const diffTime = end - today;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays > 0 ? diffDays : 0;
};

// Progress percentage for membership
const calculateProgress = (startDate, endDate) => {
  if (!startDate || !endDate) return 0;
  const start = new Date(startDate);
  const end = new Date(endDate);
  const today = new Date();
  
  const totalDays = (end - start) / (1000 * 60 * 60 * 24);
  const daysUsed = (today - start) / (1000 * 60 * 60 * 24);
  
  if (daysUsed < 0) return 0;
  if (daysUsed > totalDays) return 100;
  
  return Math.round((daysUsed / totalDays) * 100);
};

// Show notification message
const showMessage = (text, type = 'success') => {
  message.value = text;
  messageType.value = type;

  // Auto-hide message after 5 seconds
  setTimeout(() => {
    message.value = '';
  }, 5000);
};

// Handle API errors
const handleApiError = (error, defaultMessage) => {
  console.error(defaultMessage, error);
  const errorMessage = error.response?.data?.message || error.message || 'Đã xảy ra lỗi không xác định';
  showMessage(`${defaultMessage}: ${errorMessage}`, 'error');
};

// Fetch customer data
const fetchCustomerData = async () => {
  try {
    isLoading.value = true;
    const token = localStorage.getItem('token');
    
    if (!token) {
      showMessage('Vui lòng đăng nhập lại', 'error');
      router.push('/');
      return;
    }
    
    // Fetch customer info
    const response = await axios.get('http://localhost:3001/customer/getInfo', {
      headers: { token }
    });
    
    customerInfo.value = response.data.info;
    
    // Fetch membership info
    const membershipResponse = await axios.get('http://localhost:3001/customer/getMembership', {
      headers: { token }
    });
    
    membershipInfo.value = membershipResponse.data.membership;
    
    // Fetch upcoming classes
    const classesResponse = await axios.get('http://localhost:3001/customer/getUpcomingClasses', {
      headers: { token }
    });
    
    upcomingClasses.value = classesResponse.data.classes || [];
    
  } catch (error) {
    handleApiError(error, 'Không thể tải dữ liệu');
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchCustomerData();
  
  // Update current date every minute
  setInterval(() => {
    currentDate.value = new Date();
  }, 60000);
});

const handleMenuClick = (menuId) => {
  activeMenu.value = menuId;
};
</script>

<template>
  <div class="app-container">
    <!-- Sidebar -->
    <SlidebarCustomer :activeMenu="activeMenu" :onMenuClick="handleMenuClick" />
    
    <!-- Main Content -->
    <div class="main-content">
      <!-- Hiển thị thông báo -->
      <div v-if="message" class="notification" :class="messageType">
        {{ message }}
      </div>
    
      <!-- Loading Indicator -->
      <div v-if="isLoading" class="loading-container">
        <div class="loader"></div>
        <p>Đang tải dữ liệu...</p>
      </div>
      
      <!-- Dashboard Content -->
      <div v-else class="dashboard">
        <!-- Welcome Section -->
        <div class="welcome-section">
          <div class="date-time">
            <div class="current-time">{{ currentDate.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) }}</div>
            <div class="current-date">{{ currentDate.toLocaleDateString('vi-VN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}</div>
          </div>
          <h1 class="welcome-text">
            Xin chào, <span class="customer-name">{{ customerInfo?.customerName || 'Khách hàng' }}</span>!
          </h1>
          <p class="welcome-message">
            Chào mừng bạn quay trở lại PIG GYM. Hãy cùng xem tiến độ tập luyện của bạn nhé.
          </p>
        </div>
        
        <!-- Quick Stats -->
        <div class="stats-grid">
          <!-- Membership Status -->
          <div class="stat-card membership-card">
            <div class="card-header">
              <h3>Thông tin gói tập</h3>
              <span class="badge" :class="calculateDaysLeft(membershipInfo?.endDate) > 0 ? 'active' : 'expired'">
                {{ calculateDaysLeft(membershipInfo?.endDate) > 0 ? 'Đang hoạt động' : 'Hết hạn' }}
              </span>
            </div>
            <div class="membership-details">
              <div class="detail-row">
                <div class="label">Gói tập:</div>
                <div class="value">{{ membershipInfo?.name || 'Chưa có gói tập' }}</div>
              </div>
              <div class="detail-row">
                <div class="label">Ngày bắt đầu:</div>
                <div class="value">{{ formatDate(membershipInfo?.startDate) }}</div>
              </div>
              <div class="detail-row">
                <div class="label">Ngày kết thúc:</div>
                <div class="value">{{ formatDate(membershipInfo?.endDate) }}</div>
              </div>
              <div class="detail-row">
                <div class="label">Còn lại:</div>
                <div class="value highlight">{{ calculateDaysLeft(membershipInfo?.endDate) }} ngày</div>
              </div>
            </div>
            <div class="progress-container">
              <div class="progress-label">
                Tiến độ gói tập: {{ calculateProgress(membershipInfo?.startDate, membershipInfo?.endDate) }}%
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: calculateProgress(membershipInfo?.startDate, membershipInfo?.endDate) + '%' }"></div>
              </div>
            </div>
            <button v-if="calculateDaysLeft(membershipInfo?.endDate) < 7" class="extend-button">
              Gia hạn gói tập
            </button>
          </div>
          
          <!-- Quick Actions -->
          <div class="stat-card action-card">
            <h3>Truy cập nhanh</h3>
            <div class="quick-actions">
              <div class="action-item" @click="router.push('/customer/schedule')">
                <div class="action-icon">
                  <i class="fas fa-calendar-alt"></i>
                </div>
                <div class="action-text">Lịch tập</div>
              </div>
              <div class="action-item" @click="router.push('/customer/classes')">
                <div class="action-icon">
                  <i class="fas fa-dumbbell"></i>
                </div>
                <div class="action-text">Đăng ký lớp</div>
              </div>
              <div class="action-item" @click="router.push('/customer/profile')">
                <div class="action-icon">
                  <i class="fas fa-user"></i>
                </div>
                <div class="action-text">Hồ sơ</div>
              </div>
              <div class="action-item" @click="router.push('/customer/trainers')">
                <div class="action-icon">
                  <i class="fas fa-user-tie"></i>
                </div>
                <div class="action-text">Huấn luyện viên</div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Upcoming Classes -->
        <div class="upcoming-classes-section">
          <h2>Lịch tập sắp tới</h2>
          <div v-if="upcomingClasses.length === 0" class="no-classes">
            <p>Bạn chưa đăng ký lớp tập nào.</p>
            <button class="primary-button" @click="router.push('/customer/classes')">Đăng ký ngay</button>
          </div>
          <div v-else class="classes-grid">
            <div v-for="(classItem, index) in upcomingClasses.slice(0, 3)" :key="index" class="class-card">
              <div class="class-time">
                <div class="day">{{ new Date(classItem.date).toLocaleDateString('vi-VN', { weekday: 'short' }) }}</div>
                <div class="date">{{ new Date(classItem.date).getDate() }}</div>
                <div class="month">{{ new Date(classItem.date).toLocaleDateString('vi-VN', { month: 'short' }) }}</div>
              </div>
              <div class="class-info">
                <h4>{{ classItem.className }}</h4>
                <div class="info-row">
                  <i class="fas fa-clock"></i>
                  <span>{{ classItem.timeStart }} - {{ classItem.timeEnd }}</span>
                </div>
                <div class="info-row">
                  <i class="fas fa-map-marker-alt"></i>
                  <span>{{ classItem.room }}</span>
                </div>
                <div class="info-row">
                  <i class="fas fa-user-tie"></i>
                  <span>{{ classItem.trainerName }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="upcomingClasses.length > 3" class="view-all">
            <button class="secondary-button" @click="router.push('/customer/schedule')">Xem tất cả</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
}

.main-content {
  margin-left: 250px;
  padding: 24px;
  background-color: #f9fafb;
  min-height: 100vh;
  width: calc(100% - 250px);
}

/* Notification styling */
.notification {
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 20px;
  font-size: 14px;
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.notification.success {
  background-color: #d1fae5;
  color: #065f46;
  border-left: 4px solid #10b981;
}

.notification.error {
  background-color: #fee2e2;
  color: #b91c1c;
  border-left: 4px solid #ef4444;
}

/* Loading indicator */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80vh;
}

.loader {
  border: 4px solid #f3f4f6;
  border-top: 4px solid #4f46e5;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* Dashboard layout */
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Welcome Section */
.welcome-section {
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  position: relative;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  background-image: linear-gradient(135deg, #f5f7fa 0%, #e4e7f0 100%);
  overflow: hidden;
}

.welcome-section::before {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  width: 20%;
  height: 100%;
  background-image: url('https://raw.githubusercontent.com/your-repo/images/main/gym-pattern.svg');
  opacity: 0.1;
  background-size: cover;
}

.welcome-text {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.customer-name {
  color: #4f46e5;
}

.welcome-message {
  font-size: 16px;
  color: #4b5563;
  max-width: 600px;
}

.date-time {
  position: absolute;
  top: 24px;
  right: 24px;
  text-align: right;
}

.current-time {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.current-date {
  font-size: 14px;
  color: #6b7280;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.stat-card {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.badge {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}

.badge.active {
  background-color: #d1fae5;
  color: #065f46;
}

.badge.expired {
  background-color: #fecaca;
  color: #b91c1c;
}

/* Membership Card */
.membership-card {
  display: flex;
  flex-direction: column;
}

.membership-details {
  flex-grow: 1;
  margin-bottom: 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.detail-row:last-child {
  border-bottom: none;
}

.label {
  color: #6b7280;
  font-size: 14px;
}

.value {
  font-weight: 500;
  color: #1f2937;
  font-size: 14px;
}

.value.highlight {
  color: #4f46e5;
  font-weight: 600;
}

.progress-container {
  margin-bottom: 16px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #6b7280;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: #e5e7eb;
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #4f46e5;
  border-radius: 999px;
  transition: width 0.3s ease;
}

.extend-button {
  padding: 10px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  width: 100%;
}

.extend-button:hover {
  background-color: #4338ca;
}

/* Action Card */
.action-card h3 {
  margin-bottom: 16px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background-color: #f3f4f6;
  cursor: pointer;
  transition: all 0.2s;
}

.action-item:hover {
  background-color: #e5e7eb;
  transform: translateY(-2px);
}

.action-icon {
  width: 40px;
  height: 40px;
  background-color: #4f46e5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.action-icon i {
  color: white;
  font-size: 18px;
}

.action-text {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  text-align: center;
}

/* Upcoming Classes */
.upcoming-classes-section {
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.upcoming-classes-section h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20px;
}

.classes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.class-card {
  display: flex;
  background-color: #f9fafb;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s;
}

.class-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.class-time {
  width: 80px;
  background-color: #4f46e5;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
}

.day {
  font-size: 12px;
  text-transform: uppercase;
}

.date {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.month {
  font-size: 12px;
}

.class-info {
  flex-grow: 1;
  padding: 16px;
}

.class-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #6b7280;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row i {
  margin-right: 8px;
  width: 16px;
}

.no-classes {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 0;
  color: #6b7280;
}

.primary-button {
  padding: 10px 20px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.primary-button:hover {
  background-color: #4338ca;
}

.view-all {
  margin-top: 20px;
  text-align: center;
}

.secondary-button {
  padding: 8px 16px;
  background-color: white;
  color: #4f46e5;
  border: 1px solid #4f46e5;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.secondary-button:hover {
  background-color: #f5f3ff;
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    width: 100%;
  }

  .welcome-section {
    padding: 20px;
  }

  .date-time {
    position: static;
    margin-bottom: 16px;
    text-align: left;
  }

  .quick-actions {
    grid-template-columns: 1fr 1fr;
  }

  .classes-grid {
    grid-template-columns: 1fr;
  }
}
</style>