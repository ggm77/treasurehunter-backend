package com.treasurehunter.treasurehunter.domain.smsVerification.service;

import com.treasurehunter.treasurehunter.domain.smsVerification.dto.VerifySmsVerificationCodeRequestDto;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.AppPhoneNumberUtil;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserRepository userRepository;
    private final AppPhoneNumberUtil appPhoneNumberUtil;

    @Transactional
    public void verifyVerificationCode(
            final VerifySmsVerificationCodeRequestDto verifySmsVerificationCodeRequestDto,
            final Long userId
    ) {

        //e164로 들어오는 값 일반화
        final String e164 = appPhoneNumberUtil.normalizeE164(verifySmsVerificationCodeRequestDto.getPhoneNumber());
        final String providedCode = verifySmsVerificationCodeRequestDto.getCode();

        final String codeK = codeKey(e164);
        final String storedCode = redisTemplate.opsForValue().get(codeK);

        // 1) 전화번호 유효성 검사
        if(!appPhoneNumberUtil.isValidE164(e164)){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 서버에 있는 인증 코드와 제출한 코드 존재 여부 확인
        if(storedCode == null || storedCode.isEmpty()) {
            throw new CustomException(ExceptionCode.CODE_NOT_EXIST);
        }
        if(providedCode == null || !providedCode.matches("\\d{6}")){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 3) 시도 횟수 증가
        final String attemptK = attKey(e164);
        final Long attempts = redisTemplate.opsForValue().increment(attemptK);
        //첫 증가 시, 코드 TTL만큼 시도키도 만료 설정
        if(attempts != null && attempts == 1L){
            redisTemplate.expire(attemptK, Duration.ofSeconds(CODE_TTL));
        }

        // 4) 시도 한도 초과 검사
        if(attempts != null && ATTEMPT_LIMIT != null && attempts > ATTEMPT_LIMIT){
            redisTemplate.delete(codeK);
            redisTemplate.delete(attemptK);

            throw new CustomException(ExceptionCode.ATTEMPT_LIMIT);
        }

        // 5) 인증 코드 비교
        final boolean isMatch = MessageDigest.isEqual(
                storedCode.getBytes(StandardCharsets.UTF_8),
                providedCode.getBytes(StandardCharsets.UTF_8)
        );

        if(!isMatch){
            throw new CustomException(ExceptionCode.CODE_NOT_MATCH);
        }

        // 6) 유저 폰 번호, Role 변경
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        user.updatePhoneNumber(e164);
        user.updateRoleToUser();

        // 7) 인증 코드 삭제
        redisTemplate.delete(codeK);
        redisTemplate.delete(attemptK);
    }
}
