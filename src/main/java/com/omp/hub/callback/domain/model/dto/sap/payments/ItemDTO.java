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
public class ItemDTO {

    private String id;
    private String materialId;
    private String quantity;
    private String unitAmount;
    private String totalAmount;
    private String discountAmount;
    private String totalDiscountAmount;
    private String serialNumber;
}
