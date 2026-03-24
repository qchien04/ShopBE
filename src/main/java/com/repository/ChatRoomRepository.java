package com.repository;

import com.constant.RoomStatus;
import com.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByCustomerIdAndStatusIn(Long customerId, List<RoomStatus> statuses);

    @Query("""
        SELECT c FROM ChatRoom c
        JOIN FETCH c.customer
        WHERE c.status <> com.constant.RoomStatus.CLOSED
        ORDER BY c.createdAt DESC
    """)
    List<ChatRoom> findNotClosedRooms();

    Optional<ChatRoom> findByIdAndCustomerIdOrIdAndStaffId(
            Long id1, Long customerId,
            Long id2, Long staffId
    );
}
