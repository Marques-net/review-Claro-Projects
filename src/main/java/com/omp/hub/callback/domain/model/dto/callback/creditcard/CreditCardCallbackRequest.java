package com.omp.hub.callback.domain.model.dto.callback.creditcard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.omp.hub.callback.domain.model.dto.callback.CallbackDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardCallbackRequest implements CallbackDTO {

    private String ompTransactionId;

    @JsonProperty(required = true)
    @NotNull(message = "O campo 'sucess' é obrigatório")
    private Boolean sucess;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'service' é obrigatório")
    private String service;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'statusCode' é obrigatório")
    private String statusCode;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'statusMessage' é obrigatório")
    private String statusMessage;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'transactionId' é obrigatório")
    private String transactionId;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'flag' é obrigatório")
    private String flag;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'card' é obrigatório")
    private String card;
    
    @JsonProperty(required = true)
    @NotNull(message = "O campo 'value' é obrigatório")
    private BigDecimal value;
    
    @JsonProperty(required = true)
    @NotNull(message = "O campo 'numberInstallments' é obrigatório")
    private Integer numberInstallments;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'orderId' é obrigatório")
    private String orderId;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'orderDate' é obrigatório")
    private String orderDate;
    
    @JsonProperty(required = true)
    @NotNull(message = "O campo 'acquirator' é obrigatório")
    @Valid
    private AcquiratorDTO acquirator;
    
    private List<RetryProcessorDTO> retryProcessor;
    
    @JsonProperty(required = true)
    @NotNull(message = "O campo 'antifraud' é obrigatório")
    @Valid
    private AntifraudDTO antifraud;
}
