package com.treasurehunter.treasurehunter.domain.chat.repository.read;

import com.treasurehunter.treasurehunter.domain.chat.entity.read.ChatRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatReadRepository extends JpaRepository<ChatRead, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO chat_read (room_id, is_caller, last_read_chat_id, created_at, updated_at)
        VALUES (:roomId, :isCaller, :lastReadChatId, NOW(), NOW())
        ON DUPLICATE KEY UPDATE
            last_read_chat_id = VALUES(last_read_chat_id),
            updated_at = NOW()
        """, nativeQuery = true)
    int upsertChatRead(
            @Param("lastReadChatId") final Long lastReadChatId,
            @Param("roomId") final String roomId,
            @Param("isCaller") final boolean isCaller
    );

    Optional<ChatRead> findByRoomIdAndIsCaller(final String roomId, final boolean isCaller);
}
