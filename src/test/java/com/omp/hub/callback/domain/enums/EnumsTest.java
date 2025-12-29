package com.omp.hub.callback.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    void statusEnum_ShouldHaveExpectedValues() {
        assertEquals(8, PaymentStatusEnum.values().length);
        assertNotNull(PaymentStatusEnum.valueOf("PENDING"));
        assertNotNull(PaymentStatusEnum.valueOf("APPROVED"));
        assertNotNull(PaymentStatusEnum.valueOf("REFUSED"));
        assertNotNull(PaymentStatusEnum.valueOf("ERROR"));
        assertNotNull(PaymentStatusEnum.valueOf("CANCELING"));
        assertNotNull(PaymentStatusEnum.valueOf("CANCELED"));
        assertNotNull(PaymentStatusEnum.valueOf("CANCEL_FAILED"));
        assertNotNull(PaymentStatusEnum.valueOf("PARTIALLY_CANCELLED"));
    }

    @Test
    void segmentEnum_ClaroControle_ShouldHaveCorrectValues() {
        SegmentEnum segment = SegmentEnum.CLARO_CONTROLE;
        
        assertEquals("150", segment.getReason1());
        assertEquals("8196", segment.getReason2());
        assertEquals("67341", segment.getReason3());
        assertEquals("67342", segment.getReason4());
        assertEquals("80752", segment.getReason5());
    }

    @Test
    void segmentEnum_BandaLargaConta_ShouldHaveCorrectValues() {
        SegmentEnum segment = SegmentEnum.BANDA_LARGA_CONTA;
        
        assertEquals("150", segment.getReason1());
        assertEquals("8198", segment.getReason2());
        assertEquals("66776", segment.getReason3());
        assertEquals("67343", segment.getReason4());
        assertEquals("80753", segment.getReason5());
    }

    @Test
    void pixAutomaticoEventEnum_ShouldHaveAllValues() {
        assertEquals(8, PixAutomaticoEventEnum.values().length);
        assertNotNull(PixAutomaticoEventEnum.OPTIN);
        assertNotNull(PixAutomaticoEventEnum.OPTOUT);
        assertNotNull(PixAutomaticoEventEnum.AGENDAMENTO);
        assertNotNull(PixAutomaticoEventEnum.PAGAMENTO);
        assertNotNull(PixAutomaticoEventEnum.ALTERACAO);
        assertNotNull(PixAutomaticoEventEnum.COBRANCA);
        assertNotNull(PixAutomaticoEventEnum.FALHA_AGENDAMENTO);
        assertNotNull(PixAutomaticoEventEnum.INCENTIVO_ADESAO);
    }

    @Test
    void pixAutomaticoEventEnum_ShouldHaveDescriptions() {
        assertEquals("Confirmação Optin", PixAutomaticoEventEnum.OPTIN.getDescription());
        assertEquals("Confirmação Optout", PixAutomaticoEventEnum.OPTOUT.getDescription());
        assertEquals("Confirmação do Agendamento da Recorrência Mensal", PixAutomaticoEventEnum.AGENDAMENTO.getDescription());
        assertEquals("Pagamento PIX Automático Executado", PixAutomaticoEventEnum.PAGAMENTO.getDescription());
    }



    @Test
    void pixAutomaticoEventEnum_FromString_WithValidString_ShouldReturnEnum() {
        assertEquals(PixAutomaticoEventEnum.OPTIN, PixAutomaticoEventEnum.fromString("OPTIN"));
        assertEquals(PixAutomaticoEventEnum.OPTOUT, PixAutomaticoEventEnum.fromString("OPTOUT"));
        assertEquals(PixAutomaticoEventEnum.PAGAMENTO, PixAutomaticoEventEnum.fromString("PAGAMENTO"));
    }

    @Test
    void pixAutomaticoEventEnum_FromString_WithLowerCase_ShouldReturnEnum() {
        assertEquals(PixAutomaticoEventEnum.OPTIN, PixAutomaticoEventEnum.fromString("optin"));
        assertEquals(PixAutomaticoEventEnum.PAGAMENTO, PixAutomaticoEventEnum.fromString("pagamento"));
    }

    @Test
    void pixAutomaticoEventEnum_FromString_WithNull_ShouldReturnNull() {
        assertNull(PixAutomaticoEventEnum.fromString(null));
    }

    @Test
    void pixAutomaticoEventEnum_FromString_WithInvalidString_ShouldReturnNull() {
        assertNull(PixAutomaticoEventEnum.fromString("INVALID"));
        assertNull(PixAutomaticoEventEnum.fromString(""));
    }
}
