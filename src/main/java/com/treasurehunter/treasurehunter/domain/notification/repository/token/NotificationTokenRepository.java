package com.treasurehunter.treasurehunter.domain.notification.repository.token;

import com.treasurehunter.treasurehunter.domain.notification.entity.PlatformType;
import com.treasurehunter.treasurehunter.domain.notification.entity.token.NotificationToken;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, Long> {

    Optional<NotificationToken> findByUserAndPlatform(User user, PlatformType platform);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM NotificationToken nt WHERE nt.user.id = :userId AND nt.platform = :platform")
    void deleteByUserIdAndPlatform(
            @Param("userId") final Long userId,
            @Param("platform") final PlatformType platform
    );
}
