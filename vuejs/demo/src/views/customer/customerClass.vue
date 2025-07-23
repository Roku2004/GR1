<script setup>
import SlidebarCustomer from '@/components/SlidebarCustomer.vue';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import timeGridPlugin from '@fullcalendar/timegrid';
import FullCalendar from '@fullcalendar/vue3';
import axios from 'axios';
import { onMounted, ref } from 'vue';

const activeMenu = ref('class');
const isLoading = ref(false);
const message = ref('');
const messageType = ref('');

// Modal state
const showModal = ref(false);
const modalData = ref({});

const token = localStorage.getItem('token');
const userid = localStorage.getItem('userid');
const API_BASE_URL = 'http://localhost:3001/customer';

const classData = ref([]);

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

const fetchClassData = async () => {
    try {
        isLoading.value = true;
        if(!token) {
            showMessenger('Vui lòng đăng nhập lại', 'error');
            return;
        }
        
        const response = await axios.post(`${API_BASE_URL}/getSession`,
            { userid },
            { headers: { token } }
        );
        
        if(response.data.status === 'Thành công') {
                classData.value = response.data.data.map(session => ({
                    sessionid: session.sessionid,
                    trainer_name: session.trainer_name,
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
                            trainer_name: session.trainer_name,
                            description: session.description,
                            sessionid: session.sessionid
                        },
                        backgroundColor: session.exercise_type,
                        borderColor: session.exercise_type,
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

onMounted(() => {
    fetchClassData();
});

const handleMenuClick = (menuId) => {
  activeMenu.value = menuId;
  console.log(`Menu clicked: ${menuId}`);
};

</script>

<template>
    <div class="app-container">
        <SlidebarCustomer :activeMenu="activeMenu" :onMenuClick="handleMenuClick"/>
        
        <div class="main-content">
            <h1 class="page-title">Lịch học của tôi</h1>
            
            <!-- Hiển thị thông báo -->
            <div v-if="message" class="notification" :class="messageType">
                {{ message }}
            </div>
            
            <!-- Loading indicator -->
            <div v-if="isLoading" class="loading-indicator">
                <div class="loader"></div>
                <div>Đang tải dữ liệu...</div>
            </div>
            
            <!-- Class Schedule Calendar -->
            <div v-else class="schedule-container">
                <div class="calendar-container">
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
                                <p><strong>Huấn luyện viên:</strong> {{ session.trainer_name }}</p>
                                <p><strong>Mô tả:</strong> {{ session.description }}</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Empty state for no classes -->
                <div v-if="classData.length === 0" class="empty-state">
                    <div class="empty-icon">
                        <i class="fas fa-calendar-times"></i>
                    </div>
                    <h3>Chưa có lịch học nào</h3>
                    <p>Hiện tại bạn chưa có buổi học nào được lên lịch.</p>
                </div>
            </div>
        </div>
        
        <!-- Modal for event details -->
        <div v-if="showModal" class="modal-overlay" @click="closeModal">
            <div class="modal-content" @click.stop>
                <div class="modal-header">
                    <h2 class="modal-title">Chi tiết lớp học</h2>
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
    padding: 24px;
    background-color: #f9fafb;
    min-height: 100vh;
    width: calc(100% - 250px);
}

.page-title {
    font-size: 24px;
    font-weight: 700;
    color: #111827;
    margin-bottom: 24px;
}

/* Notification styling */
.notification {
    padding: 12px 16px;
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

.notification.info {
    background-color: #dbeafe;
    color: #1e40af;
    border-left: 4px solid #3b82f6;
    white-space: pre-line;
}

/* Loading indicator */
.loading-indicator {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 60vh;
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

/* Schedule container styling */
.schedule-container {
    margin-top: 20px;
}

.calendar-container {
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

/* Responsive styling */
@media (max-width: 768px) {
    .main-content {
        margin-left: 0;
        width: 100%;
        padding: 16px;
    }
    
    .calendar-container {
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