package com.treasurehunter.treasurehunter.global.config;

import com.treasurehunter.treasurehunter.global.auth.filter.JwtAuthenticationFilter;
import com.treasurehunter.treasurehunter.global.auth.filter.Oauth2RegistrationPrecheckFilter;
import com.treasurehunter.treasurehunter.global.auth.oauth.CustomOauth2UserService;
import com.treasurehunter.treasurehunter.global.auth.oauth.handler.Oauth2FailureHandler;
import com.treasurehunter.treasurehunter.global.auth.oauth.handler.Oauth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Oauth2RegistrationPrecheckFilter oauth2AuthenticationRequestExceptionHandlingFilter;
    private final Oauth2SuccessHandler oauth2SuccessHandler;
    private final Oauth2FailureHandler oauth2FailureHandler;
    private final CustomOauth2UserService customOauth2UserService;

    @Bean
    protected SecurityFilterChain configure(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(Customizer.withDefaults())
                .csrf((csrf) -> csrf.disable())
                .httpBasic((httpBasic) -> httpBasic.disable())
                .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests.requestMatchers(
                        "/ping",
                        "/ready",
                        "/login/oauth2/code/**", //oauth 리다이렉트 하는 곳
                        "/oauth2/authorization/**", //프론트에서 로그인 요청하는 곳
                        "/api/swagger/**",
                        "/api/v1/auth/**"
                ).permitAll().anyRequest().authenticated())
                .oauth2Login(customConfigurer -> customConfigurer
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(customOauth2UserService))
                );

        httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterBefore(
                oauth2AuthenticationRequestExceptionHandlingFilter,
                OAuth2AuthorizationRequestRedirectFilter.class
        );

        return httpSecurity.build();
    }
}
