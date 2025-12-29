package com.omp.hub.callback.domain.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import com.omp.hub.callback.infrastructure.persistence.message.sqs.SqsMessageRepository;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;
    private SqsMessageRepository sqsMessageRepository;

    @BeforeEach
    void setUp() {
        sqsMessageRepository = mock(SqsMessageRepository.class);
        handler = new GlobalExceptionHandler(sqsMessageRepository);
        webRequest = mock(WebRequest.class);
    }

    @Test
    void handleBusinessException_WithFullErrorResponse_ShouldReturnResponse() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(400)
            .message("Business error")
            .errorCode("BUS_001")
            .build();
        BusinessException exception = new BusinessException(errorResponse);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorResponse, response.getBody());
        assertEquals("Business error", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleBusinessException_WithNullStatus_ShouldDefaultToBadRequest() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
            .message("Error without status")
            .errorCode("ERR_001")
            .build();
        BusinessException exception = new BusinessException(errorResponse);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Error without status", response.getBody().getMessage());
    }

    @Test
    void handleBusinessException_WithNullMessage_ShouldUseExceptionMessage() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(500)
            .errorCode("ERR_002")
            .build();
        errorResponse.setMessage(null); // Força message = null
        
        BusinessException exception = new BusinessException("Exception message", "ERR_002");
        // Sobrescreve o ErrorResponse interno com um sem mensagem
        exception = new BusinessException(errorResponse);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals("Erro de negócio", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    void handleBusinessException_WithBothNullMessageAndExceptionMessage_ShouldUseDefault() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(403)
            .errorCode("ERR_003")
            .build();
        errorResponse.setMessage(null);
        
        // Cria exception sem mensagem customizada
        BusinessException exception = new BusinessException(errorResponse);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals("Erro de negócio", response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void handleBusinessException_WithDifferentStatusCodes_ShouldReturnCorrectHttpStatus() {
        // Given - Testa vários status codes
        int[] statusCodes = {400, 401, 403, 404, 500, 503};

        for (int statusCode : statusCodes) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                .status(statusCode)
                .message("Test error")
                .errorCode("TEST_" + statusCode)
                .build();
            BusinessException exception = new BusinessException(errorResponse);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, webRequest);

            // Then
            assertEquals(HttpStatus.valueOf(statusCode), response.getStatusCode());
            assertEquals(statusCode, response.getBody().getStatus());
        }
    }

    @Test
    void handleBusinessException_ShouldPreserveAllErrorResponseFields() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(422)
            .message("Validation error")
            .errorCode("VAL_001")
            .path("/api/test")
            .details("Field 'name' is required")
            .build();
        BusinessException exception = new BusinessException(errorResponse);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, webRequest);

        // Then
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(422, body.getStatus());
        assertEquals("Validation error", body.getMessage());
        assertEquals("VAL_001", body.getErrorCode());
        assertEquals("/api/test", body.getPath());
        assertEquals("Field 'name' is required", body.getDetails());
    }

    @Test
    void handleCallbackValidationException_ShouldReturnBadRequest() {
        // Given
        com.omp.hub.callback.application.validator.CallbackValidationException exception = 
            new com.omp.hub.callback.application.validator.CallbackValidationException("Validation failed", "Field 'txId' is required");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleCallbackValidationException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("Field 'txId' is required", response.getBody().getDetails());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleUnprocessable422Exception_WithBusinessException_ShouldReturnUnprocessableEntity() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(422)
            .message("Unprocessable data")
            .errorCode("UNPROC_001")
            .build();
        BusinessException businessException = new BusinessException(errorResponse);
        Object callbackData = java.util.Map.of("txId", "12345");
        Unprocessable422Exception exception = new Unprocessable422Exception("Error 422", businessException, callbackData);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUnprocessable422Exception(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Unprocessable data", response.getBody().getMessage());
        assertEquals("UNPROC_001", response.getBody().getErrorCode());
    }

    @Test
    void handleUnprocessable422Exception_WithNullOriginalException_ShouldReturnDefaultErrorResponse() {
        // Given
        Object callbackData = java.util.Map.of("txId", "12345");
        Unprocessable422Exception exception = new Unprocessable422Exception("Error 422", null, callbackData);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUnprocessable422Exception(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Erro de validação", response.getBody().getMessage());
        assertEquals(422, response.getBody().getStatus());
    }

    @Test
    void handleUnprocessable422Exception_WithNullCallbackData_ShouldNotSendToDLQ() {
        // Given
        BusinessException businessException = new BusinessException("Error", "ERR_001");
        Unprocessable422Exception exception = new Unprocessable422Exception("Error 422", businessException, null);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUnprocessable422Exception(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        // DLQ não deve ser chamado com callback data null
        verify(sqsMessageRepository, never()).sendToDLQ(any(), any());
    }

    @Test
    void handleUnprocessable422Exception_WhenSendToDLQFails_ShouldStillReturnResponse() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(422)
            .message("Unprocessable")
            .build();
        BusinessException businessException = new BusinessException(errorResponse);
        Object callbackData = java.util.Map.of("txId", "12345");
        Unprocessable422Exception exception = new Unprocessable422Exception("Error 422", businessException, callbackData);
        
        doThrow(new RuntimeException("DLQ error")).when(sqsMessageRepository).sendToDLQ(any(), any());

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUnprocessable422Exception(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }
}
