package com.pedacinhodemaria.modules.menu.service;

import com.pedacinhodemaria.modules.menu.domain.Drink;
import com.pedacinhodemaria.modules.menu.dto.DrinkResponse;
import com.pedacinhodemaria.modules.menu.mapper.DrinkMapper;
import com.pedacinhodemaria.modules.menu.repository.DrinkRepository;
import com.pedacinhodemaria.shared.exception.DrinkNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DrinkService {

    private final DrinkRepository drinkRepository;
    private final DrinkMapper drinkMapper;

    public List<DrinkResponse> getAvailableDrinks() {
        return drinkRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(drinkMapper::toResponse)
                .toList();
    }

    public Drink getActiveDrinkOrThrow(String drinkId) {
        return drinkRepository.findByIdAndActiveTrue(drinkId)
                .orElseThrow(() -> new DrinkNotFoundException(drinkId));
    }
}