package com.omp.hub.callback.domain.model.dto.journey.single;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private String name;
    private String value;
    private String amount;
    private String discountValue;
    private String totalDiscountValue;
    private String code;
    private String sku;
    private String serialNumber;

}
