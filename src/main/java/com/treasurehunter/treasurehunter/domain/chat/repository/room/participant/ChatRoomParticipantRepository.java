package com.treasurehunter.treasurehunter.domain.chat.repository.room.participant;

import com.treasurehunter.treasurehunter.domain.chat.entity.room.participant.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    @Query("""
        select (count(c) > 0)
        from ChatRoomParticipant c
        where c.chatRoom.roomId = :roomId
            and c.participant.id = :userId
""")
    boolean existsParticipantInChatRoom(@Param("roomId") String roomId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from ChatRoomParticipant c
        where c.chatRoom.roomId = :roomId
            and c.participant.id = :userId
""")
    int deleteByChatRoom_RoomIdAndParticipant_Id(@Param("roomId") String roomId, @Param("userId") Long userId);

    @Query("""
        select u.id from ChatRoomParticipant c
            join c.participant u
        where c.chatRoom.roomId = :roomId
    """)
    List<Long> findUserIdsByRoomId(@Param("roomId") String roomId);
}
