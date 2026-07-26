import { createElement, formatCurrency } from '../utils/domHelpers.js';

/**
 * Renderiza a lista de pratos como cards clicáveis. `onSelect` é chamado com
 * o objeto do prato inteiro (não só o id) para o próximo passo do wizard já
 * ter nome/preço/tempo de preparo disponíveis sem precisar buscar de novo.
 */
export function renderMenu(container, meals, onSelect) {
    container.innerHTML = '';

    if (meals.length === 0) {
        container.append(createElement('p', { className: 'menu-empty-state' },
            ['Cardápio indisponível no momento. Volte em instantes.']));
        return;
    }

    for (const meal of meals) {
        const card = createElement('button', {
            className: 'meal-card',
            type: 'button',
            'aria-label': `Selecionar ${meal.name}`,
            onClick: () => onSelect(meal),
        }, [
            createElement('img', {
                className: 'meal-card__image',
                src: meal.imageUrl || 'assets/icons/meal-placeholder.svg',
                alt: meal.name,
                loading: 'lazy',
                onError: (e) => { e.target.src = 'assets/icons/meal-placeholder.svg'; },
            }),
            createElement('div', { className: 'meal-card__content' }, [
                createElement('h3', { className: 'meal-card__name' }, [meal.name]),
                createElement('p', { className: 'meal-card__description' }, [meal.description || '']),
                createElement('div', { className: 'meal-card__footer' }, [
                    createElement('span', { className: 'meal-card__price' }, [formatCurrency(meal.price)]),
                    createElement('span', { className: 'meal-card__prep-time' },
                        [`~${meal.estimatedPrepTimeMinutes} min`]),
                ]),
            ]),
        ]);

        container.append(card);
    }
}

/**
 * Acompanhamentos: seleção única, com imagem — visualmente parecido com o
 * card de prato, mas menor, porque é uma decisão secundária no fluxo.
 * `selectedId` permite re-renderizar destacando a escolha atual quando o
 * cliente volta pra essa etapa depois de já ter escolhido.
 */
export function renderSideDishes(container, sideDishes, selectedId, onSelect) {
    container.innerHTML = '';

    if (sideDishes.length === 0) {
        container.append(createElement('p', { className: 'menu-empty-state' },
            ['Nenhum acompanhamento disponível no momento.']));
        return;
    }

    for (const sideDish of sideDishes) {
        const isSelected = sideDish.id === selectedId;
        const card = createElement('button', {
            className: `side-dish-card${isSelected ? ' side-dish-card--selected' : ''}`,
            type: 'button',
            'aria-pressed': String(isSelected),
            onClick: () => onSelect(sideDish),
        }, [
            createElement('img', {
                className: 'side-dish-card__image',
                src: sideDish.imageUrl || 'assets/icons/meal-placeholder.svg',
                alt: sideDish.name,
                loading: 'lazy',
                onError: (e) => { e.target.src = 'assets/icons/meal-placeholder.svg'; },
            }),
            createElement('span', { className: 'side-dish-card__name' }, [sideDish.name]),
            sideDish.price
                ? createElement('span', { className: 'side-dish-card__price' }, [`+${formatCurrency(sideDish.price)}`])
                : createElement('span', { className: 'side-dish-card__price side-dish-card__price--included' }, ['Incluso']),
        ]);

        container.append(card);
    }
}

/**
 * Extras: lista de checkboxes simples, deliberadamente sem imagem — decisão
 * de produto explícita (a tela de extras não é ilustrada como o cardápio
 * principal). `selectedIds` é um Set para lookup O(1) a cada render.
 */
export function renderExtras(container, extras, selectedIds, onToggle) {
    container.innerHTML = '';

    if (extras.length === 0) {
        container.append(createElement('p', { className: 'menu-empty-state' }, ['Nenhum extra disponível hoje.']));
        return;
    }

    for (const extra of extras) {
        const isChecked = selectedIds.has(extra.id);
        const label = createElement('label', { className: 'extra-item' }, [
            createElement('input', {
                type: 'checkbox',
                checked: isChecked,
                onChange: () => onToggle(extra),
            }),
            createElement('span', { className: 'extra-item__name' }, [extra.name]),
            createElement('span', { className: 'extra-item__price' }, [`+${formatCurrency(extra.price)}`]),
        ]);
        container.append(label);
    }
}
