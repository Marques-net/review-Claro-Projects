package com.omp.hub.callback.domain.service.impl;

import com.omp.hub.callback.domain.service.generate.impl.GenerateCallbackCardServiceImpl;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenerateCallbackCardServiceImplTest {

    private GenerateCallbackCardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GenerateCallbackCardServiceImpl();
    }

    @Test
    void generateRequest_WithCompleteCreditCardRequest_ShouldGenerateValidRequest() {
        // Given
        CreditCardCallbackRequest request = CreditCardCallbackRequest.builder()
                .orderId("order-123")
                .service("CARD")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertEquals("CARD", result.getData().getCallbackTarget());
        assertEquals(1, result.getData().getEvent().getPayment().size());
    }

    @Test
    void generateRequest_WithNullService_ShouldUseEmptyString() {
        // Given
        CreditCardCallbackRequest request = CreditCardCallbackRequest.builder()
                .orderId("order-456")
                .service(null)
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertEquals("", result.getData().getCallbackTarget());
    }

    @Test
    void generateRequest_WithOmpTransactionId_ShouldUseOmpTransactionId() {
        // Given
        CreditCardCallbackRequest request = CreditCardCallbackRequest.builder()
                .ompTransactionId("omp-tx-123")
                .transactionId("tx-456")
                .service("CARD")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertNull(result.getData().getOmpTransactionId()); // Campo está comentado
    }

    @Test
    void generateRequest_WithoutOmpTransactionId_ShouldUseTransactionIdAsFallback() {
        // Given
        CreditCardCallbackRequest request = CreditCardCallbackRequest.builder()
                .ompTransactionId(null)
                .transactionId("tx-456")
                .service("CARD")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertNull(result.getData().getOmpTransactionId()); // Campo está comentado
    }

    @Test
    void generateRequest_WithBothOmpTransactionIdAndTransactionId_ShouldPrioritizeOmpTransactionId() {
        // Given
        CreditCardCallbackRequest request = CreditCardCallbackRequest.builder()
                .ompTransactionId("omp-priority")
                .transactionId("tx-fallback")
                .service("CARD")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNull(result.getData().getOmpTransactionId()); // Campo está comentado
    }
}
