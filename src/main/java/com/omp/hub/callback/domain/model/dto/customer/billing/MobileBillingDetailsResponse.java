package com.omp.hub.callback.domain.model.dto.customer.billing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileBillingDetailsResponse {

    @JsonProperty("apiVersion")
    private String apiVersion;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("data")
    private MobileBillingDetailsData data;
}