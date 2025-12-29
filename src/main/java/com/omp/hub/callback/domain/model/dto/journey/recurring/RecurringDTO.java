package com.omp.hub.callback.domain.model.dto.journey.recurring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringDTO {
    private BigDecimal value;
    private BigDecimal receiverMinimumValue;
}
