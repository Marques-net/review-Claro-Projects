package com.omp.hub.callback.domain.model.dto.sap.payments;

import com.omp.hub.callback.application.utils.apigee.ErrorDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SapPaymentsResponse {

    private String apiVersion;
    private String transactionId;
    private ErrorDTO error;

}
