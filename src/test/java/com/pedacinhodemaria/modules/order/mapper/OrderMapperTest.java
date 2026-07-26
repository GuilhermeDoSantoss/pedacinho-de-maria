package com.pedacinhodemaria.modules.order.mapper;

import com.pedacinhodemaria.modules.order.domain.Order;
import com.pedacinhodemaria.modules.order.domain.OrderExtraSnapshot;
import com.pedacinhodemaria.modules.order.domain.OrderStatus;
import com.pedacinhodemaria.modules.order.domain.OrderType;
import com.pedacinhodemaria.modules.order.domain.PaymentMethod;
import com.pedacinhodemaria.modules.order.domain.TimerState;
import com.pedacinhodemaria.modules.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * `Mappers.getMapper` instancia a implementação gerada pelo MapStruct
 * diretamente (sem contexto Spring) — é o teste mais direto possível de que
 * OrderMapperImpl.java compila e funciona corretamente, incluindo a
 * expressão Java que chama TimerCalculator (ver ADR em OrderMapper.java
 * sobre o `imports = TimerCalculator.class` necessário para isso compilar).
 */
class OrderMapperTest {

    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void mapsAllFieldsAndComputesTimerStateFromCreatedAt() {
        Order order = Order.builder()
                .orderCode("PM-ABCDE")
                .customerName("Maria Silva")
                .mealName("Feijoada")
                .mealPrice(new BigDecimal("28.90"))
                .mealPrepTimeMinutes(30)
                .sideDishName("Arroz")
                .sideDishPrice(BigDecimal.ZERO)
                .extras(List.of(new OrderExtraSnapshot("extra-1", "Ovo", new BigDecimal("2.00"))))
                .totalPrice(new BigDecimal("30.90"))
                .pickupTime(LocalTime.of(19, 30))
                .orderType(OrderType.DINE_IN)
                .needsDisposableCutlery(null)
                .paymentMethod(PaymentMethod.PIX)
                .status(OrderStatus.RECEIVED)
                .createdAt(Instant.now()) // recém-criado -> timerState deve ser GREEN
                .build();

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.orderCode()).isEqualTo("PM-ABCDE");
        assertThat(response.mealName()).isEqualTo("Feijoada");
        assertThat(response.sideDishName()).isEqualTo("Arroz");
        assertThat(response.totalPrice()).isEqualByComparingTo(new BigDecimal("30.90"));
        assertThat(response.extras()).hasSize(1);
        assertThat(response.extras().get(0).extraName()).isEqualTo("Ovo");
        // A parte que realmente prova a correção do bug: se o import de
        // TimerCalculator na classe gerada estivesse faltando, este mapper
        // nem teria compilado — chegar até aqui já é a prova. O valor abaixo
        // confirma que, além de compilar, o cálculo está correto.
        assertThat(response.timerState()).isEqualTo(TimerState.GREEN);
    }

    @Test
    void computesRedTimerStateWhenOrderIsPastPrepTime() {
        Order order = Order.builder()
                .orderCode("PM-LATE1")
                .customerName("João")
                .mealName("Lasanha")
                .mealPrice(new BigDecimal("32.00"))
                .mealPrepTimeMinutes(30)
                .sideDishName("Salada")
                .sideDishPrice(BigDecimal.ZERO)
                .extras(List.of())
                .totalPrice(new BigDecimal("32.00"))
                .pickupTime(LocalTime.of(20, 0))
                .orderType(OrderType.TAKEAWAY)
                .needsDisposableCutlery(true)
                .paymentMethod(PaymentMethod.CASH)
                .status(OrderStatus.PREPARING)
                .createdAt(Instant.now().minus(45, ChronoUnit.MINUTES)) // 45min > 30min de preparo
                .build();

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.timerState()).isEqualTo(TimerState.RED);
    }
}
