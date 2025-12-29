package com.omp.hub.callback.domain.model.dto.journey.single;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardDataDTO {
    private Integer paymentType;
    private Integer originCard;
    private String orderId;
    private String orderDate;
    private String sellerId;
    private Boolean registerCard;
    private String numberInstallments;
    private List<InstallmentPaymentDTO> installmentPayment;

}

