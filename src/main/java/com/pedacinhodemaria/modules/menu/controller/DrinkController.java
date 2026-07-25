package com.pedacinhodemaria.modules.menu.controller;

import com.pedacinhodemaria.modules.menu.dto.DrinkResponse;
import com.pedacinhodemaria.modules.menu.service.DrinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drinks")
@RequiredArgsConstructor
@Tag(name = "Drinks", description = "Consulta de bebidas disponíveis")
public class DrinkController {

    private final DrinkService drinkService;

    @GetMapping
    @Operation(summary = "Lista bebidas ativas, ordenadas por displayOrder")
    public ResponseEntity<List<DrinkResponse>> getAvailableDrinks() {
        return ResponseEntity.ok(drinkService.getAvailableDrinks());
    }
}