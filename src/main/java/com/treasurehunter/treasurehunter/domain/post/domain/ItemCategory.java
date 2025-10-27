package com.treasurehunter.treasurehunter.domain.post.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemCategory {

    WALLET("WALLET", "지갑"),
    CLOTHES("CLOTHES", "의류"),
    PHONE("PHONE", "핸드폰"),
    BAG("BAG", "가방"),
    ELECTRONICS("ELECTRONIC_DEVICE", "전자제품"),
    ACCESSORY("ACCESSORY", "액세서리"),
    STATIONERY("STATIONERY", "문구류"),
    ETC("ETC", "기타 물품")
    ;

    private String key;
    private String description;
}
