package com.treasurehunter.treasurehunter.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
