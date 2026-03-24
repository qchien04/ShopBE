package com.repository;

import com.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);

    List<ChatMessage> findByRoomIdAndIsReadFalseAndSenderIdNot(Long roomId, Long senderId);

    int countByRoomIdAndIsReadFalse(Long roomId);
}