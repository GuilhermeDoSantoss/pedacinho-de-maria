package com.pedacinhodemaria.modules.menu.dto;

import java.math.BigDecimal;

public record DrinkResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl
) {
}