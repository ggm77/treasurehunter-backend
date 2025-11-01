package com.treasurehunter.treasurehunter.domain.admin.badge.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BadgeName {

    FIRST_POST("FIRST_POST"),
    TEN_POST("TEN_POST"),

    FIRST_REVIEW("FIRST_REVIEW"),
    TEN_REVIEW("TEN_REVIEW"),

    // 테스트용 -> 삭제해야함
    TEST_POST("TEST_POST"),
    TEST1_POST("TEST_POST"),
    TEST2_POST("TEST_POST"),
    TEST3_POST("TEST_POST"),
    TEST4_POST("TEST_POST"),
    TEST5_POST("TEST_POST")
    ;

    private final String name;
}
