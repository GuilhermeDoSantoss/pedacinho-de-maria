package com.pedacinhodemaria.modules.order.service;

import com.pedacinhodemaria.shared.exception.InvalidPickupTimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * "Agora" fixado deliberadamente em UTC (Clock.fixed com ZoneOffset.UTC) em
 * todos os testes — não em America/Sao_Paulo — justamente para provar que
 * PickupTimePolicy converte corretamente por conta própria (via
 * clock.withZone(...)), independente da zona do Clock injetado. É esse
 * ponto que causava o bug em produção.
 */
class PickupTimePolicyTest {

    /**
     * 2026-01-15T13:00:00Z = 10:00 em America/Sao_Paulo (UTC-3) — antes da
     * abertura (11:00). Usado para testar a janela de funcionamento de
     * forma isolada, sem a regra de "não pode estar no passado" interferir
     * (qualquer horário dentro de 11:00–15:30 já é, por construção, depois
     * das 10:00 de "agora").
     */
    private static final Clock BEFORE_OPENING = Clock.fixed(Instant.parse("2026-01-15T13:00:00Z"), ZoneOffset.UTC);

    private PickupTimePolicy policyBeforeOpening() {
        return new PickupTimePolicy(BEFORE_OPENING);
    }

    // ---------- Os 9 casos pedidos, com "agora" = 10:00 BRT (antes de abrir) ----------

    @ParameterizedTest
    @ValueSource(strings = {"11:30", "12:00", "12:30", "13:00", "14:00", "15:00", "15:30"})
    void acceptsPickupTimesWithinBusinessHours(String time) {
        PickupTimePolicy policy = policyBeforeOpening();
        assertThatCode(() -> policy.validate(LocalTime.parse(time))).doesNotThrowAnyException();
    }

    @Test
    void acceptsElevenTwentyNine_withinOpeningWindow() {
        // 11:29 está dentro da janela de funcionamento (>= OPENING_TIME 11:00),
        // e depois do "agora" fixado (10:00) — deve ser aceito.
        PickupTimePolicy policy = policyBeforeOpening();
        assertThatCode(() -> policy.validate(LocalTime.of(11, 29))).doesNotThrowAnyException();
    }

    @Test
    void rejectsFifteenThirtyOne_afterClosing() {
        PickupTimePolicy policy = policyBeforeOpening();
        assertThatThrownBy(() -> policy.validate(LocalTime.of(15, 31)))
                .isInstanceOf(InvalidPickupTimeException.class)
                .hasMessage("Horário de retirada deve estar entre 11:00 e 15:30");
    }

    @Test
    void rejectsTimeBeforeOpeningWindow() {
        PickupTimePolicy policy = policyBeforeOpening();
        assertThatThrownBy(() -> policy.validate(LocalTime.of(10, 59)))
                .isInstanceOf(InvalidPickupTimeException.class)
                .hasMessage("Horário de retirada deve estar entre 11:00 e 15:30");
    }

    @Test
    void acceptsExactlyAtOpeningTime() {
        // 11:00 é o próprio OPENING_TIME — isBefore(OPENING_TIME) é falso pra
        // esse valor (comparação estrita), então o limite inferior é inclusivo.
        PickupTimePolicy policy = policyBeforeOpening();
        assertThatCode(() -> policy.validate(LocalTime.of(11, 0))).doesNotThrowAnyException();
    }

    @Test
    void acceptsExactlyAtClosingTime() {
        // 15:30 é o próprio CLOSING_TIME — isAfter(CLOSING_TIME) é falso pra
        // esse valor, então o limite superior também é inclusivo.
        PickupTimePolicy policy = policyBeforeOpening();
        assertThatCode(() -> policy.validate(LocalTime.of(15, 30))).doesNotThrowAnyException();
    }

