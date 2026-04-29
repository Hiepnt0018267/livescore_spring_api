package com.livescore.api.repositories;

import com.livescore.api.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    // Thêm dòng này: Dạy Spring Boot cách tìm đội bóng bằng apiId
    Optional<Team> findByApiId(String apiId);
}