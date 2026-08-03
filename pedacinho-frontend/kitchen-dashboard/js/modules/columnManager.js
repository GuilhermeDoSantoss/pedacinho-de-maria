import { qs } from '../utils/domHelpers.js';
import { createOrderTicket, updateTicketTimerState } from './orderRenderer.js';

const COLUMN_BY_STATUS = {
    RECEIVED: 'column-waiting',
    PREPARING: 'column-preparing',
    READY: 'column-ready',
    DELIVERED: 'column-delivered',
};

/**
 * Fonte de verdade do estado visual do board. Mantém um Map de orderCode →
 * {order, element} para poder mover/atualizar um ticket específico sem
 * precisar re-renderizar as 4 colunas inteiras a cada evento — importante
 * porque em um turno movimentado o board recebe eventos com frequência, e
 * recriar todo o DOM a cada um deles causaria flicker perceptível.
 */
export class ColumnManager {
    #orders = new Map(); // orderCode -> { order, element, preparingEnteredAt }
    #onAdvance;
    #onNotifyReady;

    constructor(onAdvance, onNotifyReady) {
        this.#onAdvance = onAdvance;
        this.#onNotifyReady = onNotifyReady;
    }

    /** Carga inicial (GET /kitchen/orders) — substitui tudo que existir nas colunas. */
    init(orders) {
        for (const column of Object.values(COLUMN_BY_STATUS)) {
            qs(`#${column} .column__list`).innerHTML = '';
        }
        this.#orders.clear();

        for (const order of orders) {
            this.#renderNew(order);
        }
    }

    /** Novo pedido chegou via ORDER_CREATED — sempre entra na coluna "Aguardando". */
    addOrder(order) {
        if (this.#orders.has(order.orderCode)) return; // evita duplicar se o evento chegar 2x
        this.#renderNew(order);
    }

    /** Pedido mudou de status (via ação local ou de outro dashboard conectado) — move o ticket de coluna. */
    moveOrder(orderCode, newStatus) {
        const entry = this.#orders.get(orderCode);
        if (!entry) return; // pedido não está carregado neste board (ex.: já tinha sido entregue antes do load)

        // Timer deixa de ser rastreado a partir de READY (o backend também para de
        // varrer esse pedido no scheduler) — zera aqui para não contar como
        // "vermelho pendente" em hasAnyRedTicket() depois que a cozinha já resolveu.
        const timerState = (newStatus === 'READY' || newStatus === 'DELIVERED') ? null : entry.order.timerState;

        entry.order = { ...entry.order, status: newStatus, timerState };
        entry.element.remove();

        // Marca o instante exato em que o pedido entrou em "Preparando" — base
        // para a automação de 35 minutos em checkAutoReadyTransitions().
        entry.preparingEnteredAt = newStatus === 'PREPARING' ? Date.now() : null;

        const newElement = createOrderTicket(entry.order, this.#onAdvance, this.#onNotifyReady);
        qs(`#${COLUMN_BY_STATUS[newStatus]} .column__list`).prepend(newElement);
        entry.element = newElement;
    }

    /**
     * Automação puramente visual: pedidos em "Preparando" há 35 minutos ou
     * mais são movidos para "Pronto" no DOM, sem chamar o backend. Não altera
     * Order.status no MongoDB — se a página recarregar, o pedido volta a
     * aparecer no status real (PREPARING), já que essa transição nunca foi
     * persistida. Decisão de produto explícita: automação visual para reduzir
     * esquecimento humano, sem side-effect no backend/WebSocket/API.
     */
    checkAutoReadyTransitions() {
        const THIRTY_FIVE_MINUTES_MS = 35 * 60 * 1000;
        const now = Date.now();

        for (const [orderCode, entry] of this.#orders.entries()) {
            if (entry.order.status !== 'PREPARING' || !entry.preparingEnteredAt) continue;

            if (now - entry.preparingEnteredAt >= THIRTY_FIVE_MINUTES_MS) {
                this.moveOrder(orderCode, 'READY');
            }
        }
    }

    /** @returns {boolean} true se o novo estado é RED e o anterior não era — sinal para o main.js disparar o alerta sonoro. */
    updateTimerState(orderCode, timerState) {
        const entry = this.#orders.get(orderCode);
        if (!entry) return false;

        const wasRed = entry.order.timerState === 'RED';
        entry.order.timerState = timerState;
        updateTicketTimerState(entry.element, timerState);

        return timerState === 'RED' && !wasRed;
    }

    /** @returns {boolean} true se existe pelo menos um ticket em vermelho no board (o alerta continua tocando enquanto isso for true). */
    hasAnyRedTicket() {
        return Array.from(this.#orders.values()).some((entry) => entry.order.timerState === 'RED');
    }

    #renderNew(order) {
        const element = createOrderTicket(order, this.#onAdvance, this.#onNotifyReady);
        qs(`#${COLUMN_BY_STATUS[order.status]} .column__list`).appendChild(element);

        // Para pedidos que já chegam em PREPARING na carga inicial (GET
        // /kitchen/orders), não sabemos o instante exato em que passaram para
        // esse status — só createdAt está disponível. Usamos createdAt como
        // aproximação (o pior caso é a automação de 35min disparar um pouco
        // mais cedo do que se o backend expusesse o timestamp exato).
        const preparingEnteredAt = order.status === 'PREPARING' ? new Date(order.createdAt).getTime() : null;

        this.#orders.set(order.orderCode, { order, element, preparingEnteredAt });
    }
}