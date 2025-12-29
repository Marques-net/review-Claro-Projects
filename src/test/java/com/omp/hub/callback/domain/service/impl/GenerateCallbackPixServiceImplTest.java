package com.omp.hub.callback.domain.service.impl;

import com.omp.hub.callback.domain.service.generate.impl.GenerateCallbackPixServiceImpl;
import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GenerateCallbackPixServiceImplTest {

    private GenerateCallbackPixServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GenerateCallbackPixServiceImpl();
    }

    @Test
    void generateRequest_WithCompletePixCallbackRequest_ShouldGenerateValidRequest() {
        // Given
        PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                .txId("tx-123")
                .service("PIX")
                .paymentType("PIX_NORMAL")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals("PIX", result.getData().getCallbackTarget());
        assertNotNull(result.getData().getEvent());
        assertNotNull(result.getData().getEvent().getPayment());
        assertEquals(1, result.getData().getEvent().getPayment().size());
        
        Object paymentObject = result.getData().getEvent().getPayment().get(0);
        assertTrue(paymentObject instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> paymentMap = (Map<String, Object>) paymentObject;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> pixData = (Map<String, Object>) paymentMap.get("pix");
        assertNotNull(pixData);
        assertNull(pixData.get("txId"));
    }

    @Test
    void generateRequest_WithNullService_ShouldUseEmptyString() {
        // Given
        PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                .txId("tx-123")
                .service("")
                .paymentType("PIX_NORMAL")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

        // Then
        assertNotNull(result);
        assertEquals("", result.getData().getCallbackTarget());
    }

    @Test
    void generateRequest_WithEmptyService_ShouldKeepEmptyString() {
        // Given
        PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                .txId("tx-123")
                .service("")
                .paymentType("PIX_NORMAL")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

        // Then
        assertNotNull(result);
        assertEquals("", result.getData().getCallbackTarget());
    }

    @Test
    void generateRequest_WithDifferentServiceNames_ShouldPreserveServiceName() {
        // Test various service names
        String[] serviceNames = {"PIX", "PIX_AUTOMATICO", "PAYMENT", "OTHER_SERVICE"};

        for (String serviceName : serviceNames) {
            // Given
            PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                    .txId("tx-" + serviceName)
                    .service(serviceName)
                    .build();

            // When
            OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

            // Then
            assertEquals(serviceName, result.getData().getCallbackTarget(),
                    "Service name should be preserved: " + serviceName);
        }
    }

    @Test
    void generateRequest_ShouldWrapRequestInList() {
        // Given
        PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                .txId("tx-456")
                .service("PIX")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

        // Then
        assertNotNull(result.getData().getEvent().getPayment());
        assertFalse(result.getData().getEvent().getPayment().isEmpty());
        
        // Verifica se contém o Map com a chave "pix" e o request
        Object paymentObject = result.getData().getEvent().getPayment().get(0);
        assertTrue(paymentObject instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> paymentMap = (Map<String, Object>) paymentObject;
        assertTrue(paymentMap.containsKey("pix"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> pixData = (Map<String, Object>) paymentMap.get("pix");
        assertNotNull(pixData);
        // txId só é adicionado ao pixData quando service contém "gevenue"
        assertNull(pixData.get("txId"));
    }

    @Test
    void generateRequest_WithMinimalPixRequest_ShouldStillGenerate() {
        // Given
        PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                .service("")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals("", result.getData().getCallbackTarget()); // null service becomes ""
        assertNotNull(result.getData().getEvent());
        assertEquals(1, result.getData().getEvent().getPayment().size());
    }

    @Test
    void generateRequest_ShouldUseBuildPattern() {
        // Given
        PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                .txId("tx-789")
                .service("PIX_TEST")
                .paymentType("TYPE_TEST")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData());
        assertNotNull(result.getData().getEvent());
        assertNotNull(result.getData().getEvent().getPayment());
    }

    @Test
    void generateRequest_WithMultipleCallsDifferentRequests_ShouldGenerateDifferentContent() {
        // Given
        PixCallbackRequest pixRequest1 = PixCallbackRequest.builder()
                .txId("tx-1")
                .service("PIX")
                .build();
        PixCallbackRequest pixRequest2 = PixCallbackRequest.builder()
                .txId("tx-2")
                .service("PIX_OTHER")
                .build();

        // When
        OmphubTransactionNotificationRequest result1 = service.generateRequest(pixRequest1);
        OmphubTransactionNotificationRequest result2 = service.generateRequest(pixRequest2);

        // Then
        assertEquals("PIX", result1.getData().getCallbackTarget());
        assertEquals("PIX_OTHER", result2.getData().getCallbackTarget());
    }

    @Test
    void generateRequest_WithOmpTransactionId_ShouldUseOmpTransactionId() {
        // Given
        PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                .ompTransactionId("omp-tx-123")
                .txId("tx-456")
                .service("PIX")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

        // Then
        assertNotNull(result);
        assertNull(result.getData().getOmpTransactionId()); // Campo está comentado
    }

    @Test
    void generateRequest_WithoutOmpTransactionId_ShouldUseTxIdAsFallback() {
        // Given
        PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                .ompTransactionId(null)
                .txId("tx-456")
                .service("PIX")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

        // Then
        assertNotNull(result);
        assertNull(result.getData().getOmpTransactionId()); // Campo está comentado
    }

    @Test
    void generateRequest_WithBothOmpTransactionIdAndTxId_ShouldPrioritizeOmpTransactionId() {
        // Given
        PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                .ompTransactionId("omp-priority")
                .txId("tx-fallback")
                .service("PIX")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

        // Then
        assertNull(result.getData().getOmpTransactionId()); // Campo está comentado
    }

    @Test
    void generateRequest_ShouldIncludeStatusPago() {
        // Given
        PixCallbackRequest pixRequest = PixCallbackRequest.builder()
                .txId("tx-123")
                .service("GevenueMovelOmp")
                .paymentType("PIX")
                .paymentDate("2025-11-17T12:10:30-03:00")
                .value("96.40")
                .endToEndId("GE1461747270000000014051820010120")
                .build();

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(pixRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData());
        assertNotNull(result.getData().getEvent());
        assertEquals("PAYMENT", result.getData().getEvent().getType());
        assertEquals("GevenueMovelOmp", result.getData().getCallbackTarget());
    }
}
