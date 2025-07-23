<script setup>
import SlidebarPT from '@/components/SlidebarPT.vue';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import timeGridPlugin from '@fullcalendar/timegrid';
import FullCalendar from '@fullcalendar/vue3';
import axios from 'axios';
import { onMounted, ref } from 'vue';

const activeMenu = ref('ptclass');
const isLoading = ref(false);
const message = ref('');
const messageType = ref('');
const token = localStorage.getItem('token');
const userid = localStorage.getItem('userid');
const API_BASE_URL = 'http://localhost:3001/pt';
const classData = ref([]);
const showAddForm = ref(false);

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

// Thêm biến để quản lý chức năng chỉnh sửa
const editingSession = ref(null);
const editFormData = ref({
  sessionid: '',
  begin_time: '',
  end_time: '',
  description: '',
  customerid: '',
  exerciseType: ''
});
const isSubmitting = ref(false);

const showMessenger = (text, type = 'success') => {
  message.value = text;
  messageType.value = type;

  // Tự động ẩn sau 5 giây
  setTimeout(() => {
    message.value = '';
  }, 5000);
};

const handleApiError = (error, defaultMessage) => {
  console.error(defaultMessage, error);
  const errorMessage = error.response?.data?.message || error.message || 'Đã xảy ra lỗi không xác định';
  showMessenger(`${defaultMessage}: ${errorMessage}`, 'error');
};

// lấy dữ liệu lớp học
const fetchClassData = async () => {
  try {
    isLoading.value = true;
    if (!token) {
      showMessenger('Vui lòng đăng nhập lại', 'error');
      return;
    }

    const response = await axios.post(`${API_BASE_URL}/getSession`,
      { userid },
      { headers: { token } }
    );

    if (response.data.status === 'success') {
      classData.value = response.data.data.map(session => ({
        sessionid: session.sessionid,
        customer_name: session.customer_name,
        begin_time: session.begin_time,
        end_time: session.end_time,
        exercise_type: session.exercise_type,
        description: session.description
      }));
      
      // Convert data to FullCalendar events format
      const events = response.data.data.map(session => {
        const startDate = parseVietnameseDate(session.begin_time);
        const endDate = parseVietnameseDate(session.end_time);
        
        return {
          id: session.sessionid.toString(),
          title: session.exercise_type,
          start: startDate,
          end: endDate,
          extendedProps: {
            customer_name: session.customer_name,
            description: session.description,
            sessionid: session.sessionid
          },
          backgroundColor: getEventColor(session.exercise_type),
          borderColor: getEventColor(session.exercise_type),
          textColor: '#ffffff'
        };
      }).filter(event => event.start); // Only include events with valid start time
      
      calendarOptions.value.events = events;
    } else {
      showMessenger(response.data.message || 'Không có lịch học nào', 'error');
      classData.value = [];
      calendarOptions.value.events = [];
    }
  } catch (error) {
    handleApiError(error, 'Lỗi khi lấy dữ liệu lớp học');
    classData.value = [];
    calendarOptions.value.events = [];
  } finally {
    isLoading.value = false;
  }
};

//addSession
const addSession = async () => {
  try {
    if (!token) {
      showMessenger('Vui lòng đăng nhập lại', 'error');
      return;
    }

    const response = await axios.post(`${API_BASE_URL}/addSessionPT`,
      {
        userid,
        beginAt: editFormData.value.begin_time.replace('T', ' '),
        endAt: editFormData.value.end_time.replace('T', ' '),
        customerid: editFormData.value.customerid,
        exerciseType: editFormData.value.exerciseType,
        description: editFormData.value.description
      },
      { headers: { token } }
    );

    if (response.data.status === 'success') {
      showMessenger('Thêm buổi học thành công', 'success');
      fetchClassData();
    } else {
      showMessenger(response.data.message || 'Thêm buổi học không thành công', 'error');
    }
  } catch (error) {
    handleApiError(error, 'Lỗi khi thêm buổi học');
  } finally {
    isLoading.value = false;
  }
};

const resetEditForm = () => {
  editFormData.value = {
    begin_time: '',
    end_time: '',
    description: '',
    customerid: '',
    exerciseType: ''
  };
};

const toggleAddForm = () => {
  showAddForm.value = !showAddForm.value;
  if (!showAddForm.value) {
    resetEditForm();
  } else {
    fetchClassData();
  }
};

// chỉnh sửa cho một buổi học
const startEditing = (session) => {
  editingSession.value = session.sessionid;
  editFormData.value = {
    sessionid: session.sessionid,
    begin_time: session.begin_time,
    end_time: session.end_time,
    description: ''
  };
};

