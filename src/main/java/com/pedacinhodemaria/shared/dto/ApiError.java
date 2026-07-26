package com.pedacinhodemaria.shared.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Formato único de erro devolvido por toda a API. Existir um formato único
 * evita que cada endpoint invente sua própria estrutura de erro — o frontend
 * (vanilla JS) trata qualquer resposta de erro da mesma forma, em um só lugar.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<Map<String, String>> fieldErrors
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    public static ApiError ofValidation(int status, String path, List<Map<String, String>> fieldErrors) {
        return new ApiError(Instant.now(), status, "Validation Failed",
                "Um ou mais campos são inválidos", path, fieldErrors);
    }
}