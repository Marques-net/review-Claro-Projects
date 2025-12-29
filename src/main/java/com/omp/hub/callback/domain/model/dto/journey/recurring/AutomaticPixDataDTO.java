package com.omp.hub.callback.domain.model.dto.journey.recurring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomaticPixDataDTO {
    private String object;
    private CalendarDTO calendar;
    private String retryPolicy;
    private ActivationDTO activation;
}
