package com.pedacinhodemaria.modules.order.dto;

import java.math.BigDecimal;

public record OrderDrinkResponse(String drinkId, String drinkName, BigDecimal drinkPrice) {
}