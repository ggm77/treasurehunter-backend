package com.treasurehunter.treasurehunter.global.event.impl.spring;

import com.treasurehunter.treasurehunter.global.event.domain.DomainEvent;
import com.treasurehunter.treasurehunter.global.event.domain.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 이벤트 발행을 스프링에서 할 수 있도록 하는 구현체
 */
@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(final DomainEvent domainEvent) {
        applicationEventPublisher.publishEvent(domainEvent);
    }
}
