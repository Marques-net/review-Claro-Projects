package com.omp.hub.callback.domain.model.dto.sap.billing.payments;

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
public class PaymentDTO {
    private String company;
    private String businessLocation;
    private String identification;
    private PosInfoDTO posInfo;
    private String customerName;
    private String date;
    private String value;
    private String username;
    private List<DetailDTO> details;
}
