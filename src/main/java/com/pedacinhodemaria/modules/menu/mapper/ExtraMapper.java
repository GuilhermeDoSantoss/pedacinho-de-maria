package com.pedacinhodemaria.modules.menu.mapper;

import com.pedacinhodemaria.modules.menu.domain.Extra;
import com.pedacinhodemaria.modules.menu.dto.ExtraResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExtraMapper {
    ExtraResponse toResponse(Extra extra);
}