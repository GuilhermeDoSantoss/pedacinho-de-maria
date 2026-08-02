package com.pedacinhodemaria.modules.menu.dto;

import java.util.List;

/**
 * Resposta unificada do cardápio público, contendo pratos e bebidas ativas.
 * Mantém o contrato do frontend simples e evita depender de múltiplos endpoints
 * para montar a tela do cliente.
 */
public record MenuResponse(
        List<MealResponse> meals,
        List<DrinkResponse> drinks
) {
}
