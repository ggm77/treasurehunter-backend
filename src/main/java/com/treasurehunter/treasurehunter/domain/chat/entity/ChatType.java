package com.treasurehunter.treasurehunter.domain.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatType {

    // 채팅방 입장
    ENTER,

    // 기본적인 채팅 메세지
    MESSAGE,

    // 사진
    IMAGE,

    //위치 정보
    LOCATION,

    // 채팅방 나기기
    EXIT
}
