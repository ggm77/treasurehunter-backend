package com.treasurehunter.treasurehunter.domain.chat.entity.read;

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
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "chat_read",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_read_room_id_is_caller",
                        columnNames = {"room_id", "is_caller"}
                )
        },
        indexes = {
                @Index(name = "idx_room_id_is_caller", columnList = "room_id, is_caller")
        }
)
@Getter
public class ChatRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long lastReadChatId;

    @Column(length = 36, nullable = false, updatable = false)
    private String roomId;

    @Column(columnDefinition = "TINYINT(1)", nullable = false, updatable = false)
    private boolean isCaller;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ChatRead(
            final Long lastReadChatId,
            final String roomId,
            final boolean isCaller
    ){
        this.lastReadChatId = lastReadChatId;
        this.roomId = roomId;
        this.isCaller = isCaller;
    }
}
