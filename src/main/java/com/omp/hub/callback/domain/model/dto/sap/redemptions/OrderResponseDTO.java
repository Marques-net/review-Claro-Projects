package com.omp.hub.callback.domain.model.dto.sap.redemptions;

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
public class OrderResponseDTO {

    private String salesCategory;
    private String salesType;
    private String salesDate;
    private String salesTime;
    private String salesTeam;
    private String issuerCode;
    private PosInfoDTO posInfo;
    private CustomerDTO customer;
    private String totalAmountReceived;
    private String discountUnitAmount;
//    private String paymentMethod;
    private String esnCheck;
    private String esnPrintLocation;
}
