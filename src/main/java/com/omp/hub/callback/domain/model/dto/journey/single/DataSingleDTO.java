package com.omp.hub.callback.domain.model.dto.journey.single;

import com.omp.hub.callback.domain.model.dto.journey.DataDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataSingleDTO extends DataDTO {
    private PaymentSingleDTO payment;
    private CustomerSingleDTO customer;
    private FraudAnalysisDataDTO fraudAnalysisData;
}
