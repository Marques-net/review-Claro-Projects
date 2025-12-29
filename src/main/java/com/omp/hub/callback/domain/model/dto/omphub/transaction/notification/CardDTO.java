package com.omp.hub.callback.domain.model.dto.omphub.transaction.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardDTO {

    private String statusCode;
    private String statusMessage;
    private String transactionId;
    private String flag;
    private String card;
    private String value;
    private String numberInstallments;
    private String orderId;
    private String orderDate;
    private AcquiratorDTO acquirator;
    private RetryProcessorDTO retryProcessor;
    private AntifraudDTO antifraud;
}
