package com.pedacinhodemaria.modules.menu.controller;

import com.pedacinhodemaria.modules.menu.dto.SideDishResponse;
import com.pedacinhodemaria.modules.menu.service.SideDishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint público — mesmo padrão de /api/v1/menu (não /api/side-dishes sem
 * versionamento, como no pedido original: mantém consistência com o resto
 * da API, já estabelecido desde a Fase 1).
 */
@RestController
@RequestMapping("/api/v1/side-dishes")
@RequiredArgsConstructor
@Tag(name = "Side Dishes", description = "Consulta de acompanhamentos disponíveis")
public class SideDishController {

    private final SideDishService sideDishService;

    @GetMapping
    @Operation(summary = "Lista acompanhamentos ativos, ordenados por displayOrder")
    public ResponseEntity<List<SideDishResponse>> getAvailableSideDishes() {
        return ResponseEntity.ok(sideDishService.getAvailableSideDishes());
    }
}