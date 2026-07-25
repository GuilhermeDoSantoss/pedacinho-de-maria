package com.pedacinhodemaria.modules.menu.controller;

import com.pedacinhodemaria.modules.menu.dto.ExtraResponse;
import com.pedacinhodemaria.modules.menu.service.ExtraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/extras")
@RequiredArgsConstructor
@Tag(name = "Extras", description = "Consulta de extras disponíveis")
public class ExtraController {

    private final ExtraService extraService;

    @GetMapping
    @Operation(summary = "Lista extras ativos, ordenados por displayOrder")
    public ResponseEntity<List<ExtraResponse>> getAvailableExtras() {
        return ResponseEntity.ok(extraService.getAvailableExtras());
    }
}