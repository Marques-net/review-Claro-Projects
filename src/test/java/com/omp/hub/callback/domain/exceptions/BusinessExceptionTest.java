package com.omp.hub.callback.domain.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructor_WithMessageAndCode_ShouldCreateException() {
        // When
        BusinessException exception = new BusinessException("Test message", "TEST_CODE");

        // Then
        assertEquals("Test message", exception.getMessage());
        assertNotNull(exception.getError());
        assertEquals("TEST_CODE", exception.getError().getErrorCode());
    }

    @Test
    void constructor_WithMessageCodeAndCause_ShouldCreateException() {
        // Given
        Throwable cause = new RuntimeException("Root cause");

        // When
        BusinessException exception = new BusinessException("Test message", "TEST_CODE", cause);

        // Then
        assertEquals("Test message", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception.getError());
        assertEquals("TEST_CODE", exception.getError().getErrorCode());
        assertNotNull(exception.getError().getTimestamp());
    }

    @Test
    void constructor_WithMessageCodeAndDescription_ShouldCreateException() {
        // When
        BusinessException exception = new BusinessException("Test message", "TEST_CODE", "Detailed description");

        // Then
        assertEquals("Test message", exception.getMessage());
        assertNotNull(exception.getError());
        assertEquals("TEST_CODE", exception.getError().getErrorCode());
        assertEquals("Detailed description", exception.getError().getDetails());
        assertNotNull(exception.getError().getTimestamp());
    }

    @Test
    void constructor_WithFullParameters_ShouldCreateException() {
        // When
        BusinessException exception = new BusinessException(
            "Test message", 
            "TEST_CODE", 
            "Detailed description",
            HttpStatus.BAD_REQUEST
        );

        // Then
        assertNotNull(exception.getError());
        assertEquals("Test message", exception.getError().getMessage());
        assertEquals("TEST_CODE", exception.getError().getErrorCode());
        assertEquals("Detailed description", exception.getError().getDetails());
        assertEquals(400, exception.getError().getStatus());
        assertNotNull(exception.getError().getTimestamp());
    }

    @Test
    void constructor_WithErrorResponse_ShouldCreateException() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
            .message("Custom error")
            .errorCode("CUSTOM_CODE")
            .status(500)
            .build();

        // When
        BusinessException exception = new BusinessException(errorResponse);

        // Then
        assertEquals("Custom error", exception.getMessage());
        assertEquals(errorResponse, exception.getError());
    }

    @Test
    void constructor_WithNullErrorResponse_ShouldUseDefaultMessage() {
        // When
        BusinessException exception = new BusinessException((ErrorResponse) null);

        // Then
        assertEquals("Erro de negócio", exception.getMessage());
        assertNull(exception.getError());
    }

    @Test
    void constructor_WithGenericException_ShouldCreateBusinessException() {
        // Given
        Exception cause = new Exception("Generic error");

        // When
        BusinessException exception = new BusinessException(cause);

        // Then
        assertEquals("Ocorreu um erro interno", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception.getError());
        assertEquals("ERROR", exception.getError().getErrorCode());
        assertEquals("Generic error", exception.getError().getDetails());
        assertEquals(500, exception.getError().getStatus());
        assertNotNull(exception.getError().getTimestamp());
    }

    @Test
    void constructor_WithGenericExceptionWithoutMessage_ShouldUseClassName() {
        // Given
        Exception cause = new NullPointerException();

        // When
        BusinessException exception = new BusinessException(cause);

        // Then
        assertEquals("Ocorreu um erro interno", exception.getMessage());
        assertNotNull(exception.getError());
        assertEquals("NullPointerException", exception.getError().getDetails());
    }
}
