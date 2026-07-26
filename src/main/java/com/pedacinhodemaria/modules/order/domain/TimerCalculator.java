package com.pedacinhodemaria.modules.order.domain;

import java.time.Duration;
import java.time.Instant;

/**
 * Regra de negócio pura para determinar o estado visual do tempo de um pedido.
 *
 * Extraída como função estática, sem estado e sem dependências, porque é
 * chamada de dois lugares com necessidades diferentes (o scheduler que varre
 * pedidos ativos a cada 15s, e o mapper que monta a resposta HTTP sob demanda)
 * e não faz sentido duplicar a fórmula ou pior, deixar as duas chamadas
 * divergirem silenciosamente ao longo do tempo.
 *
 * Janela de alerta: os últimos 10 minutos antes do tempo estimado de preparo
 * viram "amarelo" (aviso antecipado), e o estouro do tempo estimado vira
 * "vermelho" (alerta sonoro + ticket piscando, tratado na camada de
 * apresentação do Kitchen Dashboard).
 */
public final class TimerCalculator {

    private static final int WARNING_WINDOW_MINUTES = 10;

    private TimerCalculator() {
    }

    public static TimerState calculate(Instant createdAt, int prepTimeMinutes) {
        long elapsedMinutes = Duration.between(createdAt, Instant.now()).toMinutes();

        if (elapsedMinutes >= prepTimeMinutes) {
            return TimerState.RED;
        }
        if (elapsedMinutes >= prepTimeMinutes - WARNING_WINDOW_MINUTES) {
            return TimerState.YELLOW;
        }
        return TimerState.GREEN;
    }

    public static long elapsedMinutes(Instant createdAt) {
        return Duration.between(createdAt, Instant.now()).toMinutes();
    }
}