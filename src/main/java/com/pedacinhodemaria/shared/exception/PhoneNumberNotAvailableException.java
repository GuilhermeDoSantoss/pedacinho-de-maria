package com.pedacinhodemaria.shared.exception;

/**
 * Lançada ao tentar enviar uma mensagem de WhatsApp para um pedido que não
 * tem telefone cadastrado (ex.: pedido DINE_IN, onde phoneNumber é sempre
 * null por regra de negócio — ver Order.needsDisposableCutlery para o mesmo
 * raciocínio de campo condicional). Mesmo padrão de exceção de domínio já
 * usado em OrderNotFoundException/InvalidPhoneNumberException — ajuste o
 * mapeamento para HTTP no seu @ControllerAdvice existente do mesmo jeito que
 * as outras exceções deste pacote já são tratadas.
 */
public class PhoneNumberNotAvailableException extends RuntimeException {
    public PhoneNumberNotAvailableException(String orderCode) {
        super("Pedido " + orderCode + " não possui telefone de contato cadastrado");
    }
}