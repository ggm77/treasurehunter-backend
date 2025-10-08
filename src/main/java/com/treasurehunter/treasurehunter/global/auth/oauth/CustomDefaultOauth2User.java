package com.treasurehunter.treasurehunter.global.auth.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomDefaultOauth2User extends DefaultOAuth2User {

    private final Long userId;

    /**
     * DefaultOAuth2User에서 userId만 추가됨
     * @param authorities
     * @param attributes
     * @param nameAttributeKey
     * @param userId 유저 아이디
     */
    public CustomDefaultOauth2User(
            final Collection<? extends GrantedAuthority> authorities,
            final Map<String, Object> attributes,
            final String nameAttributeKey,
            final Long userId
    ){
        super(authorities, attributes, nameAttributeKey);
        this.userId = userId;
    }

    //유저 아이디로 유저를 구분
    @Override
    public String getName(){
        return userId.toString();
    }
}
