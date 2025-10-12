package com.treasurehunter.treasurehunter.global.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * OAuth2 등록 ID를 잘못 입력한 경우를 처리하는 필터
 * ID가 올바르지 않다면 400을 던짐
 */
@Component
@RequiredArgsConstructor
public class Oauth2RegistrationPrecheckFilter extends OncePerRequestFilter {

    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest httpServletRequest){
        final String requestURI = httpServletRequest.getRequestURI();
        return (requestURI == null) || (!requestURI.startsWith("/oauth2/authorization/"));
    }

    @Override
    public void doFilterInternal(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final FilterChain filterChain
    ) throws IOException, ServletException {

        final String requestId = extractRegistrationId(httpServletRequest.getRequestURI());
        final ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(requestId);

        //등록된 ID 중에서 찾을 수 없다면
        if(registration == null){

            //응답이 이미 전송되고 있다면
            if(httpServletResponse.isCommitted()){
                return;
            }

            Map<String, Object> responseBody = Map.of("status", "400", "message", "잘못된 OAuth2 클라이언트 등록 ID입니다.");

            httpServletResponse.resetBuffer();
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
            httpServletResponse.flushBuffer();

            return;
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    //uri에서 맨 마지막에 있는 id만 가져오는 메서드
    private String extractRegistrationId(final String uri){
        final int idx = uri.lastIndexOf("/");
        return (idx >= 0 && idx + 1 < uri.length()) ? uri.substring(idx + 1) : "";
    }
}
