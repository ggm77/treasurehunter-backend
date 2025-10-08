package com.treasurehunter.treasurehunter.global.auth.oauth;

import com.treasurehunter.treasurehunter.domain.user.service.oauth.UserOauth2Service;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsRequestDto;
import com.treasurehunter.treasurehunter.global.auth.oauth.dto.UserOauth2AccountsResponseDto;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserOauth2Service userOauth2Service;

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

        //oauth에서 받아온 엑세스 토큰
        final String oauthAccessToken = oauth2UserRequest.getAccessToken().getTokenValue();

        //정보 담을 DTO 생성
        final UserOauth2AccountsRequestDto userOauth2AccountsRequestDto;

        switch (provider) {
            case "google" -> {

                userOauth2AccountsRequestDto = UserOauth2AccountsRequestDto.builder()
                        .provider(provider)
                        .providerUserId( (String) attributes.get("sub") )
                        .email( (String) attributes.get("email") )
                        .name( (String) attributes.getOrDefault("name", null) )
                        .profileImage((String) attributes.get("picture") )
                        .accessToken(oauthAccessToken)
                        .build();
            }
//            case "apple" -> {}
            default -> throw new CustomException(ExceptionCode.UNSUPPORTED_PROVIDER);
        }

        // 정보가 존재하면 로그인, 없으면 유저 등록만 하는 메서드
        final UserOauth2AccountsResponseDto userOauth2AccountsResponseDto = userOauth2Service.upsertOAuthUser(userOauth2AccountsRequestDto);

        return new CustomDefaultOauth2User(
                Collections.singletonList(new SimpleGrantedAuthority(userOauth2AccountsResponseDto.getUserRole().getKey())),
                attributes,
                userNameAttributeName,
                userOauth2AccountsResponseDto.getUserId()
        );
    }

}
