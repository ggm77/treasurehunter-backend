package com.treasurehunter.treasurehunter.global.auth.filter;

import com.treasurehunter.treasurehunter.global.auth.jwt.JwtProvider;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
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
        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(ExceptionCode.AUTHENTICATION_ERROR);
        }

        final String jwt =  authorizationHeader.substring(7);

        //토큰 검증 오류시 JwtProvider에서 401보냄
        final String userIdStr = jwtProvider.validateToken(jwt);

        //이미 JWT 검증으로 인증 완료됨
        final Authentication auth =
                new UsernamePasswordAuthenticationToken(userIdStr, null, AuthorityUtils.NO_AUTHORITIES);

        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
