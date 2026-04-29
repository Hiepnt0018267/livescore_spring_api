package com.livescore.api.controllers;

import com.livescore.api.models.User;
import com.livescore.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // API này được gọi từ Flutter NGAY SAU KHI đăng nhập Firebase thành công
    @PostMapping("/sync")
    public ResponseEntity<User> syncUser(@RequestBody Map<String, String> payload) {
        try {
            String uid = payload.get("uid");
            String email = payload.get("email");
            String name = payload.get("name");

            if (uid == null || uid.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Kiểm tra xem khách này đã có trong MySQL chưa
            Optional<User> userOpt = userRepository.findByFirebaseUid(uid);
            User user;

            if (userOpt.isPresent()) {
                // Khách cũ -> Cập nhật lại tên/email phòng khi họ đổi trên Google
                user = userOpt.get();
                user.setName(name);
                user.setEmail(email);
                System.out.println("Khách cũ quay lại: " + email);
            } else {
                // Khách mới tinh -> Tạo hồ sơ lưu vào MySQL
                user = new User();
                user.setFirebaseUid(uid);
                user.setName(name);
                user.setEmail(email);
                System.out.println("Tạo hồ sơ cho khách mới: " + email);
            }

            // Lưu vào database và trả về hồ sơ (kèm MySQL ID) cho Flutter
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}