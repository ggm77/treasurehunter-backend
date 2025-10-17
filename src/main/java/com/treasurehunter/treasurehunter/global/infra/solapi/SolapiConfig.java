package com.treasurehunter.treasurehunter.global.infra.solapi;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolapiConfig {

    @Value("${solapi.api.key}")
    private String API_KEY;

    @Value("${solapi.api.secret}")
    private String API_SECRET;

    @Value("${solapi.sender.number}")
    private String SENDER_NUMBER;

    @Bean
    public DefaultMessageService solapiMessageService() {
        return SolapiClient.INSTANCE.createInstance(API_KEY, API_SECRET);
    }

    public String getSenderNumber(){
        return SENDER_NUMBER;
    }
}
