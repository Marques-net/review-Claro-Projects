package com.omp.hub.callback.domain.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    @Test
    void builder_WithAllFields_ShouldCreateErrorResponse() {
        // Given
        Instant timestamp = Instant.now();

        // When
        ErrorResponse response = ErrorResponse.builder()
            .status(400)
            .timestamp(timestamp)
            .message("Test message")
            .path("/test")
            .errorCode("TEST_CODE")
            .details("Test details")
            .build();

        // Then
        assertEquals(400, response.getStatus());
        assertEquals(timestamp, response.getTimestamp());
        assertEquals("Test message", response.getMessage());
        assertEquals("/test", response.getPath());
        assertEquals("TEST_CODE", response.getErrorCode());
        assertEquals("Test details", response.getDetails());
    }

    @Test
    void builder_WithMinimalFields_ShouldCreateErrorResponse() {
        // When
        ErrorResponse response = ErrorResponse.builder()
            .message("Error")
            .errorCode("CODE")
            .build();

        // Then
        assertNull(response.getStatus());
        assertNull(response.getTimestamp());
        assertEquals("Error", response.getMessage());
        assertNull(response.getPath());
        assertEquals("CODE", response.getErrorCode());
        assertNull(response.getDetails());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyErrorResponse() {
        // When
        ErrorResponse response = new ErrorResponse();

        // Then
        assertNull(response.getStatus());
        assertNull(response.getTimestamp());
        assertNull(response.getMessage());
        assertNull(response.getPath());
        assertNull(response.getErrorCode());
        assertNull(response.getDetails());
    }

    @Test
    void allArgsConstructor_ShouldCreateErrorResponse() {
        // Given
        Instant timestamp = Instant.now();

        // When
        ErrorResponse response = new ErrorResponse(
            400, 
            timestamp, 
            "Message", 
            "/path", 
            "CODE", 
            "Details"
        );

        // Then
        assertEquals(400, response.getStatus());
        assertEquals(timestamp, response.getTimestamp());
        assertEquals("Message", response.getMessage());
        assertEquals("/path", response.getPath());
        assertEquals("CODE", response.getErrorCode());
        assertEquals("Details", response.getDetails());
    }

    @Test
    void jsonSerialization_WithAllFields_ShouldIncludeAllFields() throws JsonProcessingException {
        // Given
        Instant timestamp = Instant.parse("2023-01-01T10:00:00Z");
        ErrorResponse response = ErrorResponse.builder()
            .status(500)
            .timestamp(timestamp)
            .message("Error occurred")
            .path("/api/test")
            .errorCode("ERR_500")
            .details("Stack trace here")
            .build();

        // When
        String json = objectMapper.writeValueAsString(response);

        // Then
        assertTrue(json.contains("\"status\":500"));
        assertTrue(json.contains("\"message\":\"Error occurred\""));
        assertTrue(json.contains("\"path\":\"/api/test\""));
        assertTrue(json.contains("\"errorCode\":\"ERR_500\""));
        assertTrue(json.contains("\"details\":\"Stack trace here\""));
    }

    @Test
    void jsonSerialization_WithNullFields_ShouldExcludeNullFields() throws JsonProcessingException {
        // Given - @JsonInclude(NON_NULL) deve excluir campos nulos
        ErrorResponse response = ErrorResponse.builder()
            .message("Error")
            .errorCode("CODE")
            .build();

        // When
        String json = objectMapper.writeValueAsString(response);

        // Then
        assertFalse(json.contains("\"status\""));
        assertFalse(json.contains("\"timestamp\""));
        assertFalse(json.contains("\"path\""));
        assertFalse(json.contains("\"details\""));
        assertTrue(json.contains("\"message\":\"Error\""));
        assertTrue(json.contains("\"errorCode\":\"CODE\""));
    }

    @Test
    void equals_WithSameValues_ShouldReturnTrue() {
        // Given
        ErrorResponse response1 = ErrorResponse.builder()
            .status(400)
            .message("Error")
            .errorCode("CODE")
            .build();

        ErrorResponse response2 = ErrorResponse.builder()
            .status(400)
            .message("Error")
            .errorCode("CODE")
            .build();

        // Then
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void equals_WithDifferentValues_ShouldReturnFalse() {
        // Given
        ErrorResponse response1 = ErrorResponse.builder()
            .status(400)
            .message("Error")
            .errorCode("CODE1")
            .build();

        ErrorResponse response2 = ErrorResponse.builder()
            .status(400)
            .message("Error")
            .errorCode("CODE2")
            .build();

        // Then
        assertNotEquals(response1, response2);
    }

    @Test
    void toString_ShouldContainAllFields() {
        // Given
        ErrorResponse response = ErrorResponse.builder()
            .status(404)
            .message("Not found")
            .errorCode("NOT_FOUND")
            .details("Resource missing")
            .build();

        // When
        String toString = response.toString();

        // Then
        assertTrue(toString.contains("status=404"));
        assertTrue(toString.contains("message=Not found"));
        assertTrue(toString.contains("errorCode=NOT_FOUND"));
        assertTrue(toString.contains("details=Resource missing"));
    }
}
