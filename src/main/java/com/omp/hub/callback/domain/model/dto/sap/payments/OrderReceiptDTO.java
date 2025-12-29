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
public class OrderReceiptDTO {

    private String sequence;
    private String id;
    private String paymentAmount;
    private String movimentType;
    private String paymentType;
    private String installments;
    private CardDetailsDTO cardDetails;
    private OthersDetailsDTO othersDetails;
}
