package com.treasurehunter.treasurehunter.domain.notification.service.token;

import com.treasurehunter.treasurehunter.domain.notification.dto.token.NotificationTokenRequestDto;
import com.treasurehunter.treasurehunter.domain.notification.entity.PlatformType;
import com.treasurehunter.treasurehunter.domain.notification.entity.token.NotificationToken;
import com.treasurehunter.treasurehunter.domain.notification.repository.token.NotificationTokenRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.EnumUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationTokenService {

    private final EnumUtil enumUtil;
    private final UserRepository userRepository;
    private final NotificationTokenRepository notificationTokenRepository;

    @Transactional
    public void upsertNotificationToken(
            final String userIdStr,
            final NotificationTokenRequestDto notificationTokenRequestDto
    ){

        // 1) 유저 아이디 변환
        final Long userId = Long.parseLong(userIdStr);

        // 2) DTO에 있는 값들 변수에 지정
        final String token = notificationTokenRequestDto.getToken();
        final String rawPlatform = notificationTokenRequestDto.getPlatform();

        // 3) Enum 변환
        final PlatformType platform = enumUtil.toEnum(PlatformType.class, rawPlatform)
                .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_ENUM_VALUE));

        // 4) 유저 조회
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 5) 토큰 저장용 엔티티 빌드
        final NotificationToken notificationToken = NotificationToken.builder()
                .token(token)
                .user(user)
                .platform(platform)
                .build();

        // 6) 토큰 upsert
        notificationTokenRepository.findByUserAndPlatform(user, platform)
                .ifPresentOrElse(
                        //토큰 이미 존재할 때
                        existingToken -> existingToken.updateToken(token),

                        //저장된 토큰이 없을 때
                        () -> notificationTokenRepository.save(notificationToken)
                );
    }

    @Transactional
    public void deleteNotificationToken(
            final String userIdStr,
            final String rawPlatform
    ) {

        // 1) 유저 아이디 변환
        final Long userId = Long.parseLong(userIdStr);

        // 2) Enum 변환
        final PlatformType platform = enumUtil.toEnum(PlatformType.class, rawPlatform)
                .orElseThrow(() -> new CustomException(ExceptionCode.INVALID_ENUM_VALUE));

        // 3) 유저 아이디와 플랫폼으로 토큰 삭제
        notificationTokenRepository.deleteByUserIdAndPlatform(userId, platform);
    }
}
