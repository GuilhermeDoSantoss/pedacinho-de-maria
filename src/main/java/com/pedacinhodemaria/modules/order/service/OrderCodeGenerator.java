package com.pedacinhodemaria.modules.order.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Gera o código público do pedido (ex.: "PM-7X2K9").
 *
 * Usa SecureRandom, não Random comum, e não usa um contador incremental —
 * o orderCode funciona como capability token (é o que dá ao cliente acesso
 * de leitura ao próprio pedido sem login). Um código sequencial ou previsível
 * permitiria a qualquer pessoa varrer todos os pedidos do dia trocando um
 * número na URL. 5 caracteres em base36 (36^5 ≈ 60 milhões de combinações)
 * é mais que suficiente para o volume diário deste restaurante sem ficar
 * longo demais para o cliente ler em voz alta no balcão.
 */
@Component
public class OrderCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 5;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder("PM-");
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}