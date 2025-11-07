package com.treasurehunter.treasurehunter.domain.chat.entity.room;

import com.treasurehunter.treasurehunter.domain.chat.entity.room.participant.ChatRoomParticipant;
import com.treasurehunter.treasurehunter.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat_room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_room_id", columnNames = "room_id")
        },
        indexes = {
                @Index(name = "idx_room_id", columnList = "room_id")
        }
)
public class ChatRoom {

    // 정보 관리용 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방 ID (UUID)
    @Column(length = 36, nullable = false)
    private String roomId;

    // 채팅방 이름
    @Column(length = 255, nullable = false)
    private String name;

    // 채팅이 발생한 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // 채팅방에 참가 중인 사람들
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomParticipant> chatRoomParticipants = new ArrayList<>();

    @Builder
    public ChatRoom(
            final String name,
            final Post post,
            final List<ChatRoomParticipant> chatRoomParticipants
    ){
        this.name = name;
        this.post = post;
        this.chatRoomParticipants = chatRoomParticipants;
        this.roomId = UUID.randomUUID().toString();
    }

    public void updateName(final String newName) {
        this.name = newName;
    }

    // 게시글과 연관 관계 끊는 메서드
    public void detachPost(){
        this.post = null;
    }
}
