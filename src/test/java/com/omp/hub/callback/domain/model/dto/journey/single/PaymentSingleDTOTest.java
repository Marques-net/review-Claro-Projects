package com.omp.hub.callback.domain.model.dto.journey.single;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentSingleDTOTest {

    @Test
    void testPaymentSingleDTOBuilder() {
        // Given
        String salesOrderId = "12345";
        String value = "100.00";
        String invoice = "INV001";

        // When
        PaymentSingleDTO dto = PaymentSingleDTO.builder()
                .salesOrderId(salesOrderId)
                .value(value)
                .invoice(invoice)
                .build();

        // Then
        assertNotNull(dto);
        assertEquals(salesOrderId, dto.getSalesOrderId());
        assertEquals(value, dto.getValue());
        assertEquals(invoice, dto.getInvoice());
    }

    @Test
    void testPaymentSingleDTOSettersAndGetters() {
        // Given
        PaymentSingleDTO dto = new PaymentSingleDTO();

        // When
        dto.setSalesOrderId("12345");
        dto.setValue("100.00");
        dto.setInvoice("INV001");

        // Then
        assertEquals("12345", dto.getSalesOrderId());
        assertEquals("100.00", dto.getValue());
        assertEquals("INV001", dto.getInvoice());
    }

    @Test
    void testPaymentSingleDTODefaultConstructor() {
        // When
        PaymentSingleDTO dto = new PaymentSingleDTO();

        // Then
        assertNotNull(dto);
        assertNull(dto.getSalesOrderId());
        assertNull(dto.getValue());
        assertNull(dto.getInvoice());
        assertNull(dto.getCardData());
        assertNull(dto.getPixData());
        assertNull(dto.getCashData());
    }
}