package com.pedacinhodemaria.shared.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException(String identifier) {
        super("Pedido não encontrado: " + identifier);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}