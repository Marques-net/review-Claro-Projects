package com.omp.hub.callback.domain.service.impl.notification.impl;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import com.omp.hub.callback.domain.service.impl.notification.CustomerDataExtractionService;
import com.omp.hub.callback.domain.service.impl.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationManagerServiceImplAdditionalTest {

    @Mock
    private CustomerDataExtractionService customerDataExtractionService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationManagerServiceImpl service;

    private UUID uuid;
    private String identifier;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        identifier = "test-identifier-123";
    }

    @Test
    void processPixAutomaticoNotification_WithNullCustomerData_ShouldReturnFalse() {
        // Given
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                .thenReturn(null);

        // When
        boolean result = service.processPixAutomaticoNotification(uuid, identifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isFalse();
        verify(notificationService, times(0)).sendPixAutomaticoNotificationWithCustomerData(any(), any(), any(), any(), any(), any());
    }

    @Test
    void processPixAutomaticoNotification_WithIncompleteCustomerData_ShouldStillProcessIfHasContact() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .email("joao@email.com")
                .msisdn(null)
                .criteriosAtendidos(true)
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenReturn(customerData);

        // When
        boolean result = service.processPixAutomaticoNotification(uuid, identifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isTrue();
        verify(notificationService, times(1)).sendPixAutomaticoNotificationWithCustomerData(
                eq(uuid), eq(identifier), eq(PixAutomaticoEventEnum.PAGAMENTO),
                eq("João"), any(), eq("joao@email.com"));
    }

    @Test
    void processPixAutomaticoNotification_WithMultiplePrefixedNames() {
        // Given - Testa casos com diferentes tipos de nomes
        String[] names = {"Dr. João Silva", "Prof. Maria", "João Silva Jr."};

        for (String name : names) {
            ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                    .name(name)
                    .cpf("12345678901")
                    .email("test@email.com")
                    .msisdn("11999999999")
                    .criteriosAtendidos(true)
                    .build();

            when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                    .thenReturn(customerData);
            when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                    .thenReturn(customerData);

            // When
            boolean result = service.processPixAutomaticoNotification(uuid, identifier, PixAutomaticoEventEnum.PAGAMENTO);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Test
    void processPixAutomaticoNotification_WithDifferentEventTypes() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .email("joao@email.com")
                .msisdn("11999999999")
                .criteriosAtendidos(true)
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenReturn(customerData);

        // When & Then
        for (PixAutomaticoEventEnum eventType : PixAutomaticoEventEnum.values()) {
            boolean result = service.processPixAutomaticoNotification(uuid, identifier, eventType);
            assertThat(result).isTrue();
        }
    }

    @Test
    void processPixAutomaticoNotification_WithEnrichedData() {
        // Given
        ExtractedCustomerDataDTO originalData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .build();

        ExtractedCustomerDataDTO enrichedData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .email("joao@enriched.com")
                .msisdn("11988888888")
                .criteriosAtendidos(true)
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                .thenReturn(originalData);
        when(customerDataExtractionService.enrichCustomerData(uuid, originalData))
                .thenReturn(enrichedData);

        // When
        boolean result = service.processPixAutomaticoNotification(uuid, identifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isTrue();
        verify(customerDataExtractionService).enrichCustomerData(uuid, originalData);
        verify(notificationService).sendPixAutomaticoNotificationWithCustomerData(
                eq(uuid), eq(identifier), eq(PixAutomaticoEventEnum.PAGAMENTO),
                eq("João"), eq("11988888888"), eq("joao@enriched.com"));
    }

    @Test
    void processPixAutomaticoNotification_WithEnrichmentReturningNull() {
        // Given
        ExtractedCustomerDataDTO originalData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                .thenReturn(originalData);
        when(customerDataExtractionService.enrichCustomerData(uuid, originalData))
                .thenReturn(null);

        // When
        boolean result = service.processPixAutomaticoNotification(uuid, identifier, PixAutomaticoEventEnum.PAGAMENTO);

        // Then
        assertThat(result).isFalse();
        verify(notificationService, times(0)).sendPixAutomaticoNotificationWithCustomerData(any(), any(), any(), any(), any(), any());
    }
}
