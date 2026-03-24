package com.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Builder
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String imageUrl;
    private Integer displayOrder = 0;
}