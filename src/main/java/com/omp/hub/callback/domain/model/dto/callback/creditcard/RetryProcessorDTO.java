package com.omp.hub.callback.domain.model.dto.callback.creditcard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class RetryProcessorDTO {

    private String nsu;
    private String authorizationCode;
    private String acquiratorCode;
    private String transactionId;
    private String responseCode;
    private String responseDescription;
    private String merchantAdviceCode;

}