const cancelEditing = () => {
  editingSession.value = null;
  editFormData.value = {
    sessionid: '',
    begin_time: '',
    end_time: '',
    description: ''
  };
};

// lưu thông tin đã chỉnh sửa
const saveSessionChanges = async () => {
  try {
    isSubmitting.value = true;

    if (!token) {
      showMessenger('Vui lòng đăng nhập lại', 'error');
      return;
    }

    if (!editFormData.value.begin_time || !editFormData.value.end_time) {
      showMessenger('Vui lòng nhập đầy đủ thời gian bắt đầu và kết thúc', 'error');
      return;
    }

    const response = await axios.post(`${API_BASE_URL}/updateSession`,
      {
        userid,
        sessionid: editFormData.value.sessionid,
        beginAt: editFormData.value.begin_time.replace('T', ' '),
        endAt: editFormData.value.end_time.replace('T', ' '),
        description: editFormData.value.description
      },
      { headers: { token } }
    );
    if (response.data.status === 'success') {
      showMessenger('Cập nhật thông tin buổi học thành công', 'success');
      cancelEditing();
      fetchClassData();

    } else {
      showMessenger(response.data.message || 'Cập nhật không thành công', 'error');
    }
  } catch (error) {
    handleApiError(error, 'Lỗi khi cập nhật thông tin buổi học');
  } finally {
    isSubmitting.value = false;
  }
};


onMounted(() => {
  fetchClassData();
});

const handleMenuClick = (menuId) => {
  activeMenu.value = menuId;
  console.log(`Menu clicked: ${menuId}`);
};
</script>

<template>
  <div class="class-page">
    <SlidebarPT :activeMenu="activeMenu" @menuClick="handleMenuClick" />

    <div class="content-area">
      <!-- Header -->
      <div class="header-section">
        <h1 class="page-title">Lịch dạy</h1>
        <button class="action-btn add-btn" @click="toggleAddForm">
          <span v-if="!showAddForm">Thêm buổi học</span>
          <span v-else>Ẩn form</span>
        </button>
      </div>

      <!-- Notification -->
      <div v-if="message" :class="['message-alert', `alert-${messageType}`]">
        {{ message }}
      </div>

      <div>
        <div v-if="showAddForm" class="add-session-form">
          <h2>Thêm buổi học</h2>
          <form @submit.prevent="addSession">
            <div class="form-group">
              <label for="customerid">Mã khách hàng:</label>
              <input type="text" id="customerid" v-model="editFormData.customerid" class="form-input" required />
            </div>

            <div class="form-group">
              <label for="begin_time">Thời gian bắt đầu:</label>
              <input type="datetime-local" id="begin_time" v-model="editFormData.begin_time" class="form-input"
                required />
            </div>

            <div class="form-group">
              <label for="end_time">Thời gian kết thúc:</label>
              <input type="datetime-local" id="end_time" v-model="editFormData.end_time" class="form-input" required />
            </div>

            <div class="form-group">
              <label for="exerciseType">Loại bài tập:</label>
              <input type="text" id="exerciseType" v-model="editFormData.exerciseType" class="form-input" required>
            </div>

            <div class="form-group">
              <label for="description">Mô tả:</label>
              <textarea id="description" v-model="editFormData.description" class="form-textarea"
                placeholder="Nhập mô tả"></textarea>
            </div>

            <button type="submit" class="save-btn" :disabled="isSubmitting">
              <i class="fas fa-plus"></i> Thêm buổi học
            </button>
          </form>
        </div>
      </div>
      <!-- Loading indicator -->
      <div v-if="isLoading" class="loading-container">
        <div class="spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>

      <!-- Main content -->
      <div v-else class="class-container">
        <!-- Calendar View -->
        <div class="calendar-container">
          <div class="calendar-wrapper">
            <FullCalendar :options="calendarOptions" />
          </div>
          
          <!-- Sessions without specific time -->
          <div v-if="classData.some(session => session.begin_time === 'Chưa xác định')" class="pending-sessions">
            <h3>Lớp học chưa xác định thời gian</h3>
            <div class="pending-list">
              <div 
                v-for="session in classData.filter(s => s.begin_time === 'Chưa xác định')" 
                :key="session.sessionid"
                class="pending-item"
              >
                <div class="pending-item-header">
                  <span class="exercise-type">{{ session.exercise_type }}</span>
                  <span class="session-id">#{{ session.sessionid }}</span>
                </div>
                <div class="pending-item-body">
                  <p><strong>Khách hàng:</strong> {{ session.customer_name }}</p>
                  <p><strong>Mô tả:</strong> {{ session.description }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty state -->
        <div v-if="classData.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="fas fa-calendar-alt"></i>
          </div>
          <h3 class="empty-title">Không có lịch học nào</h3>
          <p class="empty-desc">Bạn chưa có buổi học nào được lên lịch.</p>
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
.class-page {
  display: flex;
  min-height: 100vh;
  background-color: #f8f9fa;
}

.content-area {
  flex: 1;
  padding: 24px;
  margin-left: 240px;
  /* Adjust based on sidebar width */
}

/* Header */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}


