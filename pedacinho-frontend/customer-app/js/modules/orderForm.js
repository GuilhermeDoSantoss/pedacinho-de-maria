import { qs, qsa, show, hide, formatCurrency, createElement } from '../utils/domHelpers.js';
import { validateCustomerName, validatePickupTime, validateObservation, validatePhoneNumber } from '../utils/validation.js';
import { createOrder, ApiRequestError } from '../api/orderApi.js';
import { fetchSideDishes, fetchDrinks, fetchExtras } from '../api/menuApi.js';
import { renderSideDishes, renderDrinks, renderExtras } from './menuRenderer.js';
import { renderConfirmation } from './confirmationView.js';

/**
 * Estado do pedido em construção. `extras` e `drinks` são Map<id, objeto>
 * (não Set de ids) porque a etapa de observações/confirmação precisa do
 * nome e preço de cada item selecionado, não só do id.
 */
const draft = {
    meal: null,
    sideDish: null,
    extras: new Map(),
    drinks: new Map(),
    customerName: '',
    pickupTime: '',
    orderType: '',
    phoneNumber: '',
    needsDisposableCutlery: null,
    paymentMethod: '',
    observation: '',
};

let allSideDishes = [];
let allDrinks = [];
let allExtras = [];
// Guardam a Promise em voo (não só o resultado) — sem isso, 2 chamadas quase
// simultâneas (ex.: duplo clique no "Continuar") passariam pelo "já carreguei?"
// ao mesmo tempo, ambas veriam a lista ainda vazia, e disparariam 2 fetches.
let sideDishesLoadPromise = null;
let drinksLoadPromise = null;
let extrasLoadPromise = null;

/**
 * Navegação por NOME de etapa. Fluxo atual:
 *
 * menu -> confirm-meal -> [side-dish] -> extras -> drinks -> name ->
 * pickup-time -> order-type -> [phone -> cutlery] -> payment -> notes ->
 * confirmation
 *
 * Extras e Bebidas são etapas independentes (cada uma com seu próprio
 * fetch, sua própria renderização e seu próprio total estimado) — antes
 * dividiam a mesma tela e o mesmo container; separar evita ter que inflar
 * a lógica de uma única etapa toda vez que uma das duas mudar.
 */
let currentStep = 'menu';

export function initOrderForm() {
    qs('#confirm-meal-continue').addEventListener('click', () => goToStep(nextStepAfterMealSelection()));
    qs('#confirm-meal-back').addEventListener('click', () => goToStep('menu'));

    qs('#side-dish-continue').addEventListener('click', handleSideDishStep);
    qs('#side-dish-back').addEventListener('click', () => goToStep('confirm-meal'));

    qs('#extras-continue').addEventListener('click', () => goToStep('drinks'));
    qs('#extras-back').addEventListener('click', () => goToStep(nextStepBeforeExtrasSelection()));

    qs('#drinks-continue').addEventListener('click', () => goToStep('name'));
    qs('#drinks-back').addEventListener('click', () => goToStep('extras'));

    qs('#name-continue').addEventListener('click', handleNameStep);
    qs('#name-back').addEventListener('click', () => goToStep('drinks'));

    qs('#pickup-time-continue').addEventListener('click', handlePickupTimeStep);
    qs('#pickup-time-back').addEventListener('click', () => goToStep('name'));

    qs('#order-type-continue').addEventListener('click', handleOrderTypeStep);
    qs('#order-type-back').addEventListener('click', () => goToStep('pickup-time'));

    qs('#phone-continue').addEventListener('click', handlePhoneStep);
    qs('#phone-back').addEventListener('click', handlePhoneBack);

    qs('#cutlery-continue').addEventListener('click', handleCutleryStep);
    qs('#cutlery-back').addEventListener('click', () => goToStep(draft.orderType === 'TAKEAWAY' ? 'phone' : 'order-type'));

    qs('#payment-continue').addEventListener('click', handlePaymentStep);
    qs('#payment-back').addEventListener('click', handlePaymentBack);

    qs('#notes-submit').addEventListener('click', handleSubmit);
    qs('#notes-back').addEventListener('click', () => goToStep('payment'));

    qs('#input-phone-number').addEventListener('input', applyPhoneMask);
}

