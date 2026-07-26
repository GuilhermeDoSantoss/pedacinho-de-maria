package com.pedacinhodemaria.shared.exception;

import org.springframework.http.HttpStatus;

public class ExtraNotFoundException extends BusinessException {

    public ExtraNotFoundException(String identifier) {
        super("Extra não encontrado ou indisponível: " + identifier);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}