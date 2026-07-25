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
 * Bebida — seleção opcional e múltipla, mesma semântica de negócio de Extra
 * (o cliente escolhe zero, uma, ou várias), mas com imagem e descrição
 * próprias, diferente de Extra (que é só checklist sem foto).
 */
@Document(collection = "drinks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Drink {

    @Id
    private String id;

    private String name;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    private boolean active;

    private Integer displayOrder;
}