/** Chamado pelo menuRenderer quando o cliente toca em um prato. */
export function selectMeal(meal) {
    draft.meal = meal;
    draft.sideDish = null;

    qs('#confirm-meal-name').textContent = meal.name;
    qs('#confirm-meal-description').textContent = meal.description || '';
    qs('#confirm-meal-price').textContent = formatCurrency(meal.price);
    qs('#confirm-meal-prep-time').textContent = `Tempo estimado: ~${meal.estimatedPrepTimeMinutes} min`;

    goToStep(nextStepAfterMealSelection());
}

/**
 * Acompanhamentos, extras e bebidas são buscados uma vez, no primeiro
 * acesso a cada etapa — não no bootstrap da página inteira — porque não há
 * necessidade de pagar essa chamada de rede para um cliente que abandona o
 * pedido antes de chegar lá.
 */
async function loadSideDishesIfNeeded() {
    if (allSideDishes.length > 0) return;
    // Reaproveita a mesma Promise se uma chamada já está em andamento, em vez
    // de checar só o resultado final — fecha a janela de corrida do duplo clique.
    sideDishesLoadPromise ??= fetchSideDishes();
    allSideDishes = await sideDishesLoadPromise;
    sideDishesLoadPromise = null;
}

async function loadDrinksIfNeeded() {
    if (allDrinks.length > 0) return;
    drinksLoadPromise ??= fetchDrinks();
    allDrinks = await drinksLoadPromise;
    drinksLoadPromise = null;
}

async function loadExtrasIfNeeded() {
    if (allExtras.length > 0) return;
    extrasLoadPromise ??= fetchExtras();
    allExtras = await extrasLoadPromise;
    extrasLoadPromise = null;
}

async function handleSideDishStepEnter() {
    const container = qs('#side-dish-list');
    try {
        await loadSideDishesIfNeeded();
        renderSideDishList(container);
    } catch (err) {
        console.error('Falha ao carregar acompanhamentos:', err);
        showFieldError('#side-dish-error', 'Não foi possível carregar os acompanhamentos. Tente novamente.');
    }
}

/**
 * Função nomeada (não uma arrow function anônima recursiva) para poder se
 * referenciar a si mesma ao re-renderizar após uma seleção — `arguments.callee`
 * faria o mesmo, mas é proibido em strict mode, e todo módulo ES6 roda em
 * strict mode por padrão. Isso teria quebrado a troca de acompanhamento em
 * runtime, não no carregamento da página.
 */
function renderSideDishList(container) {
    renderSideDishes(container, allSideDishes, draft.sideDish?.id, (sideDish) => {
        draft.sideDish = sideDish;
        renderSideDishList(container);
    });
}

function handleSideDishStep() {
    if (draft.meal?.requiresSideDish === false) {
        showFieldError('#side-dish-error', null);
        goToStep('extras');
        return;
    }

    if (!draft.sideDish) {
        showFieldError('#side-dish-error', 'Escolha um acompanhamento');
        return;
    }
    showFieldError('#side-dish-error', null);
    goToStep('extras');
}

/**
 * ---------- Extras (etapa própria) ----------
 */
async function handleExtrasStepEnter() {
    const container = qs('#extras-list');
    try {
        await loadExtrasIfNeeded();
        renderExtrasStep(container);
    } catch (err) {
        console.error('Falha ao carregar extras:', err);
        showFieldError('#extras-error', 'Não foi possível carregar os extras. Tente novamente.');
    }
}

function renderExtrasStep(container) {
    const selectedExtraIds = new Set(draft.extras.keys());

    renderExtras(container, allExtras, selectedExtraIds, (extra) => {
        if (draft.extras.has(extra.id)) {
            draft.extras.delete(extra.id);
        } else {
            draft.extras.set(extra.id, extra);
        }
        renderExtrasStep(container);
    });

    updateEstimatedTotal('#extras-estimated-total');
}

