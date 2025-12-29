package com.omp.hub.callback.domain.service.impl;

import com.omp.hub.callback.domain.service.generate.impl.GenerateCallbackTefWebServiceImpl;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.SalesDTO;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenerateCallbackTefWebServiceImplTest {

    private GenerateCallbackTefWebServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GenerateCallbackTefWebServiceImpl();
    }

    @Test
    void generateRequest_WithCompleteTefWebRequest_ShouldGenerateValidRequest() {
        // Given
        SalesDTO sale = new SalesDTO();
        TefWebCallbackRequest request = TefWebCallbackRequest.builder()
                .service("TEFWEB")
                .sales(List.of(sale))
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request, "049d019a-2923-4dff-9c70-ad577505bebe");

        // Then
        assertNotNull(result);
        assertEquals("TEFWEB", result.getData().getCallbackTarget());
        assertEquals("PAYMENT", result.getData().getEvent().getType());
        assertEquals("PAGO", result.getData().getEvent().getStatus());
        assertEquals("049d019a-2923-4dff-9c70-ad577505bebe", result.getData().getEvent().getTransactionOrderId());
        assertEquals(1, result.getData().getEvent().getPayment().size());
    }

    @Test
    void generateRequest_WithNullService_ShouldUseEmptyString() {
        // Given
        TefWebCallbackRequest request = TefWebCallbackRequest.builder()
                .service(null)
                .sales(List.of())
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request, "omp-tx-123");

        // Then
        assertEquals("", result.getData().getCallbackTarget());
    }

    @Test
    void generateRequest_WithEmptySalesList_ShouldGenerateEmptyPayments() {
        // Given
        TefWebCallbackRequest request = TefWebCallbackRequest.builder()
                .service("TEFWEB")
                .sales(List.of())
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request, "omp-tx-123");

        // Then
        assertTrue(result.getData().getEvent().getPayment().isEmpty());
    }

    @Test
    void generateRequest_WithNullSalesList_ShouldGenerateEmptyPayments() {
        // Given
        TefWebCallbackRequest request = TefWebCallbackRequest.builder()
                .service("TEFWEB")
                .sales(null)
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request, "omp-tx-123");

        // Then
        assertTrue(result.getData().getEvent().getPayment().isEmpty());
    }

    @Test
    void generateRequest_WithOmpTransactionId_ShouldIncludeTransactionOrderIdAndStatus() {
        // Given
        TefWebCallbackRequest request = TefWebCallbackRequest.builder()
                .ompTransactionId("omp-tx-123")
                .service("TEFWEB")
                .sales(List.of())
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request, "omp-tx-123");

        // Then
        assertNotNull(result);
        assertEquals("omp-tx-123", result.getData().getEvent().getTransactionOrderId());
        assertEquals("PAGO", result.getData().getEvent().getStatus());
    }

    @Test
    void generateRequest_WithNullOmpTransactionId_ShouldHaveNullTransactionOrderId() {
        // Given
        TefWebCallbackRequest request = TefWebCallbackRequest.builder()
                .ompTransactionId(null)
                .service("TEFWEB")
                .sales(List.of())
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request, null);

        // Then
        assertNotNull(result);
        assertNull(result.getData().getEvent().getTransactionOrderId());
        assertEquals("PAGO", result.getData().getEvent().getStatus());
    }
}
