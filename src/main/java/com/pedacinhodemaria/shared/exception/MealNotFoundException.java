package com.pedacinhodemaria.shared.exception;

import org.springframework.http.HttpStatus;

public class MealNotFoundException extends BusinessException {

    public MealNotFoundException(String mealId) {
        super("Prato não encontrado ou indisponível: " + mealId);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}