package com.omp.hub.callback.domain.service.notification.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.service.impl.notification.impl.PixEventMappingServiceImpl;

class PixEventMappingServiceImplTest {

    private static final String TX_ID = "test-tx-id-123";
    private PixEventMappingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PixEventMappingServiceImpl();
        
        // Configura os valores dos templates via reflection para o teste
        ReflectionTestUtils.setField(service, "templateCodePayment", "pagamento_executado_pix");
        ReflectionTestUtils.setField(service, "templateCodeAlteracao", "alteracao_forma_pagamento");
        ReflectionTestUtils.setField(service, "templateCodeCobranca", "cobranca_processada_pix");
    }

    @Disabled
    @Test
    void shouldNotify_withValidEvents_returnsTrue() {
        assertTrue(service.shouldNotify(TX_ID, "PAGAMENTO"));
        assertTrue(service.shouldNotify(TX_ID, "CHANGE_PAYMENT_METHOD"));
        assertTrue(service.shouldNotify(TX_ID, "ALTERACAO"));
        assertTrue(service.shouldNotify(TX_ID, "CHARGE"));
        assertTrue(service.shouldNotify(TX_ID, "COBRANCA"));
    }

    @Test
    void shouldNotify_withInvalidEvents_returnsFalse() {
        assertFalse(service.shouldNotify(TX_ID, "INVALID_EVENT"));
        assertFalse(service.shouldNotify(TX_ID, "OTHER_EVENT"));
        assertFalse(service.shouldNotify(TX_ID, null));
        assertFalse(service.shouldNotify(TX_ID, ""));
    }

    @Test
    void mapEventTypeToEnum_withValidEvents_returnsCorrectEnum() {
        assertEquals(PixAutomaticoEventEnum.PAGAMENTO, service.mapEventTypeToEnum(TX_ID, "PAGAMENTO"));
        assertEquals(PixAutomaticoEventEnum.ALTERACAO, service.mapEventTypeToEnum(TX_ID, "CHANGE_PAYMENT_METHOD"));
        assertEquals(PixAutomaticoEventEnum.ALTERACAO, service.mapEventTypeToEnum(TX_ID, "ALTERACAO"));
        assertEquals(PixAutomaticoEventEnum.COBRANCA, service.mapEventTypeToEnum(TX_ID, "CHARGE"));
        assertEquals(PixAutomaticoEventEnum.COBRANCA, service.mapEventTypeToEnum(TX_ID, "COBRANCA"));
    }

    @Test
    void mapEventTypeToEnum_withInvalidEvents_returnsNull() {
        assertNull(service.mapEventTypeToEnum(TX_ID, "INVALID_EVENT"));
        assertNull(service.mapEventTypeToEnum(TX_ID, null));
        assertNull(service.mapEventTypeToEnum(TX_ID, ""));
    }

    @Test
    void getTemplateCodeForEventType_withValidEvents_returnsCorrectTemplate() {
        assertEquals("pagamento_executado_pix", 
                    service.getTemplateCodeForEventType(TX_ID, PixAutomaticoEventEnum.PAGAMENTO));
        assertEquals("alteracao_forma_pagamento", 
                    service.getTemplateCodeForEventType(TX_ID, PixAutomaticoEventEnum.ALTERACAO));
        assertEquals("cobranca_processada_pix", 
                    service.getTemplateCodeForEventType(TX_ID, PixAutomaticoEventEnum.COBRANCA));
    }

    @Test
    void getTemplateCodeForEventType_withNullEvent_returnsDefaultTemplate() {
        assertEquals("pagamento_executado_pix", service.getTemplateCodeForEventType(TX_ID, null));
    }
}