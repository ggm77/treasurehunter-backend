package com.treasurehunter.treasurehunter.global.infra.solapi;

import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import com.treasurehunter.treasurehunter.global.exception.CustomException;
import com.treasurehunter.treasurehunter.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolapiSmsSender {

    private final DefaultMessageService messageService;
    private final SolapiConfig solapiConfig;

    /**
     * sms 인증을 위한 문자 발송하는 메서드
     * SOLAPI의 API를 사용함
     * @param phoneNumber 문자를 전송할 전화번호 ex) 01012341234
     * @param code 전송할 인증번호
     */
    public void sendSmsVerificationCode(
            final String phoneNumber,
            final String code
    ){
        final Message message = new Message();
        message.setFrom(solapiConfig.getSenderNumber());
        message.setTo(phoneNumber);
        message.setText("[TreasureHunter] 인증번호 [" + code + "] 타인 유출로 인한 피해 주의\n\n@treasurehunter.seohamin.com #"+code);

        try{
            messageService.send(message);
        } catch(SolapiMessageNotReceivedException ex){
            throw new CustomException(ExceptionCode.SMS_SEND_FAILED);
        } catch(Exception ex){
            throw new CustomException(ExceptionCode.SMS_SEND_FAILED);
        }
    }
}
