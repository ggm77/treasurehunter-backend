package com.treasurehunter.treasurehunter.global.auth.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(final OAuth2UserRequest oauth2UserRequest){

        //oauth에서 받은 정보 가져오기
        final OAuth2User oauth2User = super.loadUser(oauth2UserRequest);

        //oauth 진행시 사용되는 키가 되는 값
        final String userNameAttributeName = oauth2UserRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName();

        //provider는 소문자
        final String provider =  oauth2UserRequest.getClientRegistration().getRegistrationId().toLowerCase(); //google, apple...
        final Map<String, Object> attributes = oauth2User.getAttributes();

        return new CustomDefaultOauth2User(
                oauth2User.getAuthorities(),
                attributes,
                userNameAttributeName,
                provider
        );
    }

}
