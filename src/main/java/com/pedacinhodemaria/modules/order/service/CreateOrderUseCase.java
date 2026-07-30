package com.pedacinhodemaria.modules.order.service;

import com.pedacinhodemaria.modules.menu.domain.Extra;
import com.pedacinhodemaria.modules.menu.domain.SideDish;
import com.pedacinhodemaria.modules.menu.service.ExtraService;
import com.pedacinhodemaria.modules.menu.service.MenuService;
import com.pedacinhodemaria.modules.menu.domain.Meal;
import com.pedacinhodemaria.modules.menu.service.SideDishService;
import com.pedacinhodemaria.modules.order.domain.Order;
import com.pedacinhodemaria.modules.order.domain.OrderExtraSnapshot;
import com.pedacinhodemaria.modules.order.domain.OrderStatus;
import com.pedacinhodemaria.modules.order.domain.OrderType;
import com.pedacinhodemaria.modules.order.dto.CreateOrderRequest;
import com.pedacinhodemaria.modules.order.dto.OrderResponse;
import com.pedacinhodemaria.modules.order.repository.OrderRepository;
import com.pedacinhodemaria.modules.order.mapper.OrderMapper;
import com.pedacinhodemaria.modules.order.websocket.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Orquestra a criação de um pedido: valida, resolve prato + acompanhamento +
 * extras, calcula o total, persiste, e dispara o efeito colateral mais
 * importante do sistema — notificar a cozinha em tempo real.
 *
 * Camada: application (use case). Não conhece detalhes de MongoDB nem de
 * STOMP — depende de abstrações (OrderRepository, OrderEventPublisher)
 * injetadas, o que permite trocar a implementação de qualquer uma das duas
 * sem alterar esta classe.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderUseCase {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 5;

    private final MenuService menuService;
    private final SideDishService sideDishService;
    private final ExtraService extraService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher eventPublisher;
    private final OrderCodeGenerator codeGenerator;
    private final PickupTimePolicy pickupTimePolicy;

    /**
     * Cria um pedido e notifica a cozinha em tempo real.
     *
     * Regra de negócio: preço/nome do prato, acompanhamento e cada extra são
     * copiados (snapshot) para o pedido no momento da criação — se o Owner
     * mudar o cardápio depois, pedidos já em andamento não são afetados.
     *
     * O total (`totalPrice`) é sempre calculado aqui, no backend — nunca
     * confiado a um valor vindo do frontend. O Customer App mostra um total
     * estimado só para UX; este método é a única fonte de verdade sobre
     * quanto o pedido de fato custa.
     *
     * @throws com.pedacinhodemaria.shared.exception.MealNotFoundException se o mealId não existir ou não estiver ativo
     * @throws com.pedacinhodemaria.shared.exception.SideDishNotFoundException se o sideDishId não existir ou não estiver ativo
     * @throws com.pedacinhodemaria.shared.exception.ExtraNotFoundException se algum extraId não existir ou não estiver ativo
     * @throws InvalidPickupTimeException se o horário estiver fora da janela de funcionamento ou no passado
     */
    public OrderResponse execute(CreateOrderRequest request) {
        Meal meal = menuService.getActiveMealOrThrow(request.mealId());
        SideDish sideDish = resolveSideDish(meal, request.sideDishId());
        List<Extra> extras = resolveExtras(request.extraIds());

        pickupTimePolicy.validate(request.pickupTime());

        BigDecimal totalPrice = calculateTotal(meal, sideDish, extras);

        Order order = Order.builder()
                .orderCode(generateUniqueOrderCode())
                .customerName(request.customerName().trim())
                .mealId(meal.getId())
                .mealName(meal.getName())
                .mealPrice(meal.getPrice())
                .mealPrepTimeMinutes(meal.getEstimatedPrepTimeMinutes())
                .sideDishId(sideDish != null ? sideDish.getId() : null)
                .sideDishName(sideDish != null ? sideDish.getName() : null)
                .sideDishPrice(sideDish != null ? sideDish.getPrice() : null)
                .extras(toExtraSnapshots(extras))
                .totalPrice(totalPrice)
                .observation(sanitizeObservation(request.observation()))
                .orderType(request.orderType())
                .needsDisposableCutlery(normalizeCutlery(request.orderType(), request.needsDisposableCutlery()))
                .pickupTime(request.pickupTime())
                .paymentMethod(request.paymentMethod())
                .status(OrderStatus.RECEIVED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Order saved = orderRepository.save(order);
        log.info("Pedido criado: {} — prato: {} + {} — total: {}",
                saved.getOrderCode(), saved.getMealName(), saved.getSideDishName(), saved.getTotalPrice());

        OrderResponse response = orderMapper.toResponse(saved);
        eventPublisher.publishOrderCreated(response);
        return response;
    }

    private SideDish resolveSideDish(Meal meal, String sideDishId) {
        if (!meal.isRequiresSideDish()) {
            return null;
        }
        return sideDishService.getActiveSideDishOrThrow(sideDishId);
    }

    /** extraIds é opcional no request — null vira lista vazia, nunca erro de validação. */
    private List<Extra> resolveExtras(List<String> extraIds) {
        if (extraIds == null || extraIds.isEmpty()) {
            return List.of();
        }
        return extraIds.stream()
                .map(extraService::getActiveExtraOrThrow)
                .toList();
    }

    private List<OrderExtraSnapshot> toExtraSnapshots(List<Extra> extras) {
        return extras.stream()
                .map(extra -> OrderExtraSnapshot.builder()
                        .extraId(extra.getId())
                        .extraName(extra.getName())
                        .extraPrice(extra.getPrice())
                        .build())
                .toList();
    }

    /**
     * sideDish.getPrice() pode ser null (acompanhamento incluso sem custo
     * adicional) — tratado como ZERO aqui, não como erro. Cada extra sempre
     * tem preço não-nulo (garantido no domínio Extra), então a soma dos
     * extras nunca precisa desse mesmo tratamento defensivo.
     */
    private BigDecimal calculateTotal(Meal meal, SideDish sideDish, List<Extra> extras) {
        BigDecimal total = meal.getPrice();

        if (sideDish != null && sideDish.getPrice() != null) {
            total = total.add(sideDish.getPrice());
        }
        for (Extra extra : extras) {
            total = total.add(extra.getPrice());
        }
        return total;
    }

    /**
     * orderCode é gerado com SecureRandom em ~60 milhões de combinações
     * (ver OrderCodeGenerator) — colisão é improvável, mas "improvável" não é
     * "impossível", e uma colisão quebraria a garantia de unicidade do
     * capability token. Tenta algumas vezes antes de desistir; se esgotar as
     * tentativas, o índice único do Mongo (order_code_unique) ainda pega
     * qualquer duplicata que escape daqui, lançando erro de integridade.
     */
    private String generateUniqueOrderCode() {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String candidate = codeGenerator.generate();
            if (orderRepository.findByOrderCode(candidate).isEmpty()) {
                return candidate;
            }
            log.warn("Colisão de orderCode detectada na tentativa {} — regenerando", attempt + 1);
        }
        throw new IllegalStateException("Não foi possível gerar um orderCode único após "
                + MAX_CODE_GENERATION_ATTEMPTS + " tentativas");
    }

    /**
     * Garante a invariante "cutelaria só existe para TAKEAWAY" no momento da
     * criação, independente do que o frontend mandou. Defesa em profundidade:
     * mesmo que um bug no Customer App envie needsDisposableCutlery=true
     * junto com orderType=DINE_IN, o backend nunca persiste essa combinação
     * inconsistente — força null, que é o valor correto para "pergunta não
     * se aplica".
     */
    private Boolean normalizeCutlery(OrderType orderType, Boolean requested) {
        if (orderType != OrderType.TAKEAWAY) {
            return null;
        }
        return requested != null && requested;
    }

    /** Corta espaços e limita tamanho — a validação de 140 chars já ocorreu no DTO, isso é defesa em profundidade. */
    private String sanitizeObservation(String observation) {
        return observation == null ? null : observation.trim();
    }
}