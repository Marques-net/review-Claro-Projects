package com.omp.hub.callback.domain.model.dto.journey.single;

import com.omp.hub.callback.domain.model.dto.InvoiceDataDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudAnalysisDataDTO {

    private InvoiceDataDTO invoiceData;
    private ComplementaryDataDTO complementaryData;
}
