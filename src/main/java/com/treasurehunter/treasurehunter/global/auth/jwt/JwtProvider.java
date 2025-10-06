package com.treasurehunter.treasurehunter.global.auth.jwt;

import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.*;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

/**
 * JWT를 생성하고 검증한다.
 * 시크릿키는 application.properties에서 가져옴
 */
@Service
public class JwtProvider {

    //HS512
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public String getTokenType(){
        return "Bearer";
    }

    /**
     * JWT를 생성하는 메서드
     * ttlSeconds을 적절하게 설정하여 엑세스 토큰, 리프레시 토큰으로 사용가능하다.
     * @param userId 유저의 고유 아이디 번호
     * @param ttlSeconds JWT의 유효 시간 (초)
     * @return JWT
     */
    public String creatToken(final Long userId, final Long ttlSeconds){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        final String userIdStr = userId.toString(); //문자열이 된 유저 아이디
        final Instant now = Instant.now(); //발행 일시
        final Instant exp = now.plusSeconds(ttlSeconds); //만료 일시

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(userIdStr)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * JWT를 검증하는 메서드
     * 문자열이 된 유저의 고유 아이디 번호를 리턴함
     * @param jwt JWT
     * @return 문자열이 된 유저 아이디
     */
    public String validateToken(final String jwt){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        //JWT검증 및 만료시 예외 처리
        final Jws<Claims> claimsJws;
        try {
            claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt);
        } catch (JwtException ex) {
            //클라이언트에게 JWT관련 예외는 전부 401로만 보여줌
            throw new CustomException(ExceptionCode.AUTHENTICATION_ERROR);
        }

        return claimsJws.getPayload().getSubject();
    }
}
