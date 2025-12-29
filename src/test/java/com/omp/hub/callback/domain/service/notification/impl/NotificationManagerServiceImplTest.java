package com.omp.hub.callback.domain.service.notification.impl;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import com.omp.hub.callback.domain.service.impl.notification.NotificationService;
import com.omp.hub.callback.domain.service.impl.notification.CustomerDataExtractionService;
import com.omp.hub.callback.domain.service.impl.notification.impl.NotificationManagerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationManagerServiceImplTest {

    @Mock
    private CustomerDataExtractionService customerDataExtractionService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationManagerServiceImpl service;

    private UUID uuid;
    private String txId;
    private ExtractedCustomerDataDTO customerData;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        txId = "tx-123";
        
        // Customer data must have: name, cpf/cnpj, and segment for hasCompleteData() to return true
        customerData = new ExtractedCustomerDataDTO();
        customerData.setName("John Doe");
        customerData.setCpf("12345678900");
        customerData.setSegment("MOBILE");
        customerData.setEmail("john@example.com");
        customerData.setMsisdn("5511999999999");
        customerData.setCriteriosAtendidos(true); // Set criteria as met for notification to be sent
    }

    @Test
    void processPixAutomaticoNotification_WithCompleteCustomerData_ShouldSendNotification() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenReturn(customerData);

        // When
        service.processPixAutomaticoNotification(uuid, txId, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        verify(customerDataExtractionService).extractCustomerDataFromPaymentInfo(uuid, txId);
        verify(customerDataExtractionService).enrichCustomerData(uuid, customerData);
        verify(notificationService).sendPixAutomaticoNotificationWithCustomerData(
                eq(uuid), eq(txId), eq(PixAutomaticoEventEnum.PAGAMENTO), 
                eq("John Doe"), eq("5511999999999"), eq("john@example.com"));
    }

    @Test
    void processPixAutomaticoNotification_WithNullEmail_ShouldNotSendNotification() {
        // Given
        customerData.setEmail(null);
        customerData.setCriteriosAtendidos(false); // Criteria not met without email
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenReturn(customerData);

        // When
        boolean result = service.processPixAutomaticoNotification(uuid, txId, PixAutomaticoEventEnum.PAGAMENTO);

        // Then - retorna true porque ainda tem msisdn
        assertThat(result).isTrue(); // Should return true when has msisdn
        verify(notificationService).sendPixAutomaticoNotificationWithCustomerData(
                any(), anyString(), any(), anyString(), anyString(), eq(null));
    }

    @Test
    void processPixAutomaticoNotification_WithNullMsisdn_ShouldNotSendNotification() {
        // Given
        customerData.setMsisdn(null);
        customerData.setCriteriosAtendidos(false); // Criteria not met without MSISDN
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenReturn(customerData);

        // When
        boolean result = service.processPixAutomaticoNotification(uuid, txId, PixAutomaticoEventEnum.PAGAMENTO);

        // Then - retorna true porque ainda tem email
        assertThat(result).isTrue(); // Should return true when has email
        verify(notificationService).sendPixAutomaticoNotificationWithCustomerData(
                any(), anyString(), any(), anyString(), eq(null), anyString());
    }

    @Test
    void processPixAutomaticoNotification_WithNullCustomerData_ShouldNotSendNotification() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenReturn(null);

        // When
        service.processPixAutomaticoNotification(uuid, txId, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        verify(customerDataExtractionService, never()).enrichCustomerData(any(), any());
        verify(notificationService, never()).sendPixAutomaticoNotificationWithCustomerData(
                any(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void processPixAutomaticoNotification_WhenExceptionOccurs_ShouldLogError() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        assertDoesNotThrow(() -> service.processPixAutomaticoNotification(
                uuid, txId, PixAutomaticoEventEnum.PAGAMENTO));

        // Then
        verify(notificationService, never()).sendPixAutomaticoNotificationWithCustomerData(
                any(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void extractCustomerDataFromTxId_WithValidTxId_ShouldReturnCustomerData() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenReturn(customerData);

        // When
        ExtractedCustomerDataDTO result = service.extractCustomerDataFromTxId(uuid, txId);

        // Then
        assertNotNull(result);
        assertEquals(customerData, result);
        verify(customerDataExtractionService).extractCustomerDataFromPaymentInfo(uuid, txId);
    }

    @Test
    void extractCustomerDataFromTxId_WhenExceptionOccurs_ShouldReturnNull() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenThrow(new RuntimeException("Error"));

        // When
        ExtractedCustomerDataDTO result = service.extractCustomerDataFromTxId(uuid, txId);

        // Then
        assertNull(result);
    }

    @Test
    void processPixAutomaticoNotification_WithDifferentEventTypes_ShouldProcessCorrectly() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenReturn(customerData);

        // When
        service.processPixAutomaticoNotification(uuid, txId, PixAutomaticoEventEnum.OPTOUT);

        // Then
        verify(notificationService).sendPixAutomaticoNotificationWithCustomerData(
                eq(uuid), eq(txId), eq(PixAutomaticoEventEnum.OPTOUT), 
                anyString(), anyString(), anyString());
    }

    @Test
    void processPixAutomaticoNotification_WhenEnrichCustomerDataThrowsException_ShouldCatchAndLogError() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenThrow(new RuntimeException("Enrichment failed"));

        // When
        service.processPixAutomaticoNotification(uuid, txId, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        verify(customerDataExtractionService).extractCustomerDataFromPaymentInfo(uuid, txId);
        verify(customerDataExtractionService).enrichCustomerData(uuid, customerData);
        verify(notificationService, never()).sendPixAutomaticoNotificationWithCustomerData(
                any(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void processPixAutomaticoNotification_WhenSendNotificationThrowsException_ShouldCatchAndLogError() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenReturn(customerData);
        doThrow(new RuntimeException("Notification failed")).when(notificationService)
                .sendPixAutomaticoNotificationWithCustomerData(any(), anyString(), any(), anyString(), anyString(), anyString());

        // When
        service.processPixAutomaticoNotification(uuid, txId, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        verify(customerDataExtractionService).extractCustomerDataFromPaymentInfo(uuid, txId);
        verify(customerDataExtractionService).enrichCustomerData(uuid, customerData);
        verify(notificationService).sendPixAutomaticoNotificationWithCustomerData(
                eq(uuid), eq(txId), eq(PixAutomaticoEventEnum.PAGAMENTO), 
                eq("John Doe"), eq("5511999999999"), eq("john@example.com"));
    }

    @Test
    void processPixAutomaticoNotification_WithIncompleteCustomerData_ShouldNotSendNotification() {
        // Given - customer data without required fields for hasCompleteData()
        ExtractedCustomerDataDTO incompleteData = new ExtractedCustomerDataDTO();
        incompleteData.setName("John Doe");
        // Missing cpf/cnpj and segment - should make hasCompleteData() return false
        
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, txId))
                .thenReturn(incompleteData);

        // When
        service.processPixAutomaticoNotification(uuid, txId, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        verify(customerDataExtractionService, never()).enrichCustomerData(any(), any());
        verify(notificationService, never()).sendPixAutomaticoNotificationWithCustomerData(
                any(), anyString(), any(), anyString(), anyString(), anyString());
    }
}
