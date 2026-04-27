package com.livescore.api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String googleId; // Lưu ID từ Firebase để nhận diện người dùng

    // PHÉP THUẬT: Tạo bảng trung gian user_team tự động
    @ManyToMany
    @JoinTable(
        name = "user_team",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<Team> followedTeams = new HashSet<>();
}