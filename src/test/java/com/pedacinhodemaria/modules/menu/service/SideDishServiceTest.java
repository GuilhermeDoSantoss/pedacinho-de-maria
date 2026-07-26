package com.pedacinhodemaria.modules.menu.service;

import com.pedacinhodemaria.modules.menu.domain.SideDish;
import com.pedacinhodemaria.modules.menu.dto.SideDishResponse;
import com.pedacinhodemaria.modules.menu.mapper.SideDishMapper;
import com.pedacinhodemaria.modules.menu.repository.SideDishRepository;
import com.pedacinhodemaria.shared.exception.SideDishNotFoundException;
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
class SideDishServiceTest {

    @Mock private SideDishRepository sideDishRepository;
    @Mock private SideDishMapper sideDishMapper;

    @InjectMocks
    private SideDishService sideDishService;

    @Test
    void getAvailableSideDishesReturnsActiveOnesOrderedByDisplayOrder() {
        SideDish rice = SideDish.builder().id("side-1").name("Arroz").price(BigDecimal.ZERO)
                .active(true).displayOrder(1).build();
        SideDishResponse response = mock(SideDishResponse.class);

        when(sideDishRepository.findByActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of(rice));
        when(sideDishMapper.toResponse(rice)).thenReturn(response);

        assertThat(sideDishService.getAvailableSideDishes()).containsExactly(response);
    }

    @Test
    void sideDishWithZeroPriceIsAValidReturnValueNotAnError() {
        // Acompanhamento com preço zero (incluso no prato) é um caso de negócio
        // legítimo, não uma configuração inválida — este teste existe porque
        // BigDecimal.ZERO é um valor fácil de confundir com "ausente"/null em
        // algum refactor futuro que adicione validação de preço.
        SideDish included = SideDish.builder().id("side-2").name("Feijão").price(BigDecimal.ZERO)
                .active(true).displayOrder(2).build();

        when(sideDishRepository.findByIdAndActiveTrue("side-2")).thenReturn(Optional.of(included));

        SideDish result = sideDishService.getActiveSideDishOrThrow("side-2");

        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getActiveSideDishOrThrowThrowsWhenNotFoundOrInactive() {
        when(sideDishRepository.findByIdAndActiveTrue("side-x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sideDishService.getActiveSideDishOrThrow("side-x"))
                .isInstanceOf(SideDishNotFoundException.class)
                .hasMessageContaining("side-x");
    }
}
