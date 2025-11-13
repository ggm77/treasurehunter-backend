package com.treasurehunter.treasurehunter.global.stomp.auth;

import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.stomp.constants.StompConstants;
import com.treasurehunter.treasurehunter.global.stomp.dto.StompAuthResultDto;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StompSessionContext {

    /**
     * 유저의 정보를 세션에 저장하는 메서드
     * @param accessor 저장을 진행할 STOMP 메세지의 헤더 정보를 포함한 객체
     * @param stompAuthResultDto 저장할 정보가 담긴 DTO
     */
    public void store(
            final StompHeaderAccessor accessor,
            final StompAuthResultDto stompAuthResultDto
    ) {
        // 1) 유저 Principal 지정
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                stompAuthResultDto.getUserIdStr(),
                null,
                stompAuthResultDto.getAuthorities()
        ));

        // 2) 복원용 attributes 설정
        accessor.getSessionAttributes().put("userId", stompAuthResultDto.getUserIdStr()); //유저 ID 저장
        accessor.getSessionAttributes().put("authorities", stompAuthResultDto.getAuthorities()); //유저 authorities 저장
        accessor.getSessionAttributes().put("exp", stompAuthResultDto.getExp()); //세션 만료시간 지정 (jwt 남은 유효 시간과 같음)
    }

    /**
     * Attributes에 존재하는 정보를 바탕으로 Principal을 복원하는 메서드
     * @param accessor 복원을 진행할 STOMP 메세지의 헤더 정보를 포함한 객체
     */
    public void restorePrincipalFromAttributes(final StompHeaderAccessor accessor) {

        // 1) attributes에서 정보 추출
        final Object userIdObj = accessor.getSessionAttributes().get(StompConstants.ATTR_USER_ID);
        final Object authoritiesObj = accessor.getSessionAttributes().get(StompConstants.ATTR_AUTHORITIES);

        // 2) null 검사
        if(userIdObj == null){
            throw new CustomException(ExceptionCode.MISSING_USER_ID);
        }
        if(authoritiesObj == null){
            throw new CustomException(ExceptionCode.MISSING_AUTHORITIES);
        }

        // 3) 원래 클래스로 캐스팅
        final String userIdStr;
        final List<SimpleGrantedAuthority> authorities;
        try {
            userIdStr = (String) userIdObj;
            authorities = (List<SimpleGrantedAuthority>) authoritiesObj;
        } catch (ClassCastException e) {
            throw new CustomException(ExceptionCode.INVALID_ATTRIBUTE_FORMAT);
        }

        // 4) Principal 설정
        accessor.setUser(new UsernamePasswordAuthenticationToken(userIdStr, null, authorities));
    }

    /**
     * 세션이 만료 되지 않았음을 보장하는 메서드
     * @param accessor 검사를 진행할 STOMP 메세지의 헤더 정보를 포함한 객체
     */
    public void assertNotExpired(final StompHeaderAccessor accessor) {

        // 1) attributes에서 정보 추출
        final Object expObj = accessor.getSessionAttributes().get(StompConstants.ATTR_EXP);

        // 2) null 검사
        if(expObj == null){
            throw new CustomException(ExceptionCode.MISSING_EXP);
        }

        // 3) 원래 클래스로 캐스팅
        final long exp;
        try {
            exp = (long) expObj;
        } catch (ClassCastException ex){
            throw new CustomException(ExceptionCode.INVALID_ATTRIBUTE_FORMAT);
        }

        if(System.currentTimeMillis() > exp){
            throw new CustomException(ExceptionCode.AUTHENTICATION_ERROR);
        }
    }
}
