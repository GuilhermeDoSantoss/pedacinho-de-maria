package com.pedacinhodemaria.modules.menu.service;

import com.pedacinhodemaria.modules.menu.domain.SideDish;
import com.pedacinhodemaria.modules.menu.dto.SideDishResponse;
import com.pedacinhodemaria.modules.menu.mapper.SideDishMapper;
import com.pedacinhodemaria.modules.menu.repository.SideDishRepository;
import com.pedacinhodemaria.shared.exception.SideDishNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço próprio para SideDish (não uma extensão de MenuService) — mesmo
 * princípio já aplicado a Order (CreateOrderUseCase, OrderQueryService,
 * UpdateOrderStatusUseCase são classes separadas, não um "OrderService"
 * genérico): cada recurso de catálogo muda por razões próprias, então cada
 * um recebe sua própria classe.
 */
@Service
@RequiredArgsConstructor
public class SideDishService {

    private final SideDishRepository sideDishRepository;
    private final SideDishMapper sideDishMapper;

    public List<SideDishResponse> getAvailableSideDishes() {
        return sideDishRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(sideDishMapper::toResponse)
                .toList();
    }

    public SideDish getActiveSideDishOrThrow(String sideDishId) {
        return sideDishRepository.findByIdAndActiveTrue(sideDishId)
                .orElseThrow(() -> new SideDishNotFoundException(sideDishId));
    }
}