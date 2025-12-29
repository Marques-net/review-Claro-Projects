package com.omp.hub.callback.application.usecase.callback.impl;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.ports.client.TransationsNotificationsPort;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackPixService;
import com.omp.hub.callback.domain.service.impl.notification.NotificationManagerService;
import com.omp.hub.callback.domain.service.impl.notification.PixEventMappingService;

import okhttp3.Headers;

@ExtendWith(MockitoExtension.class)
class PixCallbackUseCaseImplTest {

    @Mock
    private InformationPaymentPort port;

    @Mock
    private GenerateCallbackPixService service;

    @Mock
    private TransationsNotificationsPort transactionsPort;

    @Mock
    private ApigeeHeaderService apigeeHeaderService;

    @Mock
    private PixEventMappingService pixEventMappingService;

    @Mock
    private NotificationManagerService notificationManagerService;

    @InjectMocks
    private PixCallbackUseCaseImpl useCase;

    private PixCallbackRequest request;
    private Headers.Builder mockHeadersBuilder;
    private OmphubTransactionNotificationRequest mockNotificationRequest;

    @BeforeEach
    void setUp() {
        request = new PixCallbackRequest();
        request.setTxId("pix-tx-123");
        request.setPaymentType("PAYMENT");
        request.setService("teste");

        mockHeadersBuilder = new Headers.Builder();
        mockNotificationRequest = new OmphubTransactionNotificationRequest();
    }

