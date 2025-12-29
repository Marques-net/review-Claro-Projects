package com.omp.hub.callback.domain.model.dto.journey.single;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.omp.hub.callback.domain.model.dto.journey.recurring.CalendarDTO;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PixDataDTO {

    private String pixKeyId;
    private String txId;
    private CalendarDTO calendar;
    private ValueDTO value;
    private LocDTO loc;
    private String payerSolicitation;
    private List<AdditionalInfoDTO> additionalInfo;
}
