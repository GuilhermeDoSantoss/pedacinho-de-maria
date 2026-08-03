package com.pedacinhodemaria.modules.order.service;

/**
 * Porta (interface) para envio de mensagens de WhatsApp — mesmo padrão de
 * abstração já usado em OrderEventPublisher para o WebSocket: o use case que
 * dispara a mensagem depende só desta interface, nunca de um provedor
 * concreto (WhatsApp Cloud API, Twilio, Z-API...). A implementação real vive
 * na camada de infraestrutura (ver WhatsAppCloudApiMessageSender) e é
 * injetada aqui pelo Spring. Trocar de provedor no futuro significa escrever
 * uma nova classe que implementa esta interface — nenhum use case muda.
 */
public interface WhatsAppMessageSender {
    void sendMessage(String phoneNumber, String message);
}