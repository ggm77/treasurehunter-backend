package com.treasurehunter.treasurehunter.global.auth.oauth.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Spring OAuth2 흐름에서 Apple을 제외시키기 위한 Resolver
 * Apple은 커스텀 API를 사용한다.
 */
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver delegate;

    public CustomAuthorizationRequestResolver(
            final ClientRegistrationRepository clientRegistrationRepository,
            final String authorizationRequestBaseUri
    ) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                authorizationRequestBaseUri
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(final HttpServletRequest httpServletRequest){
        final String uri = httpServletRequest.getRequestURI();

        //애플 oauth2 요청일 경우에는 Spring OAuth2 흐름에서 제외 -> 커스텀 컨트롤러로 감
        if(uri != null && uri.startsWith("/oauth2/authorization/apple")){
            return null;
        }

        return delegate.resolve(httpServletRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(
            final HttpServletRequest httpServletRequest,
            final String clientRegistrationId
    ){

        //애플 oauth2 요청일 경우에는 Spring OAuth2 흐름에서 제외 -> 커스텀 컨트롤러로 감
        if("apple".equals(clientRegistrationId)){
            return null;
        }

        return delegate.resolve(httpServletRequest, clientRegistrationId);
    }
}
