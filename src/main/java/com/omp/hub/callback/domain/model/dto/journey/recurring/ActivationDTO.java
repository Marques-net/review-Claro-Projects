package com.omp.hub.callback.domain.model.dto.journey.recurring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivationDTO {
    private JourneyDataDTO journeyData;
}
