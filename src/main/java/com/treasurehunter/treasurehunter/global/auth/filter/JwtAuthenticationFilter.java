package com.treasurehunter.treasurehunter.global.auth.filter;

import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 필터에 JWT 검증 과정 추가
 * 토큰이 아예 존재하지 않으면 검증하지 않고 익명으로 진행됨
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final FilterChain filterChain
    ) throws ServletException, IOException {

        final String authorizationHeader = httpServletRequest.getHeader("Authorization");

        //JWT를 제대로 가지고 있는지 검사
        //없으면 익명으로 진행
        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        try {
            final String jwt =  authorizationHeader.substring(7);

            //JWT 검증
            //검증 실패하면 익명으로 진행
            final String userIdStr;
            try {
                userIdStr = jwtProvider.getPayload(jwt);
            } catch (CustomException ex) { //검증 실패시 던져지는 CustomException 무시하고 진행시키기
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            //토큰에서 Claims 추출
            final List<SimpleGrantedAuthority> authorities = getAuthorities(jwt);

            //이미 JWT 검증으로 인증 완료됨
            final Authentication auth =
                    new UsernamePasswordAuthenticationToken(userIdStr, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (Exception ex) {
            //JWT 검증 실패해서 401 던짐
            SecurityContextHolder.clearContext();
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().write(
                    "{\"status\":\"UNAUTHORIZED\",\"message\":\"토큰이 만료되었거나 없습니다.\",\"timestamp\":\"" + LocalDateTime.now() + "\"}"
            );
        }
    }

    private List<SimpleGrantedAuthority> getAuthorities(final String jwt){
        final List<String> roles = jwtProvider.getClaims(jwt).get("authorities", List.class);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
