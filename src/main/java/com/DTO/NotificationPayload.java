package com.DTO;

public record NotificationPayload(
        Long roomId,
        Long customerId,
        String message,
        int unreadCount
) {}