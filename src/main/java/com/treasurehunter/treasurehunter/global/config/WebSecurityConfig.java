package com.treasurehunter.treasurehunter.global.config;

import com.treasurehunter.treasurehunter.global.auth.filter.JwtAuthenticationFilter;
import com.treasurehunter.treasurehunter.global.auth.filter.Oauth2RegistrationPrecheckFilter;
import com.treasurehunter.treasurehunter.global.auth.oauth.handler.Oauth2FailureHandler;
import com.treasurehunter.treasurehunter.global.auth.oauth.handler.Oauth2SuccessHandler;
import com.treasurehunter.treasurehunter.global.auth.oauth.resolver.CustomAuthorizationRequestResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Oauth2RegistrationPrecheckFilter oauth2AuthenticationRequestExceptionHandlingFilter;
    private final Oauth2SuccessHandler oauth2SuccessHandler;
    private final Oauth2FailureHandler oauth2FailureHandler;

    @Bean
    protected SecurityFilterChain configure(
            final HttpSecurity httpSecurity,
            final ClientRegistrationRepository clientRegistrationRepository
            ) throws Exception {
        httpSecurity
                .cors(Customizer.withDefaults())
                .csrf((csrf) -> csrf.disable())
                .httpBasic((httpBasic) -> httpBasic.disable())
                .securityContext(c -> c.securityContextRepository(new NullSecurityContextRepository())) //http에서 세션 생성 방지
                .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        //인증 필요 없는 API들
                        .requestMatchers(
                                "/ping",
                                "/ready",
                                "/login/oauth2/code/**", //oauth 리다이렉트 하는 곳
                                "/auth/apple/callback", //애플 로그인 리다이렉트 하는 곳
                                "/oauth2/authorization/**", //프론트에서 로그인 요청하는 곳
                                "/api/swagger/**",
                                "/api/v1/auth/**",
                                "/api/v1/file/image",
                                "/ws/**" //웹소켓 대응
                        ).permitAll()

                        //사진 조회 API GET만 제외
                        .requestMatchers(HttpMethod.GET, "/api/v1/file/image").permitAll()

                        //어드민 API는 어드민만 사용가능
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        //그 외 요청은 인증 필요
                        .anyRequest().authenticated())
                .oauth2Login(customConfigurer -> customConfigurer
                        .authorizationEndpoint(
                                auth -> auth
                                        .baseUri("/oauth2/authorization")
                                        .authorizationRequestResolver(
                                                new CustomAuthorizationRequestResolver(
                                                        clientRegistrationRepository,
                                                        "/oauth2/authorization"
                                                )
                                        )
                        )
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                );

        httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterBefore(
                oauth2AuthenticationRequestExceptionHandlingFilter,
                OAuth2AuthorizationRequestRedirectFilter.class
        );

        return httpSecurity.build();
    }
}
