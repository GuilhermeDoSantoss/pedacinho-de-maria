package com.pedacinhodemaria.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Registra explicitamente como as imagens do cardápio (pratos, bebidas etc.)
 * são servidas em /uploads/**.
 *
 * Tecnicamente, o Spring Boot já serve conteúdo de classpath:/static/**
 * automaticamente por convenção (WebMvcAutoConfiguration, mapeado em "/**"),
 * mesmo sem esta classe. Registrar aqui de forma explícita, mesmo assim, é
 * proposital: documenta exatamente onde as imagens vivem (em vez de depender
 * de alguém lembrar da convenção do Boot), permite configurar Cache-Control
 * apropriado para conteúdo estático (o handler automático não define
 * nenhum), e cria um ponto único de mudança se o armazenamento migrar no
 * futuro (ex.: para S3 — troca só a linha addResourceLocations).
 *
 * IMPORTANTE: isso só funciona se as imagens estiverem fisicamente em
 * src/main/resources/static/uploads/... no repositório — é esse caminho que
 * o Maven empacota automaticamente dentro do .jar (RUN mvn clean package no
 * Dockerfile já cobre isso, sem precisar de COPY adicional). Se as imagens
 * estiverem em outro lugar do projeto, ajuste addResourceLocations abaixo
 * para o caminho real antes de fazer deploy desta mudança.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/")
                // Cache de 7 dias: imagens de cardápio mudam raramente, e quando
                // mudam, normalmente é o Owner trocando a foto de um prato — se
                // isso acontecer, trocar o NOME do arquivo (não só o conteúdo)
                // evita servir a versão antiga em cache durante esses 7 dias.
                .setCachePeriod((int) TimeUnit.DAYS.toSeconds(7));
    }
}