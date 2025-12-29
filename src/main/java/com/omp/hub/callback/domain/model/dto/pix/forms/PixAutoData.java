package com.omp.hub.callback.domain.model.dto.pix.forms;

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
public class PixAutoData {
    private String paymentMethod;
    private String ompTransactionId;
    private String txId;
    private Contract contract;
    private Recipient recipient;
    private Calendar calendar;
    private ValueDTO value;
    private String retryPolicy;
    private ActivationPixAuto activationPixAuto;
}
