package com.livescore.api.controllers;

import com.livescore.api.models.Team;
import com.livescore.api.models.User;
import com.livescore.api.repositories.TeamRepository;
import com.livescore.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    // API xử lý Follow nhận dữ liệu chuẩn JSON
    @PostMapping("/follow")
    public ResponseEntity<String> followTeam(@RequestBody Map<String, String> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId"));
            String apiId = payload.get("apiId");
            String teamName = payload.get("teamName");
            String logoUrl = payload.get("logoUrl");

            // 1. Tìm người dùng
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy User trong hệ thống!");
            }
            User user = userOpt.get();

            // 2. Tìm hoặc Tạo mới Đội bóng (Cơ chế Upsert)
            Optional<Team> teamOpt = teamRepository.findByApiId(apiId);
            Team team;

            if (teamOpt.isPresent()) {
                // Đã có trong DB -> Lấy ra dùng
                team = teamOpt.get();
                System.out.println("Đội bóng đã có sẵn: " + team.getName());
            } else {
                // Chưa có -> Tạo mới và lưu vào DB
                team = new Team();
                team.setApiId(apiId);
                team.setName(teamName);
                team.setLogoUrl(logoUrl);
                
                team = teamRepository.save(team);
                System.out.println("Đã tự động thêm đội mới vào Database: " + teamName);
            }

            // 3. Tiến hành ghép cặp Follow
            user.getFollowedTeams().add(team);
            userRepository.save(user);

            return ResponseEntity.ok("Đã theo dõi " + team.getName() + " thành công!");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }
    // 1. API KÉO DANH SÁCH ĐỘI BÓNG ĐÃ FOLLOW
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getFollowedTeams(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) { 
            return ResponseEntity.badRequest().body("Không tìm thấy người dùng!");
        }
        
        // Trả về toàn bộ danh sách đội bóng nằm trong biến followedTeams
        return ResponseEntity.ok(userOpt.get().getFollowedTeams());
    }

    // 2. API BỎ THEO DÕI (UNFOLLOW)
    @PostMapping("/unfollow")
    public ResponseEntity<String> unfollowTeam(@RequestBody Map<String, String> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId"));
            String apiId = payload.get("apiId");

            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Team> teamOpt = teamRepository.findByApiId(apiId);

            if (userOpt.isPresent() && teamOpt.isPresent()) {
                User user = userOpt.get();
                Team team = teamOpt.get();

                // Gỡ đội bóng ra khỏi danh sách của user và lưu lại
                user.getFollowedTeams().remove(team);
                userRepository.save(user);

                return ResponseEntity.ok("Đã bỏ theo dõi đội bóng!");
            }
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ!");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }
}