package com.omp.hub.callback.domain.model.dto.journey.single;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawDTO {

    private String value;
    private Integer alterationModality;
    private String agentModality;
    private String withdrawalServiceProvider;
}
