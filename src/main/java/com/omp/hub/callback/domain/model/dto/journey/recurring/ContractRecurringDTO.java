package com.omp.hub.callback.domain.model.dto.journey.recurring;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractRecurringDTO {
    private String mobileBan;
    private String contractNumber;
    private String operatorCode;
    private String cityCode;
    private String id;
}