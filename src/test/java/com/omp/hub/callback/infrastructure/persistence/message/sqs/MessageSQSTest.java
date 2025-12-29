package com.omp.hub.callback.infrastructure.persistence.message.sqs;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;

class MessageSQSTest {

    @Test
    void messageSQS_ShouldBeCreatedWithBuilder() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        LocalDateTime now = LocalDateTime.now();

        MessageSQS<String> messageSQS = MessageSQS.<String>builder()
                .messageId("test-message-id")
                .callbackRequest(callbackRequest)
                .timestamp(now)
                .retryCount(0)
                .receiptHandle("test-receipt-handle")
                .build();

        assertThat(messageSQS).isNotNull();
        assertThat(messageSQS.getMessageId()).isEqualTo("test-message-id");
        assertThat(messageSQS.getCallbackRequest()).isEqualTo(callbackRequest);
        assertThat(messageSQS.getTimestamp()).isEqualTo(now);
        assertThat(messageSQS.getRetryCount()).isEqualTo(0);
        assertThat(messageSQS.getReceiptHandle()).isEqualTo("test-receipt-handle");
    }

    @Test
    void messageSQS_ShouldHaveGettersAndSetters() {
        MessageSQS<String> messageSQS = new MessageSQS<>();
        CallbackRequest<String> callbackRequest = new CallbackRequest<>();
        callbackRequest.setData("test-data");
        LocalDateTime now = LocalDateTime.now();

        messageSQS.setMessageId("test-id");
        messageSQS.setCallbackRequest(callbackRequest);
        messageSQS.setTimestamp(now);
        messageSQS.setRetryCount(1);
        messageSQS.setReceiptHandle("receipt-handle");

        assertThat(messageSQS.getMessageId()).isEqualTo("test-id");
        assertThat(messageSQS.getCallbackRequest()).isEqualTo(callbackRequest);
        assertThat(messageSQS.getTimestamp()).isEqualTo(now);
        assertThat(messageSQS.getRetryCount()).isEqualTo(1);
        assertThat(messageSQS.getReceiptHandle()).isEqualTo("receipt-handle");
    }

    @Test
    void messageSQS_ShouldHaveAllArgsConstructor() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime failureTime = LocalDateTime.now().plusMinutes(5);

        MessageSQS<String> messageSQS = new MessageSQS<>(
                "test-message-id",
                callbackRequest,
                now,
                2,
                "test-receipt-handle",
                "Test error message",
                "Stack trace here",
                failureTime
        );

        assertThat(messageSQS.getMessageId()).isEqualTo("test-message-id");
        assertThat(messageSQS.getCallbackRequest()).isEqualTo(callbackRequest);
        assertThat(messageSQS.getTimestamp()).isEqualTo(now);
        assertThat(messageSQS.getRetryCount()).isEqualTo(2);
        assertThat(messageSQS.getReceiptHandle()).isEqualTo("test-receipt-handle");
        assertThat(messageSQS.getErrorMessage()).isEqualTo("Test error message");
        assertThat(messageSQS.getErrorStackTrace()).isEqualTo("Stack trace here");
        assertThat(messageSQS.getFailureTimestamp()).isEqualTo(failureTime);
    }

    @Test
    void messageSQS_ShouldHaveNoArgsConstructor() {
        MessageSQS<String> messageSQS = new MessageSQS<>();

        assertThat(messageSQS.getMessageId()).isNull();
        assertThat(messageSQS.getCallbackRequest()).isNull();
        assertThat(messageSQS.getTimestamp()).isNull();
        assertThat(messageSQS.getRetryCount()).isNull();
        assertThat(messageSQS.getReceiptHandle()).isNull();
        assertThat(messageSQS.getErrorMessage()).isNull();
        assertThat(messageSQS.getErrorStackTrace()).isNull();
        assertThat(messageSQS.getFailureTimestamp()).isNull();
    }

    @Test
    void messageSQS_ShouldSetErrorFields() {
        MessageSQS<String> messageSQS = new MessageSQS<>();
        LocalDateTime failureTime = LocalDateTime.now();

        messageSQS.setErrorMessage("Processing failed");
        messageSQS.setErrorStackTrace("java.lang.RuntimeException: Processing failed");
        messageSQS.setFailureTimestamp(failureTime);

        assertThat(messageSQS.getErrorMessage()).isEqualTo("Processing failed");
        assertThat(messageSQS.getErrorStackTrace()).isEqualTo("java.lang.RuntimeException: Processing failed");
        assertThat(messageSQS.getFailureTimestamp()).isEqualTo(failureTime);
    }

    @Test
    void messageSQS_ShouldSupportEqualsAndHashCode() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        LocalDateTime now = LocalDateTime.now();

        MessageSQS<String> messageSQS1 = MessageSQS.<String>builder()
                .messageId("test-message-id")
                .callbackRequest(callbackRequest)
                .timestamp(now)
                .retryCount(0)
                .receiptHandle("test-receipt-handle")
                .build();

        MessageSQS<String> messageSQS2 = MessageSQS.<String>builder()
                .messageId("test-message-id")
                .callbackRequest(callbackRequest)
                .timestamp(now)
                .retryCount(0)
                .receiptHandle("test-receipt-handle")
                .build();

        assertThat(messageSQS1).isEqualTo(messageSQS2);
        assertThat(messageSQS1.hashCode()).isEqualTo(messageSQS2.hashCode());
    }

    @Test
    void messageSQS_ShouldSupportToString() {
        CallbackRequest<String> callbackRequest = CallbackRequest.<String>builder()
                .data("test-data")
                .build();

        LocalDateTime now = LocalDateTime.now();

        MessageSQS<String> messageSQS = MessageSQS.<String>builder()
                .messageId("test-message-id")
                .callbackRequest(callbackRequest)
                .timestamp(now)
                .retryCount(0)
                .receiptHandle("test-receipt-handle")
                .build();

        String toString = messageSQS.toString();

        assertThat(toString).contains("MessageSQS");
        assertThat(toString).contains("test-message-id");
        assertThat(toString).contains("test-receipt-handle");
    }
}
