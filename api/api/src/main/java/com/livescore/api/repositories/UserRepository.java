package com.livescore.api.repositories;

import com.livescore.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tự động tìm người dùng thông qua mã UID của Firebase
    Optional<User> findByFirebaseUid(String firebaseUid);
}