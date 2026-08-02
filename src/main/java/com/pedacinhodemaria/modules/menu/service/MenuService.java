package com.pedacinhodemaria.modules.menu.service;

import com.pedacinhodemaria.modules.menu.domain.Drink;
import com.pedacinhodemaria.modules.menu.domain.Meal;
import com.pedacinhodemaria.modules.menu.dto.DrinkResponse;
import com.pedacinhodemaria.modules.menu.dto.MealResponse;
import com.pedacinhodemaria.modules.menu.dto.MenuResponse;
import com.pedacinhodemaria.modules.menu.mapper.DrinkMapper;
import com.pedacinhodemaria.modules.menu.mapper.MealMapper;
import com.pedacinhodemaria.modules.menu.repository.DrinkRepository;
import com.pedacinhodemaria.modules.menu.repository.MealRepository;
import com.pedacinhodemaria.shared.exception.MealNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Camada de aplicação do módulo de cardápio. Responsabilidade única: servir
 * o cardápio do dia (4 fixos + prato do dia) e resolver um Meal específico
 * para outros módulos (o CreateOrderUseCase depende deste service, não do
 * MealRepository diretamente — mantém a regra "pedido só aceita prato ativo"
 * em um único lugar).
 */
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MealRepository mealRepository;
    private final DrinkRepository drinkRepository;
    private final MealMapper mealMapper;
    private final DrinkMapper drinkMapper;

    public MenuResponse getMenu() {
        List<MealResponse> meals = mealRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(mealMapper::toResponse)
                .toList();

        List<DrinkResponse> drinks = drinkRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(drinkMapper::toResponse)
                .toList();

        return new MenuResponse(meals, drinks);
    }

    public List<MealResponse> getTodayMenu() {
        return getMenu().meals();
    }

    /**
     * Resolve um prato ativo por id. Lançar aqui (em vez de devolver Optional
     * para o chamador decidir) centraliza a regra "pedido só pode referenciar
     * prato ativo" em um único ponto — hoje só o CreateOrderUseCase usa este
     * método, mas qualquer novo consumidor futuro herda a regra automaticamente.
     */
    public Meal getActiveMealOrThrow(String mealId) {
        return mealRepository.findByIdAndActiveTrue(mealId)
                .orElseThrow(() -> new MealNotFoundException(mealId));
    }
}