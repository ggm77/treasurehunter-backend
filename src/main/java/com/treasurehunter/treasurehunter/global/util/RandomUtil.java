package com.treasurehunter.treasurehunter.global.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomUtil {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 랜덤한 숫자로 이루어진 문자열을 반환하는 메서드
     * length로 길이를 조정할 수 있다.
     * @param length
     * @return 길이가 length인 숫자로 이루어진 문자열
     */
    public String getRandomNumber(final int length) {

        if (length <= 0) {
            throw new IllegalArgumentException("Code length must be positive.");
        }

        int min = (int) Math.pow(10, length - 1);
        int bound = (int) Math.pow(10, length) - min;
        int code = secureRandom.nextInt(bound) + min;

        return String.valueOf(code);
    }
}
