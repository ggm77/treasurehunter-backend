package com.treasurehunter.treasurehunter.domain.userBadge.repository;

import com.treasurehunter.treasurehunter.domain.userBadge.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    boolean existsByBadgeId(Long badgeId);
    List<UserBadge> findByUserId(Long userId);
    boolean existsByBadgeIdAndUserId(Long badgeId, Long userId);
}
