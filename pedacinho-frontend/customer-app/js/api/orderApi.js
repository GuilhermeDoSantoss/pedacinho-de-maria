import { CONFIG } from '../config.js';

/**
 * Erro de API com o campo `apiError` anexado — carrega o ApiError real
 * devolvido pelo backend (message, fieldErrors) para a UI decidir como
 * exibir, em vez de perder essa informação num Error genérico.
 */
export class ApiRequestError extends Error {
    constructor(apiError) {
        super(apiError.message);
        this.apiError = apiError;
    }
}

export async function fetchPickupTimePolicy() {
    const response = await fetch(`${CONFIG.API_BASE_URL}/orders/pickup-time-policy`);
    if (!response.ok) {
        throw new Error(`Falha ao carregar política de horário (status ${response.status})`);
    }
    return response.json();
}

export async function createOrder(payload) {
    const response = await fetch(`${CONFIG.API_BASE_URL}/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    });

    if (!response.ok) {
        const apiError = await response.json();
        throw new ApiRequestError(apiError);
    }

    return response.json();
}

export async function fetchOrder(orderCode) {
    const response = await fetch(`${CONFIG.API_BASE_URL}/orders/${encodeURIComponent(orderCode)}`);

    if (!response.ok) {
        const apiError = await response.json();
        throw new ApiRequestError(apiError);
    }

    return response.json();
}
