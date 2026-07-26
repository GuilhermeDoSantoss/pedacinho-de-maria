package com.pedacinhodemaria.modules.order.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.pedacinhodemaria.modules.order.domain.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa exaustivamente a máquina de estados — esta é a regra de negócio que,
 * se quebrada, permitiria a cozinha mover um pedido para um status inválido
 * (ex.: DELIVERED direto de RECEIVED, pulando o preparo).
 */
class OrderStatusTest {

    @ParameterizedTest(name = "{0} pode ir para {1}: {2}")
    @CsvSource({
            "RECEIVED, PREPARING, true",
            "RECEIVED, CANCELLED, true",
            "RECEIVED, READY, false",
            "RECEIVED, DELIVERED, false",
            "PREPARING, READY, true",
            "PREPARING, CANCELLED, true",
            "PREPARING, RECEIVED, false",
            "READY, DELIVERED, true",
            "READY, PREPARING, false",
            "READY, CANCELLED, false",
            "DELIVERED, RECEIVED, false",
            "CANCELLED, PREPARING, false",
    })
    void validatesAllowedTransitions(OrderStatus from, OrderStatus to, boolean expected) {
        assertThat(from.canTransitionTo(to)).isEqualTo(expected);
    }

    @Test
    void terminalStatesHaveNoOutgoingTransitions() {
        assertThat(DELIVERED.allowedNextStates()).isEmpty();
        assertThat(CANCELLED.allowedNextStates()).isEmpty();
    }

    @Test
    void activeStatesUsedByTimerSchedulerAreExactlyReceivedAndPreparing() {
        assertThat(OrderStatus.activeStates()).containsExactlyInAnyOrder(RECEIVED, PREPARING);
    }
}
