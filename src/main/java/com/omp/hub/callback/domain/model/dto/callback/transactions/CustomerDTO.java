package com.omp.hub.callback.domain.model.dto.callback.transactions;

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
public class CustomerDTO {
    private String msisdn;
    private String mobileBan;
    private String contractNumber;
    private String contractCity;
    private String operatorCode;
}
