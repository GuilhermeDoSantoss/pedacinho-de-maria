/**
 * Utilitários mínimos de DOM. Não é um mini-framework — só as 5 operações
 * repetidas em todo módulo (selecionar, mostrar/esconder, criar elemento).
 * Existir aqui evita duplicar `document.querySelector` com tratamento de
 * null em 10 arquivos diferentes.
 */

export function qs(selector, scope = document) {
    return scope.querySelector(selector);
}

export function qsa(selector, scope = document) {
    return Array.from(scope.querySelectorAll(selector));
}

export function show(element) {
    element.classList.remove('hidden');
}

export function hide(element) {
    element.classList.add('hidden');
}

/**
 * Cria um elemento com atributos e filhos em uma chamada — reduz o
 * boilerplate de createElement + setAttribute + appendChild repetido em
 * cada função de renderização.
 */
export function createElement(tag, attributes = {}, children = []) {
    const element = document.createElement(tag);

    for (const [key, value] of Object.entries(attributes)) {
        if (key === 'className') {
            element.className = value;
        } else if (key === 'dataset') {
            Object.assign(element.dataset, value);
        } else if (key.startsWith('on') && typeof value === 'function') {
            element.addEventListener(key.slice(2).toLowerCase(), value);
        } else if (typeof value === 'boolean') {
            // Atributos booleanos do HTML (checked, disabled, required...) são
            // "presença = true" no DOM — setAttribute('checked', false) ainda
            // deixaria o elemento marcado, porque o valor string "false" conta
            // como presente. Setar a propriedade IDL diretamente (element.checked
            // = false) é o jeito correto de refletir o valor booleano real.
            element[key] = value;
        } else {
            element.setAttribute(key, value);
        }
    }

    for (const child of children) {
        element.append(child instanceof Node ? child : document.createTextNode(child));
    }

    return element;
}

/** Formata BigDecimal vindo do backend (ex.: 28.9) como moeda brasileira. */
export function formatCurrency(value) {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
}
