package com.pedacinhodemaria.modules.order.service;

import com.pedacinhodemaria.modules.order.domain.Order;
import com.pedacinhodemaria.modules.order.repository.OrderRepository;
import com.pedacinhodemaria.shared.exception.OrderNotFoundException;
import com.pedacinhodemaria.shared.exception.PhoneNumberNotAvailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Dispara manualmente, via WhatsApp, o aviso de "pedido pronto" — ação
 * explícita de clique no telefone do cliente no Dashboard, independente do
 * status atual do pedido. Isso é INDEPENDENTE do evento automático de status
 * já existente (ORDER_STATUS_CHANGED via STOMP em /topic/order-status/{code},
 * ver OrderEventPublisher e confirmationView.js) — uma notificação acontece
 * na tela do cliente em tempo real, a outra é uma mensagem direta enviada
 * para o celular dele; as duas continuam existindo lado a lado.
 *
 * Camada: application (use case), mesmo nível de CreateOrderUseCase — não
 * conhece o provedor de WhatsApp por trás de WhatsAppMessageSender.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SendOrderReadyWhatsAppMessageUseCase {

    private static final String READY_MESSAGE = """
            Olá! \uD83D\uDE0A
            Seu pedido no Pedacinhos de Maria está pronto para retirada.
            Agradecemos a preferência!""";

    private final OrderRepository orderRepository;
    private final WhatsAppMessageSender whatsAppMessageSender;

    public void execute(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(orderCode));

        if (order.getPhoneNumber() == null || order.getPhoneNumber().isBlank()) {
            throw new PhoneNumberNotAvailableException(orderCode);
        }

        whatsAppMessageSender.sendMessage(order.getPhoneNumber(), READY_MESSAGE);
        log.info("Mensagem de WhatsApp de pedido pronto enviada para o pedido {}", orderCode);
    }
}
