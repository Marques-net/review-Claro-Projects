package com.omp.hub.callback.domain.model.dto.callback.tefweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class TransactionDataDTO {

    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'transactionDate' é obrigatório")
    private String transactionDate;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'hour' é obrigatório")
    private String hour;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'transactionStatus' é obrigatório")
    private String transactionStatus;
    
    @JsonProperty(required = true)
    @NotNull(message = "O campo 'paymentType' é obrigatório")
    @Valid
    private PaymentTypeDTO paymentType;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'value' é obrigatório")
    private String value;
}
