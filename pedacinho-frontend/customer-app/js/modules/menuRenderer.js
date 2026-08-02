import { createElement, formatCurrency } from '../utils/domHelpers.js';

/**
 * ==========================
 * PRATOS
 * ==========================
 *
 * Estrutura de saída (única, sem grid/container aninhado):
 *
 * <div class="menu-section">
 *   <h3 class="step__subtitle">Pratos</h3>
 *   <div class="meal-grid">
 *     <button class="meal-card">...</button>
 *     ...
 *   </div>
 * </div>
 */
export function renderMenu(container, menu, onSelect) {
    const meals = Array.isArray(menu) ? menu : (menu?.meals ?? []);

    container.innerHTML = '';

    if (meals.length === 0) {
        container.append(
            createElement('p', { className: 'menu-empty-state' }, ['Cardápio indisponível no momento.'])
        );
        return;
    }

    const grid = createElement('div', { className: 'meal-grid' });

    const section = createElement('div', { className: 'menu-section' }, [
        createElement('h3', { className: 'step__subtitle' }, ['Pratos']),
        grid,
    ]);

    container.append(section);

    meals.forEach(meal => {
        const card = createElement('button', {
            className: 'meal-card',
            type: 'button',
            onClick: () => onSelect(meal)
        }, [
            createElement('img', {
                className: 'meal-card__image',
                src: meal.imageUrl || 'assets/icons/meal-placeholder.svg',
                alt: meal.name,
                loading: 'lazy',
                onError: e => {
                    e.target.src = 'assets/icons/meal-placeholder.svg';
                }
            }),

            createElement('div', { className: 'meal-card__content' }, [
                createElement('h3', { className: 'meal-card__name' }, [meal.name]),
                createElement('p', { className: 'meal-card__description' }, [meal.description || '']),
                createElement('div', { className: 'meal-card__footer' }, [
                    createElement('span', { className: 'meal-card__price' }, [formatCurrency(meal.price)]),
                    createElement('span', { className: 'meal-card__prep-time' }, [`~${meal.estimatedPrepTimeMinutes} min`])
                ])
            ])
        ]);

        grid.append(card);
    });
}

/**
 * ==========================
 * ACOMPANHAMENTOS
 * ==========================
 */
export function renderSideDishes(container, sideDishes, selectedId, onSelect) {

    container.innerHTML = '';

    if (sideDishes.length === 0) {
        container.append(
            createElement('p', { className: 'menu-empty-state' }, ['Nenhum acompanhamento disponível.'])
        );
        return;
    }

    const grid = createElement('div', { className: 'side-dish-grid' });
    container.append(grid);

    sideDishes.forEach(sideDish => {

        const selected = sideDish.id === selectedId;

        const card = createElement(
            'button',
            {
                className: `side-dish-card${selected ? ' side-dish-card--selected' : ''}`,
                type: 'button',
                onClick: () => onSelect(sideDish)
            },
            [
                createElement('img', {
                    className: 'side-dish-card__image',
                    src: sideDish.imageUrl || 'assets/icons/meal-placeholder.svg',
                    alt: sideDish.name,
                    loading: 'lazy',
                    onError: e => {
                        e.target.src = 'assets/icons/meal-placeholder.svg';
                    }
                }),

                createElement('span', { className: 'side-dish-card__name' }, [sideDish.name]),

                createElement('span', { className: 'side-dish-card__price' }, [
                    sideDish.price ? `+${formatCurrency(sideDish.price)}` : 'Incluso'
                ])
            ]
        );

        grid.append(card);
    });
}

/**
 * ==========================
 * BEBIDAS
 * ==========================
 *
 * Etapa própria (não mais misturada com Extras). Reaproveita exatamente o
 * mesmo layout visual dos acompanhamentos (grid de 2 colunas, imagem
 * pequena), mas com seleção múltipla via checkbox — mesmo padrão de
 * "input invisível + card reage ao :checked" já usado em .extra-item e
 * .order-type-option.
 *
 * <div class="menu-section">
 *   <h3 class="step__subtitle">Bebidas</h3>
 *   <div class="drink-grid">
 *     <label class="drink-card"><input type="checkbox">...</label>
 *     ...
 *   </div>
 * </div>
 */
export function renderDrinks(container, drinks, selectedIds, onToggle) {

    container.innerHTML = '';

    if (drinks.length === 0) {
        container.append(
            createElement('p', { className: 'menu-empty-state' }, ['Nenhuma bebida disponível.'])
        );
        return;
    }

    const grid = createElement('div', { className: 'drink-grid' });

    const section = createElement('div', { className: 'menu-section' }, [
        createElement('h3', { className: 'step__subtitle' }, ['Bebidas']),
        grid,
    ]);

    container.append(section);

    drinks.forEach(drink => {

        const checked = selectedIds.has(drink.id);

        const card = createElement('label', { className: 'drink-card' }, [
            createElement('input', {
                type: 'checkbox',
                checked,
                onChange: () => onToggle(drink)
            }),

            createElement('img', {
                className: 'drink-card__image',
                src: drink.imageUrl || 'assets/icons/meal-placeholder.svg',
                alt: drink.name,
                loading: 'lazy',
                onError: e => {
                    e.target.src = 'assets/icons/meal-placeholder.svg';
                }
            }),

            createElement('span', { className: 'drink-card__name' }, [drink.name]),
            createElement('span', { className: 'drink-card__price' }, [formatCurrency(drink.price)]),
        ]);

        grid.append(card);
    });
}

/**
 * ==========================
 * EXTRAS
 * ==========================
 */
export function renderExtras(container, extras, selectedIds, onToggle) {

    container.innerHTML = '';

    if (extras.length === 0) {
        container.append(
            createElement('p', { className: 'menu-empty-state' }, ['Nenhum extra disponível.'])
        );
        return;
    }

    const list = createElement('div', { className: 'extras-list' });

    const section = createElement('div', { className: 'menu-section' }, [
        createElement('h3', { className: 'step__subtitle' }, ['Extras']),
        list,
    ]);

    container.append(section);

    extras.forEach(extra => {

        const checked = selectedIds.has(extra.id);

        const label = createElement('label', { className: 'extra-item' }, [
            createElement('input', {
                type: 'checkbox',
                checked,
                onChange: () => onToggle(extra)
            }),

            createElement('span', { className: 'extra-item__name' }, [extra.name]),
            createElement('span', { className: 'extra-item__price' }, [`+${formatCurrency(extra.price)}`]),
        ]);

        list.append(label);
    });
}
