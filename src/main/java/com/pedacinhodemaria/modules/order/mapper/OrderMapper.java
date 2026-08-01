package com.pedacinhodemaria.modules.order.mapper;

import com.pedacinhodemaria.modules.order.domain.Order;
import com.pedacinhodemaria.modules.order.domain.OrderExtraSnapshot;
import com.pedacinhodemaria.modules.order.domain.TimerCalculator;
import com.pedacinhodemaria.modules.order.dto.OrderExtraResponse;
import com.pedacinhodemaria.modules.order.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * `timerState` não existe como campo em Order — é derivado aqui, no momento
 * exato em que a entidade vira resposta HTTP. MapStruct permite expressões
 * Java arbitrárias via `expression`, então a chamada ao TimerCalculator entra
 * dentro do mapeamento gerado sem precisar de um passo manual depois.
 *
 * `imports = TimerCalculator.class` é necessário porque TimerCalculator está
 * em outro pacote (modules.order.domain) — o import dele nesta INTERFACE não
 * se propaga para OrderMapperImpl.java, a classe separada que o MapStruct
 * gera no mesmo pacote deste arquivo. Sem essa declaração, o texto dentro de
 * `expression = "java(...)"` é tratado como string opaca: o processador não
 * sabe que TimerCalculator precisa de import na classe gerada, e o build
 * falha com "cannot find symbol: variable TimerCalculator" — exatamente o
 * erro deste ticket. `imports` é o mecanismo oficial do MapStruct para expor
 * uma classe auxiliar a uma expressão Java livre.
 */
@Mapper(componentModel = "spring", imports = TimerCalculator.class)
public interface OrderMapper {

    @Mapping(target = "timerState",
            expression = "java(TimerCalculator.calculate(order.getCreatedAt(), order.getMealPrepTimeMinutes()))")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    OrderResponse toResponse(Order order);

    /**
     * Declarado explicitamente (em vez de deixar o MapStruct inferir) para
     * que o mapeamento de List<OrderExtraSnapshot> → List<OrderExtraResponse>
     * dentro de toResponse() use exatamente este método, sem ambiguidade.
     */
    OrderExtraResponse toExtraResponse(OrderExtraSnapshot snapshot);
}