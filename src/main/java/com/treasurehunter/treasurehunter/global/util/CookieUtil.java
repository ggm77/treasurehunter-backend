package com.treasurehunter.treasurehunter.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieUtil {

    /**
     * HttpServletRequest에서 쿠키를 찾는 메서드
     * 쿠키 이름 중에 같은 걸 찾아서 쿠키 값을 리턴해줌
     * @param httpServletRequest
     * @param cookieName
     * @return 찾는 쿠키의 값
     */
    public String getCookieValue(
            final HttpServletRequest httpServletRequest,
            final String cookieName
    ){
        if(httpServletRequest.getCookies() == null){
            return null;
        }

        return Arrays.stream(httpServletRequest.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
