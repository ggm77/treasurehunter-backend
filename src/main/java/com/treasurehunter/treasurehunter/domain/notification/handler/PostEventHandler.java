package com.treasurehunter.treasurehunter.domain.notification.handler;

import com.treasurehunter.treasurehunter.domain.notification.service.event.NotificationEventService;
import com.treasurehunter.treasurehunter.global.event.model.PostCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PostEventHandler {

    private final NotificationEventService notificationEventService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(final PostCreateEvent postCreateEvent){
        notificationEventService.sendLocalPostNotification(postCreateEvent);
    }
}
