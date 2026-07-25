package com.pedacinhodemaria.modules.menu.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Acompanhamento do prato principal (arroz, feijão, farofa, salada...).
 * Seleção obrigatória no pedido — todo pedido tem exatamente um.
 *
 * `price` pode ser null: alguns acompanhamentos vêm inclusos no prato sem
 * custo adicional, outros são cobrados à parte. CreateOrderUseCase trata
 * null como zero no cálculo do total, nunca lança erro por isso.
 */
@Document(collection = "side_dishes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SideDish {

    @Id
    private String id;

    private String name;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    private boolean active;

    private Integer displayOrder;
}