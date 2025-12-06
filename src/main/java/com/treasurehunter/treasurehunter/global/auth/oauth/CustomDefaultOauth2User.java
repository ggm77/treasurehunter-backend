package com.treasurehunter.treasurehunter.global.auth.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomDefaultOauth2User extends DefaultOAuth2User {

    private final String provider;

    /**
     * DefaultOAuth2User에서 provider만 추가됨
     * @param authorities
     * @param attributes
     * @param nameAttributeKey
     * @param provider OAuth 제공자 (google, naver ...)
     */
    public CustomDefaultOauth2User(
            final Collection<? extends GrantedAuthority> authorities,
            final Map<String, Object> attributes,
            final String nameAttributeKey,
            final String provider
    ){
        super(authorities, attributes, nameAttributeKey);
        this.provider = provider;
    }

    //provider 리턴하는 메서드
    public String getProvider() {
        return this.provider;
    }
}
