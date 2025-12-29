package com.omp.hub.callback.application.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SqsException;

@ExtendWith(MockitoExtension.class)
class SqsHealthCheckServiceTest {

    @Mock
    private SqsClient sqsClient;

    @InjectMocks
    private SqsHealthCheckService sqsHealthCheckService;

    private final String queueUrl = "https://sqs.sa-east-1.amazonaws.com/123456789/test-queue.fifo";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sqsHealthCheckService, "queueUrl", queueUrl);
    }

    @Test
    void checkSqsHealth_ShouldReturnUP_WhenSqsIsHealthy() {
        // Given
        when(sqsClient.serviceName()).thenReturn("sqs");

        // When
        Map<String, Object> result = sqsHealthCheckService.checkSqsHealth();

        // Then
        assertNotNull(result);
        assertEquals("UP", result.get("status"));
        assertEquals(queueUrl, result.get("queueUrl"));
        assertEquals("sqs", result.get("service"));
        assertEquals("SQS client configured successfully", result.get("message"));
        assertFalse(result.containsKey("error"));
        assertFalse(result.containsKey("errorCode"));
        
        verify(sqsClient).serviceName();
    }

    @Test
    void checkSqsHealth_ShouldReturnDOWN_WhenSqsExceptionOccurs() {
        // Given
        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorCode("AWS.SimpleQueueService.NonExistentQueue")
                .errorMessage("The specified queue does not exist")
                .build();

        SqsException sqsException = (SqsException) SqsException.builder()
                .awsErrorDetails(errorDetails)
                .message("The specified queue does not exist")
                .build();

        when(sqsClient.serviceName()).thenThrow(sqsException);

        // When
        Map<String, Object> result = sqsHealthCheckService.checkSqsHealth();

        // Then
        assertNotNull(result);
        assertEquals("DOWN", result.get("status"));
        assertEquals(queueUrl, result.get("queueUrl"));
        assertNotNull(result.get("error"));
        assertEquals("AWS.SimpleQueueService.NonExistentQueue", result.get("errorCode"));
    }

    @Test
    void checkSqsHealth_ShouldReturnDOWN_WhenGenericExceptionOccurs() {
        // Given
        when(sqsClient.serviceName()).thenThrow(new RuntimeException("Connection timeout"));

        // When
        Map<String, Object> result = sqsHealthCheckService.checkSqsHealth();

        // Then
        assertNotNull(result);
        assertEquals("DOWN", result.get("status"));
        assertEquals(queueUrl, result.get("queueUrl"));
        assertTrue(result.get("error").toString().contains("Connection timeout"));
        assertFalse(result.containsKey("errorCode"));
    }

    @Test
    void isSqsHealthy_ShouldReturnTrue_WhenSqsIsHealthy() {
        // Given
        when(sqsClient.serviceName()).thenReturn("sqs");

        // When
        boolean result = sqsHealthCheckService.isSqsHealthy();

        // Then
        assertTrue(result);
    }

    @Test
    void isSqsHealthy_ShouldReturnFalse_WhenSqsIsDown() {
        // Given
        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorCode("AWS.SimpleQueueService.NonExistentQueue")
                .errorMessage("The specified queue does not exist")
                .build();

        SqsException sqsException = (SqsException) SqsException.builder()
                .awsErrorDetails(errorDetails)
                .message("The specified queue does not exist")
                .build();

        when(sqsClient.serviceName()).thenThrow(sqsException);

        // When
        boolean result = sqsHealthCheckService.isSqsHealthy();

        // Then
        assertFalse(result);
    }

    @Test
    void checkSqsHealth_ShouldIncludeQueueUrl() {
        // Given
        when(sqsClient.serviceName()).thenReturn("sqs");

        // When
        Map<String, Object> result = sqsHealthCheckService.checkSqsHealth();

        // Then
        assertTrue(result.containsKey("queueUrl"));
        assertEquals(queueUrl, result.get("queueUrl"));
        assertTrue(result.containsKey("service"));
        assertEquals("sqs", result.get("service"));
    }

    @Test
    void checkSqsHealth_ShouldReturnDOWN_WhenSqsClientIsNull() {
        // Create a service instance with null client for testing
        SqsHealthCheckService serviceWithNullClient = new SqsHealthCheckService(null);
        ReflectionTestUtils.setField(serviceWithNullClient, "queueUrl", queueUrl);

        // When
        Map<String, Object> result = serviceWithNullClient.checkSqsHealth();

        // Then
        assertNotNull(result);
        assertEquals("DOWN", result.get("status"));
        assertEquals(queueUrl, result.get("queueUrl"));
        assertTrue(result.get("error").toString().contains("SQS client não foi configurado"));
    }

    @Test
    void checkSqsHealth_ShouldReturnDOWN_WhenServiceNameIsNotSqs() {
        // Given
        when(sqsClient.serviceName()).thenReturn("ec2");

        // When
        Map<String, Object> result = sqsHealthCheckService.checkSqsHealth();

        // Then
        assertNotNull(result);
        assertEquals("DOWN", result.get("status"));
        assertEquals(queueUrl, result.get("queueUrl"));
        assertTrue(result.get("error").toString().contains("Cliente SQS não está configurado corretamente"));
    }

    @Test
    void checkSqsHealth_ShouldReturnDOWN_WhenServiceNameIsNull() {
        // Given
        when(sqsClient.serviceName()).thenReturn(null);

        // When
        Map<String, Object> result = sqsHealthCheckService.checkSqsHealth();

        // Then
        assertNotNull(result);
        assertEquals("DOWN", result.get("status"));
        assertEquals(queueUrl, result.get("queueUrl"));
        assertTrue(result.get("error").toString().contains("Cliente SQS não está configurado corretamente"));
    }

    @Test
    void checkDlqHealth_ShouldReturnUP_WhenDlqIsEmpty() {
        // Given
        String dlqUrl = "https://sqs.sa-east-1.amazonaws.com/123456789/test-dlq.fifo";
        ReflectionTestUtils.setField(sqsHealthCheckService, "dlqUrl", dlqUrl);

        var mockResponse = mock(software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse.class);
        var attributes = Map.of(
            software.amazon.awssdk.services.sqs.model.QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES, "0",
            software.amazon.awssdk.services.sqs.model.QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE, "0"
        );
        
        when(mockResponse.attributes()).thenReturn(attributes);
        when(sqsClient.getQueueAttributes(any(software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest.class)))
            .thenReturn(mockResponse);

        // When
        Map<String, Object> result = sqsHealthCheckService.checkDlqHealth();

        // Then
        assertNotNull(result);
        assertEquals("UP", result.get("status"));
        assertEquals(dlqUrl, result.get("dlqUrl"));
        assertEquals(0, result.get("approximateNumberOfMessages"));
        assertEquals(0, result.get("approximateNumberOfMessagesNotVisible"));
        assertEquals("DLQ está vazia", result.get("message"));
    }

    @Test
    void checkDlqHealth_ShouldReturnWARNING_WhenDlqHasMessages() {
        // Given
        String dlqUrl = "https://sqs.sa-east-1.amazonaws.com/123456789/test-dlq.fifo";
        ReflectionTestUtils.setField(sqsHealthCheckService, "dlqUrl", dlqUrl);

        var mockResponse = mock(software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse.class);
        var attributes = Map.of(
            software.amazon.awssdk.services.sqs.model.QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES, "5",
            software.amazon.awssdk.services.sqs.model.QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE, "2"
        );
        
        when(mockResponse.attributes()).thenReturn(attributes);
        when(sqsClient.getQueueAttributes(any(software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest.class)))
            .thenReturn(mockResponse);

        // When
        Map<String, Object> result = sqsHealthCheckService.checkDlqHealth();

        // Then
        assertNotNull(result);
        assertEquals("WARNING", result.get("status"));
        assertEquals(dlqUrl, result.get("dlqUrl"));
        assertEquals(5, result.get("approximateNumberOfMessages"));
        assertEquals(2, result.get("approximateNumberOfMessagesNotVisible"));
        assertEquals("DLQ contém 5 mensagem(ns) que requer(em) atenção", result.get("message"));
    }

    @Test
    void checkDlqHealth_ShouldReturnDOWN_WhenSqsExceptionOccurs() {
        // Given
        String dlqUrl = "https://sqs.sa-east-1.amazonaws.com/123456789/test-dlq.fifo";
        ReflectionTestUtils.setField(sqsHealthCheckService, "dlqUrl", dlqUrl);

        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorCode("AWS.SimpleQueueService.NonExistentQueue")
                .errorMessage("The specified queue does not exist")
                .build();

        SqsException sqsException = (SqsException) SqsException.builder()
                .awsErrorDetails(errorDetails)
                .message("The specified queue does not exist")
                .build();

        when(sqsClient.getQueueAttributes(any(software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest.class)))
            .thenThrow(sqsException);

        // When
        Map<String, Object> result = sqsHealthCheckService.checkDlqHealth();

        // Then
        assertNotNull(result);
        assertEquals("DOWN", result.get("status"));
        assertEquals(dlqUrl, result.get("dlqUrl"));
        assertNotNull(result.get("error"));
        assertEquals("AWS.SimpleQueueService.NonExistentQueue", result.get("errorCode"));
    }

    @Test
    void checkDlqHealth_ShouldReturnDOWN_WhenGenericExceptionOccurs() {
        // Given
        String dlqUrl = "https://sqs.sa-east-1.amazonaws.com/123456789/test-dlq.fifo";
        ReflectionTestUtils.setField(sqsHealthCheckService, "dlqUrl", dlqUrl);

        when(sqsClient.getQueueAttributes(any(software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest.class)))
            .thenThrow(new RuntimeException("Connection timeout"));

        // When
        Map<String, Object> result = sqsHealthCheckService.checkDlqHealth();

        // Then
        assertNotNull(result);
        assertEquals("DOWN", result.get("status"));
        assertEquals(dlqUrl, result.get("dlqUrl"));
        assertTrue(result.get("error").toString().contains("Connection timeout"));
        assertFalse(result.containsKey("errorCode"));
    }

    @Test
    void checkDlqHealth_ShouldHandleNullAttributeValues() {
        // Given
        String dlqUrl = "https://sqs.sa-east-1.amazonaws.com/123456789/test-dlq.fifo";
        ReflectionTestUtils.setField(sqsHealthCheckService, "dlqUrl", dlqUrl);

        var mockResponse = mock(software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse.class);
        Map<software.amazon.awssdk.services.sqs.model.QueueAttributeName, String> attributes = Map.of(); // Empty attributes map
        
        when(mockResponse.attributes()).thenReturn(attributes);
        when(sqsClient.getQueueAttributes(any(software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest.class)))
            .thenReturn(mockResponse);

        // When
        Map<String, Object> result = sqsHealthCheckService.checkDlqHealth();

        // Then
        assertNotNull(result);
        assertEquals("UP", result.get("status"));
        assertEquals(dlqUrl, result.get("dlqUrl"));
        assertEquals(0, result.get("approximateNumberOfMessages"));
        assertEquals(0, result.get("approximateNumberOfMessagesNotVisible"));
        assertEquals("DLQ está vazia", result.get("message"));
    }

    @Test
    void isSqsHealthy_ShouldReturnFalse_WhenGenericExceptionOccurs() {
        // Given
        when(sqsClient.serviceName()).thenThrow(new RuntimeException("Connection error"));

        // When
        boolean result = sqsHealthCheckService.isSqsHealthy();

        // Then
        assertFalse(result);
    }
}
