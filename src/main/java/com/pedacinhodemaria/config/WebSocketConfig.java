package com.pedacinhodemaria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração STOMP para comunicação em tempo real com a cozinha e o cliente.
 * Sem autenticação nesta versão (ver SecurityConfig) — qualquer cliente que
 * conecta em /ws pode assinar /topic/kitchen-orders. Aceitável para o
 * cenário de um único restaurante com dashboard interno; se o projeto
 * precisar de autenticação de WebSocket no futuro, a implementação com
 * handshake interceptor + channel interceptor está no histórico do projeto.
 *
 * Por que STOMP (e não um protocolo custom): Spring oferece upgrade nativo
 * de "simple broker" (memória, uma instância) para "broker relay" (RabbitMQ,
 * múltiplas instâncias) trocando só esta configuração.
 *
 * Dois endpoints expostos:
 *  - /ws        → WebSocket nativo puro, usado pelos frontends vanilla JS
 *                 com parser STOMP escrito à mão.
 *  - /ws-sockjs → mesmo broker, com fallback SockJS habilitado.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins);

        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}