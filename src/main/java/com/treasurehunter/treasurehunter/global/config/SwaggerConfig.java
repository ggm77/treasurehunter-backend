package com.treasurehunter.treasurehunter.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(securityRequirement())
                .components(components())
                .info(apiInfo());
    }

    public SecurityRequirement securityRequirement(){
        return new SecurityRequirement().addList("jwtAuth");
    }

    public Components components(){
        return new Components()
                .addSecuritySchemes("jwtAuth", new SecurityScheme()
                        .name("jwtAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }

    public Info apiInfo(){
        return new Info()
                .title("Treasure Hunter")
                .description("Treasure Hunter")
                .version("0.0.1");
    }
}
