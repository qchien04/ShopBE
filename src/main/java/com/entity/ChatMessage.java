package com.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    @Id @GeneratedValue
    private Long id;

    private Long roomId;
    private Long senderId;
    private String senderRole;    // "CUSTOMER" | "STAFF"
    private String content;
    private boolean isRead;
    private LocalDateTime sentAt;
}
