/**
 * Idêntico ao domHelpers.js do Customer App. Duplicação consciente: os 2
 * frontends são deploys independentes (ciclos de release e times diferentes
 * no mundo real), e um utilitário de 30 linhas não justifica criar um
 * terceiro pacote/CDN compartilhado só para não repetir isso — o custo de
 * coordenar versão entre 2 deploys estáticos seria maior que o benefício.
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
            // Atributos booleanos do HTML (checked, disabled...) são "presença =
            // true" no DOM — setAttribute com valor false ainda deixaria o
            // elemento marcado. Setar a propriedade IDL diretamente é o certo.
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

export function formatCurrency(value) {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
}
