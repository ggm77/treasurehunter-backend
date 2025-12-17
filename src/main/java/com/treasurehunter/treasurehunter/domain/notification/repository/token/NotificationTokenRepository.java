package com.treasurehunter.treasurehunter.domain.notification.repository.token;

import com.treasurehunter.treasurehunter.domain.notification.entity.PlatformType;
import com.treasurehunter.treasurehunter.domain.notification.entity.token.NotificationToken;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, Long> {

    Optional<NotificationToken> findByUserAndPlatform(User user, PlatformType platform);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM NotificationToken nt WHERE nt.user.id = :userId AND nt.platform = :platform")
    void deleteByUserIdAndPlatform(
            @Param("userId") final Long userId,
            @Param("platform") final PlatformType platform
    );

    //파이어베이스로 알림 전송 실패시 사용하는 메서드
    //이 메서드를 호출하는 메서드에 Transactional을 붙일 수 없음
    @Transactional
    void deleteByToken(final String token);
}
