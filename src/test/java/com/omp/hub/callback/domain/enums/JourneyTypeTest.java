package com.omp.hub.callback.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JourneyTypeTest {

    @Test
    void testJourneyTypeValues() {
        // Then
        assertThat(JourneyType.values()).containsExactly(
            JourneyType.JORNADA_1,
            JourneyType.JORNADA_2,
            JourneyType.JORNADA_3,
            JourneyType.JORNADA_4,
            JourneyType.AGUARDANDO_DEFINICAO
        );
    }

    @Test
    void testJourneyTypeValueOf() {
        // When & Then
        assertThat(JourneyType.valueOf("JORNADA_1")).isEqualTo(JourneyType.JORNADA_1);
        assertThat(JourneyType.valueOf("JORNADA_2")).isEqualTo(JourneyType.JORNADA_2);
        assertThat(JourneyType.valueOf("JORNADA_3")).isEqualTo(JourneyType.JORNADA_3);
        assertThat(JourneyType.valueOf("JORNADA_4")).isEqualTo(JourneyType.JORNADA_4);
        assertThat(JourneyType.valueOf("AGUARDANDO_DEFINICAO")).isEqualTo(JourneyType.AGUARDANDO_DEFINICAO);
    }

    @Test
    void testJourneyTypeOrdinal() {
        // Then
        assertThat(JourneyType.JORNADA_1.ordinal()).isEqualTo(0);
        assertThat(JourneyType.JORNADA_2.ordinal()).isEqualTo(1);
        assertThat(JourneyType.JORNADA_3.ordinal()).isEqualTo(2);
        assertThat(JourneyType.JORNADA_4.ordinal()).isEqualTo(3);
        assertThat(JourneyType.AGUARDANDO_DEFINICAO.ordinal()).isEqualTo(4);
    }

    @Test
    void testJourneyTypeComparison() {
        // Given
        JourneyType jornada1 = JourneyType.JORNADA_1;
        JourneyType jornada2 = JourneyType.JORNADA_2;

        // Then
        assertThat(jornada1).isNotEqualTo(jornada2);
        assertThat(jornada1).isEqualTo(JourneyType.JORNADA_1);
    }

    @Test
    void testJourneyTypeToString() {
        // Then
        assertThat(JourneyType.JORNADA_1.toString()).isEqualTo("JORNADA_1");
        assertThat(JourneyType.JORNADA_2.toString()).isEqualTo("JORNADA_2");
        assertThat(JourneyType.AGUARDANDO_DEFINICAO.toString()).isEqualTo("AGUARDANDO_DEFINICAO");
    }

    @Test
    void testJourneyTypeEnumSize() {
        // Then
        assertThat(JourneyType.values()).hasSize(5);
    }

    @Test
    void testJourneyTypeSwitch() {
        // Given
        JourneyType journeyType = JourneyType.JORNADA_3;

        // When & Then
        String result = switch (journeyType) {
            case JORNADA_1 -> "Journey 1";
            case JORNADA_2 -> "Journey 2";
            case JORNADA_3 -> "Journey 3";
            case JORNADA_4 -> "Journey 4";
            case AGUARDANDO_DEFINICAO -> "Undefined";
        };

        assertThat(result).isEqualTo("Journey 3");
    }
}
