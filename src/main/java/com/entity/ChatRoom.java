package com.entity;

import com.constant.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;         // null nếu chưa có nhân viên nhận

    @Enumerated(EnumType.STRING)
    private RoomStatus status;    // WAITING, ACTIVE, CLOSED

    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
