package com.pedacinhodemaria.modules.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Snapshot de uma Drink selecionada, mesmo padrão do OrderExtraSnapshot. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDrinkSnapshot {
    private String drinkId;
    private String drinkName;
    private BigDecimal drinkPrice;
}