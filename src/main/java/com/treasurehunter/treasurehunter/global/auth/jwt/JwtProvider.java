package com.treasurehunter.treasurehunter.global.auth.jwt;

import com.treasurehunter.treasurehunter.domain.user.domain.Role;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.*;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * JWT를 생성하고 검증한다.
 * 시크릿키는 application.properties에서 가져옴
 */
@Component
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
    public String creatToken(final Long userId, final Role role, final Long ttlSeconds){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        final String userIdStr = userId.toString(); //문자열이 된 유저 아이디
        final Instant now = Instant.now(); //발행 일시
        final Instant exp = now.plusSeconds(ttlSeconds); //만료 일시

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(userIdStr)
                .claim("authorities", List.of(role.getKey()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * JWT를 검증하는 메서드
     * 문자열이 된 유저의 고유 아이디 번호를 리턴함
     * 검증 실패시 io.jsonwebtoken.JwtException 예외 던짐
     * @param jwt JWT
     * @return 문자열이 된 유저 아이디
     */
    public String getPayload(final String jwt){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        try {
            final Jws<Claims> claimsJws =
                    Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(jwt);

            return claimsJws.getPayload().getSubject();
        } catch (JwtException ex) {
            throw new CustomException(ExceptionCode.INVALID_TOKEN);
        }
    }

    /**
     * JWT에서 claims만 얻어오는 메서드
     * @param jwt JWT
     * @return JWT의 Claims
     */
    public Claims getClaims(final String jwt){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        try{
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (JwtException ex) {
            throw new CustomException(ExceptionCode.INVALID_TOKEN);
        }
    }
}
