package com.pedacinhodemaria.modules.order.dto;

import com.pedacinhodemaria.modules.order.domain.OrderStatus;

import java.time.Instant;

public record OrderStatusChangedEvent(
        String orderCode,
        OrderStatus newStatus,
        Instant updatedAt
) {
}