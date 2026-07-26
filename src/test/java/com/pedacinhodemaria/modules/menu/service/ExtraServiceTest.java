package com.pedacinhodemaria.modules.menu.service;

import com.pedacinhodemaria.modules.menu.domain.Extra;
import com.pedacinhodemaria.modules.menu.dto.ExtraResponse;
import com.pedacinhodemaria.modules.menu.mapper.ExtraMapper;
import com.pedacinhodemaria.modules.menu.repository.ExtraRepository;
import com.pedacinhodemaria.shared.exception.ExtraNotFoundException;
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
class ExtraServiceTest {

    @Mock private ExtraRepository extraRepository;
    @Mock private ExtraMapper extraMapper;

    @InjectMocks
    private ExtraService extraService;

    @Test
    void getAvailableExtrasReturnsActiveOnesOrderedByDisplayOrder() {
        Extra friedEgg = Extra.builder().id("extra-1").name("Ovo").price(new BigDecimal("2.00"))
                .active(true).displayOrder(1).build();
        ExtraResponse response = mock(ExtraResponse.class);

        when(extraRepository.findByActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of(friedEgg));
        when(extraMapper.toResponse(friedEgg)).thenReturn(response);

        assertThat(extraService.getAvailableExtras()).containsExactly(response);
    }

    @Test
    void getActiveExtraOrThrowReturnsExtraWhenFoundAndActive() {
        Extra beef = Extra.builder().id("extra-2").name("Carne").price(new BigDecimal("6.00"))
                .active(true).displayOrder(2).build();
        when(extraRepository.findByIdAndActiveTrue("extra-2")).thenReturn(Optional.of(beef));

        Extra result = extraService.getActiveExtraOrThrow("extra-2");

        assertThat(result.getName()).isEqualTo("Carne");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("6.00"));
    }

    @Test
    void getActiveExtraOrThrowThrowsWhenNotFoundOrInactive() {
        when(extraRepository.findByIdAndActiveTrue("extra-x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> extraService.getActiveExtraOrThrow("extra-x"))
                .isInstanceOf(ExtraNotFoundException.class)
                .hasMessageContaining("extra-x");
    }
}