/**
 * ---------- Bebidas (etapa própria) ----------
 */
async function handleDrinksStepEnter() {
    const container = qs('#drinks-list');
    try {
        await loadDrinksIfNeeded();
        renderDrinksStep(container);
    } catch (err) {
        console.error('Falha ao carregar bebidas:', err);
        showFieldError('#drinks-error', 'Não foi possível carregar as bebidas. Tente novamente.');
    }
}

function renderDrinksStep(container) {
    const selectedDrinkIds = new Set(draft.drinks.keys());

    renderDrinks(container, allDrinks, selectedDrinkIds, (drink) => {
        if (draft.drinks.has(drink.id)) {
            draft.drinks.delete(drink.id);
        } else {
            draft.drinks.set(drink.id, drink);
        }
        renderDrinksStep(container);
    });

    updateEstimatedTotal('#drinks-estimated-total');
}

/**
 * Total ESTIMADO, só para UX — recalculado localmente a cada seleção pra dar
 * feedback instantâneo (aparece tanto na etapa de Extras quanto na de
 * Bebidas, sempre como total acumulado do pedido até aquele ponto). O
 * backend recalcula o valor real na criação do pedido
 * (CreateOrderUseCase.calculateTotal) e esse é o valor que de fato é
 * cobrado; se os dois divergirem por algum motivo, o backend sempre vence.
 */
function updateEstimatedTotal(targetSelector) {
    const mealPrice = draft.meal?.price ?? 0;
    const sideDishPrice = draft.sideDish?.price ?? 0;
    const extrasTotal = Array.from(draft.extras.values()).reduce((sum, e) => sum + e.price, 0);
    const drinksTotal = Array.from(draft.drinks.values()).reduce((sum, d) => sum + d.price, 0);
    const total = mealPrice + sideDishPrice + extrasTotal + drinksTotal;

    const totalEl = qs(targetSelector);
    if (totalEl) totalEl.textContent = `Total estimado: ${formatCurrency(total)}`;
}

function handleNameStep() {
    const input = qs('#input-customer-name');
    const error = validateCustomerName(input.value);
    if (!showFieldError('#name-error', error)) return;

    draft.customerName = input.value.trim();
    goToStep('pickup-time');
}

function handlePickupTimeStep() {
    const input = qs('#input-pickup-time');
    const error = validatePickupTime(input.value);
    if (!showFieldError('#pickup-time-error', error)) return;

    draft.pickupTime = input.value;
    goToStep('order-type');
}

function handleOrderTypeStep() {
    const selected = qsa('input[name="order-type"]').find((radio) => radio.checked);
    if (!selected) {
        showFieldError('#order-type-error', 'Escolha se é para consumir no local ou levar');
        return;
    }

    draft.orderType = selected.value;

    if (draft.orderType === 'TAKEAWAY') {
        qs('#input-phone-number').value = draft.phoneNumber ? formatPhoneNumber(draft.phoneNumber) : '';
        goToStep('phone');
        return;
    }

    draft.phoneNumber = '';
    draft.needsDisposableCutlery = null;
    goToStep('payment');
}

function handlePhoneStep() {
    const input = qs('#input-phone-number');
    const error = validatePhoneNumber(input.value, draft.orderType === 'TAKEAWAY');
    if (!showFieldError('#phone-error', error)) return;

    draft.phoneNumber = normalizePhoneNumber(input.value);
    goToStep('cutlery');
}

function handlePhoneBack() {
    goToStep('order-type');
}

function applyPhoneMask(event) {
    const input = event.currentTarget;
    const digitsOnly = (input.value || '').replace(/\D/g, '').slice(0, 11);
    input.value = formatPhoneNumber(digitsOnly);
}

function normalizePhoneNumber(value) {
    const digitsOnly = (value || '').replace(/\D/g, '');
    return digitsOnly || null;
}

