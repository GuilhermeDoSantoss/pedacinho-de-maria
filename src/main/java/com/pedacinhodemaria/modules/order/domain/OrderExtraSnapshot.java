package com.pedacinhodemaria.modules.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Snapshot de um Extra selecionado, embutido dentro do documento Order —
 * mesma razão do snapshot de mealPrice/mealPrepTimeMinutes em Order: se o
 * Owner mudar o preço de um extra depois, pedidos já feitos não podem ser
 * afetados retroativamente. Não é uma referência (extraId sozinho seria
 * insuficiente), é uma cópia completa do que valia no momento do pedido.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderExtraSnapshot {
    private String extraId;
    private String extraName;
    private BigDecimal extraPrice;
}