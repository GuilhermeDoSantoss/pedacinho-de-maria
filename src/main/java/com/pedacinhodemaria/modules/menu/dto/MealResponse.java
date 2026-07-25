package com.pedacinhodemaria.modules.menu.dto;

import com.pedacinhodemaria.modules.menu.domain.MealType;

import java.math.BigDecimal;

public record MealResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        Integer estimatedPrepTimeMinutes,
        MealType type,
        Integer displayOrder
) {
}