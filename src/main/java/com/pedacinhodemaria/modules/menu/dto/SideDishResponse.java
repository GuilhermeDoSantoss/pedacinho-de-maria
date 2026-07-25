package com.pedacinhodemaria.modules.menu.dto;

import java.math.BigDecimal;

public record SideDishResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl
) {
}