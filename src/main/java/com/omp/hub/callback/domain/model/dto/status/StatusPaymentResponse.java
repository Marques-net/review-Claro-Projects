package com.omp.hub.callback.domain.model.dto.status;

import com.omp.hub.callback.domain.enums.PaymentStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusPaymentResponse {
    private PaymentStatusEnum status;
}
