package com.omp.hub.callback.application.utils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.omp.hub.callback.domain.exceptions.ErrorResponse;

@ExtendWith(MockitoExtension.class)
class ErrorResponseMapperTest {

    @InjectMocks
    private ErrorResponseMapper errorResponseMapper;

    private Exception testException;
    private String testPath;

    @BeforeEach
    void setUp() {
        testException = new RuntimeException("Erro de teste");
        testPath = "/api/callback/test";
    }

    @Test
    void mapExceptionToErrorResponse_WithValidException_ShouldReturnErrorResponse() {
        // When
        ErrorResponse result = errorResponseMapper.mapExceptionToErrorResponse(testException, testPath);

        // Then
        assertNotNull(result);
        assertEquals(422, result.getStatus());
        assertEquals("Não foi possível processar o callback do pagamento devido a um erro interno", result.getMessage());
        assertEquals(testPath, result.getPath());
        assertEquals("CALLBACK_PROCESSING_ERROR", result.getErrorCode());
        assertEquals("Falha no processamento: Erro de teste", result.getDetails());
        assertNotNull(result.getTimestamp());
        assertTrue(result.getTimestamp().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void mapExceptionToErrorResponse_WithExceptionWithNullMessage_ShouldUseDefaultMessage() {
        // Given
        Exception exceptionWithNullMessage = new RuntimeException((String) null);

        // When
        ErrorResponse result = errorResponseMapper.mapExceptionToErrorResponse(exceptionWithNullMessage, testPath);

        // Then
        assertNotNull(result);
        assertEquals(422, result.getStatus());
        assertEquals("Não foi possível processar o callback do pagamento devido a um erro interno", result.getMessage());
        assertEquals(testPath, result.getPath());
        assertEquals("CALLBACK_PROCESSING_ERROR", result.getErrorCode());
        assertEquals("Falha no processamento: Erro desconhecido", result.getDetails());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void mapExceptionToErrorResponse_WithEmptyPath_ShouldAcceptEmptyPath() {
        // Given
        String emptyPath = "";

        // When
        ErrorResponse result = errorResponseMapper.mapExceptionToErrorResponse(testException, emptyPath);

        // Then
        assertNotNull(result);
        assertEquals(422, result.getStatus());
        assertEquals(emptyPath, result.getPath());
        assertEquals("CALLBACK_PROCESSING_ERROR", result.getErrorCode());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void mapExceptionToErrorResponse_WithNullPath_ShouldAcceptNullPath() {
        // When
        ErrorResponse result = errorResponseMapper.mapExceptionToErrorResponse(testException, null);

        // Then
        assertNotNull(result);
        assertEquals(422, result.getStatus());
        assertNull(result.getPath());
        assertEquals("CALLBACK_PROCESSING_ERROR", result.getErrorCode());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void mapMaxRetriesExceededError_WithValidParameters_ShouldReturnErrorResponse() {
        // Given
        String identifier = "TXN-12345";
        int maxRetries = 3;
        Exception lastException = new RuntimeException("Conexão falhada");

        // When
        ErrorResponse result = errorResponseMapper.mapMaxRetriesExceededError(identifier, maxRetries, lastException);

        // Then
        assertNotNull(result);
        assertEquals(424, result.getStatus());
        assertEquals("O processamento do callback do pagamento falhou após múltiplas tentativas. " +
                "Por favor, verifique o status do pagamento e entre em contato com o suporte se necessário.", 
                result.getMessage());
        assertEquals("/callback", result.getPath());
        assertEquals("CALLBACK_MAX_RETRIES_EXCEEDED", result.getErrorCode());
        assertEquals("Transação TXN-12345 falhou após 3 tentativas. Último erro: Conexão falhada", 
                result.getDetails());
        assertNotNull(result.getTimestamp());
        assertTrue(result.getTimestamp().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void mapMaxRetriesExceededError_WithNullException_ShouldUseDefaultErrorMessage() {
        // Given
        String identifier = "TXN-67890";
        int maxRetries = 5;

        // When
        ErrorResponse result = errorResponseMapper.mapMaxRetriesExceededError(identifier, maxRetries, null);

        // Then
        assertNotNull(result);
        assertEquals(424, result.getStatus());
        assertEquals("O processamento do callback do pagamento falhou após múltiplas tentativas. " +
                "Por favor, verifique o status do pagamento e entre em contato com o suporte se necessário.", 
                result.getMessage());
        assertEquals("/callback", result.getPath());
        assertEquals("CALLBACK_MAX_RETRIES_EXCEEDED", result.getErrorCode());
        assertEquals("Transação TXN-67890 falhou após 5 tentativas. Último erro: Erro não especificado durante o processamento do callback", 
                result.getDetails());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void mapMaxRetriesExceededError_WithExceptionWithNullMessage_ShouldUseDefaultErrorMessage() {
        // Given
        String identifier = "TXN-11111";
        int maxRetries = 2;
        Exception exceptionWithNullMessage = new RuntimeException((String) null);

        // When
        ErrorResponse result = errorResponseMapper.mapMaxRetriesExceededError(identifier, maxRetries, exceptionWithNullMessage);

        // Then
        assertNotNull(result);
        assertEquals(424, result.getStatus());
        assertEquals("O processamento do callback do pagamento falhou após múltiplas tentativas. " +
                "Por favor, verifique o status do pagamento e entre em contato com o suporte se necessário.", 
                result.getMessage());
        assertEquals("/callback", result.getPath());
        assertEquals("CALLBACK_MAX_RETRIES_EXCEEDED", result.getErrorCode());
        assertEquals("Transação TXN-11111 falhou após 2 tentativas. Último erro: Erro não especificado durante o processamento do callback", 
                result.getDetails());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void mapMaxRetriesExceededError_WithZeroRetries_ShouldHandleEdgeCase() {
        // Given
        String identifier = "TXN-00000";
        int maxRetries = 0;
        Exception lastException = new RuntimeException("Falha imediata");

        // When
        ErrorResponse result = errorResponseMapper.mapMaxRetriesExceededError(identifier, maxRetries, lastException);

        // Then
        assertNotNull(result);
        assertEquals(424, result.getStatus());
        assertEquals("CALLBACK_MAX_RETRIES_EXCEEDED", result.getErrorCode());
        assertEquals("Transação TXN-00000 falhou após 0 tentativas. Último erro: Falha imediata", 
                result.getDetails());
    }

    @Test
    void mapMaxRetriesExceededError_WithEmptyIdentifier_ShouldAcceptEmptyIdentifier() {
        // Given
        String emptyIdentifier = "";
        int maxRetries = 1;
        Exception lastException = new RuntimeException("Teste");

        // When
        ErrorResponse result = errorResponseMapper.mapMaxRetriesExceededError(emptyIdentifier, maxRetries, lastException);

        // Then
        assertNotNull(result);
        assertEquals(424, result.getStatus());
        assertTrue(result.getDetails().contains("Transação  falhou após 1 tentativas"));
    }

    @Test
    void mapMaxRetriesExceededError_WithNullIdentifier_ShouldAcceptNullIdentifier() {
        // Given
        int maxRetries = 1;
        Exception lastException = new RuntimeException("Teste");

        // When
        ErrorResponse result = errorResponseMapper.mapMaxRetriesExceededError(null, maxRetries, lastException);

        // Then
        assertNotNull(result);
        assertEquals(424, result.getStatus());
        assertTrue(result.getDetails().contains("Transação null falhou após 1 tentativas"));
    }
}