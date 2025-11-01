package com.treasurehunter.treasurehunter.domain.admin.badge.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BadgeName {

    FIRST_POST("FIRST_POST"),
    TEN_POST("TEN_POST"),

    FIRST_REVIEW("FIRST_REVIEW"),
    TEN_REVIEW("TEN_REVIEW")
    ;

    private final String name;
}
