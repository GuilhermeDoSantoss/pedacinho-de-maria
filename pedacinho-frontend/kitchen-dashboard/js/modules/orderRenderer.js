import { createElement, formatCurrency } from '../utils/domHelpers.js';

const ORDER_TYPE_LABELS = { DINE_IN: 'Local', TAKEAWAY: 'Viagem' };
const PAYMENT_LABELS = { CASH: 'Dinheiro', PIX: 'PIX', CREDIT_CARD: 'Crédito', DEBIT_CARD: 'Débito' };

const NEXT_ACTION_LABEL = {
    RECEIVED: 'Iniciar preparo',
    PREPARING: 'Marcar como pronto',
    READY: 'Marcar como entregue',
};

const NEXT_STATUS = {
    RECEIVED: 'PREPARING',
    PREPARING: 'READY',
    READY: 'DELIVERED',
};

/**
 * Monta o ticket completo de um pedido. `onAdvance` é chamado com
 * (orderCode, nextStatus) quando o funcionário confirma o avanço — a decisão
 * de QUAL é o próximo status vive aqui (NEXT_STATUS), não espalhada pelos
 * botões de cada coluna.
 */
export function createOrderTicket(order, onAdvance) {
    const ticket = createElement('article', {
        className: 'ticket',
        dataset: { orderCode: order.orderCode, status: order.status, timerState: order.timerState },
    });

    const showsTimer = order.status === 'RECEIVED' || order.status === 'PREPARING';

    ticket.append(
        createElement('header', { className: 'ticket__header' }, [
            createElement('span', { className: 'ticket__code' }, [order.orderCode]),
            showsTimer
                ? createElement('span', { className: 'ticket__timer', dataset: { role: 'timer-badge' } },
                    [timerLabel(order.timerState)])
                : createElement('span', {}, []),
        ]),

        createElement('h3', { className: 'ticket__customer' }, [order.customerName]),
        createElement('p', { className: 'ticket__meal' }, [order.mealName]),
        order.sideDishName
            ? createElement('p', { className: 'ticket__side-dish' }, [`+ ${order.sideDishName}`])
            : createElement('span', {}, []),

        order.extras && order.extras.length > 0
            ? createElement('p', { className: 'ticket__extras' },
                [`Extras: ${order.extras.map((e) => e.extraName).join(', ')}`])
            : createElement('span', {}, []),

        createElement('div', { className: 'ticket__meta' }, [
            createElement('span', { className: `ticket__badge ticket__badge--${order.orderType.toLowerCase()}` },
                [ORDER_TYPE_LABELS[order.orderType]]),
            createElement('span', { className: 'ticket__badge' }, [order.pickupTime]),
            createElement('span', { className: 'ticket__badge' }, [PAYMENT_LABELS[order.paymentMethod]]),
        ]),

        order.orderType === 'TAKEAWAY'
            ? createElement('p', { className: 'ticket__cutlery' },
                [`Talher descartável: ${order.needsDisposableCutlery ? 'Sim' : 'Não'}`])
            : createElement('span', {}, []),

        order.observation
            ? createElement('p', { className: 'ticket__observation' }, [`"${order.observation}"`])
            : createElement('span', {}, []),

        // Total do pedido (prato + acompanhamento + extras), não só o preço do
        // prato principal — é o valor que a cozinha/caixa precisa conferir.
        createElement('p', { className: 'ticket__price' }, [formatCurrency(order.totalPrice)]),
    );

    if (NEXT_ACTION_LABEL[order.status]) {
        ticket.append(
            createElement('button', {
                className: 'ticket__action',
                type: 'button',
                onClick: () => onAdvance(order.orderCode, NEXT_STATUS[order.status]),
            }, [NEXT_ACTION_LABEL[order.status]])
        );
    }

    return ticket;
}

/** Atualiza só o badge de timer de um ticket já renderizado — sem recriar o DOM inteiro. */
export function updateTicketTimerState(ticket, timerState) {
    ticket.dataset.timerState = timerState;
    const badge = ticket.querySelector('[data-role="timer-badge"]');
    if (badge) badge.textContent = timerLabel(timerState);
}

function timerLabel(timerState) {
    if (timerState === 'RED') return 'Atrasado';
    if (timerState === 'YELLOW') return 'Atenção';
    return 'No prazo';
}
