package com.omp.hub.callback.domain.model.dto.transactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<RetryProcessorDTO> retryProcessor;
    private AntiFraudDTO antifraud;
}
