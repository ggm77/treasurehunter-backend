package com.treasurehunter.treasurehunter.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    /**
     * 객체를 JSON 문자열로 변환하는 메서드
     * @param object 직렬화할 객체
     * @return JSON 문자열, 실패시 ""
     */
    public String toJson(final Object object){

        if(object == null){
            return "";
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex){
            return "";
        }
    }

    /**
     * 문자열로 된 JSON을 Map으로 변환하는 메서드
     * @param str 변환할 문자열로 된 JSON
     * @return Map으로 변환된 JSON
     */
    public Map<String, Object> toMap(final String str){
        if(str == null ||  str.isEmpty()){
            return Collections.emptyMap();
        }

        try{
            return objectMapper.readValue(str, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * escape 제거가 제대로 되어있지 않은 경우를 처리하기 위한 메서드
     * @param str 처리할 문자열
     * @return 처리된 문자열
     */
    public String removeEscapes(String str){
        if(str == null || str.isEmpty()){
            return str;
        }

        try {
            return objectMapper.readValue("\"" + str + "\"", String.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
