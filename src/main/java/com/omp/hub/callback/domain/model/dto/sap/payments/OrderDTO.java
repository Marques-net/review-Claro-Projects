package com.omp.hub.callback.domain.model.dto.sap.payments;

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
    private String id;
    private String companyId;
    private String businessLocationId;
    private PosInfoDTO posInfo;
    private String salesCategory;
    private String salesType;
    private String salesDate;
    private String salesTime;
    private String totalAmountReceived;
    private String totalChangeAmount;
    private CustomerDTO customer;
    private Boolean isIncomplete;
    private String userName;
    private String sefazFlag;
    private String sefazCustomerIdentificationId;
    private String taxCouponCounter;
    private List<ItemDTO> items;
    private List<OrderReceiptDTO> orderReceipts;
}
