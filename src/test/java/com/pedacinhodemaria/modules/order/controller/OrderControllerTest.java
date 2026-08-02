package com.pedacinhodemaria.modules.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pedacinhodemaria.modules.order.domain.OrderStatus;
import com.pedacinhodemaria.modules.order.domain.OrderType;
import com.pedacinhodemaria.modules.order.domain.PaymentMethod;
import com.pedacinhodemaria.modules.order.domain.TimerState;
import com.pedacinhodemaria.modules.order.dto.CreateOrderRequest;
import com.pedacinhodemaria.modules.order.dto.OrderResponse;
import com.pedacinhodemaria.modules.order.service.CreateOrderUseCase;
import com.pedacinhodemaria.modules.order.service.OrderQueryService;
import com.pedacinhodemaria.modules.order.service.PickupTimePolicy;
import com.pedacinhodemaria.shared.exception.GlobalExceptionHandler;
import com.pedacinhodemaria.shared.exception.InvalidPhoneNumberException;
import com.pedacinhodemaria.shared.exception.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc standalone (não @WebMvcTest) — não sobe contexto Spring, monta só
 * este controller com os mocks. Mais rápido e mais isolado; a validação de
 * segurança (rotas públicas) já é coberta pela leitura de SecurityConfig, não
 * precisa de um teste de integração completo só para confirmar isso de novo.
 * O GlobalExceptionHandler é registrado manualmente aqui — sem isso, o
 * MockMvc devolveria o 500 padrão do Spring em vez do nosso ApiError.
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock private CreateOrderUseCase createOrderUseCase;
    @Mock private OrderQueryService orderQueryService;
    @Mock private PickupTimePolicy pickupTimePolicy;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(createOrderUseCase, orderQueryService, pickupTimePolicy);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private OrderResponse sampleResponse() {
        return new OrderResponse("PM-ABCDE", "Maria Silva", "Feijoada", new BigDecimal("28.90"), 30,
                "Arroz", BigDecimal.ZERO, List.of(), List.of(), new BigDecimal("28.90"),
                LocalTime.of(19, 30), OrderType.DINE_IN, null, PaymentMethod.PIX, null, null,
                OrderStatus.RECEIVED, TimerState.GREEN, java.time.Instant.now());
    }

    @Test
    void createOrderReturns201WithBodyOnValidRequest() throws Exception {
        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null,
                LocalTime.of(19, 30), OrderType.DINE_IN, null, PaymentMethod.PIX, null, null);

        when(createOrderUseCase.execute(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderCode").value("PM-ABCDE"))
                .andExpect(jsonPath("$.mealName").value("Feijoada"));
    }

    @Test
    void createOrderReturns400WhenCustomerNameIsBlank() throws Exception {
        // Exercita o @Valid de verdade através do MockMvc — diferente dos
        // testes de CreateOrderUseCase, que chamam o use case diretamente e
        // nunca passam pela camada de Bean Validation do Spring MVC.
        var invalidRequest = new CreateOrderRequest("", "meal-1", "side-1", null, null,
                LocalTime.of(19, 30), OrderType.DINE_IN, null, PaymentMethod.PIX, null, null);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrderReturns400WhenTakeawayOrderIsMissingPhoneNumber() throws Exception {
        var request = new CreateOrderRequest("Maria Silva", "meal-1", "side-1", null, null,
                LocalTime.of(19, 30), OrderType.TAKEAWAY, null, PaymentMethod.PIX, null, null);

        when(createOrderUseCase.execute(any())).thenThrow(new InvalidPhoneNumberException(
                "O telefone é obrigatório para pedidos para viagem."));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O telefone é obrigatório para pedidos para viagem."));
    }

    @Test
    void getOrderReturns200WithOrderWhenFound() throws Exception {
        when(orderQueryService.getByOrderCode("PM-ABCDE")).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/orders/PM-ABCDE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode").value("PM-ABCDE"));
    }

    @Test
    void getOrderReturns404WhenOrderCodeDoesNotExist() throws Exception {
        when(orderQueryService.getByOrderCode(eq("PM-ZZZZZ")))
                .thenThrow(new OrderNotFoundException("PM-ZZZZZ"));

        mockMvc.perform(get("/api/v1/orders/PM-ZZZZZ"))
                .andExpect(status().isNotFound());
    }
}
