package com.pedacinhodemaria.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pedacinhoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Pedacinho de Maria — API")
                .description("Sistema de pedidos do restaurante.")
                .version("v1")
                .contact(new Contact().name("Guilherme")));
    }
}