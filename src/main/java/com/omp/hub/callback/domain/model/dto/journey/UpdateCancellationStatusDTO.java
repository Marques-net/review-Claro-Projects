package com.omp.hub.callback.domain.model.dto.journey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCancellationStatusDTO {
    private String status;
    private String paymentType;
    private String transactionOrderId;
}
