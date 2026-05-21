package com.controller;

import com.DTO.ChatRoomDTO;
import com.DTO.MessagePayload;
import com.DTO.NotificationPayload;
import com.entity.ChatMessage;
import com.entity.ChatRoom;
import com.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long getUserId(Principal principal) {
        return (Long) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    }

    private boolean hasRole(Principal principal, String role) {
        return ((UsernamePasswordAuthenticationToken) principal)
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    @SuppressWarnings("unchecked")
    private void addUserToSession(Map<String, Object> session, Long roomId, Long userId) {
        session.put("roomId", roomId);
        session.put("userId", userId);

        // Lấy hoặc tạo mới Set userIds
        Set<Long> userIds = (Set<Long>) session.getOrDefault("userIds", new HashSet<>());
        userIds.add(userId);
        session.put("userIds", userIds);
    }

    @SuppressWarnings("unchecked")
    private boolean isInRoom(Map<String, Object> session, Long userId) {
        Set<Long> userIds = (Set<Long>) session.get("userIds");
        return userIds != null && userIds.contains(userId);
    }

    // ── Join room ─────────────────────────────────────────────────────────────
    @MessageMapping("/chat.join")
    public void joinRoom(@Payload Long roomId,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getUserId(principal);

        if (!chatService.isMemberOfRoom(userId, roomId)) {
            throw new AccessDeniedException("Không có quyền vào room này");
        }

        Map<String, Object> session = headerAccessor.getSessionAttributes();
        addUserToSession(session, roomId, userId); //
    }

    // ── Gửi tin nhắn ─────────────────────────────────────────────────────────
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload String content,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> session = headerAccessor.getSessionAttributes();
        Long roomId = (Long) session.get("roomId");
        Long userId = getUserId(principal);

        if (roomId == null) {
            throw new IllegalStateException("Chưa join room");
        }

        if (!isInRoom(session, userId)) {
            throw new AccessDeniedException("Bạn không ở trong room này");
        }

        String senderRole = hasRole(principal, "ADMIN") ? "ADMIN" : "CLIENT";
        System.out.println("Send role nay: "+senderRole);
        ChatMessage saved = chatService.saveMessage(new MessagePayload(roomId, userId, senderRole, content));

        // Gửi tin nhắn đến room
        messagingTemplate.convertAndSend("/topic/room." + roomId, saved);

        // Dùng userIds từ session, không cần query DB
        Set<Long> userIds = (Set<Long>) session.getOrDefault("userIds", new HashSet<>());
        userIds.stream()
                .filter(id -> !id.equals(userId)) // không gửi lại cho người gửi
                .forEach(receiverId -> messagingTemplate.convertAndSendToUser(
                        receiverId.toString(),
                        "/queue/notifications",
                        new NotificationPayload(
                                roomId, userId,
                                "Tin nhắn mới từ " + senderRole,
                                chatService.countUnread(roomId))));
    }

    // ── Tạo room mới ──────────────────────────────────────────────────────────
    @MessageMapping("/chat.start")
    public void startChat(Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        Long customerId = getUserId(principal);
        ChatRoomDTO room = chatService.createRoom(customerId);

        Map<String, Object> session = headerAccessor.getSessionAttributes();
        addUserToSession(session, room.getId(), customerId); //

        messagingTemplate.convertAndSend("/topic/staff.newRoom", room);
    }

    // ── Nhân viên nhận room ───────────────────────────────────────────────────
    @MessageMapping("/chat.accept")
    public void acceptRoom(@Payload Long roomId,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        if (!hasRole(principal, "ADMIN")) {
            throw new AccessDeniedException("Không có quyền");
        }

        Long staffId = getUserId(principal);
        ChatRoom room = chatService.assignStaff(roomId, staffId);

        Map<String, Object> session = headerAccessor.getSessionAttributes();
        addUserToSession(session, roomId, staffId); //

        // Broadcast danh sách thành viên hiện tại trong room
        Set<Long> userIds = (Set<Long>) session.get("userIds");
        messagingTemplate.convertAndSend("/topic/room." + room.getId() + ".members", userIds);

        messagingTemplate.convertAndSend("/topic/room." + room.getId(), room);
    }

    @MessageMapping("/chat.leave")
    public void leaveRoom(Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getUserId(principal);
        Map<String, Object> session = headerAccessor.getSessionAttributes();
        Long roomId = (Long) session.get("roomId");

        if (roomId == null)
            return;

        // Xóa userId khỏi danh sách
        Set<Long> userIds = (Set<Long>) session.getOrDefault("userIds", new HashSet<>());
        userIds.remove(userId);
        session.put("userIds", userIds);

        // Broadcast danh sách mới
        messagingTemplate.convertAndSend("/topic/room." + roomId + ".members", userIds);
    }
}