    @Test
    void sendCallback_WithValidTxIdAndNoPixAutomatico_ShouldOnlySendHubNotification() {
        // Given
        when(pixEventMappingService.isPixAutomaticoEvent(anyString(), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        Assertions.assertDoesNotThrow(() -> useCase.sendCallback(request));
    }

    @Test
    void sendCallback_WithPixAutomaticoEvent_ShouldSendBothNotifications() {
        // Given
        when(pixEventMappingService.isPixAutomaticoEvent(anyString(), anyString())).thenReturn(true);
        when(pixEventMappingService.mapPaymentTypeToEvent(anyString(), anyString()))
                .thenReturn(PixAutomaticoEventEnum.PAGAMENTO);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        Assertions.assertDoesNotThrow(() -> useCase.sendCallback(request));
    }

    @Test
    void sendCallback_WithNullTxId_ShouldNotUpdateInfo() {
        // Given
        request.setTxId(null);
        when(pixEventMappingService.isPixAutomaticoEvent(isNull(), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        Assertions.assertDoesNotThrow(() -> useCase.sendCallback(request));
    }

    @Test
    void sendCallback_WithEmptyTxId_ShouldNotUpdateInfo() {
        // Given
        request.setTxId("");
        when(pixEventMappingService.isPixAutomaticoEvent(eq(""), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        Assertions.assertDoesNotThrow(() -> useCase.sendCallback(request));
    }

    @Test
    void sendCallback_WhenNotificationManagerFails_ShouldPropagateException() {
        // Given
        BusinessException businessException = new BusinessException("Notification error", "NOTIF_ERR");
        when(pixEventMappingService.isPixAutomaticoEvent(anyString(), anyString())).thenReturn(true);
        when(pixEventMappingService.mapPaymentTypeToEvent(anyString(), anyString()))
                .thenReturn(PixAutomaticoEventEnum.PAGAMENTO);
        doThrow(businessException).when(notificationManagerService)
                .processPixAutomaticoNotification(any(), anyString(), any());

        // When & Then
        BusinessException thrown = Assertions.assertThrows(BusinessException.class, () -> useCase.sendCallback(request));
        Assertions.assertEquals(businessException, thrown);
    }

    @Test
    void sendCallback_WhenTransactionsPortFails_ShouldPropagateException() {
        // Given
        BusinessException businessException = new BusinessException("Transaction port error", "TRANS_ERR");
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);
        doThrow(businessException).when(transactionsPort).send(any(), any(), any());

        // When & Then
        BusinessException thrown = Assertions.assertThrows(BusinessException.class, () -> useCase.sendCallback(request));
        Assertions.assertEquals(businessException, thrown);
    }

    @Test
    void sendCallback_WithDifferentPaymentTypes_ShouldMapCorrectly() {
        // Given
        request.setPaymentType("REFUND");
        when(pixEventMappingService.isPixAutomaticoEvent(eq("pix-tx-123"), eq("REFUND"))).thenReturn(true);
        when(pixEventMappingService.mapPaymentTypeToEvent(eq("pix-tx-123"), eq("REFUND")))
                .thenReturn(PixAutomaticoEventEnum.AGENDAMENTO);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        Assertions.assertDoesNotThrow(() -> useCase.sendCallback(request));
    }

    @Test
    void sendCallback_WithOmpTransactionId_ShouldUpdatePaymentInfo() {
        // Given
        request.setOmpTransactionId("omp-tx-456");
        request.setTxId("pix-tx-123");
        when(pixEventMappingService.isPixAutomaticoEvent(anyString(), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(port, times(1)).sendUpdate(any());
    }

    @Test
    void sendCallback_WithGevenueService_ShouldOnlySendHubNotification() {
        // Given
        request.setService("gevenue-service");
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(transactionsPort, times(1)).send(any(), any(), any());
        verify(pixEventMappingService, never()).isPixAutomaticoEvent(anyString(), anyString());
    }

    @Test
    void sendCallback_WithPixAutomaticoEventButNullEventType_ShouldThrowException() {
        // Given
        when(pixEventMappingService.isPixAutomaticoEvent(anyString(), anyString())).thenReturn(true);
        when(pixEventMappingService.mapPaymentTypeToEvent(anyString(), anyString())).thenReturn(null);

        // When & Then
        BusinessException thrown = Assertions.assertThrows(BusinessException.class, 
            () -> useCase.sendCallback(request));
        
        Assertions.assertEquals("PIX_CALLBACK_ERROR", thrown.getError().getErrorCode());
        Assertions.assertTrue(thrown.getError().getDetails().contains("Não foi possível mapear evento PIX Automático"));
    }

    @Test
    void sendCallback_WithGenericException_ShouldWrapInBusinessException() {
        // Given
        RuntimeException genericException = new RuntimeException("Generic error");
        when(service.generateRequest(any())).thenThrow(genericException);

        // When & Then
        BusinessException thrown = Assertions.assertThrows(BusinessException.class, 
            () -> useCase.sendCallback(request));
        
        Assertions.assertEquals("PIX_CALLBACK_ERROR", thrown.getError().getErrorCode());
        Assertions.assertEquals("Ocorreu um erro interno no callback PIX", thrown.getError().getMessage());
    }

    @Test
    void sendCallback_WithPixAutomaticoAndCriteriosMet_ShouldLogSuccess() {
        // Given
        when(pixEventMappingService.isPixAutomaticoEvent(anyString(), anyString())).thenReturn(true);
        when(pixEventMappingService.mapPaymentTypeToEvent(anyString(), anyString()))
                .thenReturn(PixAutomaticoEventEnum.PAGAMENTO);
        when(notificationManagerService.processPixAutomaticoNotification(any(), anyString(), any()))
                .thenReturn(true); // Critérios atendidos
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When & Then
        Assertions.assertDoesNotThrow(() -> useCase.sendCallback(request));
        
        verify(notificationManagerService, times(1))
                .processPixAutomaticoNotification(any(), eq("pix-tx-123"), eq(PixAutomaticoEventEnum.PAGAMENTO));
    }

    @Test
    void sendCallback_WithPixAutomaticoAndCriteriosNotMet_ShouldLogNotSent() {
        // Given
        when(pixEventMappingService.isPixAutomaticoEvent(anyString(), anyString())).thenReturn(true);
        when(pixEventMappingService.mapPaymentTypeToEvent(anyString(), anyString()))
                .thenReturn(PixAutomaticoEventEnum.PAGAMENTO);
        when(notificationManagerService.processPixAutomaticoNotification(any(), anyString(), any()))
                .thenReturn(false); // Critérios NÃO atendidos
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When & Then
        Assertions.assertDoesNotThrow(() -> useCase.sendCallback(request));
        
        verify(notificationManagerService, times(1))
                .processPixAutomaticoNotification(any(), eq("pix-tx-123"), eq(PixAutomaticoEventEnum.PAGAMENTO));
    }

    @Test
    void sendCallback_WithEmptyOmpTransactionId_ShouldNotUpdatePaymentInfo() {
        // Given
        request.setOmpTransactionId("");
        request.setTxId("pix-tx-123");
        when(pixEventMappingService.isPixAutomaticoEvent(anyString(), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(port, times(1)).sendUpdate(any()); // Should use txId instead
    }

    @Test
    void sendCallback_WithNullOmpTransactionId_ShouldUseTxId() {
        // Given
        request.setOmpTransactionId(null);
        request.setTxId("pix-tx-123");
        when(pixEventMappingService.isPixAutomaticoEvent(anyString(), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(port, times(1)).sendUpdate(any());
    }
}
