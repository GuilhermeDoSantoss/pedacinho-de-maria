import { qs, qsa, show, hide, formatCurrency } from '../utils/domHelpers.js';
import { validateCustomerName, validatePickupTime, validateObservation } from '../utils/validation.js';
import { createOrder, ApiRequestError } from '../api/orderApi.js';
import { fetchSideDishes, fetchExtras } from '../api/menuApi.js';
import { renderSideDishes, renderExtras } from './menuRenderer.js';
import { renderConfirmation } from './confirmationView.js';

/**
 * Estado do pedido em construção. `extras` é um Map<id, extraObject> (não um
 * Set de ids) porque a etapa de observações/confirmação precisa do nome e
 * preço de cada extra selecionado, não só do id — evita ter que buscar de
 * novo algo que já veio na resposta de fetchExtras().
 */
const draft = {
    meal: null,
    sideDish: null,
    extras: new Map(),
    customerName: '',
    pickupTime: '',
    orderType: '',
    needsDisposableCutlery: null,
    paymentMethod: '',
    observation: '',
};

let allSideDishes = [];
let allExtras = [];
// Guardam a Promise em voo (não só o resultado) — sem isso, 2 chamadas quase
// simultâneas (ex.: duplo clique no "Continuar") passariam pelo "já carreguei?"
// ao mesmo tempo, ambas veriam a lista ainda vazia, e disparariam 2 fetches.
let sideDishesLoadPromise = null;
let extrasLoadPromise = null;

/**
 * Navegação por NOME de etapa — ver decisão registrada quando a etapa de
 * cutelaria (condicional) foi adicionada. Side dish e extras entram na
 * mesma lógica: nomear o destino de cada transição evita recalcular índices
 * toda vez que o wizard ganha ou perde uma etapa.
 */
let currentStep = 'menu';

export function initOrderForm() {
    qs('#confirm-meal-continue').addEventListener('click', () => goToStep('side-dish'));
    qs('#confirm-meal-back').addEventListener('click', () => goToStep('menu'));

    qs('#side-dish-continue').addEventListener('click', handleSideDishStep);
    qs('#side-dish-back').addEventListener('click', () => goToStep('confirm-meal'));

    qs('#extras-continue').addEventListener('click', () => goToStep('name'));
    qs('#extras-back').addEventListener('click', () => goToStep('side-dish'));

    qs('#name-continue').addEventListener('click', handleNameStep);
    qs('#name-back').addEventListener('click', () => goToStep('extras'));

    qs('#pickup-time-continue').addEventListener('click', handlePickupTimeStep);
    qs('#pickup-time-back').addEventListener('click', () => goToStep('name'));

    qs('#order-type-continue').addEventListener('click', handleOrderTypeStep);
    qs('#order-type-back').addEventListener('click', () => goToStep('pickup-time'));

    qs('#cutlery-continue').addEventListener('click', handleCutleryStep);
    qs('#cutlery-back').addEventListener('click', () => goToStep('order-type'));

    qs('#payment-continue').addEventListener('click', handlePaymentStep);
    qs('#payment-back').addEventListener('click', handlePaymentBack);

    qs('#notes-submit').addEventListener('click', handleSubmit);
    qs('#notes-back').addEventListener('click', () => goToStep('payment'));
}

/** Chamado pelo menuRenderer quando o cliente toca em um prato. */
export function selectMeal(meal) {
    draft.meal = meal;

    qs('#confirm-meal-name').textContent = meal.name;
    qs('#confirm-meal-description').textContent = meal.description || '';
    qs('#confirm-meal-price').textContent = formatCurrency(meal.price);
    qs('#confirm-meal-prep-time').textContent = `Tempo estimado: ~${meal.estimatedPrepTimeMinutes} min`;

    goToStep('side-dish');
}

/**
 * Acompanhamentos e extras são buscados uma vez, no primeiro acesso a cada
 * etapa — não no bootstrap da página inteira — porque não há necessidade de
 * pagar essa chamada de rede para um cliente que abandona o pedido na tela
 * do prato principal.
 */
async function loadSideDishesIfNeeded() {
    if (allSideDishes.length > 0) return;
    // Reaproveita a mesma Promise se uma chamada já está em andamento, em vez
    // de checar só o resultado final — fecha a janela de corrida do duplo clique.
    sideDishesLoadPromise ??= fetchSideDishes();
    allSideDishes = await sideDishesLoadPromise;
    sideDishesLoadPromise = null;
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
    if (!draft.sideDish) {
        showFieldError('#side-dish-error', 'Escolha um acompanhamento');
        return;
    }
    showFieldError('#side-dish-error', null);
    goToStep('extras');
}

async function handleExtrasStepEnter() {
    const container = qs('#extras-list');
    try {
        await loadExtrasIfNeeded();
        renderExtrasAndTotal(container);
    } catch (err) {
        console.error('Falha ao carregar extras:', err);
        showFieldError('#extras-error', 'Não foi possível carregar os extras. Tente novamente.');
    }
}

function renderExtrasAndTotal(container) {
    const selectedIds = new Set(draft.extras.keys());
    renderExtras(container, allExtras, selectedIds, (extra) => {
        if (draft.extras.has(extra.id)) {
            draft.extras.delete(extra.id);
        } else {
            draft.extras.set(extra.id, extra);
        }
        renderExtrasAndTotal(container);
    });
    updateEstimatedTotal();
}

/**
 * Total ESTIMADO, só para UX — recalculado localmente a cada seleção pra dar
 * feedback instantâneo. O backend recalcula o valor real na criação do
 * pedido (CreateOrderUseCase.calculateTotal) e esse é o valor que de fato
 * é cobrado; se os dois divergissem por algum motivo, o backend sempre vence.
 */
function updateEstimatedTotal() {
    const mealPrice = draft.meal?.price ?? 0;
    const sideDishPrice = draft.sideDish?.price ?? 0;
    const extrasTotal = Array.from(draft.extras.values()).reduce((sum, e) => sum + e.price, 0);
    const total = mealPrice + sideDishPrice + extrasTotal;

    const totalEl = qs('#extras-estimated-total');
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
        goToStep('cutlery');
    } else {
        draft.needsDisposableCutlery = null;
        goToStep('payment');
    }
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
            sideDishId: draft.sideDish.id,
            extraIds: Array.from(draft.extras.keys()),
            pickupTime: draft.pickupTime,
            orderType: draft.orderType,
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
}
