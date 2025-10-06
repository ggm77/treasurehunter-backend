package com.treasurehunter.treasurehunter.domain.user.service;

import com.treasurehunter.treasurehunter.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);
}
