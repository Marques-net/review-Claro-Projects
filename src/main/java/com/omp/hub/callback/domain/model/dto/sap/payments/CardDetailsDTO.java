package com.omp.hub.callback.domain.model.dto.sap.payments;

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
public class CardDetailsDTO {

    private String cardNumber;
    private String transactionApprovalCode;
    private String authorizationId;
    private String taxTransactionReceiptId;
    private String totalAmount;
    private String issuerCode;
    private String issuerDescription;
    private String valueAddedNetworkId;
    private String valueAddedNetworkDescription;
}
