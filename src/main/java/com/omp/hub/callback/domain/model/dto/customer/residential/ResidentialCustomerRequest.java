package com.omp.hub.callback.domain.model.dto.customer.residential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResidentialCustomerRequest {

    private String customerCode;
    private String customerDocument;
    private String phoneNumber;
    private String interactionId;
}
