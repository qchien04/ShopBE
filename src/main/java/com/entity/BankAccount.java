package com.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "bank_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên ngân hàng
    @Column(nullable = false)
    private String bankName;

    // Số tài khoản
    @Column(nullable = false)
    private String accountNumber;

    // Tên chủ tài khoản
    @Column(nullable = false)
    private String accountName;

    // Chi nhánh
    private String branch;

    // Logo ngân hàng
    private String logo;

    // Kích hoạt
    private Boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}