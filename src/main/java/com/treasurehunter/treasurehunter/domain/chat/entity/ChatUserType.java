package com.treasurehunter.treasurehunter.domain.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatUserType {

    // 게시글 작성자
    AUTHOR,

    // 채팅 건 사람
    CALLER
    ;
}
