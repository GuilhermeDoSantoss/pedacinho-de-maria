package com.pedacinhodemaria.modules.menu.service;

import com.pedacinhodemaria.modules.menu.domain.Meal;
import com.pedacinhodemaria.modules.menu.domain.MealType;
import com.pedacinhodemaria.modules.menu.dto.MealResponse;
import com.pedacinhodemaria.modules.menu.mapper.MealMapper;
import com.pedacinhodemaria.modules.menu.repository.MealRepository;
import com.pedacinhodemaria.shared.exception.MealNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock private MealRepository mealRepository;
    @Mock private MealMapper mealMapper;

    @InjectMocks
    private MenuService menuService;

    private Meal activeMeal() {
        return Meal.builder()
                .id("meal-1")
                .name("Feijoada")
                .price(new BigDecimal("28.90"))
                .estimatedPrepTimeMinutes(30)
                .type(MealType.FIXED)
                .active(true)
                .displayOrder(1)
                .build();
    }

    @Test
    void getTodayMenuReturnsOnlyActiveMealsOrderedByDisplayOrder() {
        Meal meal = activeMeal();
        MealResponse response = mock(MealResponse.class);

        when(mealRepository.findByActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of(meal));
        when(mealMapper.toResponse(meal)).thenReturn(response);

        List<MealResponse> result = menuService.getTodayMenu();

        assertThat(result).containsExactly(response);
    }

    @Test
    void getTodayMenuReturnsEmptyListWhenNoActiveMeals() {
        when(mealRepository.findByActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of());

        assertThat(menuService.getTodayMenu()).isEmpty();
    }

    @Test
    void getActiveMealOrThrowReturnsMealWhenFoundAndActive() {
        Meal meal = activeMeal();
        when(mealRepository.findByIdAndActiveTrue("meal-1")).thenReturn(Optional.of(meal));

        Meal result = menuService.getActiveMealOrThrow("meal-1");

        assertThat(result).isEqualTo(meal);
    }

    @Test
    void getActiveMealOrThrowThrowsWhenMealDoesNotExistOrIsInactive() {
        // findByIdAndActiveTrue já filtra por active=true na query — um prato inativo
        // e um prato inexistente produzem o mesmo Optional.empty() aqui, então o
        // mesmo teste cobre os dois casos de negócio de uma vez.
        when(mealRepository.findByIdAndActiveTrue("meal-x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getActiveMealOrThrow("meal-x"))
                .isInstanceOf(MealNotFoundException.class)
                .hasMessageContaining("meal-x");
    }
}
