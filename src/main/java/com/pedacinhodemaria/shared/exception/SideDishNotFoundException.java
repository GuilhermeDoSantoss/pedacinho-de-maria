package com.pedacinhodemaria.shared.exception;

import org.springframework.http.HttpStatus;

public class SideDishNotFoundException extends BusinessException {

    public SideDishNotFoundException(String identifier) {
        super("Acompanhamento não encontrado ou indisponível: " + identifier);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}