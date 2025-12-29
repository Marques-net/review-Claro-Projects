package com.omp.hub.callback.domain.model.dto.pix.forms;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OmpPaymentMethodsCancellationForms {
    private String paymentMethod;
    private String recurrenceId;
    private String reasonDescription;
    private Contract contract;
}
