package com.omp.hub.callback.domain.model.dto.journey.single;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValueDTO {

    private String original;
    private Integer alterationModality;
    private WithdrawalDTO withdrawal;
    private MulctDTO mulct;
    private InterestDTO interest;
    private AbatementDTO abatement;
    private DiscountDTO discount;
}
