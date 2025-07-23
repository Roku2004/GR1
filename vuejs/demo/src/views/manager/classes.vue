<script setup>
import Sidebar from '@/components/SlidebarManager.vue';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import timeGridPlugin from '@fullcalendar/timegrid';
import FullCalendar from '@fullcalendar/vue3';
import axios from 'axios';
import { onMounted, ref } from 'vue';


// API base URL
const API_BASE_URL = 'http://localhost:3001/manager';

const activeMenu = ref('classes');
const classes = ref([]);
const message = ref('');
const messageType = ref('');
const showAddForm = ref(false);
const showUpdateForm = ref(false);
const isLoading = ref(false);

// Modal state
const showModal = ref(false);
const modalData = ref({});

// FullCalendar configuration
const calendarOptions = ref({
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    locale: 'vi',
    events: [],
    eventClick: handleEventClick,
    height: 'auto'
});

// Helper function to parse Vietnamese date format
const parseVietnameseDate = (dateStr) => {
    if (!dateStr || dateStr === 'Chưa xác định') {
        return null;
    }
    
    try {
        // Convert "18/05/2025 00:01" to ISO format
        const [datePart, timePart] = dateStr.split(' ');
        const [day, month, year] = datePart.split('/');
        return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}T${timePart || '00:00'}`;
    } catch (error) {
        console.error('Error parsing date:', dateStr, error);
        return null;
    }
};

// Handle event click
function handleEventClick(clickInfo) {
    const event = clickInfo.event;
    const sessionData = event.extendedProps;
    
    modalData.value = {
        title: event.title,
        customer_name: sessionData.customer_name,
        trainer_name: sessionData.trainer_name,
        sessionid: sessionData.sessionid,
        start_time: event.start ? event.start.toLocaleString('vi-VN') : 'Chưa xác định',
        end_time: event.end ? event.end.toLocaleString('vi-VN') : 'Chưa xác định',
        description: sessionData.description
    };
    
    showModal.value = true;
}

// Close modal function
const closeModal = () => {
    showModal.value = false;
    modalData.value = {};
};

// Function to get color for different exercise types
const getEventColor = (exerciseType) => {
    const colors = {
        'yoga': '#10b981',
        'Tăng chiều cao': '#3b82f6',
        'Tăng cơ tay': '#f59e0b',
        'Tăng cơ bụng': '#ef4444',
        'Tăng cơ chân': '#8b5cf6',
        'Lên xà': '#06b6d4'
    };
    return colors[exerciseType] || '#6b7280';
};

// Form data để thêm lớp học mới
const formData = ref({
  sessionid: '',
  cufirstname: '',
  culastname: '',
  ptfirstname: '',
  ptlastname: '',
  exerciseType: ''
});

// Hiển thị thông báo
const showMessage = (text, type = 'success') => {
  message.value = text;
  messageType.value = type;

  // Auto-hide sau 5 giây
  setTimeout(() => {
    message.value = '';
  }, 5000);
};

// Xử lý lỗi API
const handleApiError = (error, defaultMessage) => {
  console.error(defaultMessage, error);
  const errorMessage = error.response?.data?.message || error.message || 'Đã xảy ra lỗi không xác định';
  showMessage(`${defaultMessage}: ${errorMessage}`, 'error');
};

// Lấy token từ localStorage
const getToken = () => {
  const token = localStorage.getItem('token');
  if (!token) {
    showMessage('Token không tồn tại. Vui lòng đăng nhập lại.', 'error');
    return null;
  }
  return token;
};

// Reset form
const resetAddForm = () => {
  formData.value = {
    customerid: '',
    ptid: '',
    exerciseType: ''
  };
  message.value = '';
};

const resetUpdateForm = () => {
  formData.value = {
    sessionid: '',
    cufirstname: '',
    culastname: '',
    ptfirstname: '',
    ptlastname: '',
    exerciseType: ''
  };
};

// Bật/tắt form thêm
const toggleAddForm = () => {
  showAddForm.value = !showAddForm.value;
  if (!showAddForm.value) {
    resetAddForm();
  }
};

const toggleUpdateForm = () => {
  showUpdateForm.value = !showUpdateForm.value;
  if (!showUpdateForm.value) {
    resetUpdateForm();
  }
};

// Thêm lớp học mới
const submitClass = async () => {
  try {
    // Validate form
    if (!formData.value.customerid || !formData.value.ptid ||
      !formData.value.exerciseType) {
      showMessage('Vui lòng điền đầy đủ thông tin buổi học', 'error');
      return;
    }

    isLoading.value = true;
    const token = getToken();
    if (!token) return;

    // Debug: Log what we're sending
    console.log('Adding class with data:', formData.value);

    const response = await axios.post(
      `${API_BASE_URL}/addExercise`,
      {
        customerid: formData.value.customerid,
        ptid: formData.value.ptid,
        exerciseType: formData.value.exerciseType
      },
      {
        headers: { token }
      }
    );

    // Handle successful response
    if (response.data && response.data.status === 'Thêm buổi tập thành công') {
      showMessage('Thêm buổi tập thành công!');
      resetAddForm();
      fetchClasses();
    } else {
      showMessage(response.data?.message || 'Thêm buổi tập không thành công', 'error');
    }
  } catch (error) {
    handleApiError(error, 'Lỗi khi thêm buổi học');
  } finally {
    isLoading.value = false;
  }
};

const updateClass = async () => {
  try {
    // Validate form
    if (!formData.value.sessionid || !formData.value.cufirstname || !formData.value.culastname ||
      !formData.value.ptfirstname || !formData.value.ptlastname ||
      !formData.value.exerciseType) {
      showMessage('Vui lòng điền đầy đủ thông tin buổi học', 'error');
      return;
    }

    isLoading.value = true;
    const token = getToken();
    if (!token) return;

    // Debug: Log what we're sending
    console.log('Updating class with data:', formData.value);

    const response = await axios.post(
      `${API_BASE_URL}/updateExercise`,
      {
        sessionid: formData.value.sessionid,
        cufirstname: formData.value.cufirstname,
        culastname: formData.value.culastname,
        ptfirstname: formData.value.ptfirstname,
        ptlastname: formData.value.ptlastname,
        exerciseType: formData.value.exerciseType
      },
      {
        headers: { token }
      }
    );

    // Handle successful response
    if (response.data && response.data.status === 'Sửa buổi tập thành công') {
      showMessage('Sửa buổi tập thành công');
      resetUpdateForm();
      fetchClasses();
    } else {
      showMessage(response.data?.message || 'Sửa buổi tập không thành công', 'error');
    }
  } catch (error) {
    handleApiError(error, 'Lỗi khi cập nhật buổi học');
  } finally {
    isLoading.value = false;
  }
};

// Fetch dữ liệu từ API khi component được mount
const fetchClasses = async () => {
  try {
    isLoading.value = true;
    // Lấy token từ localStorage
    const token = getToken();
    if (!token) return;

    // Gửi request đến server với token trong header
    const response = await axios.get(`${API_BASE_URL}/getExercise`, {
      headers: {
        token: token
      }
    });

    // Xử lý dữ liệu trả về từ server
    if (response.data && response.data.list) {
      classes.value = response.data.list.map(classe => ({
        sessionid: classe.sessionid,
        customer_name: classe.customer_name,
        trainer_name: classe.trainer_name,
        begin_time: classe.begin_time,
        end_time: classe.end_time,
        exercise_type: classe.exercise_type,
        description: classe.description
      }));
      
      // Convert data to FullCalendar events format
      const events = response.data.list.map(classe => {
        const startDate = parseVietnameseDate(classe.begin_time);
        const endDate = parseVietnameseDate(classe.end_time);
        
        return {
          id: classe.sessionid.toString(),
          title: classe.exercise_type,
          start: startDate,
          end: endDate,
          extendedProps: {
            customer_name: classe.customer_name,
            trainer_name: classe.trainer_name,
            description: classe.description,
            sessionid: classe.sessionid
          },
          backgroundColor: getEventColor(classe.exercise_type),
          borderColor: getEventColor(classe.exercise_type),
          textColor: '#ffffff'
        };
      }).filter(event => event.start); // Only include events with valid start time
      
      calendarOptions.value.events = events;
    } else {
      console.warn('Dữ liệu trả về không hợp lệ:', response.data);
      classes.value = [];
      calendarOptions.value.events = [];
    }
  } catch (error) {
    handleApiError(error, 'Lỗi khi lấy danh sách lớp học');
    classes.value = [];
    calendarOptions.value.events = [];
  } finally {
    isLoading.value = false;
  }
};

// Methods
const handleMenuClick = (menuId) => {
  activeMenu.value = menuId;
  console.log(`Menu clicked: ${menuId}`);
};

// Initialize data when component is mounted
onMounted(() => {
  fetchClasses();
});

</script>

<template>
  <div class="app-container">
    <!-- Sidebar -->
    <Sidebar :activeMenu="activeMenu" :onMenuClick="handleMenuClick" />

    <!-- Main Content -->
    <div class="main-content">
      <div class="header-actions">
        <h1 class="title">Danh sách buổi học của khách hàng</h1>

        <div class="formAction">
          <button @click="toggleAddForm" class="add-button">
            <span v-if="!showAddForm">+ Thêm buổi học mới</span>
            <span v-else>Ẩn form</span>
          </button>

          <button @click="toggleUpdateForm" class="update-button">
            <span v-if="!showUpdateForm"> Cập nhật buổi học</span>
            <span v-else>Ẩn form</span>
          </button>
        </div>
      </div>
      <!-- Hiển thị thông báo -->
      <div v-if="message" class="notification" :class="messageType">
        {{ message }}
      </div>

      <!-- Add Class Form -->
      <div v-if="showAddForm" class="form-container">
        <h2 class="form-title">Thêm buổi học mới</h2>

        <form @submit.prevent="submitClass" class="add-form">

          <div class="form-group">
            <label for="customerid">Mã khách hàng</label>
            <input type="text" id="customerid" v-model="formData.customerid" placeholder="Nhập mã khách hàng"
              class="form-control" required />
          </div>

          <div class="form-group">
            <label for="ptid">Mã huấn luyện viên</label>
            <input type="text" id="ptid" v-model="formData.ptid" placeholder="Nhập mã huấn luyện viên"
              class="form-control" required />
          </div>

          <div class="form-group">
            <label for="exerciseType">Loại bài tập</label>
            <input type="text" id="exerciseType" v-model="formData.exerciseType" placeholder="Nhập loại bài tập"
              class="form-control" required />
          </div>

          <div class="form-actions">
            <button type="button" @click="resetAddForm" class="reset-button">Đặt lại</button>
            <button type="submit" class="submit-button" :disabled="isLoading">
              <span v-if="isLoading">Đang thêm...</span>
              <span v-else>Thêm buổi học</span>
            </button>
          </div>
        </form>
      </div>
      <!-- form update-->
      <div v-if="showUpdateForm" class="form-container">
        <h2 class="form-title">Sửa buổi học</h2>

        <form @submit.prevent="updateClass" class="update-form">

          <div class="form-group">
            <label for="sessionid">Mã lớp học</label>
            <input type="text" id="sessionid" v-model="formData.sessionid" placeholder="Nhập mã lớp học"
              class="form-control" required />
          </div>

          <div class="form-group">
            <label for="cufirstname">Họ khách hàng</label>
            <input type="text" id="cufirstname" v-model="formData.cufirstname" placeholder="Nhập họ khách hàng"
              class="form-control" required />
          </div>

          <div class="form-group">
            <label for="culastname">Tên khách hàng</label>
            <input type="text" id="culastname" v-model="formData.culastname" placeholder="Nhập tên khách hàng"
              class="form-control" required />
          </div>

          <div class="form-group">
            <label for="ptfirstname">Họ huấn luyện viên</label>
            <input type="text" id="ptfirstname" v-model="formData.ptfirstname" placeholder="Nhập họ huấn luyện viên"
              class="form-control" required />
          </div>

          <div class="form-group">
            <label for="ptlastname">Tên huấn luyện viên</label>
            <input type="text" id="ptlastname" v-model="formData.ptlastname" placeholder="Nhập tên huấn luyện viên"
              class="form-control" required />
          </div>

          <div class="form-group">
            <label for="exerciseType">Loại bài tập</label>
            <input type="text" id="exerciseType" v-model="formData.exerciseType" placeholder="Nhập loại bài tập"
              class="form-control" required />
          </div>

          <div class="form-actions">
            <button type="button" @click="resetUpdateForm" class="reset-button">Đặt lại</button>
            <button type="submit" class="submit-button" :disabled="isLoading">
              <span v-if="isLoading">Đang cập nhật...</span>
              <span v-else>Cập nhật</span>
            </button>
          </div>
        </form>
      </div>
      <!-- Loading indicator -->
      <div v-if="isLoading" class="loading-indicator">
        <div class="loader"></div>
        <div>Đang tải dữ liệu...</div>
      </div>

      <!-- Classes Calendar -->
      <div class="calendar-container" v-else>
        <div class="calendar-wrapper">
          <FullCalendar :options="calendarOptions" />
        </div>
        
        <!-- Sessions without specific time -->
        <div v-if="classes.some(classe => classe.begin_time === 'Chưa xác định')" class="pending-sessions">
          <h3>Lớp học chưa xác định thời gian</h3>
          <div class="pending-list">
            <div 
              v-for="classe in classes.filter(c => c.begin_time === 'Chưa xác định')" 
              :key="classe.sessionid"
              class="pending-item"
            >
              <div class="pending-item-header">
                <span class="exercise-type">{{ classe.exercise_type }}</span>
                <span class="session-id">#{{ classe.sessionid }}</span>
              </div>
              <div class="pending-item-body">
                <p><strong>Khách hàng:</strong> {{ classe.customer_name }}</p>
                <p><strong>Huấn luyện viên:</strong> {{ classe.trainer_name }}</p>
                <p><strong>Mô tả:</strong> {{ classe.description }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Empty state for no classes -->
        <div v-if="classes.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="fas fa-calendar-times"></i>
          </div>
          <h3>Không có buổi học nào</h3>
          <p>Hiện tại không có buổi học nào được tìm thấy.</p>
        </div>
      </div>
    </div>
    
    <!-- Modal for event details -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2 class="modal-title">Chi tiết buổi học</h2>
          <button class="modal-close" @click="closeModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="detail-item">
            <span class="detail-label">Mã lớp học:</span>
            <span class="detail-value">#{{ modalData.sessionid }}</span>
          </div>
          
          <div class="detail-item">
            <span class="detail-label">Loại bài tập:</span>
            <span class="detail-value exercise-badge">{{ modalData.title }}</span>
          </div>
          
          <div class="detail-item">
            <span class="detail-label">Khách hàng:</span>
            <span class="detail-value">{{ modalData.customer_name }}</span>
          </div>
          
          <div class="detail-item">
            <span class="detail-label">Huấn luyện viên:</span>
            <span class="detail-value">{{ modalData.trainer_name }}</span>
          </div>
          
          <div class="detail-item">
            <span class="detail-label">Thời gian bắt đầu:</span>
            <span class="detail-value">{{ modalData.start_time }}</span>
          </div>
          
          <div class="detail-item">
            <span class="detail-label">Thời gian kết thúc:</span>
            <span class="detail-value">{{ modalData.end_time }}</span>
          </div>
          
          <div class="detail-item">
            <span class="detail-label">Mô tả:</span>
            <span class="detail-value">{{ modalData.description }}</span>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-close" @click="closeModal">Đóng</button>
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
  padding: 20px;
  background-color: #f9fafb;
  min-height: 100vh;
  width: 100%;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
  margin: 0;
}

.formAction {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 5px;
  margin-bottom: 20px;
}

.add-button {
  padding: 8px 16px;
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.add-button:hover {
  background-color: #059669;
}

.update-button {
  padding: 8px 16px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.update-button:hover {
  background-color: #2563eb;
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

.notification.info {
  background-color: #dbeafe;
  color: #1e40af;
  border-left: 4px solid #3b82f6;
  white-space: pre-line;
}

/* Form container */
.form-container {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
  animation: slideDown 0.3s ease-in-out;
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

.form-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-top: 0;
  margin-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 12px;
}

.add-form {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.update-form {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #4b5563;
  margin-bottom: 6px;
}

.form-control {
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-control:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.2);
}

.form-actions {
  grid-column: span 2;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}

.reset-button {
  padding: 10px 20px;
  background-color: #f3f4f6;
  color: #4b5563;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.reset-button:hover {
  background-color: #e5e7eb;
}

.submit-button {
  padding: 10px 20px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.submit-button:hover:not(:disabled) {
  background-color: #4338ca;
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

/* Calendar container styling */
.calendar-container {
  margin-top: 20px;
}

.calendar-wrapper {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 24px;
}

/* FullCalendar custom styling */
:deep(.fc) {
  font-family: inherit;
}

:deep(.fc-toolbar-title) {
  font-size: 1.5rem;
  font-weight: 600;
  color: #111827;
}

:deep(.fc-button) {
  background-color: #4f46e5;
  border-color: #4f46e5;
  text-transform: capitalize;
  font-size: 0.875rem;
}

:deep(.fc-button:hover) {
  background-color: #4338ca;
  border-color: #4338ca;
}

:deep(.fc-button-active) {
  background-color: #3730a3 !important;
  border-color: #3730a3 !important;
}

:deep(.fc-event) {
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
}

:deep(.fc-daygrid-event) {
  border-radius: 4px;
  padding: 2px 4px;
}

/* Pending sessions styling */
.pending-sessions {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.pending-sessions h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 8px;
}

.pending-list {
  display: grid;
  gap: 12px;
}

.pending-item {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 16px;
  transition: all 0.2s ease;
}

.pending-item:hover {
  border-color: #d1d5db;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.pending-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.exercise-type {
  font-weight: 600;
  color: #374151;
  font-size: 1rem;
}

.session-id {
  background-color: #f3f4f6;
  color: #6b7280;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.pending-item-body p {
  margin: 4px 0;
  font-size: 0.875rem;
  color: #4b5563;
}

.pending-item-body strong {
  color: #374151;
}

/* Empty state styling */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 48px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.empty-state p {
  color: #6b7280;
  max-width: 400px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

/* Table styling */
.table-container {
  overflow-x: auto;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 0;
}

thead {
  background-color: #f3f4f6;
}

th {
  text-align: center;
  padding: 12px;
  font-size: 14px;
  font-weight: bold;
  color: #374151;
  border-bottom: 2px solid #e5e7eb;
}

td {
  text-align: center;
  padding: 12px;
  font-size: 14px;
  color: #4b5563;
  border-bottom: 1px solid #e5e7eb;
}

tr:hover {
  background-color: #f9fafb;
}

.no-data {
  text-align: center;
  font-style: italic;
  color: #6b7280;
}

/* Responsive styling */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 15px;
  }

  .add-form {
    grid-template-columns: 1fr;
  }

  .form-actions {
    grid-column: 1;
  }
  
  .calendar-wrapper {
    padding: 12px;
  }
  
  :deep(.fc-toolbar) {
    flex-direction: column;
    gap: 8px;
  }
  
  :deep(.fc-toolbar-chunk) {
    display: flex;
    justify-content: center;
  }
  
  .pending-item-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .modal-content {
    margin: 20px;
    max-height: calc(100vh - 40px);
  }
}

/* Modal styling */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeInOverlay 0.3s ease-out;
}

@keyframes fadeInOverlay {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  animation: slideInModal 0.3s ease-out;
}

@keyframes slideInModal {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0 24px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

.modal-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  color: #6b7280;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.modal-close:hover {
  color: #374151;
  background-color: #f3f4f6;
}

.modal-body {
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.detail-value {
  font-size: 1rem;
  color: #111827;
  font-weight: 500;
}

.exercise-badge {
  display: inline-block;
  background-color: #4f46e5;
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  width: fit-content;
}

.modal-footer {
  padding: 24px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e5e7eb;
  margin-top: 24px;
}

.btn-close {
  background-color: #4f46e5;
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-close:hover {
  background-color: #4338ca;
  transform: translateY(-1px);
}
</style>