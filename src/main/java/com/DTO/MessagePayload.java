package com.DTO;

public record MessagePayload(
        Long roomId,
        Long senderId,
        String senderRole,
        String content
) {}