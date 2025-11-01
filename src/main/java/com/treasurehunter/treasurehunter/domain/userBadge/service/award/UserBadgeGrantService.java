package com.treasurehunter.treasurehunter.domain.userBadge.service.award;

import com.treasurehunter.treasurehunter.domain.admin.badge.entity.Badge;
import com.treasurehunter.treasurehunter.domain.admin.badge.entity.BadgeName;
import com.treasurehunter.treasurehunter.domain.admin.badge.repository.BadgeRepository;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.userBadge.entity.UserBadge;
import com.treasurehunter.treasurehunter.domain.userBadge.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserBadgeGrantService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    /**
     * 배지를 수여할 수 있는지 검사하고 자격이 충분하면
     * 배지를 수여하는 메서드
     * 자격이 충분하지 않거나 배지가 없다면 에러 대신 로그만 찍힌다.
     * @param user 배지 수여 받을 유저 엔티티
     * @param badgeName 수여할 배지 이름
     * @param earnedDate 배지 얻은 LocalDateTime
     */
    @Transactional
    public void awardIfNotExists(final User user, final BadgeName badgeName, final LocalDateTime earnedDate){
        // 1) 배지 조회
        final Badge badge = badgeRepository.findByName(badgeName);

        // 2) 배지가 존재하지 않으면 로그 찍고 종료
        if(badge == null){
            log.error("[EVENT ERROR] BADGE NOT FOUND: badge name: {}", badgeName);
            return;
        }

        // 3) 배지 저장
        try {
            userBadgeRepository.save(
                    UserBadge.builder()
                            .earnedDate(earnedDate)
                            .user(user)
                            .badge(badge)
                            .build()
            );
        } catch (DataIntegrityViolationException ex) {
            // UK 위반인 경우 (배지를 이미 가진 경우) 제외
            if (ex.getMostSpecificCause().getMessage().contains("uq_user_badge")) {
                return;
            }
            throw ex;
        }
    }
}
