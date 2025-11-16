package com.treasurehunter.treasurehunter.domain.chat.repository;

import com.treasurehunter.treasurehunter.domain.chat.entity.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    //마지막 메세지의 아이디 기반으로 그 이후 메세지 가져오기
    List<Chat> findByRoomIdAndIdGreaterThanOrderByIdAsc(
            String roomId,
            Long lastChatId,
            Pageable pageable
    );
}
