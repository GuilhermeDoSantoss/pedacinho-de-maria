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
 * Item opcional adicionado ao pedido (ovo extra, carne extra, fritas...).
 * Sem imagem e sem descrição por decisão de produto explícita — a tela de
 * extras é uma lista de checkboxes simples, não cards ilustrados como o
 * cardápio principal ou os acompanhamentos.
 *
 * `price` aqui NUNCA é null (diferente de SideDish) — um extra sem preço
 * definido não faz sentido de negócio, então é obrigatório desde a criação.
 */
@Document(collection = "extras")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Extra {

    @Id
    private String id;

    private String name;

    private BigDecimal price;

    private boolean active;

    private Integer displayOrder;
}