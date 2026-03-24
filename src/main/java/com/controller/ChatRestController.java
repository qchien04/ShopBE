package com.controller;

import com.DTO.ChatRoomDTO;
import com.constant.RoomStatus;
import com.entity.ChatMessage;
import com.entity.ChatRoom;
import com.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    // Lấy lịch sử tin nhắn của 1 room
    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessage> getMessages(@PathVariable Long roomId) {
        return chatService.getMessages(roomId);
    }

    // Nhân viên lấy danh sách tất cả room
    @GetMapping("/rooms")
    public List<ChatRoomDTO> getRooms() {
        return chatService.getRoomsByStatus();
    }
    // Đánh dấu đã đọc
    @PatchMapping("/rooms/{roomId}/read")
    public void markAsRead(@PathVariable Long roomId, @RequestParam Long userId) {
        chatService.markAsRead(roomId, userId);
    }

    @PostMapping("/start")
    public ChatRoomDTO startRoom(@RequestBody Map<String, Long> body) {
        ChatRoomDTO room=chatService.createRoom(body.get("customerId"));
        return room;
    }

    @PostMapping("/accept")
    public ChatRoom acceptRoom(@RequestBody Map<String, Long> body) {
        return chatService.assignStaff(body.get("roomId"), body.get("staffId"));
    }
}
