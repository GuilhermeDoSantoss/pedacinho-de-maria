package com.pedacinhodemaria.modules.order.dto;

import java.math.BigDecimal;

public record OrderExtraResponse(String extraId, String extraName, BigDecimal extraPrice) {
}