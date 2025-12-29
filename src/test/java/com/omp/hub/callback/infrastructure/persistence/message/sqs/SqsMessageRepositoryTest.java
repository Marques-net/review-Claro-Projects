package com.omp.hub.callback.infrastructure.persistence.message.sqs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.Gson;
import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;
import software.amazon.awssdk.services.sqs.model.KmsAccessDeniedException;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;

@ExtendWith(MockitoExtension.class)
class SqsMessageRepositoryTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private Gson gson;

    @InjectMocks
    private SqsMessageRepository sqsMessageRepository;

    private static final String QUEUE_URL = "https://sqs.sa-east-1.amazonaws.com/123456789/test-queue.fifo";
    private static final String DLQ_URL = "https://sqs.sa-east-1.amazonaws.com/123456789/test-queue-dlq.fifo";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sqsMessageRepository, "queueUrl", QUEUE_URL);
        ReflectionTestUtils.setField(sqsMessageRepository, "dlqUrl", DLQ_URL);
        
        lenient().when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("test-message-id").build());
    }

    @Test
    void sendMessage_ShouldSendMessageSuccessfully() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        sqsMessageRepository.sendMessage(callbackRequest);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.queueUrl()).isEqualTo(QUEUE_URL);
        assertThat(request.messageAttributes()).containsKey("timestamp");
    }

    @Test
    void sendMessage_ShouldThrowException_WhenSqsExceptionOccurs() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(SqsException.builder().message("SQS Error").build());

        assertThatThrownBy(() -> sqsMessageRepository.sendMessage(callbackRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Falha ao enviar mensagem para SQS");
    }

    @Test
    void sendMessage_ShouldThrowException_WhenKmsAccessDeniedExceptionOccurs() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorCode("KMSAccessDeniedException")
                .errorMessage("Access denied to KMS key")
                .build();

        KmsAccessDeniedException kmsException = (KmsAccessDeniedException) KmsAccessDeniedException.builder()
                .awsErrorDetails(errorDetails)
                .message("Access denied to KMS key")
                .build();

        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenThrow(kmsException);

        assertThatThrownBy(() -> sqsMessageRepository.sendMessage(callbackRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha de permissao KMS ao enviar mensagem para SQS")
                .hasCause(kmsException);
    }

    @Test
    void sendToDLQ_ShouldSendMessageWithCorrectAttributes() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .callbackRequest(callbackRequest)
                .timestamp(LocalDateTime.now())
                .retryCount(3)
                .build();

        Exception error = new RuntimeException("Processing failed");

        lenient().when(gson.toJson(any())).thenReturn("{}");

        sqsMessageRepository.sendToDLQ(message, error);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.queueUrl()).isEqualTo(DLQ_URL);
        assertThat(request.messageAttributes()).containsKey("originalTimestamp");
        assertThat(request.messageAttributes()).containsKey("failureTimestamp");
        assertThat(request.messageAttributes()).containsKey("retryCount");
        assertThat(request.messageAttributes()).containsKey("errorMessage");
        assertThat(request.messageAttributes().get("errorMessage").stringValue()).isEqualTo("Processing failed");
    }

    @Test
    void sendToDLQ_ShouldHandleNullErrorMessage() {
        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .timestamp(LocalDateTime.now())
                .retryCount(3)
                .build();

        Exception error = new RuntimeException();

        lenient().when(gson.toJson(any())).thenReturn("{}");

        sqsMessageRepository.sendToDLQ(message, error);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageAttributes().get("errorMessage").stringValue()).isEqualTo("Unknown error");
    }

    @Test
    void sendToDLQ_ShouldHandleNullTimestamp() {
        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .timestamp(null)
                .retryCount(3)
                .build();

        Exception error = new RuntimeException("Processing failed");

        lenient().when(gson.toJson(any())).thenReturn("{}");

        sqsMessageRepository.sendToDLQ(message, error);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageAttributes().get("originalTimestamp").stringValue()).isNotNull();
    }

    @Test
    void sendToDLQ_ShouldHandleNullRetryCount() {
        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .timestamp(LocalDateTime.now())
                .retryCount(null)
                .build();

        Exception error = new RuntimeException("Processing failed");

        lenient().when(gson.toJson(any())).thenReturn("{}");

        sqsMessageRepository.sendToDLQ(message, error);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageAttributes().get("retryCount").stringValue()).isEqualTo("0");
    }

    @Test
    void sendToDLQ_ShouldHandleNullMessageId() {
        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId(null)
                .timestamp(LocalDateTime.now())
                .retryCount(3)
                .build();

        Exception error = new RuntimeException("Processing failed");

        lenient().when(gson.toJson(any())).thenReturn("{}");

        sqsMessageRepository.sendToDLQ(message, error);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageDeduplicationId()).isNotNull();
    }

    @Test
    void sendToDLQ_ShouldThrowException_WhenKmsAccessDeniedExceptionOccurs() {
        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .timestamp(LocalDateTime.now())
                .retryCount(3)
                .build();

        Exception error = new RuntimeException("Processing failed");

        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorCode("KMSAccessDeniedException")
                .errorMessage("Access denied to KMS key")
                .build();

        KmsAccessDeniedException kmsException = (KmsAccessDeniedException) KmsAccessDeniedException.builder()
                .awsErrorDetails(errorDetails)
                .message("Access denied to KMS key")
                .build();

        lenient().when(gson.toJson(any())).thenReturn("{}");
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenThrow(kmsException);

        assertThatThrownBy(() -> sqsMessageRepository.sendToDLQ(message, error))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha de permissao KMS ao enviar mensagem para DLQ");
    }

    @Test
    void sendToDLQ_ShouldThrowException_WhenSqsExceptionOccurs() {
        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .timestamp(LocalDateTime.now())
                .retryCount(3)
                .build();

        Exception error = new RuntimeException("Processing failed");

        lenient().when(gson.toJson(any())).thenReturn("{}");
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(SqsException.builder().message("SQS Error").build());

        assertThatThrownBy(() -> sqsMessageRepository.sendToDLQ(message, error))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Falha ao enviar mensagem para DLQ");
    }

    @Test
    void sendToDLQ_ShouldThrowException_WhenGenericExceptionOccurs() {
        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .timestamp(LocalDateTime.now())
                .retryCount(3)
                .build();

        Exception error = new RuntimeException("Processing failed");

        lenient().when(gson.toJson(any())).thenReturn("{}");
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThatThrownBy(() -> sqsMessageRepository.sendToDLQ(message, error))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erro inesperado ao enviar mensagem para DLQ");
    }

    @Test
    void sendMessage_ShouldUseOmpTransactionIdAsMessageGroupId() {
        String testData = "test-data";
        String testDataJson = "{\"ompTransactionId\":\"tx-12345\"}";
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data(testData)
                .build();

        lenient().when(gson.toJson(testData)).thenReturn(testDataJson);

        sqsMessageRepository.sendMessage(callbackRequest);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageGroupId()).isEqualTo("callback-tx-12345");
    }

    @Test
    void sendMessage_ShouldUseIdentifierAsMessageGroupId_WhenOmpTransactionIdNotPresent() {
        String testData = "test-data";
        String testDataJson = "{\"identifier\":\"id-67890\"}";
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data(testData)
                .build();

        lenient().when(gson.toJson(testData)).thenReturn(testDataJson);

        sqsMessageRepository.sendMessage(callbackRequest);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageGroupId()).isEqualTo("callback-id-67890");
    }

    @Test
    void sendMessage_ShouldUseTxIdAsMessageGroupId_WhenOthersNotPresent() {
        String testData = "test-data";
        String testDataJson = "{\"txId\":\"txid-999\"}";
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data(testData)
                .build();

        lenient().when(gson.toJson(testData)).thenReturn(testDataJson);

        sqsMessageRepository.sendMessage(callbackRequest);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageGroupId()).isEqualTo("callback-txid-999");
    }

    @Test
    void sendMessage_ShouldUseDefaultMessageGroupId_WhenNoIdentifiersPresent() {
        String testData = "{\"otherField\":\"value\"}";
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data(testData)
                .build();

        lenient().when(gson.toJson(any())).thenReturn(testData);

        sqsMessageRepository.sendMessage(callbackRequest);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageGroupId()).isEqualTo("payment-callback-default");
    }

    @Test
    void sendMessage_ShouldUseDefaultMessageGroupId_WhenJsonParsingFails() {
        String invalidJson = "invalid json";
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data(invalidJson)
                .build();

        lenient().when(gson.toJson(any())).thenReturn(invalidJson);

        sqsMessageRepository.sendMessage(callbackRequest);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageGroupId()).isEqualTo("payment-callback-default");
    }

    @Test
    void resendForRetry_ShouldIncrementRetryCountAndSendMessage() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .callbackRequest(callbackRequest)
                .timestamp(LocalDateTime.now())
                .retryCount(1)
                .build();

        lenient().when(gson.toJson(any())).thenReturn("{}");

        sqsMessageRepository.resendForRetry(message);

        assertThat(message.getRetryCount()).isEqualTo(2);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.queueUrl()).isEqualTo(QUEUE_URL);
        assertThat(request.messageAttributes()).containsKey("retryCount");
        assertThat(request.messageAttributes().get("retryCount").stringValue()).isEqualTo("2");
    }

    @Test
    void resendForRetry_ShouldHandleNullRetryCount() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .callbackRequest(callbackRequest)
                .timestamp(LocalDateTime.now())
                .retryCount(null)
                .build();

        lenient().when(gson.toJson(any())).thenReturn("{}");

        sqsMessageRepository.resendForRetry(message);

        assertThat(message.getRetryCount()).isEqualTo(1);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageAttributes().get("retryCount").stringValue()).isEqualTo("1");
    }

    @Test
    void resendForRetry_ShouldHandleNullTimestamp() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .callbackRequest(callbackRequest)
                .timestamp(null)
                .retryCount(2)
                .build();

        lenient().when(gson.toJson(any())).thenReturn("{}");

        sqsMessageRepository.resendForRetry(message);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageAttributes().get("timestamp").stringValue()).isNotNull();
    }

    @Test
    void resendForRetry_ShouldThrowException_WhenSqsExceptionOccurs() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .callbackRequest(callbackRequest)
                .timestamp(LocalDateTime.now())
                .retryCount(1)
                .build();

        lenient().when(gson.toJson(any())).thenReturn("{}");
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(SqsException.builder().message("SQS Error").build());

        assertThatThrownBy(() -> sqsMessageRepository.resendForRetry(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Falha ao reenviar mensagem para retry");
    }

    @Test
    void resendForRetry_ShouldUseDefaultMessageGroupId_WhenNoCallbackRequest() {
        MessageSQS<String> message = MessageSQS.<String>builder()
                .messageId("msg-123")
                .callbackRequest(null)
                .timestamp(LocalDateTime.now())
                .retryCount(1)
                .build();

        lenient().when(gson.toJson(any())).thenReturn("{}");

        sqsMessageRepository.resendForRetry(message);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageGroupId()).isEqualTo("payment-callback-default");
    }

    @Test
    void sendMessage_ShouldThrowException_WhenUnexpectedExceptionOccurs() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThatThrownBy(() -> sqsMessageRepository.sendMessage(callbackRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erro inesperado ao enviar mensagem");
    }

    @Test
    void sendMessage_ShouldUseDefaultMessageGroupId_WhenNullCallbackRequest() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data(null)
                .build();

        lenient().when(gson.toJson(any())).thenReturn("null");

        sqsMessageRepository.sendMessage(callbackRequest);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.messageGroupId()).isEqualTo("payment-callback-default");
    }
}