    @Test
    void acceptsPickupTimeExactlyEqualToCurrentTime() {
        // Limite exato da regra "não pode estar no passado": pickupTime ==
        // currentTime não é isBefore(currentTime) (comparação estrita), então
        // pedir retirada para "agora mesmo" é válido, não rejeitado.
        Clock noonBrasilia = Clock.fixed(Instant.parse("2026-01-15T15:00:00Z"), ZoneOffset.UTC);
        PickupTimePolicy policy = new PickupTimePolicy(noonBrasilia);

        assertThatCode(() -> policy.validate(LocalTime.of(12, 0))).doesNotThrowAnyException();
    }

    // ---------- Regressão: reproduz o bug relatado em produção ----------

    @Test
    void acceptsPickupTimeShortlyAfterCurrentBrasiliaTime_duringBusinessHours() {
        // "agora" = 2026-01-15T15:00:00Z = 12:00 em America/Sao_Paulo.
        // Antes da correção, 12:30 era rejeitado como "no passado" porque a
        // comparação usava a hora UTC (15:00) em vez da hora de Brasília (12:00).
        Clock noonBrasilia = Clock.fixed(Instant.parse("2026-01-15T15:00:00Z"), ZoneOffset.UTC);
        PickupTimePolicy policy = new PickupTimePolicy(noonBrasilia);

        assertThatCode(() -> policy.validate(LocalTime.of(12, 30))).doesNotThrowAnyException();
    }

    @Test
    void stillRejectsPickupTimeGenuinelyInThePast_inBrasiliaTime() {
        // Mesmo "agora" do teste acima (12:00 BRT) — 11:30 BRT já passou de
        // verdade, então isso continua tendo que ser rejeitado. Garante que
        // a correção não "desligou" a regra, só corrigiu a zona usada nela.
        Clock noonBrasilia = Clock.fixed(Instant.parse("2026-01-15T15:00:00Z"), ZoneOffset.UTC);
        PickupTimePolicy policy = new PickupTimePolicy(noonBrasilia);

        assertThatThrownBy(() -> policy.validate(LocalTime.of(11, 30)))
                .isInstanceOf(InvalidPickupTimeException.class)
                .hasMessage("Horário de retirada não pode estar no passado");
    }

    @Test
    void acceptsLunchWindowPickupTime_whenTestedAfterMidnightBrasilia() {
        // Reproduz "testei depois da meia-noite e funcionou": agora = 03:30
        // em America/Sao_Paulo — bem antes das 11:00, então qualquer horário
        // dentro da janela de almoço passa a regra de "não está no passado".
        Clock afterMidnightBrasilia = Clock.fixed(Instant.parse("2026-01-15T06:30:00Z"), ZoneOffset.UTC);
        PickupTimePolicy policy = new PickupTimePolicy(afterMidnightBrasilia);

        assertThatCode(() -> policy.validate(LocalTime.of(12, 30))).doesNotThrowAnyException();
    }

    @Test
    void convertsCorrectly_evenWhenInjectedClockIsInAnUnrelatedTimezone() {
        // O Clock injetado aqui está em Asia/Tokyo (UTC+9) — nem UTC, nem
        // America/Sao_Paulo. Mesmo instante do teste de "agora = meio-dia em
        // Brasília" acima, só que a zona-base do Clock é outra completamente.
        // clock.withZone(...) ignora a zona original e usa só o instante, então
        // o resultado tem que ser idêntico — prova que a correção não depende
        // de qual zona o ambiente (Render, ou qualquer outro) configurou no
        // Clock; funciona com qualquer uma.
        Clock tokyoClockSameInstant = Clock.fixed(Instant.parse("2026-01-15T15:00:00Z"), ZoneId.of("Asia/Tokyo"));
        PickupTimePolicy policy = new PickupTimePolicy(tokyoClockSameInstant);

        assertThatCode(() -> policy.validate(LocalTime.of(12, 30))).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validate(LocalTime.of(11, 30)))
                .isInstanceOf(InvalidPickupTimeException.class)
                .hasMessage("Horário de retirada não pode estar no passado");
    }
}