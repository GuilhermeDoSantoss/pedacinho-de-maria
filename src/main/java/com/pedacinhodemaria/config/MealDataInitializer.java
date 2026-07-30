package com.pedacinhodemaria.config;

import com.pedacinhodemaria.modules.menu.domain.Meal;
import com.pedacinhodemaria.modules.menu.domain.MealType;
import com.pedacinhodemaria.modules.menu.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seed inicial do cardápio com a regra de negócio atual: apenas pratos que
 * realmente exigem acompanhamento recebem requiresSideDish=true.
 */
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class MealDataInitializer implements ApplicationRunner {

    private final MealRepository mealRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (mealRepository.count() > 0) {
            return;
        }

        List<Meal> meals = List.of(
                Meal.builder()
                        .name("Feijoada")
                        .description("Feijoada tradicional com acompanhamentos")
                        .price(new BigDecimal("28.90"))
                        .imageUrl("/assets/images/meals/feijoada.jpg")
                        .estimatedPrepTimeMinutes(30)
                        .type(MealType.FIXED)
                        .active(true)
                        .displayOrder(1)
                        .requiresSideDish(true)
                        .build(),
                Meal.builder()
                        .name("Pernil Assado")
                        .description("Pernil assado com molho caseiro")
                        .price(new BigDecimal("32.50"))
                        .imageUrl("/assets/images/meals/pernil.jpg")
                        .estimatedPrepTimeMinutes(35)
                        .type(MealType.FIXED)
                        .active(true)
                        .displayOrder(2)
                        .requiresSideDish(true)
                        .build(),
                Meal.builder()
                        .name("Lasanha")
                        .description("Lasanha de carne e queijo")
                        .price(new BigDecimal("26.00"))
                        .imageUrl("/assets/images/meals/lasanha.jpg")
                        .estimatedPrepTimeMinutes(25)
                        .type(MealType.FIXED)
                        .active(true)
                        .displayOrder(3)
                        .requiresSideDish(false)
                        .build(),
                Meal.builder()
                        .name("Frango ao Molho")
                        .description("Frango ensopado com legumes")
                        .price(new BigDecimal("24.00"))
                        .imageUrl("/assets/images/meals/frango.jpg")
                        .estimatedPrepTimeMinutes(20)
                        .type(MealType.FIXED)
                        .active(true)
                        .displayOrder(4)
                        .requiresSideDish(false)
                        .build()
        );

        mealRepository.saveAll(meals);
    }
}