/* Message alert */
.message-alert {
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  font-size: 15px;
  animation: fadeIn 0.3s ease;
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

.alert-success {
  background-color: #d1fae5;
  color: #065f46;
  border-left: 4px solid #10b981;
}

.alert-error {
  background-color: #fee2e2;
  color: #b91c1c;
  border-left: 4px solid #ef4444;
}

.alert-info {
  background-color: #dbeafe;
  color: #1e40af;
  border-left: 4px solid #3b82f6;
  white-space: pre-line;
}

/* Loading */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #3498db;
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

/* Class container */
.class-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Calendar container styling */
.calendar-container {
  margin-top: 20px;
}

.calendar-wrapper {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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
  color: #1e293b;
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
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
  margin-bottom: 20px;
}

.pending-sessions h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 8px;
}

.pending-list {
  display: grid;
  gap: 12px;
}

.pending-item {
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 16px;
  transition: all 0.2s ease;
}

.pending-item:hover {
  border-color: #cbd5e1;
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
  color: #1e293b;
  font-size: 1rem;
}

.session-id {
  background-color: #f1f5f9;
  color: #64748b;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.pending-item-body p {
  margin: 4px 0;
  font-size: 0.875rem;
  color: #64748b;
}

.pending-item-body strong {
  color: #1e293b;
}

/* Class list */
.class-list {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  margin-top: 25px;
}


.class-table {
  width: 100%;
  border-collapse: collapse;
}

.class-table th,
.class-table td {
  padding: 16px;
  text-align: left;
}

.class-table th {
  background-color: #f8fafc;
  color: #64748b;
  font-weight: 600;
  font-size: 14px;
  border-bottom: 1px solid #e2e8f0;
}

.class-row {
  border-bottom: 1px solid #e2e8f0;
  transition: background-color 0.2s;
}

.class-row:last-child {
  border-bottom: none;
}

.class-row:hover {
  background-color: #f8fafc;
}

.col-id {
  width: 80px;
  color: #64748b;
  font-weight: 600;
}

.col-customer {
  font-weight: 500;
}

.col-time {
  width: 180px;
  color: #1e293b;
}

.col-type {
  width: 150px;
}

.exercise-badge {
  display: inline-block;
  padding: 6px 12px;
  background-color: #e0f2fe;
  color: #0284c7;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.col-desc {
  max-width: 300px;
  color: #64748b;
}

.col-actions {
  width: 120px;
}

.actions-wrapper {
  display: flex;
  gap: 7px;
}

.action-btn {
  padding: 8px 12px;
  border-radius: 6px;
  border: none;
  background-color: rgb(105, 105, 236);
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.edit-btn:hover {
  background-color: #e0f2fe;
  color: #0284c7;
}

/* Edit mode styles */
.edit-cell {
  padding: 8px;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.form-textarea {
  min-height: 80px;
  resize: vertical;
}

.edit-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.save-btn,
.cancel-btn {
  padding: 8px 12px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.save-btn {
  background-color: #10b981;
  color: white;
}

.save-btn:hover {
  background-color: #059669;
}

.save-btn:disabled {
  background-color: #94a3b8;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #f1f5f9;
  color: #64748b;
}

.cancel-btn:hover {
  background-color: #e2e8f0;
  color: #475569;
}

/* Empty state */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  text-align: center;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.empty-icon {
  width: 80px;
  height: 80px;
  background-color: #f1f5f9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.empty-icon i {
  font-size: 32px;
  color: #94a3b8;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

/* Responsive styles */
@media (max-width: 768px) {
  .content-area {
    margin-left: 0;
    padding: 16px;
  }

  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
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
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 24px;
}

.modal-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  color: #64748b;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.modal-close:hover {
  color: #1e293b;
  background-color: #f1f5f9;
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
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.detail-value {
  font-size: 1rem;
  color: #1e293b;
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
  border-top: 1px solid #e2e8f0;
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

<script>
// Hàm định dạng ngày giờ để hiển thị
function formatDisplayDateTime(dateTimeStr) {
  if (!dateTimeStr) return 'N/A';

  try {
    const date = new Date(dateTimeStr);
    if (isNaN(date)) return 'N/A';

    // Format: DD/MM/YYYY HH:MM
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${day}/${month}/${year} ${hours}:${minutes}`;
  } catch (e) {
    console.error('Error formatting date:', e);
    return 'N/A';
  }
}
</script>