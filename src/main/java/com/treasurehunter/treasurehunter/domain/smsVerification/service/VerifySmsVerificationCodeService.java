package com.treasurehunter.treasurehunter.domain.smsVerification.service;

import com.treasurehunter.treasurehunter.domain.smsVerification.dto.VerifySmsVerificationCodeRequestDto;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class VerifySmsVerificationCodeService {

    @Value("${sms.verification.code.ttl}")
    private Long CODE_TTL;

    @Value("${sms.verification.attempt.limit}")
    private Long ATTEMPT_LIMIT;

    //인증번호
    private static String codeKey(final String e164){ return "sv:code:" + e164; }
    //시도 횟수
    private static String attKey(final String e164){ return "sv:att:" + e164; }

    private final RedisTemplate<String, String> redisTemplate;

    public void verifyVerificationCode(final VerifySmsVerificationCodeRequestDto verifySmsVerificationCodeRequestDto) {

        final String e164 = verifySmsVerificationCodeRequestDto.getPhoneNumber();
        final String providedCode = verifySmsVerificationCodeRequestDto.getCode();

        final String codeK = codeKey(e164);
        final String storedCode = redisTemplate.opsForValue().get(codeK);

        // 1) 인증 코드가 존재하는지 확인
        if(storedCode == null || storedCode.isEmpty()) {
            throw new CustomException(ExceptionCode.CODE_NOT_EXIST);
        }

        // 2) 시도 횟수 증가
        final String attemptK = attKey(e164);
        final Long attempts = redisTemplate.opsForValue().increment(attemptK);
        //첫 증가 시, 코드 TTL만큼 시도키도 만료 설정
        if(attempts != null && attempts == 1L){
            redisTemplate.expire(attemptK, Duration.ofSeconds(CODE_TTL));
        }

        // 3) 시도 한도 초과 검사
        if(attempts != null && ATTEMPT_LIMIT != null && attempts > ATTEMPT_LIMIT){
            redisTemplate.delete(codeK);
            redisTemplate.delete(attemptK);

            throw new CustomException(ExceptionCode.ATTEMPT_LIMIT);
        }

        // 4) 인증 코드 비교
        final boolean isMatch = MessageDigest.isEqual(
                storedCode.getBytes(StandardCharsets.UTF_8),
                providedCode.getBytes(StandardCharsets.UTF_8)
        );

        if(!isMatch){
            throw new CustomException(ExceptionCode.CODE_NOT_MATCH);
        }

        // 5) 성공 -> 인증 코드 삭제
        redisTemplate.delete(codeK);
        redisTemplate.delete(attemptK);
    }
}
