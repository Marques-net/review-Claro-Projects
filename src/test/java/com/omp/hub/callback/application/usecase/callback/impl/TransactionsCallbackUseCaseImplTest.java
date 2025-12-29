package com.omp.hub.callback.application.usecase.callback.impl;

import com.omp.hub.callback.application.service.RetryService;
import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.transactions.*;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import com.omp.hub.callback.domain.ports.client.TransationsNotificationsPort;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackTransactionsService;
import com.omp.hub.callback.domain.service.impl.notification.PixEventMappingService;
import com.omp.hub.callback.domain.service.impl.notification.NotificationManagerService;
import okhttp3.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionsCallbackUseCaseImplTest {

    @Mock
    private InformationPaymentPort port;

    @Mock
    private GenerateCallbackTransactionsService service;

    @Mock
    private TransationsNotificationsPort transactionsPort;

    @Mock
    private ApigeeHeaderService apigeeHeaderService;

    @Mock
    private NotificationManagerService notificationManagerService;

    @Mock
    private PixEventMappingService pixEventMappingService;

    @Mock
    private RetryService retryService;

    @InjectMocks
    private TransactionsCallbackUseCaseImpl useCase;

    private TransactionsRequest request;
    private EventDTO event;
    private Headers.Builder mockHeadersBuilder;
    private OmphubTransactionNotificationRequest mockNotificationRequest;

    @BeforeEach
    void setUp() {
        event = new EventDTO();
        event.setType("PAYMENT");
        event.setTxId("tx-123");
        
        request = new TransactionsRequest();
        request.setEvent(event);
        
        mockHeadersBuilder = new Headers.Builder();
        mockNotificationRequest = new OmphubTransactionNotificationRequest();
        
        // Mock RetryService para executar a ação diretamente (lenient para evitar unnecessary stubbing)
        lenient().doAnswer(invocation -> {
            Runnable action = invocation.getArgument(2);
            action.run();
            return null;
        }).when(retryService).executeWithRetrySyncVoid(any(UUID.class), anyString(), any(Runnable.class), any());
    }

    @Test
    void sendCallback_WithTxIdInEventAndNoNotification_ShouldSendTransactionOnly() {
        // Given
        when(pixEventMappingService.shouldNotify(anyString(), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(pixEventMappingService).shouldNotify("tx-123", "PAYMENT");
        verify(notificationManagerService, never()).processPixAutomaticoNotification(any(), anyString(), any());
        verify(transactionsPort).send(any(UUID.class), eq(mockNotificationRequest), eq(mockHeadersBuilder));
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WithTxIdAndEligibleForNotification_ShouldProcessBoth() {
        // Given
        when(pixEventMappingService.shouldNotify(eq("tx-123"), eq("PAYMENT"))).thenReturn(true);
        when(pixEventMappingService.mapEventTypeToEnum(eq("tx-123"), eq("PAYMENT"), isNull(), isNull(), isNull()))
                .thenReturn(PixAutomaticoEventEnum.PAGAMENTO);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(pixEventMappingService).shouldNotify("tx-123", "PAYMENT");
        verify(pixEventMappingService).mapEventTypeToEnum(eq("tx-123"), eq("PAYMENT"), isNull(), isNull(), isNull());
        verify(notificationManagerService).processPixAutomaticoNotification(any(UUID.class), eq("tx-123"), eq(PixAutomaticoEventEnum.PAGAMENTO));
        verify(transactionsPort).send(any(UUID.class), eq(mockNotificationRequest), eq(mockHeadersBuilder));
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WithTxIdInOriginPaymentMethod_ShouldExtractTxIdCorrectly() {
        // Given
        event.setTxId(null);
        OriginPaymentMethodDTO originPayment = new OriginPaymentMethodDTO();
        originPayment.setTxId("origin-tx-456");
        event.setOriginPaymentMethod(originPayment);
        
        when(pixEventMappingService.shouldNotify(anyString(), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(pixEventMappingService).shouldNotify("origin-tx-456", "PAYMENT");
        verify(transactionsPort).send(any(UUID.class), eq(mockNotificationRequest), eq(mockHeadersBuilder));
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WithTxIdInPaymentPix_ShouldExtractTxIdCorrectly() {
        // Given
        event.setTxId(null);
        PaymentDTO payment = new PaymentDTO();
        PixDTO pix = new PixDTO();
        pix.setTxId("pix-tx-789");
        payment.setPix(pix);
        event.setPayment(java.util.Collections.singletonList(payment));
        
        when(pixEventMappingService.shouldNotify(anyString(), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(pixEventMappingService).shouldNotify("pix-tx-789", "PAYMENT");
        verify(transactionsPort).send(any(UUID.class), eq(mockNotificationRequest), eq(mockHeadersBuilder));
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WithTxIdInActivationJourneyData_ShouldExtractTxIdCorrectly() {
        // Given
        event.setTxId(null);
        ActivationDTO activation = new ActivationDTO();
        JourneyDataDTO journeyData = new JourneyDataDTO();
        journeyData.setTxId("journey-tx-abc");
        activation.setJourneyData(journeyData);
        event.setActivation(activation);
        
        when(pixEventMappingService.shouldNotify(anyString(), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(pixEventMappingService).shouldNotify("journey-tx-abc", "PAYMENT");
        verify(transactionsPort).send(any(UUID.class), eq(mockNotificationRequest), eq(mockHeadersBuilder));
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WithNoTxId_ShouldNotCallUpdateInfo() {
        // Given
        event.setTxId(null);
        
        when(pixEventMappingService.shouldNotify(isNull(), anyString())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(port, never()).sendUpdate(any(InformationPaymentDTO.class));
        verify(transactionsPort).send(any(UUID.class), eq(mockNotificationRequest), eq(mockHeadersBuilder));
    }

    @Test
    void sendCallback_WhenNotificationMapReturnsNull_ShouldNotCallNotificationManager() {
        // Given
        when(pixEventMappingService.shouldNotify(eq("tx-123"), eq("PAYMENT"))).thenReturn(true);
        when(pixEventMappingService.mapEventTypeToEnum(eq("tx-123"), eq("PAYMENT"), isNull(), isNull(), isNull())).thenReturn(null);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(pixEventMappingService).mapEventTypeToEnum(eq("tx-123"), eq("PAYMENT"), isNull(), isNull(), isNull());
        verify(notificationManagerService, never()).processPixAutomaticoNotification(any(), anyString(), any());
        verify(transactionsPort).send(any(UUID.class), eq(mockNotificationRequest), eq(mockHeadersBuilder));
    }

    @Test
    void sendCallback_WhenBusinessExceptionOccurs_ShouldRethrow() {
        // Given
        BusinessException businessException = new BusinessException("Business error", "BUS_ERR");
        when(pixEventMappingService.shouldNotify(anyString(), anyString())).thenThrow(businessException);

        // When & Then
        BusinessException thrown = assertThrows(BusinessException.class, () -> useCase.sendCallback(request));
        assertEquals(businessException, thrown);
    }

    @Test
    void sendCallback_WhenGenericExceptionOccurs_ShouldWrapInBusinessException() {
        // Given
        RuntimeException genericException = new RuntimeException("Generic error");
        when(pixEventMappingService.shouldNotify(anyString(), anyString())).thenThrow(genericException);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.sendCallback(request));
        assertEquals("Ocorreu um erro interno no callback de transação", exception.getError().getMessage());
        assertEquals("TRANSACTION_CALLBACK_ERROR", exception.getError().getErrorCode());
        assertTrue(exception.getError().getDetails().contains("Generic error"));
    }

    @Test
    void sendCallback_WithNullEventType_ShouldHandleGracefully() {
        // Given
        event.setType(null);
        
        when(pixEventMappingService.shouldNotify(anyString(), isNull())).thenReturn(false);
        when(service.generateRequest(any())).thenReturn(mockNotificationRequest);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(pixEventMappingService).shouldNotify("tx-123", null);
        verify(transactionsPort).send(any(UUID.class), eq(mockNotificationRequest), eq(mockHeadersBuilder));
    }
}
