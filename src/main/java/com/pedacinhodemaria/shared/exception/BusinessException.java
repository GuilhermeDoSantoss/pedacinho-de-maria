package com.pedacinhodemaria.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Base para todas as exceções que representam uma violação de regra de
 * negócio (não um erro de infraestrutura). Carrega o HttpStatus junto porque
 * cada subclasse sabe qual código HTTP faz sentido para o próprio caso —
 * o GlobalExceptionHandler só precisa perguntar, nunca decidir por switch/case.
 */
public abstract class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public abstract HttpStatus getHttpStatus();
}