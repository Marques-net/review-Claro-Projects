package com.omp.hub.callback.domain.model.dto.journey.recurring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDTO {
    private String initialDate;
    private String finalDate;
    private String requestExpirationDate;
    private String frequencyType;
}
