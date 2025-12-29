package com.omp.hub.callback.infrastructure.persistence.message.sqs;

import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageSQS<T> {

    private String messageId;
    private CallbackRequest<T> callbackRequest;
    private LocalDateTime timestamp;
    private Integer retryCount;
    private String receiptHandle;
    private String errorMessage;
    private String errorStackTrace;
    private LocalDateTime failureTimestamp;
}
