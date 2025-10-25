package com.treasurehunter.treasurehunter.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class FileNameUtil {

    @Value("${file.name.secret.key}")
    private String SECRET_KEY;

    @Value("${server.base-url}")
    private String SERVER_BASE_URL;

    @Value("${server.port}")
    private String SERVER_PORT;

    @Value("${server.isHttps}")
    private boolean isHttps;

    /**
     * 원본 파일명을 샤딩된 path로 바꿔주는 메서드
     * 원본 이름 넣으면 UUID와 비밀 키를 합쳐서 해시로 만든 뒤 샤딩함
     * ex) hello.txt -> ab/cd/abcd...qwer.txt
     * @param originalFileName 원본 파일명
     * @return 해시로 만들어진 파일 이름
     */
    public String generateShardedPath(final String originalFileName, final String extension){

        // 1) 원본 파일명 검사
        if(originalFileName == null || originalFileName.isEmpty()){
            throw new IllegalArgumentException("originalFileName이 비어있습니다.");
        }

        // 2) 확장자 검사
        if(extension == null || extension.isEmpty()){
            throw new IllegalArgumentException("extension이 비어있습니다.");
        }

        // 3) UUID + 파일명 + 비밀 키를 통해 해싱
        final String uuid = UUID.randomUUID().toString();
        final String base = uuid + ":" + originalFileName;

        final String hashedName;
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            hashedName = HexFormat.of().formatHex(mac.doFinal(base.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("파일 이름 생성 실패");
        }

        // 4) 샤딩
        final String shard = hashedName.substring(0,2) + "/" + hashedName.substring(2,4);

        return shard + "/" + hashedName + "." + extension;
    }

    /**
     * 파일 objectKey를 넣으면 URL로 바꿔주는 메서드
     * 서버 base url, 포트 번호 등을 자동으로 붙임
     * @param objectKey 파일 objectKey
     * @return 파일에 접근할 수 있는 URL
     */
    public String buildImageUrl(final String objectKey){

        //https일 경우 포트 번호 필요 없음
        if(isHttps){
            return SERVER_BASE_URL + "/api/v1/file/image?objectKey=" + objectKey;
        } else {
            return SERVER_BASE_URL + ":" + SERVER_PORT + "/api/v1/file/image?objectKey=" + objectKey;
        }
    }

    /**
     * 파일명이 올바른지 확인하는 메서드
     * \ / : * ? " < > | 9가지 기호가 들어가있거나 제어문자, 보이지 않는 문자를 검사함
     * 점(".")과 공백은 검사하지 않음
     * 확장자는 검사하지 않음
     * @param fileName 확장자를 포함한 파일명
     * @return 올바른지 아닌지 여부
     */
    public boolean isValidFileName(final String fileName){
        final String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFC);
        // \ / : * ? " < > | 9가지 기호와 제어문자, 보이지 않는 문자 방지
        return !normalized.matches(".*([\\\\/:*?\"<>|]|\\p{Cntrl}|[\\u200B-\\u200D\\uFEFF]).*");
    }
}
