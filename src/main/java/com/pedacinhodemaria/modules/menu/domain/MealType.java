package com.pedacinhodemaria.modules.menu.domain;

/**
 * Distingue os 4 pratos fixos (sempre no cardápio) do prato do dia (trocado
 * diariamente pelo Owner). Essa distinção existe porque a tela inicial do
 * cliente precisa renderizar os dois grupos de forma visualmente diferente
 * (o prato do dia costuma ganhar destaque).
 */
public enum MealType {
    FIXED,
    DAILY
}