package com.pedacinhodemaria.modules.order.dto;

import com.pedacinhodemaria.modules.order.domain.TimerState;

public record TimerStateChangedEvent(
        String orderCode,
        TimerState timerState,
        long elapsedMinutes
) {
}