package com.omp.hub.callback.domain.model.dto.sap.redemptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.omp.hub.callback.application.utils.apigee.ErrorDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SapRedemptionsResponse {

    private String apiVersion;
    private String transactionId;
    private DataResponseDTO data;
    private ErrorDTO error;

}
