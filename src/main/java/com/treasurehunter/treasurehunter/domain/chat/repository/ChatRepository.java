package com.treasurehunter.treasurehunter.domain.chat.repository;

import com.treasurehunter.treasurehunter.domain.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
