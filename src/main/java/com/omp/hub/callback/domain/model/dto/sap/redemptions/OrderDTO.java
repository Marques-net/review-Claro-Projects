package com.omp.hub.callback.domain.model.dto.sap.redemptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private String id;
    private String companyId;
    private String businessLocationId;
    private String type;
    private PosInfoDTO posInfo;

}
