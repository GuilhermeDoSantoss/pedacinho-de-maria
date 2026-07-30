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
 * Representa um prato do cardápio (fixo ou do dia).
 *
 * Camada: domain. Persistida diretamente via Spring Data MongoDB, o volume
 * e a simplicidade do domínio (5 pratos ativos por vez) não justificam uma
 * camada de agregado/entidade rica separada do documento persistido.
 *
 * Decisão de design importante (revisada): `imageUrl` guarda um CAMINHO
 * RELATIVO servido pelo próprio Customer App (ex.:
 * "/assets/images/meals/feijoada.jpg"), não uma URL de um object storage
 * externo. As imagens do cardápio ficam versionadas junto com o frontend, em
 * `customer-app/assets/images/meals/`; o MongoDB nunca guarda binário nem
 * base64, só essa string de caminho — o Mongo continua agnóstico de onde a
 * imagem realmente mora. Essa decisão substitui uma tentativa anterior com
 * Amazon S3, revertida por decisão de produto (conta AWS não disponível).
 */
@Document(collection = "meals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meal {

    @Id
    private String id;

    private String name;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    /**
     * Tempo estimado de preparo em minutos. É copiado (snapshot) para o Order
     * no momento da criação do pedido — ver Order.mealPrepTimeMinutes para o
     * raciocínio completo por trás dessa decisão.
     */
    private Integer estimatedPrepTimeMinutes;

    private MealType type;

    /**
     * Controla se o prato aparece no cardápio do cliente. Usado para o Owner
     * pausar um prato sem precisar deletá-lo (ex.: acabou o ingrediente hoje).
     */
    private boolean active;

    /**
     * Define a ordem de exibição no cardápio. Sem esse campo, a ordem dependeria
     * da ordem de inserção no Mongo, que não é uma garantia estável nem
     * controlável pelo Owner.
     */
    private Integer displayOrder;

    /**
     * Controla se este prato exige a seleção de um acompanhamento no fluxo de
     * pedido. Quando false, o cliente pode finalizar o pedido sem escolher um
     * acompanhamento, e o backend não valida nem persiste esse campo.
     */
    private boolean requiresSideDish;
}