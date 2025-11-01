package com.treasurehunter.treasurehunter.domain.userBadge.handler;

import com.treasurehunter.treasurehunter.domain.userBadge.service.award.UserBadgeAwardService;
import com.treasurehunter.treasurehunter.global.event.model.PostCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 발행 된 이벤트를 처리하는 핸들러
 * 발행 된 이벤트에 맞는 메서드로 연결해 준다.
 */
@Component
@RequiredArgsConstructor
public class BadgeEventHandler {

    private final UserBadgeAwardService userBadgeAwardService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(final PostCreateEvent postCreateEvent){
        userBadgeAwardService.evaluateAndAwardForPostCreateEvent(postCreateEvent);
    }
}
