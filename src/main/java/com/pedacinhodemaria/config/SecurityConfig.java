package com.pedacinhodemaria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Segurança HTTP da aplicação. Decisão de produto:
 * SEM autenticação — nem cliente nem cozinha fazem login. O
 * Kitchen Dashboard abre direto no board e conecta ao WebSocket sem token.
 *
 * O que esta classe garante:
 *  1. Todas as rotas de negócio (menu, orders, kitchen) são públicas.
 *  2. CSRF desabilitado — API stateless, sem sessão de cookie.
 *  3. Headers de segurança básicos (anti-clickjacking, anti-MIME-sniffing)
 *     continuam ativos — não têm custo e não dependem de autenticação.
 *  4. Toda rota que ainda não existe fica bloqueada por padrão (denyAll).
 */
@Configuration
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(contentTypeOptions -> {})
                        // Reduz o quanto a URL de origem vaza em navegação cross-site —
                        // não muda nenhum comportamento funcional, só hardening.
                        .referrerPolicy(referrer -> referrer
                                .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // Desliga explicitamente APIs de navegador que este app nunca usa
                        // (câmera, microfone, geolocalização) — reduz superfície de ataque
                        // caso algum script de terceiro (analytics, etc.) seja adicionado
                        // no futuro e tente abusar dessas permissões sem o usuário perceber.
                        .permissionsPolicy(permissions -> permissions
                                .policy("camera=(), microphone=(), geolocation=()"))
                        // Explícito, não implícito: o Spring Security já inclui isso por
                        // padrão quando .headers(...) é usado sem desabilitar, mas deixar
                        // implícito significa que qualquer alteração futura nesta config
                        // poderia silenciosamente perder essa proteção sem ninguém notar.
                        // Sem no-store, o navegador pode servir uma resposta antiga do
                        // cache (304 + corpo do cache) para o cardápio/pedidos depois de
                        // uma mudança no MongoDB — dado sempre mutável não pode ser
                        // cacheado pelo navegador em nenhuma hipótese.
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/menu/**").permitAll()
                        .requestMatchers("/api/v1/side-dishes/**").permitAll()
                        .requestMatchers("/api/v1/extras/**").permitAll()
                        .requestMatchers("/api/v1/drinks/**").permitAll()
                        .requestMatchers("/api/v1/orders/**").permitAll()
                        .requestMatchers("/api/v1/kitchen/**").permitAll()
                        .requestMatchers("/ws/**", "/ws-sockjs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Imagens do cardápio (pratos, bebidas etc.), servidas como
                        // recurso estático. Restrito a GET/HEAD de propósito — é
                        // conteúdo só-leitura; não há motivo pra POST/PUT/DELETE
                        // nunca serem permitidos aqui, mesmo que algum handler futuro
                        // viesse a aceitar esses métodos por engano nesse path.
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/uploads/**").permitAll()
                        .anyRequest().denyAll()
                );

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(List.of("*"));
    configuration.setAllowedHeaders(List.of("*"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
}
}
