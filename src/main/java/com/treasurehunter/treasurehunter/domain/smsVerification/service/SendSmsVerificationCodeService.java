package com.treasurehunter.treasurehunter.domain.smsVerification.service;

import com.treasurehunter.treasurehunter.domain.smsVerification.dto.SendSmsVerificationCodeRequestDto;
import com.treasurehunter.treasurehunter.domain.user.entity.Role;
import com.treasurehunter.treasurehunter.domain.user.entity.User;
import com.treasurehunter.treasurehunter.domain.user.repository.UserRepository;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import com.treasurehunter.treasurehunter.global.infra.solapi.SolapiSmsSender;
import com.treasurehunter.treasurehunter.global.util.AppPhoneNumberUtil;
import com.treasurehunter.treasurehunter.global.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SendSmsVerificationCodeService {

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
    private final UserRepository userRepository;
    private final RandomUtil randomUtil;
    private final AppPhoneNumberUtil appPhoneNumberUtil;
    private final SolapiSmsSender solapiSmsSender;

    public void createVerificationCode(
            final SendSmsVerificationCodeRequestDto sendSmsVerificationCodeRequestDto,
            final Long userId
    ) {

        final String e164 = appPhoneNumberUtil.normalizeE164(sendSmsVerificationCodeRequestDto.getPhoneNumber());

        // 1) 전화번호 유효성 확인
        if(!appPhoneNumberUtil.isValidE164(e164)){
            throw new CustomException(ExceptionCode.INVALID_REQUEST);
        }

        // 2) 본인 인증 해야하는지 확인
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));
        //유저 role이 NOT_VERIFIED나 USER가 아닌 경우에는 에러
        if( !(user.getRole() == Role.NOT_VERIFIED || user.getRole() == Role.USER) ){
            throw new CustomException(ExceptionCode.PERMISSION_DENIED);
        }

        // 3) 쿨다운
        final Boolean ok = redisTemplate.opsForValue().setIfAbsent(
                cdKey(e164),
                "1",
                Duration.ofSeconds(COOL_TTL)
        );

        //쿨다운에 걸렸다면
        if(Boolean.FALSE.equals(ok)){
            throw new CustomException(ExceptionCode.COOL_DOWN);
        }

        // 4) 일일 한도
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

        // 5) 시도 횟수 초기화
        redisTemplate.delete(attKey(e164));

        // 6) 6자리 인증번호 생성
        final String code = randomUtil.getRandomNumber(6);

        // 7) 인증번호 저장
        redisTemplate.opsForValue().set(codeKey(e164), code, Duration.ofSeconds(CODE_TTL));

        // 8) 인증번호 SMS로 전송
        solapiSmsSender.sendSmsVerificationCode(appPhoneNumberUtil.e164ToNational(e164), code);
    }
}
