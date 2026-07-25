package com.pedacinhodemaria.modules.menu.mapper;

import com.pedacinhodemaria.modules.menu.domain.Meal;
import com.pedacinhodemaria.modules.menu.dto.MealResponse;
import org.mapstruct.Mapper;

/**
 * MapStruct gera a implementação em tempo de compilação (sem reflection em
 * runtime, ao contrário de ModelMapper/Dozer) — mantém o mapeamento
 * type-safe e com performance equivalente a código escrito à mão.
 */
@Mapper(componentModel = "spring")
public interface MealMapper {
    MealResponse toResponse(Meal meal);
}