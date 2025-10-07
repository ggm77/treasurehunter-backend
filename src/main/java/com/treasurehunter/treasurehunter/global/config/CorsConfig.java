package com.treasurehunter.treasurehunter.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer(){

        return new WebMvcConfigurer(){
            @Override
            public void addCorsMappings(final CorsRegistry corsRegistry){
                corsRegistry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000") //프론트 주소
                        .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
