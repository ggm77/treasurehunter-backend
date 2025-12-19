package com.treasurehunter.treasurehunter.domain.notification.service;

import com.treasurehunter.treasurehunter.domain.notification.dto.NotificationDto;
import com.treasurehunter.treasurehunter.global.infra.fcm.FcmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FcmClient fcmClient;

    /**
     * 알림을 전송하는 메서드
     * @param notificationDto 알림의 정보가 든 DTO
     */
    public void sendNotification(final NotificationDto notificationDto) {
        fcmClient.send(notificationDto);
    }
}
