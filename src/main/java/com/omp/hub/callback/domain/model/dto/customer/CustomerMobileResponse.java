package com.omp.hub.callback.domain.model.dto.customer;

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
public class CustomerMobileResponse {

    private String apiVersion;
    private String transactionId;
    private MobileSubscriptionDataDTO data;
    private CustomerMobileErrorDTO error;
}
