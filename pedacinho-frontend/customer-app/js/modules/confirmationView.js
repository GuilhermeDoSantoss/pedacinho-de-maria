import { qs, createElement, formatCurrency } from '../utils/domHelpers.js';

const STATUS_LABELS = {
    RECEIVED: 'Recebido',
    PREPARING: 'Em preparo',
    READY: 'Pronto para retirada',
    DELIVERED: 'Entregue',
    CANCELLED: 'Cancelado',
};

const PAYMENT_LABELS = {
    CASH: 'Dinheiro',
    PIX: 'PIX',
    CREDIT_CARD: 'Cartão de crédito',
    DEBIT_CARD: 'Cartão de débito',
};

const ORDER_TYPE_LABELS = {
    DINE_IN: 'Consumir no local',
    TAKEAWAY: 'Para viagem',
};

/**
 * Renderiza a tela de confirmação. `order` é a OrderResponse completa vinda
 * do POST /orders — já contém o status inicial (RECEIVED), então a primeira
 * renderização não depende do WebSocket ter conectado ainda.
 */
export function renderConfirmation(container, order) {
    container.innerHTML = '';

    const details = [
        createElement('dt', {}, ['Nome']),
        createElement('dd', {}, [order.customerName]),
        createElement('dt', {}, ['Prato']),
        createElement('dd', {}, [order.mealName]),
    ];

    if (order.sideDishName) {
        details.push(
            createElement('dt', {}, ['Acompanhamento']),
            createElement('dd', {}, [order.sideDishName]),
        );
    }

    if (order.extras && order.extras.length > 0) {
        const extrasText = order.extras.map((e) => e.extraName).join(', ');
        details.push(createElement('dt', {}, ['Extras']), createElement('dd', {}, [extrasText]));
    }

    if (order.drinks && order.drinks.length > 0) {
        const drinksText = order.drinks.map((d) => d.drinkName).join(', ');
        details.push(createElement('dt', {}, ['Bebidas']), createElement('dd', {}, [drinksText]));
    }

    details.push(
        createElement('dt', {}, ['Total']),
        createElement('dd', { className: 'confirmation__total-value' }, [formatCurrency(order.totalPrice)]),
        createElement('dt', {}, ['Retirada']),
        createElement('dd', {}, [order.pickupTime]),
        createElement('dt', {}, ['Tipo']),
        createElement('dd', {}, [ORDER_TYPE_LABELS[order.orderType] ?? order.orderType]),
    );

    // Cutelaria só aparece na confirmação quando é relevante (para viagem) —
    // omitir a linha inteira para DINE_IN, em vez de mostrar "Cutelaria: —".
    if (order.orderType === 'TAKEAWAY') {
        details.push(
            createElement('dt', {}, ['Talher descartável']),
            createElement('dd', {}, [order.needsDisposableCutlery ? 'Sim' : 'Não']),
        );
    }

    details.push(
        createElement('dt', {}, ['Pagamento']),
        createElement('dd', {}, [PAYMENT_LABELS[order.paymentMethod] ?? order.paymentMethod]),
    );

    container.append(
        createElement('div', { className: 'confirmation' }, [
            createElement('div', { className: 'confirmation__icon' }, ['✓']),
            createElement('h2', { className: 'confirmation__title' }, ['Seu pedido foi confirmado!']),
            createElement('p', { className: 'confirmation__order-code' }, [order.orderCode]),

            createElement('dl', { className: 'confirmation__details' }, details),

            createElement('div', { className: 'confirmation__status', id: 'live-status' }, [
                createElement('span', { className: 'status-badge', id: 'status-badge' },
                    [STATUS_LABELS[order.status] ?? order.status]),
            ]),

            createElement('button', {
                className: 'btn btn--secondary',
                type: 'button',
                onClick: () => window.location.reload(),
            }, ['Fazer novo pedido']),
        ])
    );
}

/** Atualiza só o badge de status quando um evento chega pelo WebSocket — sem re-renderizar a tela inteira. */
export function updateStatusBadge(newStatus) {
    const badge = qs('#status-badge');
    if (!badge) return; // usuário pode ter saído da tela de confirmação

    badge.textContent = STATUS_LABELS[newStatus] ?? newStatus;
    badge.dataset.status = newStatus;
}
