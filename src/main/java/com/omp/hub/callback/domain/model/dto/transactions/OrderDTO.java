package com.omp.hub.callback.domain.model.dto.transactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private String customerName;
    private String customerDocument;
    private String indexer;
    private String totalValue;
    private String receiptNumber;
    private String valueToPay;
    private String issueDate;
    private String storeCode;
    private String customerCode;
    private String salesPointClient;
    private List<OrdersTransDTO> orders;
}
