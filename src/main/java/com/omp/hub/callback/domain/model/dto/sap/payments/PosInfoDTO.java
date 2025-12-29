package com.omp.hub.callback.domain.model.dto.sap.payments;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PosInfoDTO {

    private String componentNumber;
    private String version;
    private String transactionId;
    private String taxCouponNumber;
}
