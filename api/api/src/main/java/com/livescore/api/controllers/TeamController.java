package com.livescore.api.controllers;

import com.livescore.api.models.Team;
import com.livescore.api.models.User;
import com.livescore.api.repositories.TeamRepository;
import com.livescore.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @PostMapping("/follow")
    public ResponseEntity<?> followTeam(@RequestBody Map<String, String> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId"));
            String apiId = payload.get("apiId");
            String teamName = payload.get("teamName");
            String logoUrl = payload.get("logoUrl");

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Không tìm thấy User!"));
            }
            User user = userOpt.get();

            Optional<Team> teamOpt = teamRepository.findByApiId(apiId);
            Team team;

            if (teamOpt.isPresent()) {
                team = teamOpt.get();
            } else {
                team = new Team();
                team.setApiId(apiId);
                team.setName(teamName);
                team.setLogoUrl(logoUrl);
                team = teamRepository.save(team);
            }

            user.getFollowedTeams().add(team);
            userRepository.save(user);

            // ✅ CHÌA KHÓA: Trả về JSON chuẩn {"message": "Thành công"}
            return ResponseEntity.ok(Collections.singletonMap("message", "Thành công"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getFollowedTeams(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) { 
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Không tìm thấy người dùng!"));
        }
        
        List<Map<String, Object>> safeTeams = new ArrayList<>();
        for (Team team : userOpt.get().getFollowedTeams()) {
            Map<String, Object> teamData = new HashMap<>();
            teamData.put("id", team.getId());
            teamData.put("apiId", team.getApiId());
            teamData.put("name", team.getName());
            teamData.put("logoUrl", team.getLogoUrl());
            safeTeams.add(teamData);
        }

        return ResponseEntity.ok(safeTeams);
    }

    @PostMapping("/unfollow")
    public ResponseEntity<?> unfollowTeam(@RequestBody Map<String, String> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId"));
            String apiId = payload.get("apiId");

            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Team> teamOpt = teamRepository.findByApiId(apiId);

            if (userOpt.isPresent() && teamOpt.isPresent()) {
                User user = userOpt.get();
                Team team = teamOpt.get();

                user.getFollowedTeams().remove(team);
                userRepository.save(user);

                // ✅ CHÌA KHÓA: Trả về JSON chuẩn
                return ResponseEntity.ok(Collections.singletonMap("message", "Thành công"));
            }
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Dữ liệu không hợp lệ!"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}