package com.pedacinhodemaria.modules.order.service;

import com.pedacinhodemaria.modules.order.domain.Order;
import com.pedacinhodemaria.modules.order.domain.OrderStatus;
import com.pedacinhodemaria.modules.order.dto.OrderResponse;
import com.pedacinhodemaria.modules.order.dto.OrderStatusChangedEvent;
import com.pedacinhodemaria.modules.order.repository.OrderRepository;
import com.pedacinhodemaria.modules.order.mapper.OrderMapper;
import com.pedacinhodemaria.modules.order.websocket.OrderEventPublisher;
import com.pedacinhodemaria.shared.exception.InvalidOrderStateException;
import com.pedacinhodemaria.shared.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Move um pedido entre colunas do Kitchen Dashboard.
 *
 * A validação de transição vive em OrderStatus.canTransitionTo, não aqui —
 * este use case só orquestra: busca, valida, persiste, publica evento.
 * Isso mantém a regra de negócio (quais transições existem) no domínio,
 * onde qualquer novo desenvolvedor vai procurá-la primeiro.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher eventPublisher;

    /**
     * Busca por orderCode, não pelo id interno do Mongo — OrderResponse
     * (o que o Kitchen Dashboard recebe) nunca expõe o id interno, só o
     * orderCode. Usar o id aqui exigiria vazar um detalhe de implementação
     * do banco para o frontend só para permitir esta chamada.
     *
     * @throws OrderNotFoundException se o orderCode não corresponder a nenhum pedido
     * @throws InvalidOrderStateException se a transição não for permitida pela máquina de estados
     */
    public OrderResponse execute(String orderCode, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(orderCode));

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(order.getStatus(), newStatus);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());
        Order saved = orderRepository.save(order);

        log.info("Pedido {} mudou de status para {}", saved.getOrderCode(), newStatus);

        eventPublisher.publishStatusChanged(
                new OrderStatusChangedEvent(saved.getOrderCode(), newStatus, saved.getUpdatedAt()));

        return orderMapper.toResponse(saved);
    }
}