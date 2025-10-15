package com.treasurehunter.treasurehunter.domain.smsVerification.service;

import com.treasurehunter.treasurehunter.domain.smsVerification.dto.RequestSmsVerificationRequestDto;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SmsVerificationService {

    //실제 표시될 만료 시간보다 조금 더 길게 설정하기
    @Value("${sms.verification.code.ttl}")
    private Long CODE_TTL;

    @Value("${sms.verification.cool.ttl}")
    private Long COOL_TTL;

    @Value("${sms.verification.daily.limit}")
    private Long DAILY_LIMIT;

    //인증번호
    private static String codeKey(final String e164){ return "sv:code:" + e164; }
    //시도 횟수
    private static String attKey(final String e164){ return "sv:att:" + e164; }
    //쿨다운
    private static String cdKey(final String e164){ return "sv:cd:" + e164; }
    //하루 동안 시도한 횟수
    private static String qKey(final String e164){ return "sv:q:" + e164; }

    private final RedisTemplate<String, String> redisTemplate;
    private final RandomUtil randomUtil;

    public void createVerificationCode(final RequestSmsVerificationRequestDto requestSmsVerificationRequestDto) {

        final String e164 = requestSmsVerificationRequestDto.getE164();

        // 1) 쿨다운
        final Boolean ok = redisTemplate.opsForValue().setIfAbsent(
                cdKey(e164),
                "1",
                Duration.ofSeconds(COOL_TTL)
        );

        //쿨다운에 걸렸다면
        if(Boolean.FALSE.equals(ok)){
            throw new CustomException(ExceptionCode.COOL_DOWN);
        }

        // 2) 일일 한도
        final String qk = qKey(e164);
        final Long used = redisTemplate.opsForValue().increment(qk);

        //새로 생성된 경우 만료시간 설정
        if(used != null && used == 1){
            redisTemplate.expire(qk, Duration.ofHours(24));
        }

        //일일 한도에 걸렸다면
        if(used != null && used > DAILY_LIMIT){
            throw new CustomException(ExceptionCode.DAILY_LIMIT);
        }

        // 3) 시도 횟수 초기화
        redisTemplate.delete(attKey(e164));

        // 4) 6자리 인증번호 생성
        final String code = randomUtil.getRandomNumber(6);

        // 5) 인증번호 저장
        redisTemplate.opsForValue().set(codeKey(e164), code, Duration.ofSeconds(CODE_TTL));
    }
}
