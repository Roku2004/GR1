<script setup>
import axios from 'axios';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

// chứa các biến 
const username = ref('');
const password = ref('');
const fullname = ref('');
const phone = ref('');
const email = ref('');
const selectedOption = ref('');
const errorMessage = ref('');
const isRegisterMode = ref(false); //Xác định chế độ hiện tại là đăng nhập hay đăng ký
const router = useRouter();

const options = ref([
  { value: '0', text: 'Nam', icon: '♂️' },
  { value: '1', text: 'Nữ', icon: '♀️' },
]);

const selectGender = (value) => {
  selectedOption.value = value;
};


//login 
const login = async () => {
  try {
    const response = await axios.post('http://localhost:3001/admin/login',
      {
        username: username.value,
        password: password.value,
      }
    );
    if (response.data.status === 'success') {
      const token = response.data.token;
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      router.push('/user');
    } else {
      errorMessage.value = response.data.message || 'Thông tin đăng nhập không đúng!';
    }
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Đăng nhập thất bại! Vui lòng thử lại.';
  }
};


const handleSubmit = () => {
  if (isRegisterMode.value) {  // Kiểm tra chế độ hiện tại
    register();               // Nếu là chế độ đăng ký, gọi hàm register
  } else {
    login();                  // Nếu là chế độ đăng nhập, gọi hàm login
  }
};

// Reset tất cả các trường dữ liệu và thông báo lỗi
const toggleMode = () => {
  isRegisterMode.value = !isRegisterMode.value; //Đảo ngược giá trị boolean của isRegisterMode
  errorMessage.value = '';
  username.value = '';
  password.value = '';
  fullname.value = '';
  phone.value = '';
  email.value = '';
  selectedOption.value = '';
};
</script>

<template>
  <div class="wrapper">
    <div class="container" :class="{ active: isRegisterMode }">

      <div class="form-container sign-up">
        <form @submit.prevent="handleSubmit"> <!--@submit.prevent: Ngăn form submit theo cách thông thường-->
          <input type="text" v-model="fullname" placeholder="Họ và tên" required />
          <input type="text" v-model="phone" placeholder="Số điện thoại" required />
          <input type="email" v-model="email" placeholder="Email" required />
          <div class="gender-select">
            <h3>Giới tính:</h3>
            <div class="options">
              <div v-for="option in options" :key="option.value" class="option-box"
                :class="{ selected: selectedOption === option.value }" @click="selectGender(option.value)">
                <span class="icon">{{ option.icon }}</span>
                <span class="text">{{ option.text }}</span>
              </div>
            </div>
          </div>
          <input type="text" v-model="username" placeholder="Tên đăng nhập" required />
          <input type="password" v-model="password" placeholder="Mật khẩu" required />
          <button type="submit">Đăng ký</button>
        </form>
      </div>

      <div class="form-container sign-in">
        <form @submit.prevent="handleSubmit">
          <h1>Đăng nhập</h1>
          <input type="text" v-model="username" placeholder="Tên đăng nhập" required />
          <input type="password" v-model="password" placeholder="Mật khẩu" required />
          <button type="submit">Đăng nhập</button>
        </form>
      </div>

      <div class="toggle-container">
        <div class="toggle">
          <div class="toggle-panel toggle-left">
            <h1>Chào mừng trở lại!</h1>
            <p>Nhập thông tin để đăng nhập</p>
            <button class="hidden" @click="toggleMode">Đăng nhập</button>
          </div>
          <div class="toggle-panel toggle-right">
            <h1>Bạn chưa có tài khoản!</h1>
            <p>Nhập thông tin để đăng ký</p>
            <button class="hidden" @click="toggleMode">Đăng ký</button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap");

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "Poppins", sans-serif;
}

.wrapper {
  background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: gradient 15s ease infinite;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  min-height: 100vh;
}

@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }

  100% {
    background-position: 0% 50%;
  }
}

.container {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  width: 768px;
  max-width: 100%;
  min-height: 480px;
}

.form-container {
  position: absolute;
  top: 0;
  height: 100%;
  transition: all 0.6s ease-in-out;
}

.sign-in {
  left: 0;
  width: 50%;
  z-index: 2;
}

.container.active .sign-in {
  transform: translateX(100%);
}

.sign-up {
  left: 0;
  width: 50%;
  opacity: 0;
  z-index: 1;
}

.container.active .sign-up {
  transform: translateX(100%);
  opacity: 1;
  z-index: 5;
  animation: move 0.6s;
}

@keyframes move {

  0%,
  49.99% {
    opacity: 0;
    z-index: 1;
  }

  50%,
  100% {
    opacity: 1;
    z-index: 5;
  }
}

.toggle-container {
  position: absolute;
  top: 0;
  left: 50%;
  width: 50%;
  height: 100%;
  overflow: hidden;
  transition: all 0.6s ease-in-out;
  border-radius: 150px 0 0 100px;
  z-index: 1000;
}

.container.active .toggle-container {
  transform: translateX(-100%);
  border-radius: 0 150px 100px 0;
}

.toggle {
  background: linear-gradient(to right, #e73c7e, #23a6d5);
  height: 100%;
  color: #fff;
  position: relative;
  left: -100%;
  width: 200%;
  transform: translateX(0);
  transition: all 0.6s ease-in-out;
}

.container.active .toggle {
  transform: translateX(50%);
}

.toggle-panel {
  position: absolute;
  width: 50%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 0 30px;
  text-align: center;
  top: 0;
  transform: translateX(0);
  transition: all 0.6s ease-in-out;
}

.toggle-left {
  transform: translateX(-200%);
}

.container.active .toggle-left {
  transform: translateX(0);
}

.toggle-right {
  right: 0;
  transform: translateX(0);
}

.container.active .toggle-right {
  transform: translateX(200%);
}

form {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 20px 40px;
  height: 100%;
}

input {
  background-color: #f4f8f7;
  border: none;
  margin: 8px 0;
  padding: 12px 15px;
  font-size: 13px;
  border-radius: 10px;
  width: 100%;
  outline: none;
  transition: background-color 0.3s ease;
}

input:focus {
  background-color: #e0e0e0;
}

button {
  background: linear-gradient(45deg, #e73c7e, #23a6d5);
  color: #fff;
  font-size: 12px;
  padding: 10px 30px;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  margin-top: 10px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

button:hover {
  transform: translateY(-2px);
}

button.hidden {
  background: transparent;
  border: 2px solid #fff;
}

h1 {
  color: #333;
  margin-bottom: 10px;
  font-weight: 700;
  font-size: 20px;
}

.toggle-panel h1 {
  color: #fff;
  font-size: 24px;
}

.toggle-panel p {
  font-size: 14px;
}

.error {
  color: #ffe6f0;
  margin-top: 20px;
}

/* Style cho box select */
.gender-select {
  margin: 10px 0;
  width: 100%;
}

.gender-select h3 {
  font-size: 13px;
  color: #333;
  margin-bottom: 8px;
  text-align: left;
  width: 100%;
}

.options {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.option-box {
  width: 60px;
  padding: 8px;
  border: none;
  border-radius: 8px;
  background-color: #f4f8f7;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.option-box:hover {
  background-color: #ffe6f0;
}

.option-box.selected {
  background-color: #ffe6f0;
}

.icon {
  font-size: 16px;
}

.text {
  font-size: 12px;
  color: #333;
}
</style>