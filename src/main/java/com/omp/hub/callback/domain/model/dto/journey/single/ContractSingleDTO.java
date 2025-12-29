package com.omp.hub.callback.domain.model.dto.journey.single;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractSingleDTO {
    private String mobileBan;
    private String contractNumber;
    private String operatorCode;
    private String cityCode;
    private String id;
}