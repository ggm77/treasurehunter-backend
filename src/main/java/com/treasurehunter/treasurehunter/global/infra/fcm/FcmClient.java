package com.treasurehunter.treasurehunter.global.infra.fcm;

import com.google.firebase.messaging.*;
import com.treasurehunter.treasurehunter.domain.notification.dto.NotificationDto;
import com.treasurehunter.treasurehunter.domain.notification.repository.token.NotificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FcmClient {

    private final NotificationTokenRepository notificationTokenRepository;

    /**
     * FCM에 메세지를 보내는 메서드
     * 전송 실패시 저장된 토큰을 삭제함
     * @param notificationDto 메세지 정보가 담긴 DTO
     */
    public void send(final NotificationDto notificationDto) {
        try{
            // 1) 메세지 빌드
            final Message message = Message.builder()
                    .setToken(notificationDto.getToken())
                    .setNotification(
                            Notification.builder()
                                    .setTitle(notificationDto.getTitle())
                                    .setBody(notificationDto.getBody())
                                    .build()
                    )

                    .putData("url", notificationDto.getUrl())
                    .putData("profileImage", notificationDto.getProfileImage())
//                    .putData("action", "취할 액션")

                    .build();

            // 2) 메세지 전송
            final String response = FirebaseMessaging.getInstance().send(message);

            // 3) 성공시 로깅
            log.info("FCM sent successfully: userId: {}, messageId: {}",notificationDto.getTargetUserId() , response);

        } catch (FirebaseMessagingException ex){
            //실패시 로깅
            log.warn("FCM send failed: userId: {}, errorCode: {}",notificationDto.getTargetUserId() , ex.getMessagingErrorCode());

            //토큰이 등록되지 않은 경우나 변수가 잘못된 경우 저장된 토큰 삭제 (불필요한 요청 방지)
            if (
                    ex.getMessagingErrorCode().equals(MessagingErrorCode.UNREGISTERED)
                    || ex.getMessagingErrorCode().equals(MessagingErrorCode.INVALID_ARGUMENT)
            ) {
                notificationTokenRepository.deleteByToken(notificationDto.getToken());
            }
        }
    }
}
