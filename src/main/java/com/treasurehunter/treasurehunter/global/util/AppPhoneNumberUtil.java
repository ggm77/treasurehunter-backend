package com.treasurehunter.treasurehunter.global.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppPhoneNumberUtil {

    final PhoneNumberUtil phoneNumberUtil =  PhoneNumberUtil.getInstance();

    /**
     * 올바른 E.164로 되어있는지 검사
     * @param e164
     * @return boolean
     */
    public boolean isValidE164(final String e164){
        if(e164 == null || e164.isEmpty()){
            return false;
        }

        final Phonenumber.PhoneNumber number;
        final boolean isValid;
        try{
            number = phoneNumberUtil.parse(e164, null);
            isValid = phoneNumberUtil.isValidNumber(number);
        } catch (NumberParseException ex) {
            return false;
        }

        return isValid;
    }

    /**
     * E.164 형태로 들어온 값을 국내 전화 번호로 변환함
     * ex) +821012341234 -> 01012341234
     * @param e164
     * @return 국내 전화 번호
     */
    public String e164ToNational(final String e164){

        if(e164 == null || e164.isEmpty()){
            return "";
        }

        final Phonenumber.PhoneNumber number;

        try{
            number = phoneNumberUtil.parse(e164, null);
        } catch (NumberParseException ex) {
            return "";
        }

        final String national = phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

        return national.replace("-", "");
    }

    /**
     * E.164 형태로 들어온 값들을 일반화 함
     * ex) +8201012341234 -> +821012341234
     * @param e164
     * @return E.164
     */
    public String normalizeE164(final String e164){

        if(e164 == null || e164.isEmpty()){
            return "";
        }

        final Phonenumber.PhoneNumber number;
        try{
            number = phoneNumberUtil.parse(e164, null);
        } catch (NumberParseException ex) {
            return "";
        }

        return phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
    }
}
