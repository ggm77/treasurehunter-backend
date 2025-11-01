package com.treasurehunter.treasurehunter.domain.admin.badge.repository;

import com.treasurehunter.treasurehunter.domain.admin.badge.entity.Badge;
import com.treasurehunter.treasurehunter.domain.admin.badge.entity.BadgeName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Badge findByName(BadgeName name);
}
