package com.treasurehunter.treasurehunter.global.auth.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.*;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT를 생성하고 검증한다.
 * 시크릿키는 application.properties에서 가져옴
 */
@Component
public class JwtProvider {

    //HS512
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.accessToken.exprTime}")
    private Integer ACCESS_TOKEN_EXPIRATION_SECONDS;

    @Value("${jwt.refreshToken.exprTime}")
    private Integer REFRESH_TOKEN_EXPIRATION_SECONDS;

    public String getTokenType(){
        return "Bearer";
    }

    /**
     * 액세스 토큰을 생성하는 메서드
     * ttlSeconds을 적절하게 설정하여 엑세스 토큰, 리프레시 토큰으로 사용가능하다.
     * @param userId 유저의 고유 아이디 번호
     * @return JWT
     */
    public String creatAccessToken(final Long userId, final Role role){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        final String userIdStr = userId.toString(); //문자열이 된 유저 아이디
        final Instant now = Instant.now(); //발행 일시
        final Instant exp = now.plusSeconds(ACCESS_TOKEN_EXPIRATION_SECONDS); //만료 일시

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
     * 리프레시 토큰을 생성하는 메서드
     * ttlSeconds을 적절하게 설정하여 엑세스 토큰, 리프레시 토큰으로 사용가능하다.
     * @param userId 유저의 고유 아이디 번호
     * @return JWT
     */
    public String creatRefreshToken(final Long userId){

        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        final String userIdStr = userId.toString(); //문자열이 된 유저 아이디
        final Instant now = Instant.now(); //발행 일시
        final Instant exp = now.plusSeconds(REFRESH_TOKEN_EXPIRATION_SECONDS); //만료 일시

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

    /**
     * 애플 idToken에서 Claims를 뽑는 메서드
     * @param token 애플 idToken
     * @param publicKey 애플 공개 키
     * @return Claims
     */
    public Claims getClaimsFromAppleToken(
            final String token,
            final PublicKey publicKey
    ){
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException ex) {
            throw new CustomException(ExceptionCode.APPLE_AUTH_ERROR);
        }
    }

    /**
     * 토큰에서 헤더의 값만 가져오는 메서드
     * @param token 헤더를 가진 토큰
     * @return 맵 형태가 된 헤더
     */
    public Map<String, String> getHeaders(final String token){
        try{
            final String header = token.split("\\.")[0];
            return new ObjectMapper().readValue(decode(header), Map.class);
        } catch (JsonProcessingException ex) {
            throw new CustomException(ExceptionCode.INVALID_TOKEN);
        }
    }

    /**
     * base64로 인코딩 된 부분을 디코딩 하는 메서드
     * @param base64 base64로 인코딩 된 문자열
     * @return 디코딩된 문자열
     */
    public String decode(final String base64){
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }

    /**
     * JWT에서 Authorities를 얻는 메서드
     * @param claims JWT의 claims
     * @return JWT의 Authorities
     */
    public List<SimpleGrantedAuthority> getAuthorities(final Claims claims){
        final List<String> roles = claims.get("authorities", List.class);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
