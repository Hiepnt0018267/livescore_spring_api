package com.livescore.api.repositories;

import com.livescore.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Chỉ cần để trống thế này, Spring Boot đã tự trang bị sẵn cho bạn 
    // các hàm như: save(), findById(), delete()...
}