package com.pedacinhodemaria.modules.order.controller;

import com.pedacinhodemaria.modules.order.service.CreateOrderUseCase;
import com.pedacinhodemaria.modules.order.service.OrderQueryService;
import com.pedacinhodemaria.modules.order.dto.CreateOrderRequest;
import com.pedacinhodemaria.modules.order.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints públicos (sem autenticação) consumidos pelo Customer App.
 * A ausência de auth é decisão deliberada — ver ADR no README sobre por que
 * o cliente nunca faz login. Não há rate limiting ainda (planejado para uma
 * fase futura de hardening) — a única proteção hoje é a validação de negócio
 * em CreateOrderUseCase.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Criação e consulta pública de pedidos")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderQueryService orderQueryService;

    @PostMapping
    @Operation(summary = "Cria um pedido e notifica a cozinha em tempo real")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = createOrderUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderCode}")
    @Operation(summary = "Cliente consulta o status do próprio pedido usando o orderCode recebido na criação")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderCode) {
        return ResponseEntity.ok(orderQueryService.getByOrderCode(orderCode));
    }
}