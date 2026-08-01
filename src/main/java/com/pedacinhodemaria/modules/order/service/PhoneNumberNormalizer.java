package com.pedacinhodemaria.modules.order.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Normaliza telefones para o formato canônico de apenas dígitos.
 * Mantém a regra em um utilitário reutilizável para evitar duplicação e
 * preparar o projeto para futuras integrações com WhatsApp/contato.
 */
@Component
public class PhoneNumberNormalizer {

    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("\\D");
    private static final int MIN_DIGITS = 10;
    private static final int MAX_DIGITS = 11;

    public String normalize(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        String digitsOnly = NON_DIGIT_PATTERN.matcher(phoneNumber).replaceAll("");
        return digitsOnly.isBlank() ? null : digitsOnly;
    }

    public String normalizeAndValidate(String phoneNumber, boolean required) {
        String normalized = normalize(phoneNumber);

        if (!required) {
            return normalized;
        }

        if (normalized == null) {
            throw new IllegalArgumentException("O telefone é obrigatório para pedidos para viagem.");
        }

        if (normalized.length() < MIN_DIGITS || normalized.length() > MAX_DIGITS) {
            throw new IllegalArgumentException("Telefone inválido. Informe um número entre 10 e 11 dígitos.");
        }

        return normalized;
    }
}
