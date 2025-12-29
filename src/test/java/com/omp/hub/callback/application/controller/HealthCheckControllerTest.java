package com.omp.hub.callback.application.controller;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.omp.hub.callback.application.service.SqsHealthCheckService;

@ExtendWith(MockitoExtension.class)
class HealthCheckControllerTest {

    @Mock
    private SqsHealthCheckService sqsHealthCheckService;

    @InjectMocks
    private HealthCheckController healthCheckController;

    @Test
    void healthCheck_ShouldReturnOkStatus() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.healthCheck();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void healthCheck_ShouldReturnStatusUP() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.healthCheck();

        // Then
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
    }

    @Test
    void healthCheck_ShouldReturnTimestamp() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.healthCheck();

        // Then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("timestamp"));
        assertTrue(response.getBody().get("timestamp") instanceof Long);
    }

    @Test
    void healthCheck_ShouldReturnServiceName() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.healthCheck();

        // Then
        assertNotNull(response.getBody());
        assertEquals("omp-hub-payment-callback-ms", response.getBody().get("service"));
    }

    @Test
    void healthCheck_ShouldReturnAllExpectedFields() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.healthCheck();

        // Then
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().containsKey("status"));
        assertTrue(response.getBody().containsKey("timestamp"));
        assertTrue(response.getBody().containsKey("service"));
    }

    @Test
    void healthCheck_TimestampShouldBeReasonable() {
        // Given
        long beforeCall = System.currentTimeMillis();

        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.healthCheck();

        // Then
        long afterCall = System.currentTimeMillis();
        Long timestamp = (Long) response.getBody().get("timestamp");
        
        assertTrue(timestamp >= beforeCall);
        assertTrue(timestamp <= afterCall);
    }

    @Test
    void sqsHealthCheck_WhenHealthy_ShouldReturnOkStatus() {
        // Given
        Map<String, Object> healthyResponse = new HashMap<>();
        healthyResponse.put("status", "UP");
        healthyResponse.put("message", "SQS is healthy");
        when(sqsHealthCheckService.checkSqsHealth()).thenReturn(healthyResponse);

        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.sqsHealthCheck();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(healthyResponse, response.getBody());
    }

    @Test
    void sqsHealthCheck_WhenUnhealthy_ShouldReturnServiceUnavailable() {
        // Given
        Map<String, Object> unhealthyResponse = new HashMap<>();
        unhealthyResponse.put("status", "DOWN");
        unhealthyResponse.put("error", "SQS connection failed");
        when(sqsHealthCheckService.checkSqsHealth()).thenReturn(unhealthyResponse);

        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.sqsHealthCheck();

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(unhealthyResponse, response.getBody());
    }

    @Test
    void dlqHealthCheck_WhenStatusUp_ShouldReturnOkStatus() {
        // Given
        Map<String, Object> dlqResponse = new HashMap<>();
        dlqResponse.put("status", "UP");
        dlqResponse.put("message", "DLQ is healthy");
        when(sqsHealthCheckService.checkDlqHealth()).thenReturn(dlqResponse);

        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.dlqHealthCheck();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dlqResponse, response.getBody());
    }

    @Test
    void dlqHealthCheck_WhenStatusWarning_ShouldReturnOkStatus() {
        // Given
        Map<String, Object> dlqResponse = new HashMap<>();
        dlqResponse.put("status", "WARNING");
        dlqResponse.put("message", "DLQ has some issues but is functional");
        when(sqsHealthCheckService.checkDlqHealth()).thenReturn(dlqResponse);

        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.dlqHealthCheck();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dlqResponse, response.getBody());
    }

    @Test
    void dlqHealthCheck_WhenStatusDown_ShouldReturnServiceUnavailable() {
        // Given
        Map<String, Object> dlqResponse = new HashMap<>();
        dlqResponse.put("status", "DOWN");
        dlqResponse.put("error", "DLQ connection failed");
        when(sqsHealthCheckService.checkDlqHealth()).thenReturn(dlqResponse);

        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.dlqHealthCheck();

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(dlqResponse, response.getBody());
    }
}
