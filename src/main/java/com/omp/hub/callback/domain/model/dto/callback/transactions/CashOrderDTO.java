package com.omp.hub.callback.domain.model.dto.callback.transactions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashOrderDTO {

    private String totalValue;
    private String customerDocument;
    private String indexer;
    private String issueDate;
    private String customerName;
}
