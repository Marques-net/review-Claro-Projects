package com.omp.hub.callback.domain.model.dto.pix.forms;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValueDTO {

    private BigDecimal recurrenceValue;
    private BigDecimal receiverMinimumValue;
    private String original;
    private String alterationModality;
    private WithdrawalDTO withdrawal;
    private MulctDTO mulct;
    private InterestDTO interest;
    private AbatementDTO abatement;
    private DiscountDTO discount;

}
