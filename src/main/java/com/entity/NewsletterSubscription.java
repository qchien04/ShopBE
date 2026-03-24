package com.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "newsletter_subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NewsletterSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private Boolean active = true;
    private LocalDateTime subscribedAt = LocalDateTime.now();
}
