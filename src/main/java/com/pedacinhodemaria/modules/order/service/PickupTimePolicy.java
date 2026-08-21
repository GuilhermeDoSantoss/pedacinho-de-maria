package com.pedacinhodemaria.modules.order.service;

import com.pedacinhodemaria.shared.exception.InvalidPickupTimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;

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

    /**
     * OPENING_TIME/CLOSING_TIME são horário de Brasília — é assim que o
     * negócio pensa a janela de atendimento. `clock` é injetado sem
     * garantia de estar nessa zona (em produção, geralmente é UTC); usar
     * `LocalTime.now(clock)` direto lia "agora" em UTC e comparava contra
     * uma janela pensada em horário de Brasília, rejeitando como "no
     * passado" praticamente qualquer pickupTime dentro do próprio horário
     * de funcionamento (UTC está 3h à frente de Brasília, então "agora"
     * sempre parecia mais tarde do que realmente era).
     *
     * `clock.withZone(...)` preserva o instante exato do Clock injetado
     * (inclusive em testes com Clock.fixed) e só troca em qual zona esse
     * instante é lido como LocalTime — corrige o bug sem depender de saber
     * ou alterar como o bean Clock é configurado em outro lugar do projeto.
     */
    private static final ZoneId RESTAURANT_ZONE = ZoneId.of("America/Sao_Paulo");

    private final Clock clock;

    public LocalTime getOpeningTime() {
        return OPENING_TIME;
    }

    public LocalTime getClosingTime() {
        return CLOSING_TIME;
    }

    public void validate(LocalTime pickupTime) {
        LocalTime currentTime = LocalTime.now(clock.withZone(RESTAURANT_ZONE));

        if (pickupTime.isBefore(currentTime)) {
            throw new InvalidPickupTimeException("Horário de retirada não pode estar no passado");
        }

        if (pickupTime.isBefore(OPENING_TIME) || pickupTime.isAfter(CLOSING_TIME)) {
            throw new InvalidPickupTimeException("Horário de retirada deve estar entre 11:00 e 15:30");
        }
    }
}