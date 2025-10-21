package com.treasurehunter.treasurehunter.domain.post.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostType {

    LOST("LOST", "물건 잃어버림"),
    FOUND("FOUND", "물건 찾음")
    ;

    private final String key;
    private final String description;
}
