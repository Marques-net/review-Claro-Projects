package com.omp.hub.callback.domain.model.dto.sap.billing.payments;

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
public class CardDTO {

    private String cardNumber;
    private String approvementId;
    private String authorizationId;
    private String financialTransactionCentralId;
    private String issuerId;
    private String issuerDescription;
    private String valueAddedNetworkId;
    private String valueAddedNetworkDescription;
}
