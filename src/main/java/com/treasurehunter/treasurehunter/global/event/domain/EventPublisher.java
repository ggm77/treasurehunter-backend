package com.treasurehunter.treasurehunter.global.event.domain;

/**
 * 이벤트 발행하는 인터페이스
 */
public interface EventPublisher {
    void publish(final DomainEvent domainEvent);
}
