package com.pedacinhodemaria.modules.menu.mapper;

import com.pedacinhodemaria.modules.menu.domain.Drink;
import com.pedacinhodemaria.modules.menu.dto.DrinkResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DrinkMapper {
    DrinkResponse toResponse(Drink drink);
}