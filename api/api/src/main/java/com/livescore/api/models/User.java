package com.livescore.api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;
import java.util.HashSet;
import com.fasterxml.jackson.annotation.JsonIgnore; // Thêm thư viện này để tránh lỗi vòng lặp JSON

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Đây là ID nội bộ của MySQL (Ví dụ: số 1, 2, 3...)

    private String name;

    @Column(unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String firebaseUid; // Đây là thẻ từ do Firebase cấp (Ví dụ: aBcXyZ123...)

    @ManyToMany
    @JoinTable(
        name = "user_team",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    @JsonIgnore // Dòng này cực kỳ quan trọng: Giúp API không bị kẹt khi trả dữ liệu về
    private Set<Team> followedTeams = new HashSet<>();
}