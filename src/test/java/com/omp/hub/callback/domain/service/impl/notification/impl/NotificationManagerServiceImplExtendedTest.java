package com.omp.hub.callback.domain.service.impl.notification.impl;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import com.omp.hub.callback.domain.service.impl.notification.CustomerDataExtractionService;
import com.omp.hub.callback.domain.service.impl.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationManagerServiceImplExtendedTest {

    @Mock
    private CustomerDataExtractionService customerDataExtractionService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationManagerServiceImpl serviceUnderTest;

    private UUID testUUID;
    private String testIdentifier;

    @BeforeEach
    void setUp() {
        testUUID = UUID.randomUUID();
        testIdentifier = "TX_123456";
        lenient().doNothing().when(notificationService).sendPixAutomaticoNotificationWithCustomerData(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testProcessPixAutomaticoNotificationWithCompleteDataAndEmail() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .email("joao@email.com")
                .msisdn("11999999999")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(testUUID, customerData))
                .thenReturn(customerData);

        // When
        boolean result = serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isTrue();
        verify(notificationService, times(1)).sendPixAutomaticoNotificationWithCustomerData(
                testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO, "João Silva", "11999999999", "joao@email.com");
    }

    @Test
    void testProcessPixAutomaticoNotificationWithCompleteDataButNoContact() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(testUUID, customerData))
                .thenReturn(customerData);

        // When
        boolean result = serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, PixAutomaticoEventEnum.OPTOUT);

        // Then
        assertThat(result).isFalse();
        verify(notificationService, never()).sendPixAutomaticoNotificationWithCustomerData(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testProcessPixAutomaticoNotificationWithNullCustomerData() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(null);

        // When
        boolean result = serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isFalse();
        verify(notificationService, never()).sendPixAutomaticoNotificationWithCustomerData(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testProcessPixAutomaticoNotificationWithIncompleteCustomerData() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(customerData);

        // When
        boolean result = serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isFalse();
        verify(notificationService, never()).sendPixAutomaticoNotificationWithCustomerData(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testProcessPixAutomaticoNotificationWithBusinessException() {
        // Given - BusinessException thrown from enrichCustomerData
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(testUUID, customerData))
                .thenThrow(new BusinessException("Error enriching data", "ERROR_CODE"));

        // When & Then
        assertThatThrownBy(() -> serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testProcessPixAutomaticoNotificationWithGenericException() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When
        boolean result = serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testProcessPixAutomaticoNotificationWithOnlyEmail() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .email("joao@email.com")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(testUUID, customerData))
                .thenReturn(customerData);

        // When
        boolean result = serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isTrue();
        verify(notificationService, times(1)).sendPixAutomaticoNotificationWithCustomerData(
                testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO, "João Silva", null, "joao@email.com");
    }

    @Test
    void testProcessPixAutomaticoNotificationWithOnlyMsisdn() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .msisdn("11999999999")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(testUUID, customerData))
                .thenReturn(customerData);

        // When
        boolean result = serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isTrue();
        verify(notificationService, times(1)).sendPixAutomaticoNotificationWithCustomerData(
                testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO, "João Silva", "11999999999", null);
    }

    @ParameterizedTest
    @EnumSource(PixAutomaticoEventEnum.class)
    void testProcessPixAutomaticoNotificationWithVariousEventTypes(PixAutomaticoEventEnum eventType) {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .email("joao@email.com")
                .msisdn("11999999999")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(testUUID, customerData))
                .thenReturn(customerData);

        // When
        boolean result = serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, eventType);

        // Then
        assertThat(result).isTrue();
        verify(notificationService, times(1)).sendPixAutomaticoNotificationWithCustomerData(
                testUUID, testIdentifier, eventType, "João Silva", "11999999999", "joao@email.com");
    }

    @Test
    void testExtractCustomerDataFromTxIdWithSuccess() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(customerData);

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.extractCustomerDataFromTxId(testUUID, testIdentifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.getCpf()).isEqualTo("12345678901");
    }

    @Test
    void testExtractCustomerDataFromTxIdWithNullResult() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(null);

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.extractCustomerDataFromTxId(testUUID, testIdentifier);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testExtractCustomerDataFromTxIdWithException() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenThrow(new RuntimeException("Extraction error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.extractCustomerDataFromTxId(testUUID, testIdentifier);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testProcessPixAutomaticoNotificationEnrichmentReturnsNull() {
        // Given
        ExtractedCustomerDataDTO initialData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(initialData);
        when(customerDataExtractionService.enrichCustomerData(testUUID, initialData))
                .thenReturn(null);

        // When
        boolean result = serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isFalse();
        verify(notificationService, never()).sendPixAutomaticoNotificationWithCustomerData(any(), any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"João", "Maria da Silva", "Dr. José", "Prof. Ana Lima"})
    void testProcessPixAutomaticoNotificationWithVariousNames(String name) {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name(name)
                .cpf("12345678901")
                .email("test@email.com")
                .msisdn("11999999999")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(testUUID, customerData))
                .thenReturn(customerData);

        // When
        boolean result = serviceUnderTest.processPixAutomaticoNotification(testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isTrue();
        verify(notificationService, times(1)).sendPixAutomaticoNotificationWithCustomerData(
                testUUID, testIdentifier, PixAutomaticoEventEnum.PAGAMENTO, name, "11999999999", "test@email.com");
    }
}
