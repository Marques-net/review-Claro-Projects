package com.omp.hub.callback.domain.model.dto.journey.recurring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyDataDTO {
    private String journeyType;
    private String txId;
    private String pixKeyId;
    private String value;
    private String expiration;
}
