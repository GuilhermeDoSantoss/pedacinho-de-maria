package com.pedacinhodemaria.modules.order.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TimerCalculatorTest {

    private static final int PREP_TIME_MINUTES = 30;

    @Test
    void ordersJustCreatedAreGreen() {
        Instant createdNow = Instant.now();
        assertThat(TimerCalculator.calculate(createdNow, PREP_TIME_MINUTES)).isEqualTo(TimerState.GREEN);
    }

    @Test
    void ordersInsideWarningWindowAreYellow() {
        // 25 min decorridos de um prazo de 30 → dentro dos últimos 10 min (janela de aviso)
        Instant createdAt = Instant.now().minus(25, ChronoUnit.MINUTES);
        assertThat(TimerCalculator.calculate(createdAt, PREP_TIME_MINUTES)).isEqualTo(TimerState.YELLOW);
    }

    @Test
    void ordersAtExactlyPrepTimeAreRed() {
        Instant createdAt = Instant.now().minus(PREP_TIME_MINUTES, ChronoUnit.MINUTES);
        assertThat(TimerCalculator.calculate(createdAt, PREP_TIME_MINUTES)).isEqualTo(TimerState.RED);
    }

    @Test
    void ordersWellPastPrepTimeAreRed() {
        Instant createdAt = Instant.now().minus(90, ChronoUnit.MINUTES);
        assertThat(TimerCalculator.calculate(createdAt, PREP_TIME_MINUTES)).isEqualTo(TimerState.RED);
    }

    @Test
    void shorterPrepTimeMovesTheYellowThresholdEarlier() {
        // Prato de 15 min: janela de aviso começa aos 5 min (15 - 10), não aos 20.
        Instant createdAt = Instant.now().minus(6, ChronoUnit.MINUTES);
        assertThat(TimerCalculator.calculate(createdAt, 15)).isEqualTo(TimerState.YELLOW);
    }
}