function formatPhoneNumber(value) {
    const digitsOnly = (value || '').toString().replace(/\D/g, '').slice(0, 11);

    if (digitsOnly.length <= 2) return `(${digitsOnly}`;
    if (digitsOnly.length <= 6) return `(${digitsOnly.slice(0, 2)}) ${digitsOnly.slice(2)}`;
    if (digitsOnly.length <= 10) return `(${digitsOnly.slice(0, 2)}) ${digitsOnly.slice(2, 6)}-${digitsOnly.slice(6)}`;
    return `(${digitsOnly.slice(0, 2)}) ${digitsOnly.slice(2, 7)}-${digitsOnly.slice(7, 11)}`;
}

function handleCutleryStep() {
    const selected = qsa('input[name="disposable-cutlery"]').find((radio) => radio.checked);
    if (!selected) {
        showFieldError('#cutlery-error', 'Escolha uma opção');
        return;
    }

    draft.needsDisposableCutlery = selected.value === 'yes';
    goToStep('payment');
}

function handlePaymentStep() {
    const selected = qsa('input[name="payment-method"]').find((radio) => radio.checked);
    if (!selected) {
        showFieldError('#payment-error', 'Escolha uma forma de pagamento');
        return;
    }

    draft.paymentMethod = selected.value;
    goToStep('notes');
}

function handlePaymentBack() {
    goToStep(draft.orderType === 'TAKEAWAY' ? 'cutlery' : 'order-type');
}

async function handleSubmit() {
    const observationInput = qs('#input-observation');
    const error = validateObservation(observationInput.value);
    if (!showFieldError('#notes-error', error)) return;

    draft.observation = observationInput.value.trim();

    const submitButton = qs('#notes-submit');
    submitButton.disabled = true;
    submitButton.textContent = 'Enviando...';

    try {
        const order = await createOrder({
            customerName: draft.customerName,
            mealId: draft.meal.id,
            sideDishId: draft.meal?.requiresSideDish ? draft.sideDish?.id ?? null : null,
            extraIds: Array.from(draft.extras.keys()),
            drinkIds: Array.from(draft.drinks.keys()),
            pickupTime: draft.pickupTime,
            orderType: draft.orderType,
            phoneNumber: draft.orderType === 'TAKEAWAY' ? draft.phoneNumber : null,
            needsDisposableCutlery: draft.needsDisposableCutlery,
            paymentMethod: draft.paymentMethod,
            observation: draft.observation || null,
        });

        renderConfirmation(qs('#step-confirmation'), order);
        goToStep('confirmation');
        window.dispatchEvent(new CustomEvent('order-created', { detail: order }));
    } catch (err) {
        handleSubmitError(err);
    } finally {
        submitButton.disabled = false;
        submitButton.textContent = 'Fazer Pedido';
    }
}

function handleSubmitError(err) {
    if (err instanceof ApiRequestError) {
        showFieldError('#notes-error', err.apiError.message);
        return;
    }
    showFieldError('#notes-error', 'Não foi possível enviar o pedido. Verifique sua conexão e tente novamente.');
}

function nextStepAfterMealSelection() {
    return draft.meal?.requiresSideDish ? 'side-dish' : 'extras';
}

function nextStepBeforeExtrasSelection() {
    return draft.meal?.requiresSideDish ? 'side-dish' : 'confirm-meal';
}

function showFieldError(selector, message) {
    const errorElement = qs(selector);
    if (!errorElement) return !message;

    if (message) {
        errorElement.textContent = message;
        show(errorElement);
        return false;
    }
    hide(errorElement);
    return true;
}

function goToStep(stepName) {
    hide(qs(`#step-${currentStep}`));
    show(qs(`#step-${stepName}`));
    currentStep = stepName;
    window.scrollTo({ top: 0, behavior: 'smooth' });

    if (stepName === 'side-dish') handleSideDishStepEnter();
    if (stepName === 'extras') handleExtrasStepEnter();
    if (stepName === 'drinks') handleDrinksStepEnter();
}
