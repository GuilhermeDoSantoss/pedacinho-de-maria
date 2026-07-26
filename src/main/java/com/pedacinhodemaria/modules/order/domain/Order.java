package com.pedacinhodemaria.modules.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

/**
 * Representa um pedido único (um prato por pedido — não é um carrinho
 * multi-item; ver ADR no README sobre por que isso mantém o fluxo do
 * cliente abaixo de 1 minuto).
 *
 * Camada: domain. O índice TTL sobre `createdAt` (configurado em
 * TtlIndexInitializer) é o que permite ao Owner definir por quantos dias os
 * pedidos ficam armazenados antes de serem removidos automaticamente pelo
 * próprio MongoDB — sem job de limpeza, sem custo de manutenção.
 */
@Document(collection = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    /**
     * Código curto e aleatório (ex.: "PM-7X2K9"), não sequencial.
     * Funciona como capability token: é o único identificador que o cliente
     * recebe, e é o que dá acesso de leitura ao status do próprio pedido sem
     * exigir login. Por isso precisa ser não-adivinhável — um código
     * sequencial (PM-001, PM-002...) permitiria a qualquer pessoa varrer
     * todos os pedidos do dia trocando um número na URL.
     */
    @Field("order_code")
    private String orderCode;

    private String customerName;

    private String mealId;

    private String mealName;

    private BigDecimal mealPrice;

    /**
     * Snapshot do tempo de preparo do Meal no momento da criação do pedido.
     * Se o Owner alterar o tempo de preparo padrão de um prato depois, pedidos
     * já em andamento não são afetados — evita confundir a cozinha no meio
     * do preparo com um timer que muda de referência sozinho.
     */
    private Integer mealPrepTimeMinutes;

    /** Snapshot do acompanhamento escolhido — mesma lógica de snapshot do prato principal. */
    private String sideDishId;
    private String sideDishName;
    private BigDecimal sideDishPrice;

    /** Lista de extras escolhidos (zero ou mais), cada um já com seu snapshot de preço. */
    private List<OrderExtraSnapshot> extras;

    /** Lista de bebidas escolhidas (zero ou mais) — mesmo padrão de snapshot de extras. */
    private List<OrderDrinkSnapshot> drinks;

    /**
     * Total final calculado e persistido pelo BACKEND no momento da criação —
     * nunca recebido do frontend nem recalculado depois. O frontend mostra um
     * total estimado só para UX (ver orderForm.js), mas este campo aqui é a
     * única fonte de verdade sobre quanto o pedido realmente custa. Persistir
     * o valor (em vez de só calcular sob demanda) preserva o total correto
     * mesmo que os preços do cardápio mudem depois — mesmo raciocínio do
     * snapshot de mealPrice.
     */
    private BigDecimal totalPrice;

    private String observation;

    private OrderType orderType;

    /**
     * Só tem significado quando orderType == TAKEAWAY. Para DINE_IN este
     * campo é sempre null — não "false", null — porque false sugeriria que a
     * pergunta foi feita e respondida "não", quando na verdade a pergunta
     * nem se aplica. CreateOrderUseCase garante essa invariante na criação
     * (ver validateAndNormalizeCutlery).
     */
    private Boolean needsDisposableCutlery;

    private LocalTime pickupTime;

    private PaymentMethod paymentMethod;

    private OrderStatus status;

    /**
     * Base de cálculo do timer de preparo (ver TimerCalculator) e também o
     * campo indexado pelo TTL index que expira pedidos antigos automaticamente.
     */
    private Instant createdAt;

    private Instant updatedAt;
}