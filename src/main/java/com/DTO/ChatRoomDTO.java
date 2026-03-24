package com.DTO;


import com.constant.RoomStatus;
import lombok.Data;


@Data
public class ChatRoomDTO {
    private Long id;
    private Long customerId;
    private Long staffId;
    private String customerAvt;
    private String customerName;
    private String staffName;
    private RoomStatus status;
}

