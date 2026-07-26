import { CONFIG } from './config.js';
import { qs, show, hide } from './utils/domHelpers.js';
import { fetchActiveOrders, updateOrderStatus } from './api/ordersApi.js';
import { ColumnManager } from './modules/columnManager.js';
import { StompClient } from './modules/wsClient.js';

// Intervalo de verificação da automação de 35 minutos — não precisa ser tão
// frequente quanto o scheduler do backend (15s); o ganho de precisão de
// checar mais rápido que isso não compensa o custo de rodar a varredura toda
// hora numa tela que já recebe eventos via WebSocket para tudo o que é
// realmente urgente.
const AUTO_READY_CHECK_INTERVAL_MS = 30_000;

let columnManager;
let stompClient;

/**
 * Sem guarda de autenticação nesta versão — o dashboard abre direto no gate
 * de início de turno. O gate continua existindo como o passo inicial do
 * turno da cozinha, independente de áudio (que foi removido — ver ADR em
 * checkAutoReadyTransitions, em columnManager.js).
 */
function bootstrap() {
    qs('#start-shift-button').addEventListener('click', startShift);
}

async function startShift() {
    hide(qs('#shift-gate'));
    show(qs('#board'));

    columnManager = new ColumnManager(handleAdvance);

    await loadInitialOrders();
    connectWebSocket();

    setInterval(() => columnManager.checkAutoReadyTransitions(), AUTO_READY_CHECK_INTERVAL_MS);
}

async function loadInitialOrders() {
    try {
        const orders = await fetchActiveOrders();
        columnManager.init(orders);
    } catch (err) {
        console.error('Falha ao carregar pedidos ativos:', err);
        qs('#board-error').textContent = 'Não foi possível carregar os pedidos. Recarregue a página.';
        show(qs('#board-error'));
    }
}

async function handleAdvance(orderCode, nextStatus) {
    try {
        await updateOrderStatus(orderCode, nextStatus);
        // O ticket não é movido aqui diretamente — o backend publica
        // ORDER_STATUS_CHANGED de volta em /topic/kitchen-orders, e o handler
        // do WebSocket abaixo move o ticket. Mesmo caminho para esta e para
        // qualquer outra tela de cozinha conectada, sem duplicar lógica.
    } catch (err) {
        console.error('Falha ao atualizar status:', err);
        alert('Não foi possível atualizar o pedido. Tente novamente.');
    }
}

function connectWebSocket() {
    stompClient = new StompClient(CONFIG.WS_URL);

    stompClient.connect(() => {
        stompClient.subscribe('/topic/kitchen-orders', handleKitchenEvent);
    });
}

function handleKitchenEvent(message) {
    if (message.order) {
        columnManager.addOrder(message.order);
        return;
    }

    if (message.newStatus) {
        columnManager.moveOrder(message.orderCode, message.newStatus);
        return;
    }

    if (message.timerState) {
        // Só atualiza o badge de cor (verde/amarelo/vermelho) — o alerta
        // sonoro que existia aqui foi removido; a automação de 35 minutos em
        // checkAutoReadyTransitions() substitui a necessidade de chamar
        // atenção manualmente para pedidos atrasados.
        columnManager.updateTimerState(message.orderCode, message.timerState);
    }
}

document.addEventListener('DOMContentLoaded', bootstrap);
