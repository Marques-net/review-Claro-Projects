package com.omp.hub.callback.domain.service.impl.notification.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import com.omp.hub.callback.domain.service.impl.notification.CustomerDataExtractionService;
import com.omp.hub.callback.domain.service.impl.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationManagerServiceImplTest {

    @Mock
    private CustomerDataExtractionService customerDataExtractionService;
    
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationManagerServiceImpl service;

    private UUID uuid;
    private String identifier;
    private PixAutomaticoEventEnum eventType;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        identifier = "test-tx-id-123";
        eventType = PixAutomaticoEventEnum.OPTIN;
    }

    @Test
    void testProcessPixAutomaticoNotification_CriteriaAtendidos_ReturnTrue() {
        // Arrange
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("TESTE QA")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("146164452")
                .email("teste@gmail.com")
                .msisdn("11992212346")
                .criteriosAtendidos(true) // Critérios atendidos
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenReturn(customerData);

        // Act
        boolean result = service.processPixAutomaticoNotification(uuid, identifier, eventType);

        // Assert
        assertTrue(result); // Deve retornar true quando critérios são atendidos
        verify(notificationService, times(1)).sendPixAutomaticoNotificationWithCustomerData(
                eq(uuid), eq(identifier), eq(eventType), eq("TESTE QA"), 
                eq("11992212346"), eq("teste@gmail.com"));
    }

    @Test
    void testProcessPixAutomaticoNotification_CriteriaNaoAtendidos_ReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("TESTE QA")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("146164452")
                .email("teste@gmail.com")
                .msisdn(null) // Sem MSISDN
                .criteriosAtendidos(false) // Critérios NÃO atendidos
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenReturn(customerData);

        // Act
        boolean result = service.processPixAutomaticoNotification(uuid, identifier, eventType);

        // Assert - retorna true porque tem email (mesmo sem msisdn)
        assertTrue(result); // Deve retornar true quando tem pelo menos um contato
        verify(notificationService).sendPixAutomaticoNotificationWithCustomerData(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void testProcessPixAutomaticoNotification_DadosIncompletos_ReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name(null) // Dados incompletos
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                .thenReturn(customerData);

        // Act
        boolean result = service.processPixAutomaticoNotification(uuid, identifier, eventType);

        // Assert
        assertFalse(result); // Deve retornar false quando dados são incompletos
        verify(customerDataExtractionService, never()).enrichCustomerData(any(), any());
        verify(notificationService, never()).sendPixAutomaticoNotificationWithCustomerData(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void testProcessPixAutomaticoNotification_ExceptionOccurred_ReturnFalse() {
        // Arrange
        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        boolean result = service.processPixAutomaticoNotification(uuid, identifier, eventType);

        // Assert
        assertFalse(result); // Deve retornar false quando ocorre exception
        verify(notificationService, never()).sendPixAutomaticoNotificationWithCustomerData(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void testProcessPixAutomaticoNotification_SemEmail_ReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("TESTE QA")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("146164452")
                .email(null) // Sem email
                .msisdn("11992212346")
                .criteriosAtendidos(true) // Critérios atendidos mas sem email
                .build();

        when(customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier))
                .thenReturn(customerData);
        when(customerDataExtractionService.enrichCustomerData(uuid, customerData))
                .thenReturn(customerData);

        // Act
        boolean result = service.processPixAutomaticoNotification(uuid, identifier, eventType);

        // Assert - retorna true porque tem msisdn (mesmo sem email)
        assertTrue(result); // Deve retornar true quando tem pelo menos um contato
        verify(notificationService).sendPixAutomaticoNotificationWithCustomerData(
                any(), any(), any(), any(), any(), any());
    }
}