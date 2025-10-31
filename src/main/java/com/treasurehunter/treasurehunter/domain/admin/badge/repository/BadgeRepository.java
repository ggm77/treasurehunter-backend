package com.treasurehunter.treasurehunter.domain.admin.badge.repository;

import com.treasurehunter.treasurehunter.domain.admin.badge.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
}
