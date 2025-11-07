package com.treasurehunter.treasurehunter.domain.chat.repository.room;

import com.treasurehunter.treasurehunter.domain.chat.entity.room.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 하나의 게시글에 중복된 채팅방 생성 방지
    @Query("""
        select cr from ChatRoom cr
        join cr.chatRoomParticipants p1
        join cr.chatRoomParticipants p2
        where cr.post.id = :postId
            and p1.participant.id = :userA
            and p2.participant.id = :userB
    """)
    Optional<ChatRoom> findExistingRoomForPostAndPair(
            @Param("postId") final Long postId,
            @Param("userA") final Long userA,
            @Param("userB") final Long userB
    );

    Optional<ChatRoom> findByRoomId(final String roomId);
}