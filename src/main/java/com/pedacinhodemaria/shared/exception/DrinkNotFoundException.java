package com.pedacinhodemaria.shared.exception;

import org.springframework.http.HttpStatus;

public class DrinkNotFoundException extends BusinessException {

    public DrinkNotFoundException(String identifier) {
        super("Bebida não encontrada: " + identifier);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}