package com.omp.hub.callback.domain.model.dto.callback.tefweb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.omp.hub.callback.domain.model.dto.callback.CallbackDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TefWebCallbackRequest implements CallbackDTO {

    private String ompTransactionId;

    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'service' é obrigatório")
    private String service;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'paymentType' é obrigatório")
    private String paymentType;
    
    @JsonProperty(required = true)
    @NotEmpty(message = "O campo 'sales' é obrigatório e deve conter ao menos um item")
    @Valid
    private List<SalesDTO> sales;

    private Boolean multiplePayment;

    private List<String> mixedPaymentTypes;
}
