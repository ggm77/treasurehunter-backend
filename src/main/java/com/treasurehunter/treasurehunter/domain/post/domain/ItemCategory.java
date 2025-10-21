package com.treasurehunter.treasurehunter.domain.post.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemCategory {

    WALLET("WALLET", "지갑"),
    PHONE("PHONE", "핸드폰"),
    ELECTRONIC_DEVICE("ELECTRONIC_DEVICE", "전자제품"),
    CLOTHES("CLOTHES", "의류"),
    BAG("BAG", "가방"),
    ETC("ETC", "기타 물품")
    ;

    private String key;
    private String description;
}
