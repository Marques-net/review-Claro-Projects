package com.omp.hub.callback.domain.model.dto.claro;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerContractsSubscribersResponse {
    
    @JsonProperty("apiVersion")
    private String apiVersion;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("data")
    private CustomerContractsSubscribersData data;
}