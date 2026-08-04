import { CONFIG } from '../config.js';

/**
 * Sem autenticação nesta versão (ver ADR no backend) — fetch simples, sem
 * header Authorization nem lógica de refresh de token.
 */
export async function fetchActiveOrders() {
    const response = await fetch(`${CONFIG.API_BASE_URL}/kitchen/orders`);
    if (!response.ok) {
        throw new Error(`Falha ao carregar pedidos (status ${response.status})`);
    }
    return response.json();
}

export async function updateOrderStatus(orderCode, newStatus) {
    const response = await fetch(`${CONFIG.API_BASE_URL}/kitchen/orders/${orderCode}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ newStatus }),
    });

    if (!response.ok) {
        const apiError = await response.json();
        throw new Error(apiError.message || 'Falha ao atualizar status do pedido');
    }

    return response.json();
}

export async function sendReadyWhatsAppMessage(orderCode) {
    const response = await fetch(
        `${CONFIG.API_BASE_URL}/orders/${encodeURIComponent(orderCode)}/whatsapp-ready-message`,
        {
            method: 'POST'
        }
    );

    if (!response.ok) {
        throw new Error("Erro ao enviar mensagem");
    }
}
