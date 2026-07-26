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
 * Segurança HTTP da aplicação. Decisão de produto (consolidação Fases 1+2):
 * SEM autenticação nesta versão — nem cliente nem cozinha fazem login. O
 * Kitchen Dashboard abre direto no board e conecta ao WebSocket sem token.
 *
 * Isso é uma reversão deliberada do JWT que existiu brevemente durante a
 * Fase 2 — removido por completo (não só desativado) porque código de
 * autenticação sem nenhum endpoint que o exija é exatamente o tipo de peso
 * morto que este projeto evita desde a Fase 1. Se autenticação voltar a ser
 * necessária no futuro (ex.: Admin Panel com ações destrutivas), a
 * implementação anterior está no histórico do projeto para ser retomada.
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
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/menu/**").permitAll()
                        .requestMatchers("/api/v1/side-dishes/**").permitAll()
                        .requestMatchers("/api/v1/extras/**").permitAll()
                        .requestMatchers("/api/v1/orders/**").permitAll()
                        .requestMatchers("/api/v1/kitchen/**").permitAll()
                        .requestMatchers("/ws/**", "/ws-sockjs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().denyAll()
                );

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}