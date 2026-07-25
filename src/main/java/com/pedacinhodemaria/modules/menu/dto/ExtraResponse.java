package com.pedacinhodemaria.modules.menu.dto;

import java.math.BigDecimal;

public record ExtraResponse(
        String id,
        String name,
        BigDecimal price
) {

}