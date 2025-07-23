<script setup>
import { ref, onMounted } from 'vue';
import axios  from 'axios';
import SlidebarPT from '@/components/SlidebarPT.vue';

const activeMenu = ref('ptcustomer');
const isLoading = ref(false);
const message = ref('');
const messageType = ref('');
const token = localStorage.getItem('token');
const userid = localStorage.getItem('userid');
const API_BASE_URL = 'http://localhost:3001/pt';

const customers = ref([]);

const showMessenger = (text, type = 'success') => {
  message.value = text;
  messageType.value = type;

  setTimeout(() => {
    message.value = '';
  }, 5000);
};

const handleApiError = (error, defaultMessage) => {
  console.error(defaultMessage, error);
  const errorMessage = error.response?.data?.message || error.message || 'Đã xảy ra lỗi không xác định';
  showMessenger(`${defaultMessage}: ${errorMessage}`, 'error');
};

const fetchCustomers = async () => {
  try {
    isLoading.value = true;
    if (!token) {
      showMessenger('Vui lòng đăng nhập lại', 'error');
      return;
    }

    const response = await axios.post(`${API_BASE_URL}/getAllCustomer`,
     { userid }, 
     { headers: { token } });
    const info = response.data;

    if (info.status === 'success') {
      customers.value = info.data.map(customer => ({
        customerId: customer.customerId,
        customerName: customer.customerName,
        customerAge: customer.customerAge,
        customerPhone: customer.customerPhone,
        customerEmail: customer.customerEmail,
        customerGender: customer.customerGender
      }));
    } else {
      showMessenger(info.message, 'error');
    }
  } catch (error) {
    handleApiError(error, 'Lỗi khi lấy danh sách khách hàng');
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchCustomers();
});

const handleMenuClick = (menuId) => {
  activeMenu.value = menuId;
  console.log(`Menu clicked: ${menuId}`);
};
</script>

<template>
  <div class="app-container">
    <!-- Sidebar -->
    <SlidebarPT :activeMenu="activeMenu" :onMenuClick="handleMenuClick" />

    <!-- Main Content -->
    <div class="main-content">
      <div class="header-actions">
        <h1 class="title">Danh sách khách hàng</h1>
      </div>

      <!-- Hiển thị thông báo -->
      <div v-if="message" class="notification" :class="messageType">
        {{ message }}
      </div>
      
      <div v-if="isLoading && !showAddForm && !showEditForm" class="loading-indicator">
        <div class="loader"></div>
        <div>Đang tải dữ liệu...</div>
      </div>
        
      <!-- Customer Table -->
      <div v-else-if="!isLoading" class="table-container">
        <table class="members-table">
          <thead>
            <tr>
              <th>Mã khách hàng</th>
              <th>Họ tên</th>
              <th>Tuổi</th>
              <th>Giới tính</th>
              <th>Số điện thoại</th>
              <th>Email</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="member in customers" :key="member.customerId">
              <td>{{ member.customerId }}</td>
              <td>{{ member.customerName }}</td>
              <td>{{ member.customerAge }}</td>
              <td>
                <span :class="{'gender-male': member.customerGender === 'Nam', 'gender-female': member.customerGender === 'Nữ'}">
                  {{ member.customerGender }}
                </span>
              </td>
              <td>{{ member.customerPhone }}</td>
              <td>{{ member.customerEmail }}</td>
            </tr>
            <!-- Hiển thị thông báo khi không có dữ liệu -->
            <tr v-if="customers.length === 0">
              <td colspan="8" class="no-data">Không có khách hàng nào được tìm thấy</td>
            </tr>
          </tbody>
        </table>
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

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}


/* Notification styling */
.notification {
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 16px;
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

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}


/* Loading indicator */
.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #6b7280;
}

.loader {
  border: 4px solid #f3f4f6;
  border-top: 4px solid #4f46e5;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}


/* Hiển thị khi không có dữ liệu */
.no-data {
  text-align: center;
  padding: 20px;
  color: #6b7280;
  font-style: italic;
}

.table-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
}

.members-table {
  width: 100%;
  border-collapse: collapse;
}

.members-table thead {
  background-color: #f3f4f6;
}

.members-table th {
  padding: 12px 16px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  border-bottom: 2px solid #e5e7eb;
}

.members-table td {
  padding: 12px 16px;
  text-align: center;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
  color: #4b5563;
}

.members-table tbody tr:hover {
  background-color: #f9fafb;
}

/* Styling for the gender column */
.gender-male {
  display: inline-block;
  padding: 2px 8px;
  background-color: #e0f2fe;
  color: #0369a1;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.gender-female {
  display: inline-block;
  padding: 2px 8px;
  background-color: #fce7f3;
  color: #be185d;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

</style>