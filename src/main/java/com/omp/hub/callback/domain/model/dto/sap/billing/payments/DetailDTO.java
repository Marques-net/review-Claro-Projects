package com.omp.hub.callback.domain.model.dto.sap.billing.payments;

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
public class DetailDTO {
    private String sequenceId;
    private String value;
    private String paymentMethod;
    private CardDTO card;
}
