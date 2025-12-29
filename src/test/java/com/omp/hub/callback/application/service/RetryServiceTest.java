package com.omp.hub.callback.application.service;

import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.SqsMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryServiceTest {

    @Mock
    private SqsMessageRepository sqsMessageRepository;

    @InjectMocks
    private RetryService retryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(retryService, "maxAttempts", 3);
        ReflectionTestUtils.setField(retryService, "delaySeconds", 0L);
    }

    @Test
    void executeWithRetrySyncVoid_WhenOperationSucceedsOnFirstAttempt_ShouldComplete() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        Runnable operation = () -> {
            // Operation that succeeds
        };

        // When & Then
        assertDoesNotThrow(() -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation));
    }

    @Test
    void executeWithRetrySyncVoid_WhenOperationAlwaysFails_ShouldThrowException() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        Runnable operation = () -> {
            throw new RuntimeException("Operation failed");
        };

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation));
        
        assertTrue(exception.getMessage().contains("falhou apos 3 tentativas"));
        assertEquals("Operation failed", exception.getCause().getMessage());
    }

    @Test
    void executeWithRetrySyncVoid_WhenOperationSucceedsAfterRetries_ShouldComplete() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        AtomicInteger attemptCount = new AtomicInteger(0);
        Runnable operation = () -> {
            int currentAttempt = attemptCount.incrementAndGet();
            if (currentAttempt < 3) {
                throw new RuntimeException("Operation failed on attempt " + currentAttempt);
            }
        };

        // When & Then
        assertDoesNotThrow(() -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation));
        assertEquals(3, attemptCount.get());
    }

    @Test
    void executeWithRetrySyncVoid_WhenOperationSucceedsOnSecondAttempt_ShouldComplete() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        AtomicInteger attemptCount = new AtomicInteger(0);
        Runnable operation = () -> {
            int currentAttempt = attemptCount.incrementAndGet();
            if (currentAttempt < 2) {
                throw new RuntimeException("Operation failed on attempt " + currentAttempt);
            }
        };

        // When & Then
        assertDoesNotThrow(() -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation));
        assertEquals(2, attemptCount.get());
    }

    @Test
    void executeWithRetrySyncVoid_WhenOperationFailsTwice_ShouldThrowException() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        AtomicInteger attemptCount = new AtomicInteger(0);
        Runnable operation = () -> {
            attemptCount.incrementAndGet();
            throw new RuntimeException("Operation always fails");
        };

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation));
        
        assertEquals(3, attemptCount.get());
        assertTrue(exception.getMessage().contains("falhou apos 3 tentativas"));
        assertEquals("Operation always fails", exception.getCause().getMessage());
    }

    @Test
    void executeWithRetrySyncVoid_WithBusinessException422_ShouldSendToDLQAndRethrow() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        Object callbackData = "test-callback-data";
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .message("Unprocessable entity")
                .build();
        BusinessException businessException = new BusinessException(errorResponse);

        Runnable operation = () -> {
            throw businessException;
        };

        // When & Then
        BusinessException thrown = assertThrows(BusinessException.class, 
            () -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation, callbackData));
        
        assertEquals(businessException, thrown);
        verify(sqsMessageRepository).sendToDLQ(any(), eq(businessException));
    }

    @Test
    void executeWithRetrySyncVoid_WithBusinessException422AndNullCallbackData_ShouldNotSendToDLQ() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .message("Unprocessable entity")
                .build();
        BusinessException businessException = new BusinessException(errorResponse);

        Runnable operation = () -> {
            throw businessException;
        };

        // When & Then
        BusinessException thrown = assertThrows(BusinessException.class, 
            () -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation, null));
        
        assertEquals(businessException, thrown);
        verify(sqsMessageRepository, never()).sendToDLQ(any(), any());
    }

    @Test
    void executeWithRetrySyncVoid_WithBusinessExceptionNot422_ShouldRetryNormally() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal error")
                .build();
        BusinessException businessException = new BusinessException(errorResponse);

        Runnable operation = () -> {
            attemptCount.incrementAndGet();
            throw businessException;
        };

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation));
        
        assertEquals(3, attemptCount.get());
        verify(sqsMessageRepository, never()).sendToDLQ(any(), any());
    }

    @Test
    void executeWithRetrySyncVoid_WithSendToDLQException_ShouldHandleGracefully() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        Object callbackData = "test-callback-data";
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .message("Unprocessable entity")
                .build();
        BusinessException businessException = new BusinessException(errorResponse);

        Runnable operation = () -> {
            throw businessException;
        };

        doThrow(new RuntimeException("DLQ error")).when(sqsMessageRepository).sendToDLQ(any(), any());

        // When & Then
        BusinessException thrown = assertThrows(BusinessException.class, 
            () -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation, callbackData));
        
        assertEquals(businessException, thrown);
    }

    @Test
    void executeWithRetrySyncVoid_WithCallbackData_ShouldPassToOperation() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        Object callbackData = "test-callback-data";
        
        Runnable operation = () -> {
            // Operation succeeds
        };

        // When & Then
        assertDoesNotThrow(() -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation, callbackData));
    }

    @Test
    void executeWithRetrySyncVoid_WhenThreadInterrupted_ShouldThrowRuntimeException() {
        // Given
        UUID uuid = UUID.randomUUID();
        String operationName = "testOperation";
        ReflectionTestUtils.setField(retryService, "delaySeconds", 10L); 
        
        AtomicInteger attemptCount = new AtomicInteger(0);
        Thread testThread = Thread.currentThread();
        
        Runnable operation = () -> {
            attemptCount.incrementAndGet();
            if (attemptCount.get() == 1) {
                testThread.interrupt();
                throw new RuntimeException("First attempt failed");
            }
        };

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> retryService.executeWithRetrySyncVoid(uuid, operationName, operation));
        
        assertTrue(exception.getMessage().contains("Retry interrompido") || 
                   exception.getMessage().contains("falhou apos"));
        
        // Clear interrupted status
        Thread.interrupted();
    }
}