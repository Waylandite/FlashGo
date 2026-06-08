package com.flashgo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flashGoOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("FlashGo API")
                .description("FlashGo V1 单体后端接口文档")
                .version("v1"));
    }
}
