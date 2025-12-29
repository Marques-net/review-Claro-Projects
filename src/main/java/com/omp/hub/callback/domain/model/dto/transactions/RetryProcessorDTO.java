package com.omp.hub.callback.domain.model.dto.transactions;

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
public class RetryProcessorDTO {

    private String nsu;
    private String authorizationCode;
    private String acquiratorCode;
    private String transactionId;
    private String responseCode;
    private String responseDescription;
    private String merchantAdviceCode;
}
