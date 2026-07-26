package com.pedacinhodemaria.modules.order.websocket;

import com.pedacinhodemaria.modules.order.dto.OrderResponse;
import com.pedacinhodemaria.modules.order.dto.OrderStatusChangedEvent;
import com.pedacinhodemaria.modules.order.dto.TimerStateChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Ponto único de publicação de eventos em tempo real. Os use cases dependem
 * desta classe, nunca de SimpMessagingTemplate diretamente — se um dia o
 * broker simples em memória for trocado por um broker relay externo
 * (RabbitMQ, necessário para múltiplas instâncias de backend atrás de um
 * load balancer), a mudança fica isolada na configuração do WebSocket
 * (WebSocketConfig) e aqui dentro; nenhum use case precisa saber que isso
 * aconteceu.
 *
 * Tópicos:
 *  - /topic/kitchen-orders           → todo evento relevante para a cozinha
 *  - /topic/order-status/{orderCode} → só o status do próprio pedido, para o cliente
 */
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private static final String KITCHEN_TOPIC = "/topic/kitchen-orders";
    private static final String CUSTOMER_TOPIC_PREFIX = "/topic/order-status/";

    private final SimpMessagingTemplate messagingTemplate;

    public void publishOrderCreated(OrderResponse order) {
        messagingTemplate.convertAndSend(KITCHEN_TOPIC, new OrderCreatedMessage(order));
    }

    public void publishStatusChanged(OrderStatusChangedEvent event) {
        messagingTemplate.convertAndSend(KITCHEN_TOPIC, event);
        messagingTemplate.convertAndSend(CUSTOMER_TOPIC_PREFIX + event.orderCode(), event);
    }

    public void publishTimerStateChanged(TimerStateChangedEvent event) {
        messagingTemplate.convertAndSend(KITCHEN_TOPIC, event);
    }

    /** Envelope simples só para diferenciar o tipo de evento no payload do tópico da cozinha. */
    public record OrderCreatedMessage(OrderResponse order) {
    }
}