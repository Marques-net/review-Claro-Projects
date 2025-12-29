package com.omp.hub.callback.domain.model.dto.journey.single;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstallmentDTO {

    private String installment;
    private String installmentValue;
    private String totalValueOfInstallments;
    private Boolean interest;
    private String rateInterest;
}
