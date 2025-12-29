package com.omp.hub.callback.domain.model.dto.sap.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDataDTO {
    private OrderDTO order;
}
