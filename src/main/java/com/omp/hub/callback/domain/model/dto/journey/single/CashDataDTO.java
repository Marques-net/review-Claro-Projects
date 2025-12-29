package com.omp.hub.callback.domain.model.dto.journey.single;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashDataDTO {

    private String orderId;
    private String orderDate;
    private String fiscalCouponCode;
}
