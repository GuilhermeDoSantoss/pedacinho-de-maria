package com.pedacinhodemaria.modules.order.service;

import com.pedacinhodemaria.modules.menu.domain.Extra;
import com.pedacinhodemaria.modules.menu.domain.SideDish;
import com.pedacinhodemaria.modules.menu.service.ExtraService;
import com.pedacinhodemaria.modules.menu.service.MenuService;
import com.pedacinhodemaria.modules.menu.domain.Meal;
import com.pedacinhodemaria.modules.menu.domain.MealType;
import com.pedacinhodemaria.modules.menu.service.SideDishService;
import com.pedacinhodemaria.modules.order.domain.Order;
import com.pedacinhodemaria.modules.order.domain.OrderType;
import com.pedacinhodemaria.modules.order.domain.PaymentMethod;
import com.pedacinhodemaria.modules.order.dto.CreateOrderRequest;
import com.pedacinhodemaria.modules.order.dto.OrderResponse;
import com.pedacinhodemaria.modules.order.repository.OrderRepository;
import com.pedacinhodemaria.modules.order.mapper.OrderMapper;
import com.pedacinhodemaria.modules.order.service.PhoneNumberNormalizer;
import com.pedacinhodemaria.modules.order.websocket.OrderEventPublisher;
import com.pedacinhodemaria.shared.exception.InvalidPhoneNumberException;
import com.pedacinhodemaria.shared.exception.InvalidPickupTimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock private MenuService menuService;
    @Mock private SideDishService sideDishService;
    @Mock private ExtraService extraService;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderEventPublisher eventPublisher;
    @Mock private OrderCodeGenerator codeGenerator;
    @Mock private PhoneNumberNormalizer phoneNumberNormalizer;

    private PickupTimePolicy pickupTimePolicy;

    @InjectMocks
    private CreateOrderUseCase useCase;

    private Meal activeMeal;
    private SideDish activeSideDish;
    private Extra friedEgg;

    @BeforeEach
    void setUp() {
        Clock fixedClock = new Clock() {
            @Override
            public ZoneId getZone() {
                return ZoneId.of("UTC");
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return Instant.parse("2024-01-01T10:30:00Z");
            }
        };
        pickupTimePolicy = new PickupTimePolicy(fixedClock);
        useCase = new CreateOrderUseCase(menuService, sideDishService, extraService, orderRepository,
                orderMapper, eventPublisher, codeGenerator, pickupTimePolicy, phoneNumberNormalizer);

        activeMeal = Meal.builder()
                .id("meal-1")
                .name("Feijoada")
                .price(new BigDecimal("28.90"))
                .estimatedPrepTimeMinutes(30)
                .type(MealType.FIXED)
                .active(true)
                .requiresSideDish(true)
                .build();

        activeSideDish = SideDish.builder()
                .id("side-1")
                .name("Arroz Branco")
                .price(null) // incluso sem custo adicional
                .active(true)
                .build();

        friedEgg = Extra.builder()
                .id("extra-1")
                .name("Ovo Extra")
                .price(new BigDecimal("2.00"))
                .active(true)
                .build();
    }

    private LocalTime aValidPickupTime() {
        return LocalTime.of(12, 30);
    }

    private void stubHappyPathDependencies() {
        when(menuService.getActiveMealOrThrow("meal-1")).thenReturn(activeMeal);
        when(sideDishService.getActiveSideDishOrThrow("side-1")).thenReturn(activeSideDish);
        when(codeGenerator.generate()).thenReturn("PM-ABCDE");
        lenient().when(orderRepository.findByOrderCode("PM-ABCDE")).thenReturn(Optional.empty());
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(orderMapper.toResponse(any(Order.class))).thenReturn(mock(OrderResponse.class));
    }

    @Test
    void createsOrderWithSideDishAndNoExtras() {
        stubHappyPathDependencies();
        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, aValidPickupTime(),
                OrderType.DINE_IN, null, PaymentMethod.PIX, null, null);

        OrderResponse result = useCase.execute(request);

        assertThat(result).isNotNull();
        verify(eventPublisher).publishOrderCreated(any());
        verify(extraService, never()).getActiveExtraOrThrow(any());
        verify(orderRepository).save(argThat(order ->
                order.getMealName().equals("Feijoada")
                        && order.getSideDishName().equals("Arroz Branco")
                        && order.getExtras().isEmpty()
                        // acompanhamento sem preço (null) => total é só o preço do prato
                        && order.getTotalPrice().compareTo(new BigDecimal("28.90")) == 0
        ));
    }

    @Test
    void calculatesTotalPriceIncludingExtras() {
        stubHappyPathDependencies();
        when(extraService.getActiveExtraOrThrow("extra-1")).thenReturn(friedEgg);

        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", List.of("extra-1"), null,
                aValidPickupTime(), OrderType.DINE_IN, null, PaymentMethod.PIX, null, null);

        useCase.execute(request);

        // 28.90 (prato) + 0 (acompanhamento sem custo) + 2.00 (extra) = 30.90
        verify(orderRepository).save(argThat(order ->
                order.getTotalPrice().compareTo(new BigDecimal("30.90")) == 0
                        && order.getExtras().size() == 1
                        && order.getExtras().get(0).getExtraName().equals("Ovo Extra")
        ));
    }

    @Test
    void createsOrderWithoutSideDishWhenMealDoesNotRequireIt() {
        activeMeal.setRequiresSideDish(false);
        when(menuService.getActiveMealOrThrow("meal-1")).thenReturn(activeMeal);
        when(codeGenerator.generate()).thenReturn("PM-ABCDE");
        when(orderRepository.findByOrderCode("PM-ABCDE")).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(mock(OrderResponse.class));

        var request = new CreateOrderRequest("Maria Silva", "meal-1", null, null, null, aValidPickupTime(),
                OrderType.DINE_IN, null, PaymentMethod.PIX, null, null);

        useCase.execute(request);

        verify(sideDishService, never()).getActiveSideDishOrThrow(any());
        verify(orderRepository).save(argThat(order ->
                order.getSideDishId() == null
                        && order.getSideDishName() == null
                        && order.getTotalPrice().compareTo(new BigDecimal("28.90")) == 0
        ));
    }

    @Test
    void rejectsOrderWithPickupTimeInThePast() {
        when(menuService.getActiveMealOrThrow("meal-1")).thenReturn(activeMeal);
        when(sideDishService.getActiveSideDishOrThrow("side-1")).thenReturn(activeSideDish);

        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, LocalTime.of(10, 15),
                OrderType.DINE_IN, null, PaymentMethod.CASH, null, null);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidPickupTimeException.class)
                .hasMessageContaining("passado");

        verifyNoInteractions(orderRepository, eventPublisher);
    }

    @Test
    void rejectsOrderWithPickupTimeBeforeOpeningHours() {
        when(menuService.getActiveMealOrThrow("meal-1")).thenReturn(activeMeal);
        when(sideDishService.getActiveSideDishOrThrow("side-1")).thenReturn(activeSideDish);

        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, LocalTime.of(11, 15),
                OrderType.DINE_IN, null, PaymentMethod.CASH, null, null);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidPickupTimeException.class)
                .hasMessageContaining("11:30");

        verifyNoInteractions(orderRepository, eventPublisher);
    }

    @Test
    void rejectsOrderWithPickupTimeAfterClosingHours() {
        when(menuService.getActiveMealOrThrow("meal-1")).thenReturn(activeMeal);
        when(sideDishService.getActiveSideDishOrThrow("side-1")).thenReturn(activeSideDish);

        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, LocalTime.of(15, 45),
                OrderType.DINE_IN, null, PaymentMethod.CASH, null, null);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidPickupTimeException.class)
                .hasMessageContaining("11:30");

        verifyNoInteractions(orderRepository, eventPublisher);
    }

    @Test
    void acceptsPickupTimeInsideBusinessHours() {
        stubHappyPathDependencies();

        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, LocalTime.of(13, 0),
                OrderType.DINE_IN, null, PaymentMethod.PIX, null, null);

        useCase.execute(request);

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void forcesCutleryToNullWhenOrderTypeIsDineInEvenIfRequested() {
        stubHappyPathDependencies();
        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, aValidPickupTime(),
                OrderType.DINE_IN, true, PaymentMethod.PIX, null, null);

        useCase.execute(request);

        verify(orderRepository).save(argThat(order -> order.getNeedsDisposableCutlery() == null));
    }

    @Test
    void preservesCutleryChoiceWhenOrderTypeIsTakeaway() {
        stubHappyPathDependencies();
        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, aValidPickupTime(),
                OrderType.TAKEAWAY, true, PaymentMethod.PIX, null, "21999999999");

        useCase.execute(request);

        verify(orderRepository).save(argThat(order -> Boolean.TRUE.equals(order.getNeedsDisposableCutlery())));
    }

    @Test
    void rejectsTakeawayOrderWithoutPhoneNumber() {
        stubHappyPathDependencies();
        when(phoneNumberNormalizer.normalizeAndValidate(null, true)).thenThrow(new IllegalArgumentException("O telefone é obrigatório para pedidos para viagem."));
        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, aValidPickupTime(),
                OrderType.TAKEAWAY, null, PaymentMethod.PIX, null, null);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidPhoneNumberException.class)
                .hasMessage("O telefone é obrigatório para pedidos para viagem.");

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void normalizesPhoneNumberBeforePersistingTakeawayOrder() {
        stubHappyPathDependencies();
        when(phoneNumberNormalizer.normalizeAndValidate("(21) 99999-9999", true)).thenReturn("21999999999");
        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, aValidPickupTime(),
                OrderType.TAKEAWAY, null, PaymentMethod.PIX, null, "(21) 99999-9999");

        useCase.execute(request);

        verify(orderRepository).save(argThat(order -> "21999999999".equals(order.getPhoneNumber())));
    }

    @Test
    void allowsDineInOrderWithoutPhoneNumber() {
        stubHappyPathDependencies();
        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, aValidPickupTime(),
                OrderType.DINE_IN, null, PaymentMethod.PIX, null, null);

        useCase.execute(request);

        verify(orderRepository).save(argThat(order -> order.getPhoneNumber() == null));
    }

    @Test
    void retriesOrderCodeGenerationOnCollision() {
        when(menuService.getActiveMealOrThrow("meal-1")).thenReturn(activeMeal);
        when(sideDishService.getActiveSideDishOrThrow("side-1")).thenReturn(activeSideDish);
        when(codeGenerator.generate()).thenReturn("PM-DUPLI", "PM-FREEE");
        when(orderRepository.findByOrderCode("PM-DUPLI")).thenReturn(Optional.of(mock(Order.class)));
        when(orderRepository.findByOrderCode("PM-FREEE")).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(mock(OrderResponse.class));

        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null, aValidPickupTime(),
                OrderType.DINE_IN, null, PaymentMethod.PIX, null, null);

        useCase.execute(request);

        verify(codeGenerator, times(2)).generate();
        verify(orderRepository).save(argThat(order -> order.getOrderCode().equals("PM-FREEE")));
    }
}
