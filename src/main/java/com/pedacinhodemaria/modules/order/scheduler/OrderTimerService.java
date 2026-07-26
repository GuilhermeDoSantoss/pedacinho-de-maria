package com.pedacinhodemaria.modules.order.scheduler;

import com.pedacinhodemaria.modules.order.domain.Order;
import com.pedacinhodemaria.modules.order.domain.OrderStatus;
import com.pedacinhodemaria.modules.order.domain.TimerCalculator;
import com.pedacinhodemaria.modules.order.domain.TimerState;
import com.pedacinhodemaria.modules.order.dto.TimerStateChangedEvent;
import com.pedacinhodemaria.modules.order.repository.OrderRepository;
import com.pedacinhodemaria.modules.order.websocket.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fonte de verdade dos timers de preparo — roda inteiramente no backend, nunca
 * no navegador. Esse requisito não é estético: se o cálculo de "quanto tempo
 * falta" dependesse de um setInterval no Kitchen Dashboard, um F5 ou uma queda
 * de conexão faria o funcionário perder a noção de quais pedidos estão
 * atrasados até a página recarregar e recalcular do zero.
 *
 * Mecanismo: varre pedidos ativos (RECEIVED/PREPARING) a cada 15s, calcula o
 * TimerState de cada um (ver TimerCalculator) e só publica um evento
 * WebSocket quando o estado muda em relação à última varredura — evita
 * inundar o dashboard com um evento a cada 15s para pedidos que continuam
 * verdes, e mantém o tráfego de rede proporcional a mudanças reais.
 *
 * O cache em memória (`lastKnownState`) é aceitável para uma única instância
 * de backend. Se este projeto crescer para múltiplas instâncias atrás de um
 * load balancer, esse cache precisaria migrar para um armazenamento
 * compartilhado (Redis) — registrado aqui como limite conhecido, não como
 * problema a resolver agora (volume atual não justifica).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTimerService {

    private static final Set<OrderStatus> TRACKED_STATUSES = OrderStatus.activeStates();

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    private final Map<String, TimerState> lastKnownState = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 15_000)
    public void checkOrderTimers() {
        var activeOrders = orderRepository.findByStatusIn(TRACKED_STATUSES);
        var activeOrderIds = activeOrders.stream().map(Order::getId).collect(java.util.stream.Collectors.toSet());

        // Remove do cache pedidos que saíram dos estados ativos (foram para READY,
        // DELIVERED ou CANCELLED) — sem isso o Map cresceria indefinidamente ao
        // longo do dia, um vazamento de memória lento mas real.
        lastKnownState.keySet().retainAll(activeOrderIds);

        for (Order order : activeOrders) {
            TimerState currentState = TimerCalculator.calculate(order.getCreatedAt(), order.getMealPrepTimeMinutes());
            TimerState previousState = lastKnownState.get(order.getId());

            if (currentState != previousState) {
                lastKnownState.put(order.getId(), currentState);
                long elapsed = TimerCalculator.elapsedMinutes(order.getCreatedAt());

                if (currentState == TimerState.RED && previousState != TimerState.RED) {
                    log.warn("Pedido {} ultrapassou o tempo estimado de preparo ({} min)",
                            order.getOrderCode(), elapsed);
                }

                eventPublisher.publishTimerStateChanged(
                        new TimerStateChangedEvent(order.getOrderCode(), currentState, elapsed));
            }
        }
    }
}