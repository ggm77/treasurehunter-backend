package com.treasurehunter.treasurehunter.domain.chat.entity.room.participant;

import com.treasurehunter.treasurehunter.domain.chat.entity.room.ChatRoom;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // updatedAt을 위해서
@Getter
@Table(
        name = "chat_room_participant",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_room_participant", columnNames = {"chat_room_id", "participant_id"})
        },
        indexes = {
                @Index(name = "idx_chat_room_participant", columnList = "chat_room_id, participant_id")
        }
)
public class ChatRoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private User participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private boolean isAnonymous;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public ChatRoomParticipant(
            final User participant,
            final ChatRoom chatRoom,
            final boolean isAnonymous
    ){
        this.participant = participant;
        this.chatRoom = chatRoom;
        this.isAnonymous = isAnonymous;
    }

    // 유저 탈퇴시 정리하는 메서드
    public void detachUser(){
        this.participant = null;
    }
}
