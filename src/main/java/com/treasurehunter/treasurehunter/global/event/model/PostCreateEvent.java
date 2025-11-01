package com.treasurehunter.treasurehunter.global.event.model;

import com.treasurehunter.treasurehunter.global.event.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 게시글 등록 관련 이벤트를 처리할 수 있도록하는 정보를 담은 메서드
 */
@Getter
public class PostCreateEvent implements DomainEvent {

    private final Long userId;
    private final Long postId;
    private final LocalDateTime occurredAt;

    @Builder
    public PostCreateEvent(
            final Long userId,
            final Long postId
    ) {
        this.userId = userId;
        this.postId = postId;
        this.occurredAt = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredAt() {
        return this.occurredAt;
    }
}
