package com.pedacinhodemaria.shared.exception;

import com.pedacinhodemaria.modules.order.domain.OrderStatus;
import org.springframework.http.HttpStatus;

/**
 * Lançada quando a cozinha tenta mover um pedido para um status que a
 * máquina de estados (OrderStatus.allowedNextStates) não permite a partir
 * do status atual — ex.: tentar marcar como DELIVERED um pedido que ainda
 * está em RECEIVED.
 */
public class InvalidOrderStateException extends BusinessException {

    public InvalidOrderStateException(OrderStatus current, OrderStatus attempted) {
        super("Não é possível mover o pedido de %s para %s".formatted(current, attempted));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}