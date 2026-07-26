package com.pedacinhodemaria.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidPickupTimeException extends BusinessException {

    public InvalidPickupTimeException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}