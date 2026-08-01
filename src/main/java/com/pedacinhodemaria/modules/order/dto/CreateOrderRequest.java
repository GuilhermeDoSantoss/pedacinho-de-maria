package com.pedacinhodemaria.modules.order.dto;

import com.pedacinhodemaria.modules.order.domain.OrderType;
import com.pedacinhodemaria.modules.order.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.List;

/**
 * Payload de criação de pedido enviado pelo Customer App.
 *
 * Todas as validações de formato (tamanho, obrigatoriedade) ficam em Bean
 * Validation, executadas antes do request chegar ao use case — assim o
 * CreateOrderUseCase só trata regras de NEGÓCIO (prato existe? horário é
 * viável dentro do expediente?), nunca validação de formato de campo.
 *
 * "Horário no passado" fica aqui e não em Bean Validation porque depende de
 * comparar com `LocalTime.now()` no momento da validação — regra dinâmica,
 * não uma restrição estática do formato do campo.
 *
 * `needsDisposableCutlery` é opcional (Boolean, não boolean) porque só se
 * aplica quando orderType é TAKEAWAY — enviar null para DINE_IN é esperado,
 * não um erro. O use case normaliza essa relação (ver
 * CreateOrderUseCase.validateAndNormalizeCutlery).
 */
public record CreateOrderRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 60, message = "Nome deve ter entre 2 e 60 caracteres")
        String customerName,

        @NotBlank(message = "Selecione um prato")
        String mealId,

        String sideDishId,

        /**
         * Lista de ids de extras — opcional de verdade (cliente pode não
         * querer nenhum), então não leva @NotNull. CreateOrderUseCase trata
         * null como lista vazia, nunca como erro de validação.
         */
        List<String> extraIds,

        /** Mesmo tratamento de extraIds: opcional, null vira lista vazia no use case. */
        List<String> drinkIds,

        @NotNull(message = "Horário de retirada é obrigatório")
        LocalTime pickupTime,

        @NotNull(message = "Escolha se o pedido é para consumir no local ou para viagem")
        OrderType orderType,

        Boolean needsDisposableCutlery,

        @NotNull(message = "Forma de pagamento é obrigatória")
        PaymentMethod paymentMethod,

        @Size(max = 140, message = "Observação deve ter no máximo 140 caracteres")
        String observation,

        @Schema(description = "Telefone do cliente para pedidos para viagem; aceito no formato (21) 99999-9999 ou 21999999999", example = "21999999999")
        String phoneNumber
) {
}