package com.pedacinhodemaria.modules.order.service;

import com.pedacinhodemaria.shared.exception.InvalidPickupTimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;

/**
 * Centraliza a regra de negócio do horário de retirada dos pedidos.
 *
 * O restaurante atende somente entre 11:00 e 15:30, e um pedido nunca pode
 * ser criado com um horário de retirada já passado.
 */
@Component
@RequiredArgsConstructor
public class PickupTimePolicy {

    public static final LocalTime OPENING_TIME = LocalTime.of(11, 00);
    public static final LocalTime CLOSING_TIME = LocalTime.of(15, 30);

    private final Clock clock;

    public LocalTime getOpeningTime() {
        return OPENING_TIME;
    }

    public LocalTime getClosingTime() {
        return CLOSING_TIME;
    }

    public void validate(LocalTime pickupTime) {
        LocalTime currentTime = LocalTime.now(clock);

        if (pickupTime.isBefore(currentTime)) {
            throw new InvalidPickupTimeException("Horário de retirada não pode estar no passado");
        }

        if (pickupTime.isBefore(OPENING_TIME) || pickupTime.isAfter(CLOSING_TIME)) {
            throw new InvalidPickupTimeException("Horário de retirada deve estar entre 11:00 e 15:30");
        }
    }
}
