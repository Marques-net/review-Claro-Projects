package com.omp.hub.callback.application.consumer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.service.CallbackErrorNotificationService;
import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;
import com.omp.hub.callback.domain.service.impl.callback.CallbackService;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.MessageSQS;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.SqsMessageRepository;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@ExtendWith(MockitoExtension.class)
class SqsCallbackListenerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private SqsMessageRepository sqsMessageRepository;

    @Mock
    private CallbackService callbackService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CallbackErrorNotificationService callbackErrorNotificationService;

    private SqsCallbackListener sqsCallbackListener;

    private MessageSQS<Object> messageSQS;
    private CallbackRequest<Object> callbackRequest;
    private String messageBody;

    @BeforeEach
    void setUp() throws Exception {
        sqsCallbackListener = new SqsCallbackListener(
                sqsClient, callbackService, objectMapper, callbackErrorNotificationService, sqsMessageRepository);
        ReflectionTestUtils.setField(sqsCallbackListener, "maxRetries", 3);
        ReflectionTestUtils.setField(sqsCallbackListener, "queueUrl", "https://sqs.test.amazonaws.com/queue");

        callbackRequest = new CallbackRequest<>();
        callbackRequest.setData(Collections.singletonMap("txId", "12345"));

        messageSQS = MessageSQS.<Object>builder()
                .messageId("msg-123")
                .callbackRequest(callbackRequest)
                .timestamp(LocalDateTime.now())
                .retryCount(0)
                .build();

        messageBody = "{\"messageId\":\"msg-123\",\"callbackRequest\":{\"data\":{\"txId\":\"12345\"}},\"retryCount\":0}";
    }

    private Message createSqsMessage(String body) {
        return Message.builder()
                .messageId("sqs-msg-id")
                .body(body)
                .receiptHandle("receipt-handle-123")
                .build();
    }

    @Test
    void startPolling_shouldProcessMessagesSuccessfully() throws Exception {
        Message sqsMessage = createSqsMessage(messageBody);
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(List.of(sqsMessage))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
                .thenAnswer(inv -> {
                    sqsCallbackListener.setRunning(false);
                    return ReceiveMessageResponse.builder().messages(Collections.emptyList()).build();
                });
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());
        when(objectMapper.readValue(anyString(), eq(MessageSQS.class))).thenReturn(messageSQS);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"txId\":\"12345\"}");
        doNothing().when(callbackService).processCallback(anyString());

        sqsCallbackListener.startPolling();

        verify(callbackService).processCallback(anyString());
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
        verify(sqsMessageRepository, never()).sendToDLQ(any(), any());
    }

    @Test
    void startPolling_shouldResendForRetryOnFirstAttempt() throws Exception {
        messageSQS.setRetryCount(1);
        Message sqsMessage = createSqsMessage(messageBody);
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(List.of(sqsMessage))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
                .thenAnswer(inv -> {
                    sqsCallbackListener.setRunning(false);
                    return ReceiveMessageResponse.builder().messages(Collections.emptyList()).build();
                });
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());
        when(objectMapper.readValue(anyString(), eq(MessageSQS.class))).thenReturn(messageSQS);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"txId\":\"12345\"}");
        doThrow(new RuntimeException("Processing error")).when(callbackService).processCallback(anyString());

        sqsCallbackListener.startPolling();

        verify(callbackService).processCallback(anyString());
        verify(sqsMessageRepository).resendForRetry(eq(messageSQS));
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
        verify(sqsMessageRepository, never()).sendToDLQ(any(), any());
    }

    @Test
    void startPolling_shouldSendToDLQAfterMaxRetries() throws Exception {
        messageSQS.setRetryCount(3);
        Message sqsMessage = createSqsMessage(messageBody);
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(List.of(sqsMessage))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
                .thenAnswer(inv -> {
                    sqsCallbackListener.setRunning(false);
                    return ReceiveMessageResponse.builder().messages(Collections.emptyList()).build();
                });
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());
        when(objectMapper.readValue(anyString(), eq(MessageSQS.class))).thenReturn(messageSQS);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"txId\":\"12345\"}");
        RuntimeException exception = new RuntimeException("Processing error");
        doThrow(exception).when(callbackService).processCallback(anyString());

        sqsCallbackListener.startPolling();

        verify(callbackService).processCallback(anyString());
        verify(callbackErrorNotificationService).notifyJourneyAboutCallbackFailure(eq("12345"), eq(3), eq(exception));
        verify(sqsMessageRepository).sendToDLQ(eq(messageSQS), eq(exception));
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void startPolling_shouldHandleParsingError() throws Exception {
        Message sqsMessage = createSqsMessage(messageBody);
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(List.of(sqsMessage))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
                .thenAnswer(inv -> {
                    sqsCallbackListener.setRunning(false);
                    return ReceiveMessageResponse.builder().messages(Collections.emptyList()).build();
                });
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());
        when(objectMapper.readValue(anyString(), eq(MessageSQS.class)))
                .thenThrow(new RuntimeException("JSON parsing error"));

        sqsCallbackListener.startPolling();

        verify(callbackService, never()).processCallback(anyString());
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void startPolling_shouldExtractTxIdFromIdentifier() throws Exception {
        callbackRequest.setData(Collections.singletonMap("identifier", "identifier-123"));
        messageSQS.setCallbackRequest(callbackRequest);
        messageSQS.setRetryCount(3);
        Message sqsMessage = createSqsMessage(messageBody);
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(List.of(sqsMessage))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
                .thenAnswer(inv -> {
                    sqsCallbackListener.setRunning(false);
                    return ReceiveMessageResponse.builder().messages(Collections.emptyList()).build();
                });
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());
        when(objectMapper.readValue(anyString(), eq(MessageSQS.class))).thenReturn(messageSQS);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"identifier\":\"identifier-123\"}");
        RuntimeException exception = new RuntimeException("Processing error");
        doThrow(exception).when(callbackService).processCallback(anyString());

        sqsCallbackListener.startPolling();

        verify(callbackErrorNotificationService).notifyJourneyAboutCallbackFailure(eq("identifier-123"), eq(3), eq(exception));
    }

    @Test
    void startPolling_shouldExtractTxIdFromOmpTransactionId() throws Exception {
        callbackRequest.setData(Collections.singletonMap("ompTransactionId", "omp-tx-456"));
        messageSQS.setCallbackRequest(callbackRequest);
        messageSQS.setRetryCount(3);
        Message sqsMessage = createSqsMessage(messageBody);
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(List.of(sqsMessage))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
                .thenAnswer(inv -> {
                    sqsCallbackListener.setRunning(false);
                    return ReceiveMessageResponse.builder().messages(Collections.emptyList()).build();
                });
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());
        when(objectMapper.readValue(anyString(), eq(MessageSQS.class))).thenReturn(messageSQS);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ompTransactionId\":\"omp-tx-456\"}");
        RuntimeException exception = new RuntimeException("Processing error");
        doThrow(exception).when(callbackService).processCallback(anyString());

        sqsCallbackListener.startPolling();

        verify(callbackErrorNotificationService).notifyJourneyAboutCallbackFailure(eq("omp-tx-456"), eq(3), eq(exception));
    }

    @Test
    void startPolling_shouldHandleNullRetryCountAsFirstAttempt() throws Exception {
        messageSQS.setRetryCount(null);
        Message sqsMessage = createSqsMessage(messageBody);
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(List.of(sqsMessage))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
                .thenAnswer(inv -> {
                    sqsCallbackListener.setRunning(false);
                    return ReceiveMessageResponse.builder().messages(Collections.emptyList()).build();
                });
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());
        when(objectMapper.readValue(anyString(), eq(MessageSQS.class))).thenReturn(messageSQS);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"txId\":\"12345\"}");
        doThrow(new RuntimeException("Processing error")).when(callbackService).processCallback(anyString());

        sqsCallbackListener.startPolling();

        verify(sqsMessageRepository).resendForRetry(eq(messageSQS));
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
        verify(sqsMessageRepository, never()).sendToDLQ(any(), any());
    }

    @Test
    void startPolling_shouldHandleNullCallbackRequest() throws Exception {
        messageSQS.setCallbackRequest(null);
        messageSQS.setRetryCount(3);
        Message sqsMessage = createSqsMessage(messageBody);
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(List.of(sqsMessage))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
                .thenAnswer(inv -> {
                    sqsCallbackListener.setRunning(false);
                    return ReceiveMessageResponse.builder().messages(Collections.emptyList()).build();
                });
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());
        when(objectMapper.readValue(anyString(), eq(MessageSQS.class))).thenReturn(messageSQS);

        sqsCallbackListener.startPolling();

        verify(callbackService, never()).processCallback(anyString());
    }

    @Test
    void startPolling_shouldHandleExceptionInErrorNotification() throws Exception {
        messageSQS.setRetryCount(3);
        Message sqsMessage = createSqsMessage(messageBody);
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(List.of(sqsMessage))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
                .thenAnswer(inv -> {
                    sqsCallbackListener.setRunning(false);
                    return ReceiveMessageResponse.builder().messages(Collections.emptyList()).build();
                });
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());
        when(objectMapper.readValue(anyString(), eq(MessageSQS.class))).thenReturn(messageSQS);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"txId\":\"12345\"}");
        RuntimeException exception = new RuntimeException("Processing error");
        doThrow(exception).when(callbackService).processCallback(anyString());
        doThrow(new RuntimeException("Notification error")).when(callbackErrorNotificationService)
                .notifyJourneyAboutCallbackFailure(anyString(), eq(3), any());

        sqsCallbackListener.startPolling();

        verify(callbackErrorNotificationService).notifyJourneyAboutCallbackFailure(eq("12345"), eq(3), eq(exception));
    }

    @Test
    void stopPolling_shouldStopPollingLoop() {
        sqsCallbackListener.setRunning(true);
        
        sqsCallbackListener.stopPolling();
        
        assert !sqsCallbackListener.isRunning();
    }
}
