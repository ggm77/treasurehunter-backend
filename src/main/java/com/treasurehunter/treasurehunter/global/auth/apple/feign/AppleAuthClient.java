package com.treasurehunter.treasurehunter.global.auth.apple.feign;

import com.treasurehunter.treasurehunter.global.auth.apple.dto.key.ApplePublicKeyResponseDto;
import com.treasurehunter.treasurehunter.global.auth.apple.dto.token.AppleTokenResponseDto;
import com.treasurehunter.treasurehunter.global.auth.apple.util.AppleKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class AppleAuthClient {

    @Value("${apple.auth.public-key-uri}")
    private String APPLE_PUBLIC_KEY_URI;

    @Value("${apple.auth.token-uri}")
    private String APPLE_TOKEN_URI;

    @Value("${apple.client-id}")
    private String APPLE_CLIENT_ID;

    private final WebClient appleWebClient;
    private final AppleKeyGenerator appleKeyGenerator;

    // https://appleid.apple.com/auth/keys에 요청을 넣고 keys를 받아온다.
    public ApplePublicKeyResponseDto requestKeys() {
        return appleWebClient.get()
                .uri(APPLE_PUBLIC_KEY_URI)
                .retrieve()
                .bodyToMono(ApplePublicKeyResponseDto.class)
                .block();
    }

    // https://appleid.apple.com/auth/token에 요청을 넣고 토큰을 받아온다.
    public AppleTokenResponseDto requestToken(final String authorizationCode) {
        final MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authorizationCode);
        formData.add("client_id", APPLE_CLIENT_ID);
        formData.add("client_secret", appleKeyGenerator.generateClientSecrete());
        formData.add("grant_type", "authorization_code");

        return appleWebClient.post()
                .uri(APPLE_TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(AppleTokenResponseDto.class)
                .block();
    }
}
