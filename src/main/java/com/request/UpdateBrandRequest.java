package com.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateBrandRequest {
    private Long id;
    private String name;
    private String description;
    private String logo;
    private String slug;
    private String website;

    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}

