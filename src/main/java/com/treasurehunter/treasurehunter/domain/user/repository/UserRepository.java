package com.treasurehunter.treasurehunter.domain.user.repository;

import com.treasurehunter.treasurehunter.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);
    boolean existsById(Long id);
}
