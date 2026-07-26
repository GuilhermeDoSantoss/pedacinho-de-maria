package com.pedacinhodemaria.modules.order.domain;

/**
 * Forma de pagamento escolhida pelo cliente. O sistema NÃO processa pagamento
 * algum, isso é apenas um registro informativo exibido no ticket da cozinha
 * (ex.: para o caixa saber se precisa gerar troco).
 */
public enum PaymentMethod {
    CASH,
    PIX,
    CREDIT_CARD,
    DEBIT_CARD
}