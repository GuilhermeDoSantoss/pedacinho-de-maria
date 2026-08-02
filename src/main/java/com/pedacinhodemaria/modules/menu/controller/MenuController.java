package com.pedacinhodemaria.modules.menu.controller;

import com.pedacinhodemaria.modules.menu.service.MenuService;
import com.pedacinhodemaria.modules.menu.dto.MenuResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público (sem autenticação) consumido pelo Customer App via QR Code.
 * Nunca há cardápio hardcoded no frontend — este é o único ponto de verdade.
 */
@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Consulta do cardápio do dia")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "Lista pratos e bebidas ativos do cardápio, ordenados por displayOrder")
    public ResponseEntity<MenuResponse> getMenu() {
        return ResponseEntity.ok(menuService.getMenu());
    }
}