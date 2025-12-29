package com.omp.hub.callback.application.service;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.omp.hub.callback.application.utils.ErrorResponseMapper;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;

@ExtendWith(MockitoExtension.class)
class CallbackErrorNotificationServiceTest {

    @Mock
    private InformationPaymentPort informationPaymentPort;

    @Mock
    private ErrorResponseMapper errorResponseMapper;

    @InjectMocks
    private CallbackErrorNotificationService callbackErrorNotificationService;

    private String testIdentifier;
    private int testMaxRetries;
    private Exception testException;
    private ErrorResponse testErrorResponse;

    @BeforeEach
    void setUp() {
        testIdentifier = "TXN-12345";
        testMaxRetries = 3;
        testException = new RuntimeException("Connection timeout");

        testErrorResponse = ErrorResponse.builder()
                .status(424)
                .timestamp(Instant.now())
                .message("Callback processing failed")
                .path("/callback")
                .errorCode("CALLBACK_MAX_RETRIES_EXCEEDED")
                .details("Transaction failed after retries")
                .build();
    }

    @Test
    void notifyJourneyAboutCallbackFailure_WithValidParameters_ShouldNotifySuccessfully() {
        // Given
        when(errorResponseMapper.mapMaxRetriesExceededError(testIdentifier, testMaxRetries, testException))
                .thenReturn(testErrorResponse);
        when(informationPaymentPort.sendUpdate(any(InformationPaymentDTO.class))).thenReturn(null);

        // When
        callbackErrorNotificationService.notifyJourneyAboutCallbackFailure(testIdentifier, testMaxRetries, testException);

        // Then
        verify(errorResponseMapper).mapMaxRetriesExceededError(testIdentifier, testMaxRetries, testException);

        ArgumentCaptor<InformationPaymentDTO> captor = ArgumentCaptor.forClass(InformationPaymentDTO.class);
        verify(informationPaymentPort).sendUpdate(captor.capture());

        InformationPaymentDTO capturedRequest = captor.getValue();
        assertEquals(testIdentifier, capturedRequest.getIdentifier());
        assertEquals(1, capturedRequest.getPayments().size());

        PaymentDTO capturedPayment = capturedRequest.getPayments().get(0);
        assertEquals(testErrorResponse, capturedPayment.getError());
    }

    @Test
    void notifyJourneyAboutCallbackFailure_WhenErrorMapperThrowsException_ShouldHandleGracefully() {
        // Given
        when(errorResponseMapper.mapMaxRetriesExceededError(testIdentifier, testMaxRetries, testException))
                .thenThrow(new RuntimeException("Error mapping failed"));

        // When - should not throw exception
        assertDoesNotThrow(() -> {
            callbackErrorNotificationService.notifyJourneyAboutCallbackFailure(testIdentifier, testMaxRetries, testException);
        });

        // Then
        verify(errorResponseMapper).mapMaxRetriesExceededError(testIdentifier, testMaxRetries, testException);
        verifyNoInteractions(informationPaymentPort);
    }

    @Test
    void notifyJourneyAboutCallbackFailure_WhenInformationPaymentPortThrowsException_ShouldHandleGracefully() {
        // Given
        when(errorResponseMapper.mapMaxRetriesExceededError(testIdentifier, testMaxRetries, testException))
                .thenReturn(testErrorResponse);
        when(informationPaymentPort.sendUpdate(any(InformationPaymentDTO.class)))
                .thenThrow(new RuntimeException("Network error"));

        // When - should not throw exception
        assertDoesNotThrow(() -> {
            callbackErrorNotificationService.notifyJourneyAboutCallbackFailure(testIdentifier, testMaxRetries, testException);
        });

        // Then
        verify(errorResponseMapper).mapMaxRetriesExceededError(testIdentifier, testMaxRetries, testException);
        verify(informationPaymentPort).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void notifyJourneyAboutCallbackFailure_WithNullIdentifier_ShouldStillWork() {
        // Given
        String nullIdentifier = null;
        when(errorResponseMapper.mapMaxRetriesExceededError(nullIdentifier, testMaxRetries, testException))
                .thenReturn(testErrorResponse);
        when(informationPaymentPort.sendUpdate(any(InformationPaymentDTO.class))).thenReturn(null);

        // When
        callbackErrorNotificationService.notifyJourneyAboutCallbackFailure(nullIdentifier, testMaxRetries, testException);

        // Then
        verify(errorResponseMapper).mapMaxRetriesExceededError(nullIdentifier, testMaxRetries, testException);
        
        ArgumentCaptor<InformationPaymentDTO> captor = ArgumentCaptor.forClass(InformationPaymentDTO.class);
        verify(informationPaymentPort).sendUpdate(captor.capture());

        InformationPaymentDTO capturedRequest = captor.getValue();
        assertNull(capturedRequest.getIdentifier());
        assertEquals(1, capturedRequest.getPayments().size());
        assertEquals(testErrorResponse, capturedRequest.getPayments().get(0).getError());
    }

    @Test
    void notifyJourneyAboutCallbackFailure_WithZeroMaxRetries_ShouldWork() {
        // Given
        int zeroRetries = 0;
        when(errorResponseMapper.mapMaxRetriesExceededError(testIdentifier, zeroRetries, testException))
                .thenReturn(testErrorResponse);
        when(informationPaymentPort.sendUpdate(any(InformationPaymentDTO.class))).thenReturn(null);

        // When
        callbackErrorNotificationService.notifyJourneyAboutCallbackFailure(testIdentifier, zeroRetries, testException);

        // Then
        verify(errorResponseMapper).mapMaxRetriesExceededError(testIdentifier, zeroRetries, testException);
        verify(informationPaymentPort).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void notifyJourneyAboutCallbackFailure_WithNullException_ShouldWork() {
        // Given
        Exception nullException = null;
        when(errorResponseMapper.mapMaxRetriesExceededError(testIdentifier, testMaxRetries, nullException))
                .thenReturn(testErrorResponse);
        when(informationPaymentPort.sendUpdate(any(InformationPaymentDTO.class))).thenReturn(null);

        // When
        callbackErrorNotificationService.notifyJourneyAboutCallbackFailure(testIdentifier, testMaxRetries, nullException);

        // Then
        verify(errorResponseMapper).mapMaxRetriesExceededError(testIdentifier, testMaxRetries, nullException);
        verify(informationPaymentPort).sendUpdate(any(InformationPaymentDTO.class));
    }
}
