package com.treasurehunter.treasurehunter.global.event.domain;

import java.time.LocalDateTime;

/**
 * 이벤트들의 인터페이스
 * 일어난 시각을 꼭 저장하도록 강제함
 */
public interface DomainEvent {
    LocalDateTime occurredAt();
}
