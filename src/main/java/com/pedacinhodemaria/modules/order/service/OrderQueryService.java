package com.pedacinhodemaria.modules.order.service;

import com.pedacinhodemaria.modules.order.domain.Order;
import com.pedacinhodemaria.modules.order.domain.OrderStatus;
import com.pedacinhodemaria.modules.order.dto.OrderResponse;
import com.pedacinhodemaria.modules.order.repository.OrderRepository;
import com.pedacinhodemaria.modules.order.mapper.OrderMapper;
import com.pedacinhodemaria.shared.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Consultas de leitura, separadas dos use cases de escrita (CreateOrderUseCase,
 * UpdateOrderStatusUseCase) — nenhuma delas tem efeito colateral (não publica
 * evento, não muda estado), então não faz sentido misturá-las com as classes
 * que orquestram mutação + notificação.
 */
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    /** Usado pelo cliente para consultar o próprio pedido — orderCode funciona como capability token. */
    public OrderResponse getByOrderCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(orderCode));
        return orderMapper.toResponse(order);
    }

    /** Usado pelo Kitchen Dashboard ao carregar/recarregar — devolve timerState já calculado. */
    public List<OrderResponse> getActiveOrders() {
        return orderRepository.findByStatusInOrderByCreatedAtAsc(OrderStatus.kitchenBoardStates()).stream()
                .map(orderMapper::toResponse)
                .toList();
    }
}