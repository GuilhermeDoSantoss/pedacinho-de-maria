package com.pedacinhodemaria.modules.order.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados possíveis de um pedido e as transições permitidas entre eles.
 *
 * A máquina de estados vive aqui, dentro do enum, em vez de dispersa em ifs
 * pelo service — assim qualquer novo status precisa declarar explicitamente
 * de onde ele pode ser alcançado, e é impossível esquecer de validar uma
 * transição em algum ponto do código.
 *
 * Fluxo normal: RECEIVED → PREPARING → READY → DELIVERED
 * Cancelamento: permitido só enquanto o pedido ainda não foi entregue.
 */
public enum OrderStatus {
    RECEIVED {
        @Override
        public Set<OrderStatus> allowedNextStates() {
            return EnumSet.of(PREPARING, CANCELLED);
        }
    },
    PREPARING {
        @Override
        public Set<OrderStatus> allowedNextStates() {
            return EnumSet.of(READY, CANCELLED);
        }
    },
    READY {
        @Override
        public Set<OrderStatus> allowedNextStates() {
            return EnumSet.of(DELIVERED);
        }
    },
    DELIVERED {
        @Override
        public Set<OrderStatus> allowedNextStates() {
            return EnumSet.noneOf(OrderStatus.class);
        }
    },
    CANCELLED {
        @Override
        public Set<OrderStatus> allowedNextStates() {
            return EnumSet.noneOf(OrderStatus.class);
        }
    };

    public abstract Set<OrderStatus> allowedNextStates();

    public boolean canTransitionTo(OrderStatus target) {
        return allowedNextStates().contains(target);
    }

    /** Estados em que o pedido ainda está ativo na cozinha e participa da varredura de timer. */
    public static Set<OrderStatus> activeStates() {
        return EnumSet.of(RECEIVED, PREPARING);
    }

    /**
     * Estados exibidos no board do Kitchen Dashboard ao carregar/recarregar a
     * página — inclui READY (diferente de activeStates(), que é só para o
     * scheduler de timer, já que um pedido pronto não precisa mais de
     * contagem regressiva). DELIVERED fica de fora propositalmente: uma vez
     * entregue, não há necessidade de continuar ocupando a query nem a tela
     * a cada reload — a coluna "Entregue" acumula só durante a sessão atual
     * do dashboard (ver columnManager.js no frontend), o que é suficiente
     * para o caso de uso ("conferir o que acabou de sair") sem crescer sem
     * limite ao longo do dia.
     */
    public static Set<OrderStatus> kitchenBoardStates() {
        return EnumSet.of(RECEIVED, PREPARING, READY);
    }
}