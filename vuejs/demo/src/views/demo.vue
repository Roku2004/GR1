<template>
  <div>
    <h2>Thêm người dùng mới</h2>
    <form @submit.prevent="submitForm"><!-- Ngăn form reload trang và gọi hàm submitForm. -->
      <div>
        <label for="name">Tên:</label>
        <input type="text" id="name" v-model="form.name" required />
      </div>
      <div>
        <label for="email">Email:</label>
        <input type="email" id="email" v-model="form.email" required />
      </div>
      <button type="submit">Gửi</button>
    </form>

    <h3>Danh sách người dùng</h3>
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Tên</th>
          <th>Email</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.name }}</td>
          <td>{{ user.email }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="js">
import axios from 'axios';
import { onMounted, ref } from 'vue';

// Dữ liệu form
const form = ref({
  name: '',
  email: ''
});

// Danh sách người dùng
const users = ref([]);

// Lấy danh sách người dùng từ API
const fetchUsers = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/users');
    users.value = response.data;
  } catch (error) {
    console.error('Lỗi khi lấy dữ liệu:', error);
  }
};

// Gửi dữ liệu form tới API
const submitForm = async () => {
  try {
    // await tạm dừng thực thi cho đến khi yêu cầu POST hoàn tất và nhận được phản hồi từ server.
    const response = await axios.post('http://localhost:8080/api/users', form.value);
    users.value.push(response.data); // Thêm user mới vào danh sách
    form.value = { name: '', email: '' }; // Reset form
    alert('Thêm người dùng thành công!');
  } catch (error) {
    console.error('Lỗi khi gửi dữ liệu:', error);
    alert('Có lỗi xảy ra!');
  }
};

// Gọi fetchUsers khi component được mount
onMounted(fetchUsers);
</script>