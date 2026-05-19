package com.service;

import com.DTO.ChatRoomDTO;
import com.DTO.MessagePayload;
import com.constant.RoomStatus;
import com.entity.ChatMessage;
import com.entity.ChatRoom;
import com.entity.User;
import com.mapper.ChatRoomMapper;
import com.repository.ChatMessageRepository;
import com.repository.ChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository roomRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatRoomMapper chatRoomMapper;
    // ─── ROOM ────────────────────────────────────────────────
    public boolean isMemberOfRoom(Long userId, Long roomId) {
        return roomRepository
                .findByIdAndCustomerIdOrIdAndStaffId(roomId, userId, roomId, userId)
                .isPresent();
    }

    @Transactional
    public ChatRoomDTO createRoom(Long customerId) {
        // Kiểm tra nếu khách đã có room WAITING/ACTIVE thì dùng lại
        ChatRoom room = roomRepository
                .findByCustomerIdAndStatusIn(
                        customerId,
                        List.of(RoomStatus.WAITING, RoomStatus.ACTIVE)
                )
                .orElseGet(() -> roomRepository.save(
                        ChatRoom.builder()
                                .customer(User.builder().id(customerId).build())
                                .status(RoomStatus.WAITING)
                                .createdAt(LocalDateTime.now())
                                .build()
                ));
        return chatRoomMapper.toSmallDto(room);
    }

    public ChatRoom assignStaff(Long roomId, Long staffId) {
        ChatRoom room = getRoomById(roomId);

        if (room.getStatus() == RoomStatus.CLOSED) {
            throw new IllegalStateException("Room đã đóng, không thể nhận.");
        }
//        if (room.getStaffId() != null && !room.getStaffId().equals(staffId)) {
//            throw new IllegalStateException("Room đã có nhân viên khác xử lý.");
//        }

        room.setStaff(User.builder().id(staffId).build());
        room.setStatus(RoomStatus.ACTIVE);
        return roomRepository.save(room);
    }

    public ChatRoom closeRoom(Long roomId) {
        ChatRoom room = getRoomById(roomId);
        room.setStatus(RoomStatus.CLOSED);
        room.setClosedAt(LocalDateTime.now());
        return roomRepository.save(room);
    }

    public List<ChatRoomDTO> getRoomsByStatus() {
        return chatRoomMapper.toDtos(roomRepository.findNotClosedRooms());
    }

    private ChatRoom getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room không tồn tại: " + roomId));
    }

    // ─── MESSAGE ─────────────────────────────────────────────

    public ChatMessage saveMessage(MessagePayload payload) {
        // Đảm bảo room tồn tại và chưa đóng
        ChatRoom room = getRoomById(payload.roomId());
        if (room.getStatus() == RoomStatus.CLOSED) {
            throw new IllegalStateException("Không thể gửi tin vào room đã đóng.");
        }

        ChatMessage message = ChatMessage.builder()
                .roomId(payload.roomId())
                .senderId(payload.senderId())
                .senderRole(payload.senderRole())
                .content(payload.content())
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }

    public List<ChatMessage> getMessages(Long roomId) {
        return messageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    public void markAsRead(Long roomId, Long userId) {
        // Đánh dấu đọc tất cả tin nhắn KHÔNG phải của mình
        List<ChatMessage> unread = messageRepository
                .findByRoomIdAndIsReadFalseAndSenderIdNot(roomId, userId);

        unread.forEach(msg -> msg.setRead(true));
        messageRepository.saveAll(unread);
    }

    public int countUnread(Long roomId) {
        return messageRepository.countByRoomIdAndIsReadFalse(roomId);
    }

    // ─── AI MESSAGES ─────────────────────────────────────────

    /**
     * Lưu toàn bộ lịch sử chat AI vào room khi khách chuyển sang nhân viên.
     * messages: list of {role: "USER"|"AI", content: "..."}
     */
    @Transactional
    public void saveAiHistory(Long roomId, List<java.util.Map<String, String>> messages) {
        ChatRoom room = getRoomById(roomId);
        for (java.util.Map<String, String> entry : messages) {
            String role = entry.getOrDefault("role", "AI");
            String content = entry.get("content");
            if (content == null || content.isBlank()) continue;

            String senderRole = role.equalsIgnoreCase("USER") ? "CUSTOMER" : "AI";
            ChatMessage msg = ChatMessage.builder()
                    .roomId(roomId)
                    .senderId(role.equalsIgnoreCase("USER") ? room.getCustomer().getId() : null)
                    .senderRole(senderRole)
                    .content(content)
                    .isRead(true)
                    .sentAt(LocalDateTime.now())
                    .build();
            messageRepository.save(msg);
        }
    }

    /**
     * Lưu 1 tin nhắn AI đơn lẻ (dùng để lưu realtime khi đang trong chế độ AI).
     */
    @Transactional
    public ChatMessage saveAiMessage(Long roomId, String role, String content) {
        ChatRoom room = getRoomById(roomId);
        Long senderId = role.equalsIgnoreCase("CUSTOMER") || role.equalsIgnoreCase("USER")
                ? room.getCustomer().getId()
                : null;
        String senderRole = role.equalsIgnoreCase("USER") || role.equalsIgnoreCase("CUSTOMER")
                ? "CUSTOMER" : "AI";

        ChatMessage msg = ChatMessage.builder()
                .roomId(roomId)
                .senderId(senderId)
                .senderRole(senderRole)
                .content(content)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
        return messageRepository.save(msg);
    }
}