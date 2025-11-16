package com.treasurehunter.treasurehunter.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat",
        indexes = {
                @Index(name = "idx_room_id", columnList = "room_id")
        }
)
public class Chat {

    //채팅의 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //채팅 타입
    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ChatType chatType;

    //채팅 보낸 사람 타입 (게시글 작성자, 채팅 건 사람)
    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ChatUserType userType;

    //채팅방 ID
    @Column(nullable = false, updatable = false)
    private String roomId;

    //채팅 보낸 유저 ID
    @Column(nullable = false, updatable = false)
    private String senderId;

    //채팅 메세지
    @Column(length = 1024, nullable = false, updatable = false)
    private String message;

    //클라이언트가 보낸 시점 (클라이언트가 입력한 값)
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    //서버에서 받은 시점
    @Column(nullable = false, updatable = false)
    private LocalDateTime serverAt;

    //DB 저장 시점
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Chat(
            final ChatType chatType,
            final ChatUserType userType,
            final String roomId,
            final String senderId,
            final String message,
            final LocalDateTime sendAt,
            final LocalDateTime serverAt
    ){
        this.chatType = chatType;
        this.userType = userType;
        this.roomId = roomId;
        this.senderId = senderId;
        this.message = message;
        this.sentAt = sendAt;
        this.serverAt = serverAt;
    }
}
