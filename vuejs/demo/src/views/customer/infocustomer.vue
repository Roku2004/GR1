<script setup>
import { ref, onMounted, reactive } from 'vue';
import axios from 'axios';
import SlidebarCustomer from '@/components/SlidebarCustomer.vue';

const activeMenu = ref('infocustomer');
const isLoading = ref(true);
const isEditing = ref(false);
const message = ref('');
const messageType = ref('');
const showPassword = ref(false);

// Dữ liệu khách hàng
const customerData = ref({
  id: '',
  name: '',
  email: '',
  phone: '',
  age: '',
  gender: '',
  joinDate: '',
  avatar: null
});

// Dữ liệu thành viên
const membershipData = ref({
  package: '',
  startDate: '',
  endDate: '',
  status: '',
  daysLeft: 0
});

// Dữ liệu form chỉnh sửa
const editForm = reactive({
  email: '',
  phone: '',
  age: '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// Format ngày theo định dạng DD/MM/YYYY
const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const options = { year: 'numeric', month: '2-digit', day: '2-digit' };
  return new Date(dateString).toLocaleDateString('vi-VN', options);
};

// Tính số ngày còn lại
const calculateDaysLeft = (endDate) => {
  if (!endDate) return 0;
  const end = new Date(endDate);
  const today = new Date();
  const diffTime = end - today;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays > 0 ? diffDays : 0;
};

// Hiển thị thông báo
const showMessage = (text, type = 'success') => {
  message.value = text;
  messageType.value = type;
  
  // Tự động ẩn sau 5 giây
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

// Lấy dữ liệu khách hàng
const fetchCustomerData = async () => {
  try {
    isLoading.value = true;
    const token = localStorage.getItem('token');
    
    if (!token) {
      showMessage('Vui lòng đăng nhập lại', 'error');
      return;
    }
    
    // Lấy thông tin khách hàng
    const customerResponse = await axios.get('http://localhost:3001/customer/getInfo', {
      headers: { token }
    });
    
    // Xử lý dữ liệu khách hàng
    const info = customerResponse.data.info;
    customerData.value = {
      id: info.customerId,
      name: info.customerName,
      email: info.customerEmail,
      phone: info.customerPhone,
      age: info.customerAge,
      gender: info.customerGender,
      joinDate: formatDate(info.joinDate),
      avatar: info.customerAvatar || null
    };
    
    // Lấy thông tin gói tập
    const membershipResponse = await axios.get('http://localhost:3001/customer/getMembership', {
      headers: { token }
    });
    
    // Xử lý dữ liệu gói tập
    const membership = membershipResponse.data.membership;
    if (membership) {
      membershipData.value = {
        package: membership.name,
        startDate: formatDate(membership.startDate),
        endDate: formatDate(membership.endDate),
        status: new Date(membership.endDate) > new Date() ? 'Đang hoạt động' : 'Hết hạn',
        daysLeft: calculateDaysLeft(membership.endDate)
      };
    }
    
    // Khởi tạo form chỉnh sửa
    editForm.email = customerData.value.email;
    editForm.phone = customerData.value.phone;
    editForm.age = customerData.value.age;
    
  } catch (error) {
    handleApiError(error, 'Không thể tải thông tin khách hàng');
  } finally {
    isLoading.value = false;
  }
};

// Bắt đầu chỉnh sửa thông tin
const startEditing = () => {
  isEditing.value = true;
  // Reset form password
  editForm.currentPassword = '';
  editForm.newPassword = '';
  editForm.confirmPassword = '';
};

// Hủy chỉnh sửa
const cancelEditing = () => {
  isEditing.value = false;
  // Khôi phục dữ liệu ban đầu
  editForm.email = customerData.value.email;
  editForm.phone = customerData.value.phone;
  editForm.age = customerData.value.age;
};

// Lưu thông tin đã chỉnh sửa
const saveChanges = async () => {
  try {
    // Kiểm tra dữ liệu
    if (!editForm.email || !editForm.phone || !editForm.age) {
      showMessage('Vui lòng điền đầy đủ thông tin cơ bản', 'error');
      return;
    }
    
    // Nếu nhập mật khẩu mới, kiểm tra mật khẩu hiện tại và xác nhận
    if (editForm.newPassword) {
      if (!editForm.currentPassword) {
        showMessage('Vui lòng nhập mật khẩu hiện tại', 'error');
        return;
      }
      if (editForm.newPassword !== editForm.confirmPassword) {
        showMessage('Mật khẩu mới không khớp', 'error');
        return;
      }
      if (editForm.newPassword.length < 6) {
        showMessage('Mật khẩu mới phải có ít nhất 6 ký tự', 'error');
        return;
      }
    }
    
    isLoading.value = true;
    const token = localStorage.getItem('token');
    
    if (!token) {
      showMessage('Vui lòng đăng nhập lại', 'error');
      return;
    }
    
    // Dữ liệu gửi lên server
    const updateData = {
      email: editForm.email,
      phone: editForm.phone,
      age: editForm.age
    };
    
    // Thêm dữ liệu mật khẩu nếu có thay đổi
    if (editForm.newPassword) {
      updateData.currentPassword = editForm.currentPassword;
      updateData.newPassword = editForm.newPassword;
    }
    
    // Gửi yêu cầu cập nhật
    const response = await axios.post(
      'http://localhost:3001/customer/updateInfo',
      updateData,
      { headers: { token } }
    );
    
    if (response.data.status === 'success') {
      showMessage('Cập nhật thông tin thành công');
      // Cập nhật dữ liệu hiển thị
      customerData.value.email = editForm.email;
      customerData.value.phone = editForm.phone;
      customerData.value.age = editForm.age;
      isEditing.value = false;
    } else {
      showMessage(response.data.message || 'Cập nhật thông tin thất bại', 'error');
    }
    
  } catch (error) {
    handleApiError(error, 'Không thể cập nhật thông tin');
  } finally {
    isLoading.value = false;
  }
};

// Tải dữ liệu khi component được mount
onMounted(() => {
  fetchCustomerData();
});

// Xử lý upload avatar
const handleAvatarUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  const maxSize = 2 * 1024 * 1024; // 2MB
  
  if (file.size > maxSize) {
    showMessage('Kích thước ảnh tối đa là 2MB', 'error');
    return;
  }
  
  try {
    isLoading.value = true;
    const token = localStorage.getItem('token');
    
    if (!token) {
      showMessage('Vui lòng đăng nhập lại', 'error');
      return;
    }
    
    // Tạo FormData để upload file
    const formData = new FormData();
    formData.append('avatar', file);
    
    const response = await axios.post(
      'http://localhost:3001/customer/uploadAvatar',
      formData,
      { 
        headers: { 
          token,
          'Content-Type': 'multipart/form-data' 
        } 
      }
    );
    
    if (response.data.status === 'success') {
      showMessage('Cập nhật ảnh đại diện thành công');
      // Cập nhật avatar
      customerData.value.avatar = response.data.avatarUrl;
    } else {
      showMessage(response.data.message || 'Cập nhật ảnh đại diện thất bại', 'error');
    }
    
  } catch (error) {
    handleApiError(error, 'Không thể cập nhật ảnh đại diện');
  } finally {
    isLoading.value = false;
  }
};
</script> 

<template>
  <div class="app-container">
    <SlidebarCustomer :activeMenu="activeMenu" />
    
    <div class="main-content">
      <h1 class="page-title">Thông tin cá nhân</h1>
      
      <!-- Thông báo -->
      <div v-if="message" class="notification" :class="messageType">
        {{ message }}
      </div>
      
      <!-- Loading indicator -->
      <div v-if="isLoading" class="loading-indicator">
        <div class="loader"></div>
        <div>Đang tải dữ liệu...</div>
      </div>
      
      <div v-else class="profile-container">
        <!-- Section: Thông tin cá nhân -->
        <div class="profile-card">
          <div class="profile-header">
            <h2>Thông tin cá nhân</h2>
            <button v-if="!isEditing" @click="startEditing" class="edit-button">
              <i class="fas fa-edit"></i> Chỉnh sửa
            </button>
          </div>
          
          <div class="profile-body">
            <!-- Avatar section -->
            <div class="avatar-section">
              <div class="avatar-container">
                <img v-if="customerData.avatar" :src="customerData.avatar" alt="Avatar" class="avatar" />
                <div v-else class="avatar default-avatar">
                  {{ customerData.name ? customerData.name.charAt(0).toUpperCase() : 'U' }}
                </div>
                
                <label v-if="!isEditing" for="avatar-upload" class="avatar-upload-label">
                  <i class="fas fa-camera"></i>
                </label>
                <input 
                  id="avatar-upload" 
                  type="file" 
                  accept="image/*" 
                  class="avatar-upload" 
                  @change="handleAvatarUpload"
                />
              </div>
            </div>
            
            <!-- Info section -->
            <div class="info-section">
              <!-- View mode -->
              <div v-if="!isEditing" class="info-view">
                <div class="info-row">
                  <div class="info-label">Mã khách hàng:</div>
                  <div class="info-value">{{ customerData.id }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Họ và tên:</div>
                  <div class="info-value">{{ customerData.name }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Email:</div>
                  <div class="info-value">{{ customerData.email }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Số điện thoại:</div>
                  <div class="info-value">{{ customerData.phone }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Tuổi:</div>
                  <div class="info-value">{{ customerData.age }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Giới tính:</div>
                  <div class="info-value">{{ customerData.gender }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Ngày gia nhập:</div>
                  <div class="info-value">{{ customerData.joinDate }}</div>
                </div>
              </div>
              
              <!-- Edit mode -->
              <form v-else @submit.prevent="saveChanges" class="edit-form">
                <div class="form-row">
                  <label for="edit-email">Email:</label>
                  <input type="email" id="edit-email" v-model="editForm.email" class="form-input" required />
                </div>
                <div class="form-row">
                  <label for="edit-phone">Số điện thoại:</label>
                  <input type="tel" id="edit-phone" v-model="editForm.phone" class="form-input" required />
                </div>
                <div class="form-row">
                  <label for="edit-age">Tuổi:</label>
                  <input type="number" id="edit-age" v-model="editForm.age" class="form-input" min="1" max="120" required />
                </div>
                
                <div class="password-section">
                  <h3>Đổi mật khẩu (tùy chọn)</h3>
                  <div class="form-row password-row">
                    <label for="current-password">Mật khẩu hiện tại:</label>
                    <div class="password-input-container">
                      <input 
                        :type="showPassword ? 'text' : 'password'" 
                        id="current-password" 
                        v-model="editForm.currentPassword" 
                        class="form-input"
                        autocomplete="current-password"
                      />
                      <button 
                        type="button" 
                        @click="showPassword = !showPassword" 
                        class="toggle-password-button"
                      >
                        <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                      </button>
                    </div>
                  </div>
                  <div class="form-row">
                    <label for="new-password">Mật khẩu mới:</label>
                    <input 
                      type="password" 
                      id="new-password" 
                      v-model="editForm.newPassword" 
                      class="form-input" 
                      placeholder="Để trống nếu không đổi mật khẩu"
                      autocomplete="new-password"
                    />
                  </div>
                  <div class="form-row">
                    <label for="confirm-password">Xác nhận mật khẩu:</label>
                    <input 
                      type="password" 
                      id="confirm-password" 
                      v-model="editForm.confirmPassword" 
                      class="form-input"
                      autocomplete="new-password"
                    />
                  </div>
                </div>
                
                <div class="form-actions">
                  <button type="button" @click="cancelEditing" class="cancel-button">Hủy</button>
                  <button type="submit" class="save-button">Lưu thay đổi</button>
                </div>
              </form>
            </div>
          </div>
        </div>
        
        <!-- Section: Thông tin gói tập -->
        <div class="membership-card">
          <div class="profile-header">
            <h2>Thông tin gói tập</h2>
          </div>
          
          <div class="membership-body">
            <div v-if="membershipData.package" class="membership-details">
              <div class="badge" :class="membershipData.status === 'Đang hoạt động' ? 'active' : 'expired'">
                {{ membershipData.status }}
              </div>
              
              <div class="membership-name">{{ membershipData.package }}</div>
              
              <div class="membership-dates">
                <div class="date-item">
                  <div class="date-label">Ngày bắt đầu</div>
                  <div class="date-value">{{ membershipData.startDate }}</div>
                </div>
                <div class="date-divider"></div>
                <div class="date-item">
                  <div class="date-label">Ngày kết thúc</div>
                  <div class="date-value">{{ membershipData.endDate }}</div>
                </div>
              </div>
              
              <div class="days-left">
                <div class="days-number">{{ membershipData.daysLeft }}</div>
                <div class="days-text">ngày còn lại</div>
              </div>
              
              <button v-if="membershipData.daysLeft < 7" class="extend-button">
                <i class="fas fa-sync-alt"></i> Gia hạn gói tập
              </button>
            </div>
            
            <div v-else class="no-membership">
              <div class="empty-state">
                <i class="fas fa-dumbbell empty-icon"></i>
                <p>Bạn chưa đăng ký gói tập nào</p>
                <button class="register-button">Đăng ký ngay</button>
              </div>
            </div>
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

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 24px;
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

/* Profile container */
.profile-container {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 24px;
}

/* Card styling */
.profile-card, .membership-card {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  overflow: hidden;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.profile-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.edit-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.edit-button:hover {
  background-color: #4338ca;
}

/* Profile body */
.profile-body {
  padding: 24px;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 24px;
}

/* Avatar section */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-container {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f3f4f6;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-avatar {
  width: 100%;
  height: 100%;
  background-color: #4f46e5;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 600;
}

.avatar-upload {
  display: none;
}

.avatar-upload-label {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 32px;
  height: 32px;
  background-color: #4f46e5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  transition: background-color 0.2s;
}

.avatar-upload-label:hover {
  background-color: #4338ca;
}

/* Info section */
.info-section {
  flex: 1;
}

.info-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.info-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 8px;
}

.info-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.info-value {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}

/* Edit form */
.edit-form {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-input {
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.form-input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}

.password-section {
  margin-top: 16px;
  grid-column: span 2;
  border-top: 1px dashed #d1d5db;
  padding-top: 16px;
}

.password-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 16px;
}

.password-input-container {
  position: relative;
}

.toggle-password-button {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
}

.form-actions {
  margin-top: 24px;
  grid-column: span 2;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-button {
  padding: 10px 16px;
  background-color: #f3f4f6;
  color: #4b5563;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.cancel-button:hover {
  background-color: #e5e7eb;
}

.save-button {
  padding: 10px 16px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.save-button:hover {
  background-color: #4338ca;
}

/* Membership card */
.membership-body {
  padding: 24px;
}

.membership-details {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.badge {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 16px;
}

.badge.active {
  background-color: #d1fae5;
  color: #065f46;
}

.badge.expired {
  background-color: #fecaca;
  color: #b91c1c;
}

.membership-name {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24px;
  text-align: center;
}

.membership-dates {
  display: flex;
  width: 100%;
  margin-bottom: 24px;
  position: relative;
  justify-content: space-between;
}

.date-item {
  width: 45%;
  text-align: center;
}

.date-divider {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 24px;
  height: 2px;
  background-color: #d1d5db;
}

.date-divider::before,
.date-divider::after {
  content: "";
  position: absolute;
  top: -4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.date-divider::before {
  left: -15px;
  background-color: #4f46e5;
}

.date-divider::after {
  right: -15px;
  background-color: #10b981;
}

.date-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.date-value {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.days-left {
  background-color: #f3f4f6;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  width: 80%;
  margin-bottom: 24px;
}

.days-number {
  font-size: 40px;
  font-weight: 700;
  color: #4f46e5;
  line-height: 1;
}

.days-text {
  font-size: 14px;
  color: #6b7280;
}

.extend-button {
  padding: 12px 20px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.extend-button:hover {
  background-color: #4338ca;
}

.no-membership {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 0;
}

.empty-icon {
  font-size: 48px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.empty-state p {
  color: #6b7280;
  margin-bottom: 24px;
}

.register-button {
  padding: 12px 20px;
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.register-button:hover {
  background-color: #059669;
}

/* Responsive */
@media (max-width: 1024px) {
  .profile-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    width: 100%;
  }
  
  .profile-body {
    grid-template-columns: 1fr;
    justify-items: center;
  }
  
  .info-view {
    grid-template-columns: 1fr;
  }
  
  .edit-form {
    grid-template-columns: 1fr;
  }
  
  .password-section {
    grid-column: span 1;
  }
  
  .form-actions {
    grid-column: span 1;
  }
}
</style>