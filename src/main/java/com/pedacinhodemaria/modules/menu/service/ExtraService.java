package com.pedacinhodemaria.modules.menu.service;

import com.pedacinhodemaria.modules.menu.domain.Extra;
import com.pedacinhodemaria.modules.menu.dto.ExtraResponse;
import com.pedacinhodemaria.modules.menu.mapper.ExtraMapper;
import com.pedacinhodemaria.modules.menu.repository.ExtraRepository;
import com.pedacinhodemaria.shared.exception.ExtraNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtraService {

    private final ExtraRepository extraRepository;
    private final ExtraMapper extraMapper;

    public List<ExtraResponse> getAvailableExtras() {
        return extraRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(extraMapper::toResponse)
                .toList();
    }

    public Extra getActiveExtraOrThrow(String extraId) {
        return extraRepository.findByIdAndActiveTrue(extraId)
                .orElseThrow(() -> new ExtraNotFoundException(extraId));
    }
}