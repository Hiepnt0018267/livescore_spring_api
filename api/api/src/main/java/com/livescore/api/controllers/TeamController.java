package com.livescore.api.controllers;

import com.livescore.api.models.Team;
import com.livescore.api.models.User;
import com.livescore.api.repositories.TeamRepository;
import com.livescore.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    // API xử lý việc Follow đội bóng
    @PostMapping("/follow")
    public ResponseEntity<String> followTeam(@RequestParam Long userId, @RequestParam Long teamId) {
        // 1. Tìm người dùng và đội bóng trong Database
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Team> teamOpt = teamRepository.findById(teamId);

        if (userOpt.isPresent() && teamOpt.isPresent()) {
            User user = userOpt.get();
            Team team = teamOpt.get();

            // 2. Thêm đội bóng vào danh sách yêu thích của người dùng
            user.getFollowedTeams().add(team);
            
            // 3. Lưu lại. Hibernate sẽ tự động chèn 1 dòng vào bảng user_team!
            userRepository.save(user); 

            return ResponseEntity.ok("Đã theo dõi đội bóng thành công!");
        }
        
        return ResponseEntity.badRequest().body("Không tìm thấy User hoặc Team");
    }
}