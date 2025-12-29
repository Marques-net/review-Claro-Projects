package com.omp.hub.callback.domain.model.dto.journey.single;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSingleDTO {
    private String value;
    private String transactionOrderId;
    private String orderDate;
    private String sellerId;
    private String salesOrderId;
    private String invoice;
    private List<PaymentDiscountDTO> discounts;
    private CardDataDTO cardData;
    private PixDataDTO pixData;
    private CashDataDTO cashData;
}
