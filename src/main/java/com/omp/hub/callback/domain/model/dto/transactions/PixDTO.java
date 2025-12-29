package com.omp.hub.callback.domain.model.dto.transactions;

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
public class PixDTO {

    private String paymentDate;
    private String value;
    private String endToEndId;
    private String txId;
}
