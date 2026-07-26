package com.pedacinhodemaria.modules.order.dto;

import com.pedacinhodemaria.modules.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Novo status é obrigatório")
        OrderStatus newStatus
) {
}