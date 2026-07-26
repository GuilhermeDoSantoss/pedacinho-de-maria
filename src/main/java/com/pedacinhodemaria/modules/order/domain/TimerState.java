package com.pedacinhodemaria.modules.order.domain;

/**
 * Estado visual do timer de preparo de um pedido, exibido como cor no ticket
 * da cozinha (verde/amarelo/vermelho).
 *
 * IMPORTANTE: este valor nunca é persistido no MongoDB. Ele é sempre derivado
 * em tempo real a partir de `Order.createdAt` + `Order.mealPrepTimeMinutes`,
 * seja pela varredura periódica (OrderTimerService) ou ao montar a resposta
 * de uma consulta (OrderMapper). Isso garante que o estado esteja sempre
 * correto mesmo que o Kitchen Dashboard seja recarregado ou reiniciado —
 * não existe "cache desatualizado" possível porque nunca existiu um valor
 * salvo para desatualizar.
 */
public enum TimerState {
    GREEN,
    YELLOW,
    RED
}