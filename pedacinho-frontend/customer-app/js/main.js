import { CONFIG } from './config.js';
import { qs, show, hide } from './utils/domHelpers.js';
import { fetchMenu } from './api/menuApi.js';
import { renderMenu } from './modules/menuRenderer.js';
import { initOrderForm, selectMeal } from './modules/orderForm.js';
import { updateStatusBadge } from './modules/confirmationView.js';
import { StompClient } from './modules/wsClient.js';

async function bootstrap() {
    initOrderForm();
    await loadMenu();

    // A conexão WebSocket só assina o status do pedido DEPOIS que o pedido
    // existe (ver o listener de 'order-created' abaixo) — não há orderCode
    // para assinar antes disso, e abrir uma conexão STOMP sem nenhum uso
    // imediato desperdiçaria um handshake sem necessidade.
    window.addEventListener('order-created', (event) => {
        subscribeToOrderStatus(event.detail.orderCode);
    });
}

async function loadMenu() {
    const menuContainer = qs('#menu-list');
    const loadingState = qs('#menu-loading');
    const errorState = qs('#menu-error');

    try {
        const meals = await fetchMenu();
        hide(loadingState);
        show(menuContainer);
        renderMenu(menuContainer, meals, selectMeal);
    } catch (err) {
        console.error('Falha ao carregar cardápio:', err);
        hide(loadingState);
        show(errorState);
    }
}

function subscribeToOrderStatus(orderCode) {
    const client = new StompClient(CONFIG.WS_URL);

    client.connect(() => {
        client.subscribe(`/topic/order-status/${orderCode}`, (event) => {
            updateStatusBadge(event.newStatus);
        });
    }, (err) => {
        // Falha de conexão não impede o cliente de acompanhar o pedido — ele
        // ainda vê o status inicial (RECEIVED) renderizado na tela de
        // confirmação; só não recebe atualizações ao vivo até reconectar.
        console.warn('WebSocket indisponível no momento, tentando reconectar automaticamente', err);
    });
}

document.addEventListener('DOMContentLoaded', bootstrap);
