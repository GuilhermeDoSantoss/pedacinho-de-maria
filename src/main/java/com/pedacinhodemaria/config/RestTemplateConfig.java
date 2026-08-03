package com.pedacinhodemaria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate usado hoje só pelo WhatsAppCloudApiMessageSender. Se o
 * projeto já tiver um bean de RestTemplate/WebClient em outro lugar,
 * remova esta classe e reaproveite o existente — o adapter só depende do
 * tipo RestTemplate injetado, não desta configuração especificamente.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}