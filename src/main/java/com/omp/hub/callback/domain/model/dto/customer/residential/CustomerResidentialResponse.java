package com.omp.hub.callback.domain.model.dto.customer.residential;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileErrorDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerResidentialResponse {

    private String apiVersion;
    private String transactionId;
    private CustomerResidentialDataDTO data;
    private CustomerMobileErrorDTO error;
}
