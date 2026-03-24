package com.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Config {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String configKey;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String configValue; // JSON string
}