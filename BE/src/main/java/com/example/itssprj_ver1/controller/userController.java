package com.example.itssprj_ver1.controller;

import com.example.itssprj_ver1.service.customerService;
import com.example.itssprj_ver1.service.userService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
public class userController {
    private final userService userService;
    private final customerService customerService;

    @PostMapping("/addUser")
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // đăng ký tài khoản
            String username = request.get("username");
            String password = request.get("password");
            int roleid = Integer.parseInt(request.get("roleid"));

            //đăng ký thông tin cá nhân
            String firstname = request.get("firstname");
            String lastname = request.get("lastname");
            String email = request.get("email");
            String phone = request.get("phone");
            String gender = request.get("gender");
            int age = Integer.parseInt(request.get("age"));

            if (userService.addUser(username, password, roleid)) {
                response.put("status", "Thêm tài khoản thành công");
                return ResponseEntity.ok(response);
            }
            if (customerService.addCustomer(firstname, lastname, age, email, gender, username, phone)) {
                response.put("status", "Thêm người dùng thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "Thêm người dùng không thành công");
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            response.put("message", "Đã xảy ra lỗi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
