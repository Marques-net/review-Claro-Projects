package com.omp.hub.callback.domain.model.dto.callback.tefweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class PaymentTypeDTO {

    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'paymentType' é obrigatório")
    private String paymentType;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'detailPaymentType' é obrigatório")
    private String detailPaymentType;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'specificPaymentType' é obrigatório")
    private String specificPaymentType;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'idModalityPayment' é obrigatório")
    private String idModalityPayment;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'numberInstallmentsPayment' é obrigatório")
    private String numberInstallmentsPayment;

}
