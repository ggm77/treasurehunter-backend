package com.treasurehunter.treasurehunter.domain.userBadge.repository;

import com.treasurehunter.treasurehunter.domain.userBadge.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    boolean existsByBadgeId(Long badgeId);
}
