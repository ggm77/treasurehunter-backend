package com.treasurehunter.treasurehunter.global.util;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ImageUtil {

    private static final byte[] JPEG = new byte[] {(byte)0xFF, (byte)0xD8, (byte)0xFF};
    private static final byte[] PNG8 = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    /**
     * 파일의 헤더 16바이트만 읽어서 올바른 파일인지 확인하는 메서드
     * jpeg, jpg, png만 지원
     * @param header 파일 헤더 16바이트
     * @param extension 확장자
     * @return 올바른지 여부
     */
    public boolean isValidSignature(final byte[] header, final String extension){
        try{
            return switch (extension.toLowerCase(Locale.ROOT)) {
                case "jpg", "jpeg" -> startsWith(header, JPEG);
                case "png" -> startsWith(header, PNG8);
                default -> false;
            };
        } catch (Exception ex) {
            return false;
        }
    }

    //두개의 바이트 비교
    private static boolean startsWith(final byte[] data, final byte[] prefix){
        if(data.length < prefix.length){
            return false;
        }

        for(int i = 0; i < prefix.length; i++){
            if(prefix[i] != data[i]){
                return false;
            }
        }

        return true;
    }
}
