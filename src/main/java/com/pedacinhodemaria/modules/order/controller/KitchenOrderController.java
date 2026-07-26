package com.pedacinhodemaria.modules.order.controller;

import com.pedacinhodemaria.modules.order.dto.OrderResponse;
import com.pedacinhodemaria.modules.order.dto.UpdateOrderStatusRequest;
import com.pedacinhodemaria.modules.order.service.OrderQueryService;
import com.pedacinhodemaria.modules.order.service.UpdateOrderStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints consumidos pelo Kitchen Dashboard. Públicos, sem autenticação
 * (decisão de produto da consolidação Fases 1+2 — ver ADR em SecurityConfig)
 * — mantidos num controller próprio, separado de OrderController, porque a
 * audiência é diferente (cozinha vs. cliente final) mesmo sem diferença de
 * autorização hoje; separa também o que evolui junto no futuro.
 */
@RestController
@RequestMapping("/api/v1/kitchen/orders")
@RequiredArgsConstructor
@Tag(name = "Kitchen", description = "Consulta e atualização de pedidos pela cozinha")
public class KitchenOrderController {

    private final OrderQueryService orderQueryService;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @GetMapping
    @Operation(summary = "Lista pedidos ativos (RECEIVED/PREPARING/READY) com timerState já calculado")
    public ResponseEntity<List<OrderResponse>> getActiveOrders() {
        return ResponseEntity.ok(orderQueryService.getActiveOrders());
    }

    @PatchMapping("/{orderCode}/status")
    @Operation(summary = "Move um pedido entre colunas, validando a transição de estado")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable String orderCode,
                                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(updateOrderStatusUseCase.execute(orderCode, request.newStatus()));
    }
}