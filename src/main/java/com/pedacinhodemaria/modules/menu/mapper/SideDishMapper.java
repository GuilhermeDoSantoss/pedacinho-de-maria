package com.pedacinhodemaria.modules.menu.mapper;

import com.pedacinhodemaria.modules.menu.domain.SideDish;
import com.pedacinhodemaria.modules.menu.dto.SideDishResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SideDishMapper {
    SideDishResponse toResponse(SideDish sideDish);
}