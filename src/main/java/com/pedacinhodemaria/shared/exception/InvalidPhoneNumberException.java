package com.pedacinhodemaria.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção de regra de negócio para telefones inválidos ou ausentes em
 * cenários em que o pedido exige contato para retirada.
 */
public class InvalidPhoneNumberException extends BusinessException {

    public InvalidPhoneNumberException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
