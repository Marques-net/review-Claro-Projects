package com.omp.hub.callback.domain.model.dto.callback.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TargetPaymentMethodDTO {
    private String paymentMethod;
    private String recurrenceId;
    private String status;
    private ActivationDTO activation;
    private List<UpdatesDTO> updates;
}
