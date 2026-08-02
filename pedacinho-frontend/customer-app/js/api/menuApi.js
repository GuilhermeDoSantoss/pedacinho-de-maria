import { CONFIG } from '../config.js';

/**
 * Busca o cardápio do dia. Nunca hardcoded no frontend — cardápio vazio ou
 * erro de rede são estados que a UI precisa tratar explicitamente (ver
 * menuRenderer.js), nunca assumidos como "não deveria acontecer".
 */
export async function fetchMenu() {
    const response = await fetch(`${CONFIG.API_BASE_URL}/menu`);
    if (!response.ok) {
        throw new Error(`Falha ao carregar cardápio (status ${response.status})`);
    }
    return response.json();
}

/** Mesmo padrão de fetchMenu — endpoint público, sem cache local, sem hardcode. */
export async function fetchSideDishes() {
    const response = await fetch(`${CONFIG.API_BASE_URL}/side-dishes`);
    if (!response.ok) {
        throw new Error(`Falha ao carregar acompanhamentos (status ${response.status})`);
    }
    return response.json();
}

export async function fetchDrinks() {
    const response = await fetch(`${CONFIG.API_BASE_URL}/drinks`);
    if (!response.ok) {
        throw new Error(`Falha ao carregar bebidas (status ${response.status})`);
    }
    return response.json();
}

export async function fetchExtras() {
    const response = await fetch(`${CONFIG.API_BASE_URL}/extras`);
    if (!response.ok) {
        throw new Error(`Falha ao carregar extras (status ${response.status})`);
    }
    return response.json();
}
