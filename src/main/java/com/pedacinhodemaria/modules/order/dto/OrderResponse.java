package com.pedacinhodemaria.modules.order.dto;

import com.pedacinhodemaria.modules.order.domain.OrderStatus;
import com.pedacinhodemaria.modules.order.domain.OrderType;
import com.pedacinhodemaria.modules.order.domain.PaymentMethod;
import com.pedacinhodemaria.modules.order.domain.TimerState;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

/**
 * `timerState` é calculado no exato momento em que esta resposta é montada
 * (ver OrderMapper) — nunca lido de um campo persistido. Isso é o que garante
 * que o Kitchen Dashboard sempre veja o estado correto do timer mesmo depois
 * de um F5 ou reinício, sem depender de nenhum cálculo feito no navegador.
 */
public record OrderResponse(
        String orderCode,
        String customerName,
        String mealName,
        BigDecimal mealPrice,
        Integer mealPrepTimeMinutes,
        String sideDishName,
        BigDecimal sideDishPrice,
        List<OrderExtraResponse> extras,
        List<OrderDrinkResponse> drinks,
        BigDecimal totalPrice,
        LocalTime pickupTime,
        OrderType orderType,
        Boolean needsDisposableCutlery,
        PaymentMethod paymentMethod,
        String observation,
        String phoneNumber,
        OrderStatus status,
        TimerState timerState,
        Instant createdAt
) {
}