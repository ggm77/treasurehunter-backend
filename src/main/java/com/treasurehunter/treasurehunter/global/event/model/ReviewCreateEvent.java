package com.treasurehunter.treasurehunter.global.event.model;

import com.treasurehunter.treasurehunter.global.event.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewCreateEvent implements DomainEvent {

    private final Long userId;
    private final Long reviewId;
    private final LocalDateTime occurredAt;

    @Builder
    public ReviewCreateEvent(
            final Long userId,
            final Long reviewId
    ){
        this.userId = userId;
        this.reviewId = reviewId;
        this.occurredAt = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredAt() {
        return this.occurredAt;
    }
}
