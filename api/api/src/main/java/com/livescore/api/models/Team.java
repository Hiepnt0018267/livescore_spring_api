package com.livescore.api.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "teams")
@Data // Tự động tạo Getter, Setter, toString...
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String apiId; // Dùng để khớp với dữ liệu từ Football API

    private String logoUrl;
}