package com.example.itssprj_ver1.controller;

import com.example.itssprj_ver1.model.staff;
import com.example.itssprj_ver1.repository.customerRepository;
import com.example.itssprj_ver1.repository.userRepository;
import com.example.itssprj_ver1.service.customerService;
import com.example.itssprj_ver1.service.exerSession;
import com.example.itssprj_ver1.service.ptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.itssprj_ver1.config.GenToken.generateToken;

@RestController
@RequestMapping("/pt")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ptController {

    private final exerSession exerSession;
    private final ptService ptService;
    private final customerRepository customerRepository;
    private final customerService customerService;

    @PostMapping("/getInfoPt")
    public ResponseEntity<Map<String, Object>> getInfoPt(@RequestHeader(value = "token", required = false) String token,
                                                         @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (token == null || token.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Token is missing");
                return ResponseEntity.badRequest().body(response);
            }
            int userId = Integer.parseInt(request.get("userid"));
            staff ptInfo = ptService.getPTById(userId);
            if (ptInfo != null) {
                response.put("status", "success");
                response.put("ptId", ptInfo.getId());
                response.put("name", ptInfo.getFirstname() + " " + ptInfo.getLastname());
                response.put("phone", ptInfo.getPhone());
                response.put("email", ptInfo.getEmail());
                response.put("age", ptInfo.getAge());
                response.put("gender", ptInfo.getGender());
                response.put("rank", ptInfo.getRank());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "No information found for the given user ID");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/getSession")
    public ResponseEntity<Map<String, Object>> getSession(@RequestHeader(value = "token", required = false) String token,
                                                          @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (token == null || token.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Token is missing");
                return ResponseEntity.badRequest().body(response);
            }
            int userId = Integer.parseInt(request.get("userid"));
            List<Map<String, Object>> sessions = exerSession.getAllSessionByPT(userId);
            if (sessions != null) {
                response.put("status", "success");
                response.put("data", sessions);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "No sessions found for the given user ID");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/updateSession")
    public ResponseEntity<Map<String, Object>> updateSession(@RequestHeader(value = "token", required = false) String token,
                                                              @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (token == null || token.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Token is missing");
                return ResponseEntity.badRequest().body(response);
            }
            int sessionId = Integer.parseInt(request.get("sessionid"));
            LocalDateTime beginAt = null;
            LocalDateTime endAt = null;
            try {
                if (request.containsKey("beginAt") && !request.get("beginAt").isEmpty()) {
                    String beginAtStr = request.get("beginAt");
                    beginAt = LocalDateTime.parse(beginAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
                if (request.containsKey("endAt") && !request.get("endAt").isEmpty()) {

                    String endAtStr = request.get("endAt");
                    endAt = LocalDateTime.parse(endAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Định dạng ngày tháng không hợp lệ: " + e.getMessage());
            }
            String description = request.get("description");

            boolean updated = exerSession.updateSessionPT(sessionId,beginAt,endAt,description);
            if (updated) {
                response.put("status", "success");
                response.put("message", "Session updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to update session");
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/addSessionPT")
    public ResponseEntity<Map<String, Object>> addSessionPT(@RequestHeader(value = "token", required = false) String token,
                                                            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (token == null || token.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Token is missing");
                return ResponseEntity.badRequest().body(response);
            }
            int customerId = Integer.parseInt(request.get("customerid"));
            int ptId = Integer.parseInt(request.get("userid"));
            String exerciseType = request.get("exerciseType");
            LocalDateTime beginAt = null;
            LocalDateTime endAt = null;
            try {
                if (request.containsKey("beginAt") && !request.get("beginAt").isEmpty()) {
                    String beginAtStr = request.get("beginAt");
                    beginAt = LocalDateTime.parse(beginAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
                if (request.containsKey("endAt") && !request.get("endAt").isEmpty()) {
                    String endAtStr = request.get("endAt");
                    endAt = LocalDateTime.parse(endAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Định dạng ngày tháng không hợp lệ: " + e.getMessage());
            }
            String description = request.get("description");
            boolean added = exerSession.addSessionPT(customerId, beginAt, endAt, exerciseType, description, ptId);
            if (added) {
                response.put("status", "success");
                response.put("message", "Session added successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to add session");
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/getAllCustomer")
    public ResponseEntity<Map<String, Object>> getAllCustomer(@RequestHeader(value = "token", required = false) String token,
                                                              @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if(token == null || token.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Token is missing");
                return ResponseEntity.badRequest().body(response);
            }
            int ptid = Integer.parseInt(request.get("userid"));
            List<Map<String, Object>> customers = customerService.getCustomerByPT(ptid);
            if (customers != null && !customers.isEmpty()) {
                response.put("status", "success");
                response.put("data", customers);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "No customers found for the given PT ID");
                return ResponseEntity.status(404).body(response);

            }
        }catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

}
